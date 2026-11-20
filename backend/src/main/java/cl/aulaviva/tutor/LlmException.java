package cl.aulaviva.tutor;

/** Error de protocolo o respuesta inválida del proveedor LLM. */
public class LlmException extends RuntimeException {
    public LlmException(String message) { super(message); }
    public LlmException(String message, Throwable cause) { super(message, cause); }
}
