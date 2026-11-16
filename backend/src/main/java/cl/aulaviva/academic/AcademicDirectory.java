package cl.aulaviva.academic;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Relaciones academicas (matriculas y apoderados) sobre tablas de union. */
@Repository
public class AcademicDirectory {

    private final JdbcTemplate jdbc;

    public AcademicDirectory(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isEnrolled(String courseId, String studentId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND student_id = ?",
            Integer.class, courseId, studentId);
        return count != null && count > 0;
    }

    public boolean isGuardianOf(String guardianId, String studentId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM student_guardians WHERE guardian_id = ? AND student_id = ?",
            Integer.class, guardianId, studentId);
        return count != null && count > 0;
    }

    public boolean teachesStudent(String teacherId, String studentId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM courses c JOIN enrollments e ON e.course_id = c.id "
                + "WHERE c.teacher_id = ? AND e.student_id = ?",
            Integer.class, teacherId, studentId);
        return count != null && count > 0;
    }

    public List<CourseSummary> coursesOfStudent(String studentId) {
        return jdbc.query(
            "SELECT c.id, c.name FROM courses c JOIN enrollments e ON e.course_id = c.id "
                + "WHERE e.student_id = ? ORDER BY c.name",
            (rs, i) -> new CourseSummary(rs.getString("id"), rs.getString("name")), studentId);
    }

    public List<StudentSummary> studentsOfGuardian(String guardianId) {
        return jdbc.query(
            "SELECT u.id, u.full_name FROM users u "
                + "JOIN student_guardians g ON g.student_id = u.id "
                + "WHERE g.guardian_id = ? ORDER BY u.full_name",
            (rs, i) -> new StudentSummary(rs.getString("id"), rs.getString("full_name")),
            guardianId);
    }

    public void enroll(String courseId, String studentId) {
        jdbc.update("INSERT INTO enrollments (course_id, student_id) VALUES (?, ?)",
            courseId, studentId);
    }

    public void linkGuardian(String guardianId, String studentId) {
        jdbc.update("INSERT INTO student_guardians (guardian_id, student_id) VALUES (?, ?)",
            guardianId, studentId);
    }
}
