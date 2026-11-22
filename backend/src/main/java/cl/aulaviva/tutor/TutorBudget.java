package cl.aulaviva.tutor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Presupuesto diario de tokens por colegio. En memoria y por instancia: con varias réplicas
 * cada una lleva su cuenta (en producción iría a Redis).
 */
@Component
public class TutorBudget {

    private final ConcurrentHashMap<String, AtomicLong> used = new ConcurrentHashMap<>();
    private final long dailyLimit;
    private final Clock clock;

    public TutorBudget(TutorProperties props, Clock clock) {
        this.dailyLimit = props.dailyTokenBudget();
        this.clock = clock;
    }

    public boolean isExhausted(String tenantId) {
        return used(tenantId) >= dailyLimit;
    }

    public long used(String tenantId) {
        AtomicLong value = used.get(key(tenantId));
        return value == null ? 0 : value.get();
    }

    /** Suma tokens de entrada y salida y descarta las cuentas de días anteriores. */
    public void add(String tenantId, long tokens) {
        if (tokens <= 0) {
            return;
        }
        String suffix = "|" + LocalDate.now(clock);
        used.keySet().removeIf(k -> !k.endsWith(suffix));
        used.computeIfAbsent(key(tenantId), k -> new AtomicLong()).addAndGet(tokens);
    }

    private String key(String tenantId) {
        return tenantId + "|" + LocalDate.now(clock);
    }
}
