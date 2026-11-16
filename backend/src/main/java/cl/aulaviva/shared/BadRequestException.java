package cl.aulaviva.shared;

import org.springframework.http.HttpStatus;

/** Request mal formado (por ejemplo, Idempotency-Key vacio). */
public class BadRequestException extends ApiException {

    public BadRequestException(String detail) {
        super(HttpStatus.BAD_REQUEST, "bad-request", "Datos de entrada inválidos", detail);
    }
}
