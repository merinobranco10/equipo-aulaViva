package cl.aulaviva.tutor;

/** Respuesta normalizada del proveedor LLM. */
public record LlmAnswer(String content, int promptTokens, int completionTokens) {
    public int totalTokens() { return promptTokens + completionTokens; }
}
