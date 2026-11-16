package cl.aulaviva;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class AulaVivaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AulaVivaApplication.class, args);
    }

    /**
     * Endpoint publico de estado. La raiz "/" queda libre para la UI estatica (Dia 2).
     */
    @RestController
    static class ApiController {

        @GetMapping("/api/v1/status")
        public Map<String, String> status() {
            return Map.of(
                "service", "aulaviva-backend",
                "status", "ok"
            );
        }
    }
}
