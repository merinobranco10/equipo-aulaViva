package cl.aulaviva.evaluations;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.aulaviva.events.OutboxEventRepository;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Flujo critico de la demo: crear, rendir con nota y feedback, y ver progreso. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class FlujoEvaluacionTest {

    static final String PASS = "solo-para-tests";
    static final String KEY = "Idempotency-Key";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OutboxEventRepository outbox;

    @Test
    void docenteCreaEstudianteRindeYApoderadoVeProgreso() throws Exception {
        String evaluationId = crearEvaluacion("Prueba flujo completo", UUID.randomUUID().toString());

        String vista = mvc.perform(get("/v1/evaluations/" + evaluationId)
                .with(httpBasic("estudiante.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questions", hasSize(2)))
            .andExpect(content().string(not(containsString("correctOption"))))
            .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(vista, "$.questions[*].id");

        mvc.perform(post("/v1/evaluations/" + evaluationId + "/submissions")
                .with(httpBasic("estudiante.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(respuestas(ids.get(0), 1, ids.get(1), 0)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.grade").value(7.0))
            .andExpect(jsonPath("$.score").value(5))
            .andExpect(jsonPath("$.maxScore").value(5))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.feedback", hasSize(2)));

        mvc.perform(get("/v1/students/u-estudiante-a/progress")
                .with(httpBasic("apoderado.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completedEvaluations", greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.averageGrade").isNumber());

        assertThat(outbox.findByTenantIdAndEventType("colegio-a", "EvaluationCompleted"))
            .anySatisfy(e -> assertThat(e.getPayload()).contains(evaluationId));
    }

    @Test
    void reenvioConMismaClaveDevuelveMismoResultadoYConOtraClaveFalla() throws Exception {
        String evaluationId = crearEvaluacion("Prueba idempotencia", UUID.randomUUID().toString());
        List<String> ids = idsPreguntas(evaluationId);
        String key = UUID.randomUUID().toString();
        String body = respuestas(ids.get(0), 0, ids.get(1), 0);

        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/v1/evaluations/" + evaluationId + "/submissions")
                    .with(httpBasic("estudiante.a", PASS)).header(KEY, key)
                    .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(3))
                .andExpect(jsonPath("$.feedback", hasSize(2)));
        }
        mvc.perform(post("/v1/evaluations/" + evaluationId + "/submissions")
                .with(httpBasic("estudiante.a", PASS)).header(KEY, "otra-clave")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void crearConLaMismaClaveNoDuplica() throws Exception {
        String key = UUID.randomUUID().toString();
        String first = crearEvaluacion("Prueba sin duplicar", key);
        String second = crearEvaluacion("Prueba sin duplicar", key);
        assertThat(second).isEqualTo(first);
    }

    @Test
    void respuestasInvalidasDan422() throws Exception {
        String evaluationId = crearEvaluacion("Prueba validaciones", UUID.randomUUID().toString());
        List<String> ids = idsPreguntas(evaluationId);

        enviar(evaluationId, respuestas("no-existe", 0, ids.get(1), 0))
            .andExpect(status().isUnprocessableEntity());
        enviar(evaluationId, respuestas(ids.get(0), 9, ids.get(1), 0))
            .andExpect(status().isUnprocessableEntity());
        enviar(evaluationId, respuestas(ids.get(0), 0, ids.get(0), 1))
            .andExpect(status().isUnprocessableEntity());
        enviar(evaluationId, "{\"answers\":[]}")
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].field").value("answers"));
    }

    @Test
    void crearConDatosInvalidos() throws Exception {
        mvc.perform(post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson("Fuera de rango", 5)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value(containsString("correctOption")));

        mvc.perform(post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson(" ", 1)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.type").value("https://api.aulaviva.cl/problems/validation-error"))
            .andExpect(jsonPath("$.instance").exists());

        mvc.perform(post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson("Sin clave", 1)))
            .andExpect(status().isBadRequest());

        mvc.perform(post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .header(KEY, " ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson("Clave vacía", 1)))
            .andExpect(status().isBadRequest());

        mvc.perform(post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{esto no es json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void misEvaluacionesYMe() throws Exception {
        mvc.perform(get("/v1/me/evaluations").with(httpBasic("estudiante.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].status", hasItem("PENDING")))
            .andExpect(jsonPath("$[*].status", hasItem("COMPLETED")))
            .andExpect(jsonPath("$[*].grade", hasItem(4.5)));
        mvc.perform(get("/v1/me/evaluations").with(httpBasic("docente.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].status", hasItem("CREATED")));
        mvc.perform(get("/v1/me/evaluations").with(httpBasic("apoderado.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(get("/v1/me").with(httpBasic("docente.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("TEACHER"))
            .andExpect(jsonPath("$.tenantId").value("colegio-a"))
            .andExpect(jsonPath("$.courses[0].id").value("matematica-8a"))
            .andExpect(content().string(not(containsString("password"))));
        mvc.perform(get("/v1/me").with(httpBasic("apoderado.a", PASS)))
            .andExpect(jsonPath("$.students[0].id").value("u-estudiante-a"));
        mvc.perform(get("/v1/me").with(httpBasic("estudiante.b", PASS)))
            .andExpect(jsonPath("$.courses[0].id").value("ciencias-8b"));
    }

    private org.springframework.test.web.servlet.ResultActions enviar(String evaluationId,
            String body) throws Exception {
        return mvc.perform(post("/v1/evaluations/" + evaluationId + "/submissions")
            .with(httpBasic("estudiante.a", PASS)).header(KEY, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String crearEvaluacion(String titulo, String key) throws Exception {
        MvcResult result = mvc.perform(post("/v1/evaluations")
                .with(httpBasic("docente.a", PASS))
                .header(KEY, key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson(titulo, 1)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private List<String> idsPreguntas(String evaluationId) throws Exception {
        String vista = mvc.perform(get("/v1/evaluations/" + evaluationId)
                .with(httpBasic("estudiante.a", PASS)))
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(vista, "$.questions[*].id");
    }

    static String evaluacionJson(String titulo, int correctaPrimera) {
        return """
            {"courseId":"matematica-8a","title":"%s","questions":[
              {"text":"¿Cuánto es 2 + 2?","options":["3","4"],"correctOption":%d,"points":2},
              {"text":"¿Cuánto es 3 · 3?","options":["9","6","3"],"correctOption":0,"points":3,
               "explanation":"Tres veces tres es nueve."}]}
            """.formatted(titulo, correctaPrimera);
    }

    static String respuestas(String q1, int o1, String q2, int o2) {
        return """
            {"answers":[{"questionId":"%s","selectedOption":%d},
                        {"questionId":"%s","selectedOption":%d}]}
            """.formatted(q1, o1, q2, o2);
    }
}
