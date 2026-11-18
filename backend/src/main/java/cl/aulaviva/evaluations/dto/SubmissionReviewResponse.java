package cl.aulaviva.evaluations.dto;

import java.math.BigDecimal;
import java.util.List;

/** Revisión de una entrega ya corregida: nota, puntaje y respuestas marcadas. */
public record SubmissionReviewResponse(String evaluationId, String title, BigDecimal grade,
        int score, int maxScore, List<ReviewQuestion> questions) {
}
