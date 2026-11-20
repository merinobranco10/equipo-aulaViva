package cl.aulaviva.shared;

import org.springframework.http.HttpStatus;

/** Presupuesto o cuota temporalmente agotados. */
public class TooManyRequestsException extends ApiException {
    public TooManyRequestsException(String detail) {
        super(HttpStatus.TOO_MANY_REQUESTS, "too-many-requests", "Demasiadas solicitudes", detail);
    }
}
