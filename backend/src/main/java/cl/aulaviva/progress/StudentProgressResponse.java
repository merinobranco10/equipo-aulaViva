package cl.aulaviva.progress;

import java.math.BigDecimal;

/** averageGrade es null si el estudiante aun no rinde evaluaciones. */
public record StudentProgressResponse(int completedEvaluations, int pendingEvaluations,
        BigDecimal averageGrade) {
}
