# ADR 0008 — Identidad y RBAC del MVP

**Estado:** Aceptada · **Fecha:** 17-11-2026
## Contexto

El MVP necesita login por rol (estudiante, docente, coordinador, apoderado) y autorización por operación en un staging pequeño. El objetivo de producción es OIDC (ver ADR 0003), pero levantar y operar un proveedor de identidad completo no cabe en el tiempo ni en la memoria disponibles.

## Decisión

Spring Security con usuarios en base de datos y **HTTP Basic sobre HTTPS** para el MVP:

- Contraseñas con BCrypt; la contraseña de demo llega por `DEMO_PASSWORD` y nunca se escribe en el repositorio.
- API stateless (sin sesión ni cookies); CSRF no aplica a `/v1/**` porque se autentica con el header `Authorization` en cada request.
- Autorización por rol con `@EnableMethodSecurity` y `@PreAuthorize`, más reglas de pertenencia en los servicios (docente del curso, estudiante inscrito, apoderado del pupilo).
- Respuestas 401/403 en ProblemDetails, **sin** `WWW-Authenticate` para que el navegador no muestre su ventana de login sobre la UI.
- Roles del MVP: `STUDENT`, `TEACHER`, `COORDINATOR`, `GUARDIAN`.

## Alternativas consideradas

| Alternativa | Seguridad | Costo/RAM | Tiempo para el MVP | Veredicto |
|---|---|---|---|---|
| Keycloak + OIDC (JWT) | Alta (estándar, SSO, MFA) | Alto (servicio extra de ~1 GB) | Alto | Objetivo de producción; descartada para el MVP por costo y tiempo |
| Servicio gestionado (Cognito, Auth0) | Alta | Costo recurrente y cuenta externa | Medio | Descartada: costo y dependencia externa en la demo |
| JWT propio | Riesgo alto si se implementa mal | Bajo | Medio | Descartada: riesgo de implementarlo mal |
| Spring Security con usuarios en BD + HTTP Basic sobre HTTPS | Media (credencial en cada request) | Bajo | Bajo | **Elegida** para el MVP; migración a OIDC en el roadmap |

## Consecuencias

- (+) Cero infraestructura adicional, fácil de probar con MockMvc y `curl`.
- (−) La credencial viaja en cada request: exige HTTPS en staging y producción.
- (−) La UI guarda las credenciales en `sessionStorage` de la pestaña (se pierden al cerrarla). No hay MFA, bloqueo por intentos ni cierre de sesión en el servidor.
- (−) Todo texto dinámico de la UI se inserta con `textContent`, y se recomienda una Content-Security-Policy estricta.


## Verificación

- `SeguridadTest#sinCredencialesOConClaveMalaDa401SinVentanaDelNavegador`
- `SeguridadTest#estudianteNoPuedeCrearEvaluaciones` (403)
- `SeguridadTest#tutorEsStubSoloParaEstudiantes` (403 para docente)
- `FlujoEvaluacionTest#misEvaluacionesYMe` (`/v1/me` no expone el hash de contraseña)
- Escenarios: `docs/scenarios/rbac.feature`.
