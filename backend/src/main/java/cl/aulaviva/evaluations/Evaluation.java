package cl.aulaviva.evaluations;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    private String id;

    private String tenantId;

    private String courseId;

    private String title;

    private String status;

    private Instant createdAt;

    private String idempotencyKey;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Question> questions = new ArrayList<>();

    protected Evaluation() {
    }

    public Evaluation(String id, String tenantId, String courseId, String title, String status,
            Instant createdAt, String idempotencyKey) {
        this.id = id;
        this.tenantId = tenantId;
        this.courseId = courseId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.idempotencyKey = idempotencyKey;
    }

    public void addQuestion(Question question) {
        question.setEvaluation(this);
        questions.add(question);
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
