package cl.aulaviva.progress;

import cl.aulaviva.academic.AcademicDirectory;
import cl.aulaviva.identity.AppUser;
import cl.aulaviva.identity.AppUserRepository;
import cl.aulaviva.identity.AulaVivaUser;
import cl.aulaviva.identity.Role;
import cl.aulaviva.shared.ForbiddenException;
import cl.aulaviva.shared.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private final JdbcTemplate jdbc;
    private final AppUserRepository users;
    private final AcademicDirectory directory;

    public ProgressService(JdbcTemplate jdbc, AppUserRepository users,
            AcademicDirectory directory) {
        this.jdbc = jdbc;
        this.users = users;
        this.directory = directory;
    }

    /**
     * Devuelve al estudiante si existe en el tenant del usuario y este tiene permiso para verlo
     * (404 si es de otro colegio, 403 si no tiene relación con él). Única fuente de esta regla.
     */
    @Transactional(readOnly = true)
    public AppUser authorizedStudent(AulaVivaUser user, String studentId) {
        AppUser student = users.findByIdAndTenantId(studentId, user.tenantId())
            .filter(u -> u.getRole() == Role.STUDENT)
            .orElseThrow(() -> new NotFoundException("El estudiante no existe."));
        boolean allowed = switch (user.role()) {
            case STUDENT -> user.id().equals(student.getId());
            case GUARDIAN -> directory.isGuardianOf(user.id(), student.getId());
            case TEACHER -> directory.teachesStudent(user.id(), student.getId());
            case COORDINATOR -> true;
        };
        if (!allowed) {
            throw new ForbiddenException("No tienes acceso a la información de este estudiante.");
        }
        return student;
    }

    @Transactional(readOnly = true)
    public StudentProgressResponse progress(AulaVivaUser user, String studentId) {
        AppUser student = authorizedStudent(user, studentId);
    
        int assigned = count(
            "SELECT COUNT(*) FROM evaluations e JOIN enrollments en ON en.course_id = e.course_id"
                + " WHERE en.student_id = ? AND e.tenant_id = ?",
            student.getId(), user.tenantId());
    
        int completed = count(
            "SELECT COUNT(*) FROM submissions WHERE student_id = ? AND tenant_id = ?"
                + " AND status = 'COMPLETED'",
            student.getId(), user.tenantId());
    
        BigDecimal average = jdbc.queryForObject(
            "SELECT AVG(g.grade) FROM grades g JOIN submissions s ON s.id = g.submission_id"
                + " WHERE s.student_id = ? AND s.tenant_id = ?",
            BigDecimal.class, student.getId(), user.tenantId());

        return new StudentProgressResponse(
            completed,
            Math.max(0, assigned - completed),
            average == null ? null : average.setScale(1, RoundingMode.HALF_UP));
    }

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }
}
