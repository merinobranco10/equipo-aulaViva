package cl.aulaviva.shared;

import org.springframework.http.HttpStatus;

/** El usuario esta autenticado pero su rol o relacion no le permite la operacion. */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String detail) {
        super(HttpStatus.FORBIDDEN, "forbidden", "Operación no permitida", detail);
    }
}
