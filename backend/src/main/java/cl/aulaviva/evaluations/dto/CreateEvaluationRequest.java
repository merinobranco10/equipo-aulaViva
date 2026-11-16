package cl.aulaviva.evaluations.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateEvaluationRequest(
        @NotBlank @Size(max = 36) String courseId,
        @NotBlank @Size(max = 200) String title,
        @NotEmpty @Size(max = 50) List<@Valid QuestionInput> questions) {
}
