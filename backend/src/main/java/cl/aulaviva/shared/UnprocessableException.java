package cl.aulaviva.shared;

import org.springframework.http.HttpStatus;

/** Datos sintacticamente validos pero que no cumplen una regla de negocio. */
public class UnprocessableException extends ApiException {

    public UnprocessableException(String detail) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "business-rule", "Regla de negocio no cumplida",
            detail);
    }
}
