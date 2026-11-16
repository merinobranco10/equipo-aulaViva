package cl.aulaviva.shared;

import org.springframework.http.HttpStatus;

/** Error de negocio que se traduce a ProblemDetails con el estado indicado. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String slug;
    private final String title;

    public ApiException(HttpStatus status, String slug, String title, String detail) {
        super(detail);
        this.status = status;
        this.slug = slug;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }
}
