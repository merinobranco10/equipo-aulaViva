package cl.aulaviva.evaluations.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerInput(@NotBlank String questionId, @NotNull @Min(0) Integer selectedOption) {
}
