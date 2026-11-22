package cl.aulaviva.identity;

import cl.aulaviva.academic.CourseSummary;
import cl.aulaviva.academic.StudentSummary;
import java.util.List;

/** Datos del usuario autenticado para la UI: cursos (docente/estudiante) y pupilos
 * (apoderado). Nunca incluye el hash de la contrasena. */
public record MeResponse(String id, String username, String fullName, String role,
        String tenantId, String tenantName, List<CourseSummary> courses,
        List<StudentSummary> students) {
}
