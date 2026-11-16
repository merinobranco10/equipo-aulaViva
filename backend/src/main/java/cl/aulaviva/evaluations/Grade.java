package cl.aulaviva.evaluations;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "grades")
public class Grade {

    @Id
    private String id;

    private String submissionId;

    private BigDecimal grade;

    private int scoreObtained;

    private int maxScore;

    /** Feedback por pregunta serializado como JSON. */
    private String feedback;

    protected Grade() {
    }

    public Grade(String id, String submissionId, BigDecimal grade, int scoreObtained,
            int maxScore, String feedback) {
        this.id = id;
        this.submissionId = submissionId;
        this.grade = grade;
        this.scoreObtained = scoreObtained;
        this.maxScore = maxScore;
        this.feedback = feedback;
    }

    public String getId() {
        return id;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public BigDecimal getGrade() {
        return grade;
    }

    public int getScoreObtained() {
        return scoreObtained;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getFeedback() {
        return feedback;
    }
}
