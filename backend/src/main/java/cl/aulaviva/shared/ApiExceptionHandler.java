package cl.aulaviva.shared;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Traduce excepciones a ProblemDetails (application/problem+json). Tiene prioridad sobre el
 * manejador generico de Spring Boot (spring.mvc.problemdetails), que responderia 400 a los
 * errores de validacion; el contrato usa 422.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApi(ApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(
            Problems.of(ex.getStatus(), ex.getSlug(), ex.getTitle(), ex.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> Map.of(
                "field", e.getField(),
                "message", e.getDefaultMessage() == null ? "inválido" : e.getDefaultMessage()))
            .toList();
        ProblemDetail problem = Problems.of(HttpStatus.UNPROCESSABLE_ENTITY, "validation-error",
            "Datos de entrada inválidos", "Uno o más campos no cumplen las reglas.", request);
        problem.setProperty("errors", errors);
        return ResponseEntity.unprocessableEntity().body(problem);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getAllErrors().stream()
            .map(e -> Map.of(
                "field", e.getCodes() == null || e.getCodes().length == 0
                    ? "request" : e.getCodes()[e.getCodes().length - 1],
                "message", e.getDefaultMessage() == null ? "inválido" : e.getDefaultMessage()))
            .toList();
        ProblemDetail problem = Problems.of(HttpStatus.UNPROCESSABLE_ENTITY, "validation-error",
            "Datos de entrada inválidos", "Uno o más campos no cumplen las reglas.", request);
        problem.setProperty("errors", errors);
        return ResponseEntity.unprocessableEntity().body(problem);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingHeader(MissingRequestHeaderException ex,
            HttpServletRequest request) {
        return ResponseEntity.badRequest().body(Problems.of(HttpStatus.BAD_REQUEST, "bad-request",
            "Datos de entrada inválidos", "Falta el header " + ex.getHeaderName() + ".", request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return ResponseEntity.badRequest().body(Problems.of(HttpStatus.BAD_REQUEST, "bad-request",
            "Datos de entrada inválidos", "El cuerpo de la solicitud no es un JSON válido.",
            request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleDenied(AccessDeniedException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Problems.of(HttpStatus.FORBIDDEN,
            "forbidden", "Operación no permitida", "Tu rol no permite esta operación.", request));
    }
}
