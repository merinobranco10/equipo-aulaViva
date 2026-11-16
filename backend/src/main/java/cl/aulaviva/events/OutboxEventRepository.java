package cl.aulaviva.events;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findByTenantIdAndEventType(String tenantId, String eventType);
}
