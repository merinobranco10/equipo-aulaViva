package cl.aulaviva.academic;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Solo consultas filtradas por tenant. */
public interface CourseRepository extends JpaRepository<Course, String> {

    Optional<Course> findByIdAndTenantId(String id, String tenantId);

    List<Course> findByTenantIdAndTeacherIdOrderByName(String tenantId, String teacherId);
}
