package cl.aulaviva.tutor;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Cliente de la API de chat compatible con OpenAI (Groq). Timeout duro, reintento y circuit
 * breaker con Resilience4j (instancia "llm"). Si el modelo principal responde 429 se usa el de
 * respaldo. Una respuesta vacía (los gpt-oss gastan los tokens razonando) cuenta como falla.
 */
@Component
public class LlmClient {

    static final int MIN_MAX_TOKENS = 300;

    private final RestClient http;
    private final LlmProperties props;

    @Autowired
    public LlmClient(LlmProperties props) {
        this(props, RestClient.builder().requestFactory(factory(props)));
    }

    LlmClient(LlmProperties props, RestClient.Builder builder) {
        this.props = props;
        this.http = builder.baseUrl(props.baseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
            .build();
    }

    private static SimpleClientHttpRequestFactory factory(LlmProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2_000);
        factory.setReadTimeout(props.timeoutSeconds() * 1_000);
        return factory;
    }

    /** Sin clave no se llama al proveedor: el tutor responde en modo extractivo. */
    public boolean enabled() {
        return props.hasApiKey();
    }

    @Retry(name = "llm")
    @CircuitBreaker(name = "llm")
    public LlmAnswer complete(String system, String user) {
        try {
            return call(props.model(), system, user);
        } catch (HttpClientErrorException.TooManyRequests e) {
            if (props.fallbackModel() == null || props.fallbackModel().isBlank()) {
                throw e;
            }
            return call(props.fallbackModel(), system, user);
        }
    }

    private LlmAnswer call(String model, String system, String user) {
        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", Math.max(props.maxTokens(), MIN_MAX_TOKENS),
            "temperature", 0.2,
            "reasoning_effort", props.reasoningEffort(),
            "messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)));
        JsonNode json = http.post().uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON).body(body)
            .retrieve().body(JsonNode.class);
        String content = json == null ? ""
            : json.path("choices").path(0).path("message").path("content").asText("");
        if (content.isBlank()) {
            throw new LlmException("El modelo devolvió una respuesta vacía.");
        }
        return new LlmAnswer(content.trim(),
            json.path("usage").path("prompt_tokens").asInt(0),
            json.path("usage").path("completion_tokens").asInt(0));
    }
}
