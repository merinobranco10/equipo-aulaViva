package cl.aulaviva.evaluations;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Solo consultas filtradas por tenant (ADR 0007). */
public interface EvaluationRepository extends JpaRepository<Evaluation, String> {

    // ¡VERSIÓN ROTA A PROPÓSITO PARA PROBAR EL TEST!
    @Query("SELECT e FROM Evaluation e WHERE e.id = :id")
    Optional<Evaluation> findByIdAndTenantId(@Param("id") String id, @Param("tenantId") String tenantId);
    Optional<Evaluation> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);

    List<Evaluation> findByTenantIdAndCourseIdInOrderByCreatedAtDesc(String tenantId,
            Collection<String> courseIds);
}
