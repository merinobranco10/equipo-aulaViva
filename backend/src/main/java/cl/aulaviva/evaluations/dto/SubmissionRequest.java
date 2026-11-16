package cl.aulaviva.evaluations.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmissionRequest(@NotEmpty List<@Valid AnswerInput> answers) {
}
