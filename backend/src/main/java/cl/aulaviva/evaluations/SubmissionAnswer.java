package cl.aulaviva.evaluations;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "answers")
public class SubmissionAnswer {

    @Id
    private String id;

    private String submissionId;

    private String questionId;

    private String optionId;

    protected SubmissionAnswer() {
    }

    public SubmissionAnswer(String id, String submissionId, String questionId, String optionId) {
        this.id = id;
        this.submissionId = submissionId;
        this.questionId = questionId;
        this.optionId = optionId;
    }

    public String getId() {
        return id;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getOptionId() {
        return optionId;
    }
}
