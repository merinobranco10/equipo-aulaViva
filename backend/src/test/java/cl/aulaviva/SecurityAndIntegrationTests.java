package cl.aulaviva;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    // TEST 1: Flujo de integración exitoso (Crear evaluación)
    @Test
    void testCreateEvaluationSuccess() throws Exception {
        // Ajustamos el JSON al payload que espera el backend de Branco
        String jsonPayload = "{\"title\": \"Prueba de Matemáticas\", \"courseId\": \"1\"}";
        
        mockMvc.perform(post("/evaluations") // <-- Ruta corregida aquí
                .header("X-Tenant-ID", "colegio-a")
                .header("Authorization", "Basic ZG9jZW50ZUBjb2xlZ2lvLWEuY2w6cGFzc3dvcmQ=") // Docente Colegio A
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated()); // Espera un 201
    }

    // TEST 2: Seguridad Multi-Tenant (Colegio A intenta ver evaluación de Colegio B)
    @Test
    void testCrossTenantIsolationIsBlocked() throws Exception {
        mockMvc.perform(get("/evaluations/999") // <-- Ruta corregida aquí
                .header("X-Tenant-ID", "colegio-a") 
                .header("Authorization", "Basic ZWwdWRpYW50ZUBjb2xlZ2lvLWEuY2w6cGFzc3dvcmQ=")) // Estudiante Colegio A
                .andExpect(status().isNotFound()); // Debe ocultarlo (404) o denegarlo (403)
    }

    // TEST 3: Seguridad por Roles (Estudiante intenta crear evaluación)
    @Test
    void testStudentCannotCreateEvaluation() throws Exception {
        String jsonPayload = "{\"title\": \"Prueba Pirata\", \"courseId\": \"1\"}";
        
        mockMvc.perform(post("/evaluations") // <-- Ruta corregida aquí
                .header("X-Tenant-ID", "colegio-a")
                .header("Authorization", "Basic ZWwdWRpYW50ZUBjb2xlZ2lvLWEuY2w6cGFzc3dvcmQ=") // Estudiante
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isForbidden()); // Espera un 403
    }
}
