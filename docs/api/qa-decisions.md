# Decisiones QA — Contrato OpenAPI AulaViva

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 28 de septiembre de 2026
**Sesión:** S05

## Objetivo

Registrar las decisiones tomadas durante la revisión del contrato OpenAPI 3.1
desde la perspectiva de calidad: códigos HTTP, formato de errores, idempotencia
y ejemplos ejecutables.

---

## 1. Códigos HTTP por endpoint

Se revisó cada endpoint y se agregaron los códigos faltantes según el tipo
de operación.

| Endpoint | Códigos definidos |
|---|---|
| POST /v1/evaluations | 201, 400, 401, 403, 422, 429, 500 |
| GET /v1/evaluations/{evaluationId} | 200, 401, 403, 404, 500 |
| POST /v1/evaluations/{evaluationId}/submissions | 201, 400, 401, 403, 404, 422, 429, 500 |
| GET /v1/students/{studentId}/progress | 200, 401, 403, 404, 500 |
| POST /v1/tutor/questions | 200, 400, 401, 403, 429, 500, 503 |

**Justificación de los agregados:**
- **422:** validaciones semánticas (ej. questionId no pertenece a la evaluación).
- **429:** rate limit por partner y para proteger el costo del LLM.
- **500:** error interno no controlado, siempre debe estar documentado.
- **503:** ya existía en Tutor IA, se mantiene por indisponibilidad del proveedor.

---

## 2. Schema ProblemDetails (RFC 7807)

Se renombró el schema `Problem` a `ProblemDetails` para alinearlo con el
nombre estándar de la industria y facilitar la comprensión de los consumidores.

Se agregaron dos campos opcionales:
- **trace_id:** permite correlacionar errores con logs de observabilidad.
- **errors[]:** permite reportar múltiples errores de validación en una sola respuesta.

El campo `instance` se marcó como obligatorio porque ayuda a ubicar el recurso
donde ocurrió el error.

---

## 3. Idempotency-Key

Se verificó que los endpoints POST que crean recursos tengan `Idempotency-Key`:

| Endpoint | Idempotency-Key | Justificación |
|---|---|---|
| POST /v1/evaluations | ✅ Sí | Evita crear evaluaciones duplicadas |
| POST /v1/evaluations/{id}/submissions | ✅ Sí | Evita envíos duplicados |
| POST /v1/tutor/questions | ✅ Sí (agregado) | Evita cobros duplicados al LLM |

Los GET no lo requieren porque son operaciones seguras e idempotentes por
naturaleza.

---

## 4. Ejemplos ejecutables

Se crearon 3 ejemplos en `api/examples/` que referencian al `openapi.yaml`
mediante el campo `_openapi_ref`:

1. `create-evaluation.json` — flujo exitoso de creación.
2. `submit-evaluation.json` — flujo exitoso de envío y corrección.
3. `tutor-question-error.json` — flujo de error 422 con ProblemDetails.

Cada ejemplo puede usarse como prueba manual con Postman/curl o como referencia
para las pruebas automatizadas del pipeline CI/CD.

---

## 5. Pendientes

- Configurar Spectral (Valentina) y correr el lint sobre el contrato corregido.
- Generar cliente TypeScript (Matías).
- Publicar documentación viva con Swagger UI o Redoc.
