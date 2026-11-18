package cl.aulaviva.evaluations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswer, String> {
  List<SubmissionAnswer> findBySubmissionId(String submissionId);
}
