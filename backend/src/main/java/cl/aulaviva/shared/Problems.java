package cl.aulaviva.shared;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Construye ProblemDetails con los campos obligatorios del contrato (type, title, status,
 * detail, instance). */
public final class Problems {

    public static final String TYPE_BASE = "https://api.aulaviva.cl/problems/";

    private Problems() {
    }

    public static ProblemDetail of(HttpStatus status, String slug, String title, String detail,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(TYPE_BASE + slug));
        problem.setInstance(URI.create(request.getRequestURL().toString()));
        return problem;
    }
}
