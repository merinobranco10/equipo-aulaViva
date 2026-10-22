package cl.aulaviva;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas unitarias para la aplicación AulaViva.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AulaVivaApplicationTests {

  @Autowired
  private MockMvc mockMvc;

  /**
   * Verifica que el contexto de Spring cargue correctamente.
   */
  @Test
  void contextLoads() {
    // Prueba de contexto
  }

  /**
   * Prueba el endpoint raíz.
   *
   * @throws Exception si falla la petición
   */
  @Test
  void testHomeEndpoint() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.service").value("aulaviva-backend"))
        .andExpect(jsonPath("$.message").value("API principal de AulaViva en ejecución"));
  }

  /**
   * Prueba el endpoint de estado.
   *
   * @throws Exception si falla la petición
   */
  @Test
  void testStatusEndpoint() throws Exception {
    mockMvc.perform(get("/api/v1/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.service").value("aulaviva-backend"))
        .andExpect(jsonPath("$.status").value("ok"));
  }
}
