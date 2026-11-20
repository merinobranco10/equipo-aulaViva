package cl.aulaviva.tutor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrato de entrada para una pregunta del estudiante. */
public record TutorQuestionRequest(
    @NotBlank @Size(max = 80) String courseId,
    @NotBlank @Size(max = 500) String question
) {}
