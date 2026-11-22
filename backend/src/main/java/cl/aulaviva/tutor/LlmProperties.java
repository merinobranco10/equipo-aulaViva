package cl.aulaviva.tutor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración única del proveedor compatible con OpenAI/Groq. */
@ConfigurationProperties(prefix = "aulaviva.llm")
public class LlmProperties {
    private String baseUrl = "https://api.groq.com/openai/v1";
    private String model = "openai/gpt-oss-120b";
    private String fallbackModel = "openai/gpt-oss-20b";
    private String apiKey = "";
    private String reasoningEffort = "low";
    private int maxTokens = 400;
    private int timeoutSeconds = 8;

    public String baseUrl() { return baseUrl; }
    public String model() { return model; }
    public String fallbackModel() { return fallbackModel; }
    public String apiKey() { return apiKey; }
    public String reasoningEffort() { return reasoningEffort; }
    public int maxTokens() { return maxTokens; }
    public int timeoutSeconds() { return timeoutSeconds; }
    public boolean hasApiKey() { return apiKey != null && !apiKey.isBlank(); }

    public void setBaseUrl(String v) { baseUrl = v; }
    public void setModel(String v) { model = v; }
    public void setFallbackModel(String v) { fallbackModel = v; }
    public void setApiKey(String v) { apiKey = v; }
    public void setReasoningEffort(String v) { reasoningEffort = v; }
    public void setMaxTokens(int v) { maxTokens = v; }
    public void setTimeoutSeconds(int v) { timeoutSeconds = v; }
}
