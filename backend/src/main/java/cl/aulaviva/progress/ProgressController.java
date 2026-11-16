package cl.aulaviva.progress;

import cl.aulaviva.tenancy.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProgressController {

    private final ProgressService service;
    private final TenantContext tenantContext;

    public ProgressController(ProgressService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/v1/students/{studentId}/progress")
    public StudentProgressResponse progress(@PathVariable String studentId) {
        return service.progress(tenantContext.currentUser(), studentId);
    }
}
