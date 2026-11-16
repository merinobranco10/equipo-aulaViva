package cl.aulaviva.evaluations.dto;

import java.math.BigDecimal;
import java.util.List;

public record SubmissionResponse(BigDecimal grade, int score, int maxScore, String status,
        List<QuestionFeedback> feedback) {
}
