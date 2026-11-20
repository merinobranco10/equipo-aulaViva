package cl.aulaviva.tutor;

import cl.aulaviva.tenancy.TenantContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tutor IA (ADR 0009). El Idempotency-Key del contrato se acepta pero no se exige: responder una
 * pregunta no modifica datos, y así run_eval.py funciona sin el header.
 */
@RestController
public class TutorController {

    private final TutorService service;
    private final TenantContext tenantContext;

    public TutorController(TutorService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/v1/tutor/questions")
    @PreAuthorize("hasRole('STUDENT')")
    public TutorAnswerResponse ask(@Valid @RequestBody TutorQuestionRequest request) {
        return service.ask(tenantContext.currentUser(), request);
    }
}
