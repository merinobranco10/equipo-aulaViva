package cl.aulaviva.evaluations;

import static cl.aulaviva.evaluations.FlujoEvaluacionTest.PASS;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Endpoints de la UI del Día 2: revisión de la propia entrega y evaluaciones de un estudiante.
 * Usa la semilla del perfil demo: estudiante.a rindió la de números enteros (nota 4,5) y tiene
 * pendiente la de potencias.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class RevisionEstudianteTest {

    private static final String ENTEROS = "Números enteros: multiplicación y división";
    private static final String POTENCIAS = "Potencias de base entera";
    private static final String STUDENT_A = "/v1/students/u-estudiante-a/evaluations";

    @Autowired
    MockMvc mvc;

    private String idPorTitulo(String titulo) throws Exception {
        String body = mvc.perform(get(STUDENT_A).with(httpBasic("estudiante.a", PASS)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(body, "$[?(@.title == '" + titulo + "')].id");
        return ids.get(0);
    }

    @Test
    void estudianteVeSusEvaluacionesConEstadoYNota() throws Exception {
        mvc.perform(get(STUDENT_A).with(httpBasic("estudiante.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.title == '" + ENTEROS + "')].status").value(hasItem("COMPLETED")))
            .andExpect(jsonPath("$[?(@.title == '" + ENTEROS + "')].grade").value(hasItem(4.5)))
            .andExpect(jsonPath("$[?(@.title == '" + POTENCIAS + "')].status").value(hasItem("PENDING")));
    }

    @Test
    void apoderadoYDocenteVenLasEvaluacionesDelEstudiante() throws Exception {
        mvc.perform(get(STUDENT_A).with(httpBasic("apoderado.a", PASS)))
            .andExpect(status().isOk());
        mvc.perform(get(STUDENT_A).with(httpBasic("docente.a", PASS)))
            .andExpect(status().isOk());
    }

    @Test
    void otroColegioRecibe404() throws Exception {
        mvc.perform(get(STUDENT_A).with(httpBasic("coordinador.b", PASS)))
            .andExpect(status().isNotFound());
        mvc.perform(get(STUDENT_A).with(httpBasic("estudiante.b", PASS)))
            .andExpect(status().isNotFound());
    }

    @Test
    void sinCredencialesRecibe401() throws Exception {
        mvc.perform(get(STUDENT_A)).andExpect(status().isUnauthorized());
    }

    @Test
    void estudianteRevisaSuEntregaMarcada() throws Exception {
        String id = idPorTitulo(ENTEROS);
        mvc.perform(get("/v1/evaluations/" + id + "/submissions/me")
                .with(httpBasic("estudiante.a", PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.evaluationId").value(id))
            .andExpect(jsonPath("$.grade").value(4.5))
            .andExpect(jsonPath("$.questions", hasSize(3)))
            .andExpect(jsonPath("$.questions[0].correctOption").exists())
            .andExpect(jsonPath("$.questions[0].message").exists());
    }

    @Test
    void evaluacionPendienteNoTieneRevision() throws Exception {
        String id = idPorTitulo(POTENCIAS);
        mvc.perform(get("/v1/evaluations/" + id + "/submissions/me")
                .with(httpBasic("estudiante.a", PASS)))
            .andExpect(status().isNotFound());
    }

    @Test
    void soloElEstudiantePuedeRevisarSuEntrega() throws Exception {
        String id = idPorTitulo(ENTEROS);
        mvc.perform(get("/v1/evaluations/" + id + "/submissions/me")
                .with(httpBasic("docente.a", PASS)))
            .andExpect(status().isForbidden());
        mvc.perform(get("/v1/evaluations/" + id + "/submissions/me")
                .with(httpBasic("estudiante.b", PASS)))
            .andExpect(status().isNotFound());
    }
}
