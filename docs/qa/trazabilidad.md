# Trazabilidad requerimiento → test → evidencia (S18)

**Responsable:** Alexander Ruiz-Tagle (QA) · **Actualizado:** 19-11-2026

Todos los tests corren en el CI (`AulaViva - CI Quality Gates`, `mvn -B verify` en `backend/`) con los perfiles `test,demo`, y el gate de JaCoCo exige ≥ 70 % de cobertura de líneas. Los usuarios de prueba son los del `DemoDataLoader` (`docente.a`, `estudiante.a` y `apoderado.a` del Colegio A; `docente.b`, `estudiante.b` y `coordinador.b` del Colegio B); en los tests la clave es `solo-para-tests` (`src/test/resources/application-test.properties`), nunca la `DEMO_PASSWORD` real.

## 1. Matriz

| Historia / regla | Endpoint | Test automatizado | Evidencia |
|---|---|---|---|
| HU-1 · El docente crea una evaluación | `POST /v1/evaluations` | `FlujoEvaluacionTest#docenteCreaEstudianteRindeYApoderadoVeProgreso`, `#crearConLaMismaClaveNoDuplica` | Run del CI |
| HU-1 · Datos inválidos o sin `Idempotency-Key` se rechazan (422 / 400) | `POST /v1/evaluations` | `FlujoEvaluacionTest#crearConDatosInvalidos` | Run del CI |
| HU-2 · El estudiante rinde y recibe nota 1,0–7,0 con feedback | `POST /v1/evaluations/{id}/submissions` | `FlujoEvaluacionTest#docenteCreaEstudianteRindeYApoderadoVeProgreso`, `GradingServiceTest` (6 casos) | Run del CI |
| HU-2 · Reenvío con la misma clave no duplica la entrega | `POST /v1/evaluations/{id}/submissions` | `FlujoEvaluacionTest#reenvioConMismaClaveDevuelveMismoResultadoYConOtraClaveFalla` | Run del CI |
| HU-2 · El estudiante no ve la respuesta correcta antes de rendir | `GET /v1/evaluations/{id}` | `FlujoEvaluacionTest#docenteCreaEstudianteRindeYApoderadoVeProgreso` (la respuesta no contiene `correctOption`) | Run del CI |
| HU-2 · El estudiante revisa su entrega corregida | `GET /v1/evaluations/{id}/submissions/me` | `RevisionEstudianteTest#estudianteRevisaSuEntregaMarcada`, `#soloElEstudiantePuedeRevisarSuEntrega` | Run del CI |
| HU-3 · El apoderado ve el progreso de su pupilo | `GET /v1/students/{id}/progress`, `GET /v1/students/{id}/evaluations` | `FlujoEvaluacionTest`, `SeguridadTest#progresoSegunRol`, `RevisionEstudianteTest#apoderadoYDocenteVenLasEvaluacionesDelEstudiante` | Run del CI |
| Aislamiento multi-tenant: otro colegio recibe 404 | `GET /v1/evaluations/{id}`, `GET /v1/students/{id}/progress` | `SeguridadTest#docenteDeOtroColegioRecibe404`, `#coordinadorDeOtroColegioRecibe404`, `#estudianteDeOtroColegioNoPuedeRendir`, `#docenteDeOtroColegioNoCreaEnCursoAjeno` | `d3-01` y `d3-02` (prueba de código roto) |
| RBAC: rol sin permiso recibe 403 | `POST /v1/evaluations` y otros | `SeguridadTest#estudianteNoPuedeCrearEvaluaciones`, `#apoderadoNoVeEvaluacionesNiOtrosEstudiantes` | Run del CI |
| Sin credenciales: 401 sin ventana del navegador | Todos los `/v1/**` | `SeguridadTest#sinCredencialesOConClaveMalaDa401SinVentanaDelNavegador` | Run del CI |
| Cabeceras de seguridad en la UI (CSP, Permissions-Policy) | `/index.html` | `SeguridadTest#cabecerasDeSeguridadEnLaUi` | `d3-03` (DAST en verde) |
| Contrato: cada respuesta real está documentada en `api/openapi.yaml` | Las 8 operaciones implementadas | `ContratoOpenApiTest#lasRespuestasRealesCumplenElContrato` | Run del CI |
| Outbox: al rendir se registra `EvaluationCompleted` | `POST /v1/evaluations/{id}/submissions` | `FlujoEvaluacionTest#docenteCreaEstudianteRindeYApoderadoVeProgreso` | Run del CI |
| HU-4 · Tutor IA responde con los apuntes del curso | `POST /v1/tutor/questions` | Pendiente (Día 4). Hoy: `SeguridadTest#tutorEsStubSoloParaEstudiantes` | — |

## 2. El test de aislamiento detecta código roto

Para comprobar que los tests de aislamiento no son falsos positivos:

1. En la rama `s18-dia3` se cambió a propósito `EvaluationRepository` para que la búsqueda ignorara el `tenantId` (commit `f16050e`).
2. El CI falló y detectó la fuga en 3 tests de `SeguridadTest` (`docenteDeOtroColegioRecibe404`, `coordinadorDeOtroColegioRecibe404`, `estudianteDeOtroColegioNoPuedeRendir`) y 1 de `RevisionEstudianteTest`. Por ejemplo, `docenteDeOtroColegioRecibe404` esperaba 404 y obtuvo 403, porque el docente del Colegio B ya podía encontrar la evaluación del Colegio A. Capturas: `evidence/s18/d3-01-test-codigo-roto-pipeline.png` y `evidence/s18/d3-02-test-codigo-roto-detalle.png`.
3. Se restauró el filtro (commit `bfc400c`) y esos tests volvieron a verde.

## 3. Contract test

`ContratoOpenApiTest` lee `api/openapi.yaml` (OpenAPI 3.1) con SnakeYAML, sin dependencias nuevas. Para cada llamada real comprueba que el código de estado esté declarado en la operación y que la respuesta traiga los campos `required` del schema. También falla si una operación del contrato queda sin probar. Solo excluye `POST /v1/tutor/questions` hasta el Día 4.

Se descartó `swagger-request-validator-mockmvc` por su soporte limitado de OpenAPI 3.1 y porque suma dependencias transitivas al escaneo SCA.

## 4. Hallazgos QA del Día 3

| Hallazgo | Estado |
|---|---|
| El primer intento de test (`SecurityAndIntegrationTests`) usaba rutas sin `/v1`, un header `X-Tenant-ID` que la API no usa y credenciales inexistentes, por eso fallaba siempre (401/403) | Corregido: se eliminó; sus casos ya estaban en `SeguridadTest` y el contrato quedó en `ContratoOpenApiTest` |
| `GET /v1/evaluations/{id}` para el estudiante no expone `correctOption` | Verificado (`FlujoEvaluacionTest`) |
