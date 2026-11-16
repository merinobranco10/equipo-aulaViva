package cl.aulaviva.evaluations;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Solo consultas filtradas por tenant (ADR 0007). */
public interface EvaluationRepository extends JpaRepository<Evaluation, String> {

    Optional<Evaluation> findByIdAndTenantId(String id, String tenantId);

    Optional<Evaluation> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);

    List<Evaluation> findByTenantIdAndCourseIdInOrderByCreatedAtDesc(String tenantId,
            Collection<String> courseIds);
}
