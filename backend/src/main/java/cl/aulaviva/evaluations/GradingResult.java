package cl.aulaviva.evaluations;

import cl.aulaviva.evaluations.dto.QuestionFeedback;
import java.math.BigDecimal;
import java.util.List;

public record GradingResult(int score, int maxScore, BigDecimal grade,
        List<QuestionFeedback> feedback) {
}
