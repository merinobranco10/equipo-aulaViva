package cl.aulaviva.evaluations.dto;

import java.util.List;

/** Pregunta sin la respuesta correcta (nunca se expone correctOption). */
public record QuestionResponse(String id, String text, List<String> options, int points) {
}
