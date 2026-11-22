package cl.aulaviva.evaluations.dto;

public record QuestionFeedback(String questionId, boolean correct, String message) {
}
