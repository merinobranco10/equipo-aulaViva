package cl.aulaviva.evaluations.dto;

import java.util.List;

/** Pregunta con la respuesta marcada por el estudiante (selectedOption es null si no respondió). */
public record ReviewQuestion(String id, String text, List<String> options, int points,
        Integer selectedOption, int correctOption, boolean correct, String message) {
}
