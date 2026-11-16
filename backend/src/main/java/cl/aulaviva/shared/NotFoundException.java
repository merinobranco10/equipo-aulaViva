package cl.aulaviva.shared;

import org.springframework.http.HttpStatus;

/** Recurso inexistente o de otro tenant: siempre 404, para no revelar que existe. */
public class NotFoundException extends ApiException {

    public NotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, "not-found", "Recurso no encontrado", detail);
    }
}
