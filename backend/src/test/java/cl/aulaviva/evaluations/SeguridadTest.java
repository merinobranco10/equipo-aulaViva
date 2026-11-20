package cl.aulaviva.evaluations;

import static cl.aulaviva.evaluations.FlujoEvaluacionTest.KEY;
import static cl.aulaviva.evaluations.FlujoEvaluacionTest.PASS;
import static cl.aulaviva.evaluations.FlujoEvaluacionTest.evaluacionJson;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Aislamiento entre colegios y permisos por rol (lo que mas pregunta el panel de seguridad). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class SeguridadTest {

    @Autowired
    private MockMvc mvc;

    private String evaluacionColegioA;

    @BeforeEach
    void crearEvaluacionDelColegioA() throws Exception {
        String body = mvc.perform(post("/v1/evaluations").with(httpBasic("docente.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson("Prueba seguridad", 1)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        evaluacionColegioA = JsonPath.read(body, "$.id");
    }

    @Test
    void docenteDeOtroColegioRecibe404() throws Exception {
        // Este test falla si EvaluationRepository deja de filtrar por tenant.
        mvc.perform(get("/v1/evaluations/" + evaluacionColegioA)
                .with(httpBasic("docente.b", PASS)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void estudianteDeOtroColegioNoPuedeRendir() throws Exception {
        mvc.perform(post("/v1/evaluations/" + evaluacionColegioA + "/submissions")
                .with(httpBasic("estudiante.b", PASS)).header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answers\":[{\"questionId\":\"x\",\"selectedOption\":0}]}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void docenteDeOtroColegioNoCreaEnCursoAjeno() throws Exception {
        mvc.perform(post("/v1/evaluations").with(httpBasic("docente.b", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson("Intento ajeno", 1)))
            .andExpect(status().isNotFound());
    }

    @Test
    void estudianteNoPuedeCrearEvaluaciones() throws Exception {
        mvc.perform(post("/v1/evaluations").with(httpBasic("estudiante.a", PASS))
                .header(KEY, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evaluacionJson("No permitido", 1)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void apoderadoNoVeEvaluacionesNiOtrosEstudiantes() throws Exception {
        mvc.perform(get("/v1/evaluations/" + evaluacionColegioA)
                .with(httpBasic("apoderado.a", PASS)))
            .andExpect(status().isForbidden());
        mvc.perform(get("/v1/students/u-estudiante-b/progress")
                .with(httpBasic("apoderado.a", PASS)))
            .andExpect(status().isNotFound());
        mvc.perform(get("/v1/students/u-docente-a/progress")
                .with(httpBasic("apoderado.a", PASS)))
            .andExpect(status().isNotFound());
    }

    @Test
    void progresoSegunRol() throws Exception {
        mvc.perform(get("/v1/students/u-estudiante-a/progress")
                .with(httpBasic("docente.a", PASS)))
            .andExpect(status().isOk());
        mvc.perform(get("/v1/students/u-estudiante-a/progress")
                .with(httpBasic("estudiante.a", PASS)))
            .andExpect(status().isOk());
        mvc.perform(get("/v1/students/u-estudiante-b/progress")
                .with(httpBasic("coordinador.b", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageGrade").value(7.0));
        mvc.perform(get("/v1/students/u-estudiante-b/progress")
                .with(httpBasic("docente.a", PASS)))
            .andExpect(status().isNotFound());
    }

    @Test
    void coordinadorDeOtroColegioRecibe404() throws Exception {
        mvc.perform(get("/v1/evaluations/" + evaluacionColegioA)
                .with(httpBasic("coordinador.b", PASS)))
            .andExpect(status().isNotFound());
    }

    @Test
    void sinCredencialesOConClaveMalaDa401SinVentanaDelNavegador() throws Exception {
        mvc.perform(get("/v1/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist("WWW-Authenticate"))
            .andExpect(jsonPath("$.title").value("Usuario no autenticado"));
        mvc.perform(get("/v1/me").with(httpBasic("docente.a", "clave-incorrecta")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tutorSoloParaEstudiantes() throws Exception {
        mvc.perform(post("/v1/tutor/questions").with(httpBasic("estudiante.a", PASS))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseId\":\"matematica-8a\",\"question\":\"¿Qué es el valor absoluto?\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.answer").isNotEmpty());
        mvc.perform(post("/v1/tutor/questions").with(httpBasic("docente.a", PASS))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseId\":\"matematica-8a\",\"question\":\"hola\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void cabecerasDeSeguridadEnLaUi() throws Exception {
        mvc.perform(get("/index.html"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Security-Policy",
                org.hamcrest.Matchers.containsString("default-src 'self'")))
            .andExpect(header().exists("Permissions-Policy"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
}
