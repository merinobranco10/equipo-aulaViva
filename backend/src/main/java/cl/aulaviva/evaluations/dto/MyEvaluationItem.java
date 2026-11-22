package cl.aulaviva.evaluations.dto;

import java.math.BigDecimal;

/** Evaluacion visible para el usuario actual (UI del Dia 2). status: PENDING | COMPLETED |
 * CREATED. */
public record MyEvaluationItem(String id, String title, String courseId, String courseName,
        String status, BigDecimal grade) {
}
