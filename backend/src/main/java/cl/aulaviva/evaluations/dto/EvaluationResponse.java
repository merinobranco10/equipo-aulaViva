package cl.aulaviva.evaluations.dto;

import java.util.List;

public record EvaluationResponse(String id, String title, List<QuestionResponse> questions) {
}
