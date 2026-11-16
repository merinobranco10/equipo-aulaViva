package cl.aulaviva.evaluations;

import cl.aulaviva.evaluations.dto.CreateEvaluationRequest;
import cl.aulaviva.evaluations.dto.EvaluationCreatedResponse;
import cl.aulaviva.evaluations.dto.EvaluationResponse;
import cl.aulaviva.evaluations.dto.MyEvaluationItem;
import cl.aulaviva.evaluations.dto.SubmissionRequest;
import cl.aulaviva.evaluations.dto.SubmissionResponse;
import cl.aulaviva.shared.BadRequestException;
import cl.aulaviva.tenancy.TenantContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class EvaluationController {

    static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final int MAX_KEY_LENGTH = 100;

    private final EvaluationService service;
    private final TenantContext tenantContext;

    public EvaluationController(EvaluationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/evaluations")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<EvaluationCreatedResponse> create(
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyKey,
            @Valid @RequestBody CreateEvaluationRequest request) {
        EvaluationCreatedResponse created =
            service.create(tenantContext.currentUser(), request, checkKey(idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/evaluations/{evaluationId}")
    public EvaluationResponse get(@PathVariable String evaluationId) {
        return service.get(tenantContext.currentUser(), evaluationId);
    }

    @PostMapping("/evaluations/{evaluationId}/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable String evaluationId,
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyKey,
            @Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse result = service.submit(tenantContext.currentUser(), evaluationId,
            request, checkKey(idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/me/evaluations")
    public List<MyEvaluationItem> myEvaluations() {
        return service.myEvaluations(tenantContext.currentUser());
    }

    private static String checkKey(String key) {
        if (key == null || key.isBlank() || key.length() > MAX_KEY_LENGTH) {
            throw new BadRequestException(
                "Idempotency-Key debe tener entre 1 y " + MAX_KEY_LENGTH + " caracteres.");
        }
        return key.trim();
    }
}
