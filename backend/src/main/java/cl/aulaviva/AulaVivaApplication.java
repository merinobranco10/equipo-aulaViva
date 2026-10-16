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

    @RestController
    static class ApiController {

        @GetMapping("/")
        public Map<String, String> home() {
            return Map.of(
                "service", "aulaviva-backend",
                "message", "API principal de AulaViva en ejecución"
            );
        }

        @GetMapping("/api/v1/status")
        public Map<String, String> status() {
            return Map.of(
                "service", "aulaviva-backend",
                "status", "ok"
            );
        }
    }
}
