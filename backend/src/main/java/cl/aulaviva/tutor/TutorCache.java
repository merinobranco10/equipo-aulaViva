package cl.aulaviva.tutor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Caché de respuestas del LLM (1.000 entradas, 24 h). Por instancia y en memoria. Se activa con
 * aulaviva.tutor.cache-enabled para poder medir el antes y el después (FinOps).
 */
@Component
public class TutorCache {

    /** Respuesta guardada con las fuentes que la respaldan. */
    public record Cached(String answer, List<String> sources) { }

    private final Cache<String, Cached> cache = Caffeine.newBuilder()
        .maximumSize(1_000)
        .expireAfterWrite(Duration.ofHours(24))
        .build();
    private final boolean enabled;

    public TutorCache(TutorProperties props) {
        this.enabled = props.cacheEnabled();
    }

    public Optional<Cached> get(String tenantId, String courseId, String question) {
        return enabled ? Optional.ofNullable(cache.getIfPresent(key(tenantId, courseId, question)))
            : Optional.empty();
    }

    public void put(String tenantId, String courseId, String question, Cached value) {
        if (enabled) {
            cache.put(key(tenantId, courseId, question), value);
        }
    }

    /** Incluye el tenant: un colegio nunca recibe una respuesta cacheada por otro. */
    private static String key(String tenantId, String courseId, String question) {
        return tenantId + "|" + courseId + "|" + Text.key(question);
    }
}
