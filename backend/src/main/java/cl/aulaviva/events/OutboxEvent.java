package cl.aulaviva.events;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Evento de dominio guardado en la misma transaccion que el cambio (patron Outbox, ADR 0004).
 * La publicacion a un broker queda diferida hasta tener mas de un consumidor real.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private String id;

    private String tenantId;

    private String eventType;

    private String payload;

    private Instant createdAt;

    private boolean published;

    protected OutboxEvent() {
    }

    public OutboxEvent(String id, String tenantId, String eventType, String payload,
            Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
        this.published = false;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isPublished() {
        return published;
    }
}
