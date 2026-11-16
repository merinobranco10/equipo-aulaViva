package cl.aulaviva.evaluations.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuestionInput(
        @NotBlank @Size(max = 1000) String text,
        @NotNull @Size(min = 2, max = 10) List<@NotBlank @Size(max = 500) String> options,
        @NotNull @Min(0) Integer correctOption,
        @NotNull @Min(1) @Max(100) Integer points,
        @Size(max = 1000) String explanation) {
}
