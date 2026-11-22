package cl.aulaviva.tutor;

import java.util.List;

/** Contrato estable de salida del tutor. */
public record TutorAnswerResponse(String answer, List<String> sources, String mode,
        String guardrail) {
    public TutorAnswerResponse {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
