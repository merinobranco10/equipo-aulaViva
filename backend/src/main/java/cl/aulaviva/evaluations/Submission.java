package cl.aulaviva.evaluations;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    private String id;

    private String tenantId;

    private String evaluationId;

    private String studentId;

    private Instant submittedAt;

    private String status;

    private String idempotencyKey;

    protected Submission() {
    }

    public Submission(String id, String tenantId, String evaluationId, String studentId,
            Instant submittedAt, String status, String idempotencyKey) {
        this.id = id;
        this.tenantId = tenantId;
        this.evaluationId = evaluationId;
        this.studentId = studentId;
        this.submittedAt = submittedAt;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public String getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
