package cl.aulaviva.tutor;

import cl.aulaviva.shared.ApiException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Stub del tutor IA: responde 501 hasta que se implemente el Dia 4 (ADR 0009). */
@RestController
public class TutorController {

    @PostMapping("/v1/tutor/questions")
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> ask(@RequestBody(required = false) Map<String, Object> body) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "not-implemented",
            "Tutor IA no disponible", "El tutor IA se habilita en el Día 4 del plan.");
    }
}
