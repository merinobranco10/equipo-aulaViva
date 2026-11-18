package cl.aulaviva.evaluations;

import cl.aulaviva.evaluations.dto.MyEvaluationItem;
import cl.aulaviva.evaluations.dto.SubmissionReviewResponse;
import cl.aulaviva.tenancy.TenantContext;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ReviewController {

    private final EvaluationReviewService service;
    private final TenantContext tenantContext;

    public ReviewController(EvaluationReviewService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    /** El estudiante revisa sus propias respuestas marcadas de una evaluación ya rendida. */
    @GetMapping("/evaluations/{evaluationId}/submissions/me")
    @PreAuthorize("hasRole('STUDENT')")
    public SubmissionReviewResponse myReview(@PathVariable String evaluationId) {
        return service.myReview(tenantContext.currentUser(), evaluationId);
    }

    /** Evaluaciones de un estudiante (apoderado, docente, coordinador o él mismo). */
    @GetMapping("/students/{studentId}/evaluations")
    public List<MyEvaluationItem> studentEvaluations(@PathVariable String studentId) {
        return service.evaluationsOfStudent(tenantContext.currentUser(), studentId);
    }
}
