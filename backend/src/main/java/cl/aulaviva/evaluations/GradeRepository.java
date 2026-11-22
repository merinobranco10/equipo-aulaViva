package cl.aulaviva.evaluations;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, String> {

    Optional<Grade> findBySubmissionId(String submissionId);
}
