package cl.aulaviva.evaluations;

import static cl.aulaviva.evaluations.FlujoEvaluacionTest.KEY;
import static cl.aulaviva.evaluations.FlujoEvaluacionTest.PASS;
import static cl.aulaviva.evaluations.FlujoEvaluacionTest.evaluacionJson;
import static cl.aulaviva.evaluations.FlujoEvaluacionTest.respuestas;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import cl.aulaviva.tutor.LlmClient;
import cl.aulaviva.tutor.TutorBudget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.yaml.snakeyaml.Yaml;

/**
 * Contract test: cada respuesta real de la API debe estar documentada en api/openapi.yaml
 * (codigo de estado declarado en la operacion y campos "required" del schema presentes).
 * Sin dependencias extra: el contrato es OpenAPI 3.1 y se lee con SnakeYAML.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
@DirtiesContext
class ContratoOpenApiTest {

    /** Operaciones que aun no se implementan. */
    private static final Set<String> FUERA_DE_ALCANCE = Set.of();
    private static final String TUTOR = "/v1/tutor/questions";

    private static Map<String, Object> spec;
    private final ObjectMapper json = new ObjectMapper();
    private final Set<String> cubiertas = new TreeSet<>();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TutorBudget budget;

    /** Sin LLM real: el tutor responde en modo extractivo. */
    @MockitoBean
    private LlmClient llm;

    @BeforeAll
    static void cargarContrato() throws Exception {
        try (InputStream in = Files.newInputStream(Path.of("..", "api", "openapi.yaml"))) {
            spec = new Yaml().load(in);
        }
    }

    @Test
    void lasRespuestasRealesCumplenElContrato() throws Exception {
        // Docente crea (201) y valida errores documentados (422, 400, 403)
        String evaluationId = JsonPath.read(verificar("POST", "/v1/evaluations",
            post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON).content(evaluacionJson("Contrato", 1)),
            201), "$.id");
        verificar("POST", "/v1/evaluations",
            post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON).content(evaluacionJson(" ", 1)), 422);
        verificar("POST", "/v1/evaluations",
            post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .contentType(MediaType.APPLICATION_JSON).content(evaluacionJson("Sin clave", 1)), 400);
        verificar("POST", "/v1/evaluations",
            post("/v1/evaluations").with(httpBasic("estudiante.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON).content(evaluacionJson("Pirata", 1)), 403);

        // Estudiante ve la evaluacion (200) y rinde (201); otro colegio recibe 404
        String vista = verificar("GET", "/v1/evaluations/{evaluationId}",
            get("/v1/evaluations/" + evaluationId).with(httpBasic("estudiante.a", PASS)), 200);
        List<String> ids = JsonPath.read(vista, "$.questions[*].id");
        verificar("GET", "/v1/evaluations/{evaluationId}",
            get("/v1/evaluations/" + evaluationId).with(httpBasic("docente.b", PASS)), 404);
        verificar("POST", "/v1/evaluations/{evaluationId}/submissions",
            post("/v1/evaluations/" + evaluationId + "/submissions").with(httpBasic("estudiante.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON).content(respuestas(ids.get(0), 1, ids.get(1), 0)),
            201);

        // Revision de la propia entrega
        verificar("GET", "/v1/evaluations/{evaluationId}/submissions/me",
            get("/v1/evaluations/" + evaluationId + "/submissions/me").with(httpBasic("estudiante.a", PASS)),
            200);
        verificar("GET", "/v1/evaluations/{evaluationId}/submissions/me",
            get("/v1/evaluations/" + evaluationId + "/submissions/me").with(httpBasic("docente.a", PASS)),
            403);

        // Progreso y evaluaciones del estudiante
        verificar("GET", "/v1/students/{studentId}/progress",
            get("/v1/students/u-estudiante-a/progress").with(httpBasic("apoderado.a", PASS)), 200);
        verificar("GET", "/v1/students/{studentId}/progress",
            get("/v1/students/u-estudiante-a/progress").with(httpBasic("coordinador.b", PASS)), 404);
        verificar("GET", "/v1/students/{studentId}/evaluations",
            get("/v1/students/u-estudiante-a/evaluations").with(httpBasic("apoderado.a", PASS)), 200);

        // Sesion
        verificar("GET", "/v1/me", get("/v1/me").with(httpBasic("docente.a", PASS)), 200);
        verificar("GET", "/v1/me", get("/v1/me"), 401);
        verificar("GET", "/v1/me/evaluations",
            get("/v1/me/evaluations").with(httpBasic("estudiante.a", PASS)), 200);

        // Tutor IA: respuesta (200), sin sesion (401), rol no estudiante (403),
        // curso de otro colegio (404), pregunta vacia (422) y presupuesto agotado (429)
        verificar("POST", TUTOR, tutor("estudiante.a", "matematica-8a", "valor absoluto contrato"), 200);
        verificar("POST", TUTOR, post(TUTOR).contentType(MediaType.APPLICATION_JSON)
            .content(tutorJson("matematica-8a", "hola")), 401);
        verificar("POST", TUTOR, tutor("docente.a", "matematica-8a", "valor absoluto"), 403);
        verificar("POST", TUTOR, tutor("estudiante.b", "matematica-8a", "valor absoluto"), 404);
        verificar("POST", TUTOR, tutor("estudiante.a", "matematica-8a", " "), 422);
        budget.add("colegio-b", 10_000_000);
        verificar("POST", TUTOR, tutor("estudiante.b", "ciencias-8b", "mitocondria contrato"), 429);

        // Toda operacion del contrato (salvo las fuera de alcance) quedo cubierta
        assertThat(cubiertas).containsAll(operacionesDelContrato());
    }

    private RequestBuilder tutor(String user, String course, String question) throws Exception {
        return post(TUTOR).with(httpBasic(user, PASS))
            .contentType(MediaType.APPLICATION_JSON).content(tutorJson(course, question));
    }

    private String tutorJson(String course, String question) throws Exception {
        return json.writeValueAsString(Map.of("courseId", course, "question", question));
    }

    // ------------------------------------------------------------------ validacion

    private String verificar(String method, String template, RequestBuilder request, int esperado)
            throws Exception {
        MockHttpServletResponse res = mvc.perform(request).andReturn().getResponse();
        String op = method + " " + template;
        assertThat(res.getStatus()).as("%s devolvio otro estado", op).isEqualTo(esperado);

        Map<String, Object> respuestas = mapa(mapa(mapa(spec, "paths"), template), method.toLowerCase());
        Map<String, Object> responses = mapa(respuestas, "responses");
        assertThat(responses).as("%s no documenta el estado %d", op, esperado)
            .containsKey(String.valueOf(esperado));

        Map<String, Object> schema = schemaDe(resolver(mapa(responses, String.valueOf(esperado))));
        String body = res.getContentAsString();
        if (schema != null && !body.isBlank()) {
            validar(op, json.readTree(body), schema);
        }
        cubiertas.add(op);
        return body;
    }

    private void validar(String op, JsonNode nodo, Map<String, Object> schema) {
        schema = resolver(schema);
        if ("array".equals(schema.get("type"))) {
            assertThat(nodo.isArray()).as("%s debe devolver un arreglo", op).isTrue();
            Map<String, Object> items = mapa(schema, "items");
            nodo.forEach(e -> validar(op, e, items));
            return;
        }
        Object required = schema.get("required");
        if (required instanceof List<?> campos) {
            for (Object campo : campos) {
                assertThat(nodo.has(campo.toString()))
                    .as("%s: falta el campo obligatorio '%s' del contrato", op, campo).isTrue();
            }
        }
    }

    private Map<String, Object> schemaDe(Map<String, Object> response) {
        Object content = response.get("content");
        if (!(content instanceof Map<?, ?> tipos) || tipos.isEmpty()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> media = (Map<String, Object>) tipos.values().iterator().next();
        return media == null ? null : mapa(media, "schema");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolver(Map<String, Object> nodo) {
        while (nodo != null && nodo.get("$ref") instanceof String ref) {
            Map<String, Object> actual = spec;
            for (String parte : ref.substring(2).split("/")) {
                actual = (Map<String, Object>) actual.get(parte);
            }
            nodo = actual;
        }
        return nodo;
    }

    private Set<String> operacionesDelContrato() {
        Set<String> ops = new TreeSet<>();
        mapa(spec, "paths").forEach((path, metodos) -> ((Map<?, ?>) metodos).keySet().forEach(m -> {
            String op = m.toString().toUpperCase() + " " + path;
            if (Set.of("GET", "POST", "PUT", "PATCH", "DELETE").contains(m.toString().toUpperCase())
                    && !FUERA_DE_ALCANCE.contains(op)) {
                ops.add(op);
            }
        }));
        return ops;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Map<String, Object> padre, String clave) {
        Object valor = padre == null ? null : padre.get(clave);
        assertThat(valor).as("el contrato no tiene '%s'", clave).isInstanceOf(Map.class);
        return (Map<String, Object>) valor;
    }
}
