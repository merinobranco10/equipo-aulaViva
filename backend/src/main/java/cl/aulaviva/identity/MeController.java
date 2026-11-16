package cl.aulaviva.identity;

import cl.aulaviva.academic.AcademicDirectory;
import cl.aulaviva.academic.CourseRepository;
import cl.aulaviva.academic.CourseSummary;
import cl.aulaviva.academic.StudentSummary;
import cl.aulaviva.tenancy.Tenant;
import cl.aulaviva.tenancy.TenantContext;
import cl.aulaviva.tenancy.TenantRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final TenantContext tenantContext;
    private final TenantRepository tenants;
    private final CourseRepository courses;
    private final AcademicDirectory directory;

    public MeController(TenantContext tenantContext, TenantRepository tenants,
            CourseRepository courses, AcademicDirectory directory) {
        this.tenantContext = tenantContext;
        this.tenants = tenants;
        this.courses = courses;
        this.directory = directory;
    }

    @GetMapping("/v1/me")
    public MeResponse me() {
        AulaVivaUser user = tenantContext.currentUser();
        String tenantName = tenants.findById(user.tenantId()).map(Tenant::getName).orElse("");
        List<CourseSummary> myCourses = switch (user.role()) {
            case TEACHER -> courses.findByTenantIdAndTeacherIdOrderByName(user.tenantId(),
                    user.id()).stream()
                .map(c -> new CourseSummary(c.getId(), c.getName())).toList();
            case STUDENT -> directory.coursesOfStudent(user.id());
            default -> List.of();
        };
        List<StudentSummary> students = user.role() == Role.GUARDIAN
            ? directory.studentsOfGuardian(user.id()) : List.of();
        return new MeResponse(user.id(), user.username(), user.fullName(), user.role().name(),
            user.tenantId(), tenantName, myCourses, students);
    }
}
