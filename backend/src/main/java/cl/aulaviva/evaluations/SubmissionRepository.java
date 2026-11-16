package cl.aulaviva.evaluations;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, String> {

    Optional<Submission> findByEvaluationIdAndStudentId(String evaluationId, String studentId);

    List<Submission> findByTenantIdAndStudentId(String tenantId, String studentId);
}
