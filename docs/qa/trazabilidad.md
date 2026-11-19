# Trazabilidad de Requerimientos y Pruebas (S18)

**Responsable:** Alexander Ruiz-Tagle (QA Lead)
**Fecha:** 18 de noviembre de 2026

## 1. Matriz de Trazabilidad

| Requerimiento / Historia de Usuario | Endpoint (API) | Test Automatizado | Evidencia |
|---|---|---|---|
| Crear evaluación (Docente) | `POST /evaluations` | `testCreateEvaluationSuccess` | Pipeline Run |
| Aislamiento Multi-Tenant (No ver datos de otro colegio) | `GET /evaluations/{id}` | `testCrossTenantIsolationIsBlocked` | `01-test-codigo-roto-pipeline.png`<br>`02-test-codigo-roto-detalle.png` |
| Control de Acceso por Roles (Estudiante no crea eval) | `POST /evaluations` | `testStudentCannotCreateEvaluation` | Pipeline Run |
| Tutor IA responde con contexto | `POST /tutor/questions` | (Pendiente para el Día 4) | - |

## 2. Validación del Test de Aislamiento (Prueba de Código Roto)
Para garantizar que el test de aislamiento multi-tenant realmente funciona (y no es un falso positivo), se realizó la siguiente prueba:
1. Se modificó intencionalmente el `EvaluationRepository` para que ignore el `tenantId` en la consulta a la base de datos.
2. El pipeline de GitHub Actions se ejecutó y el test `testCrossTenantIsolationIsBlocked` **falló exitosamente** (Captura: `01-test-codigo-roto-pipeline.png` y `02-test-codigo-roto-detalle.png`).
3. Se restauró el código original y el test volvió a su estado esperado.

## 3. Hallazgos y Pendientes (QA)
- **Hallazgo:** Los tests de seguridad están devolviendo códigos `401` (No autorizado) y `403` (Prohibido) en lugar de evaluar la lógica del negocio. Esto se debe a que las credenciales (usuarios y contraseñas) definidas en los tests no coinciden con los datos semilla (DemoDataLoader) creados en la rama.
- **Acción requerida:** Solicitar al Product Owner (Branco) o al Tech Lead (Matías) las credenciales reales de los 6 usuarios de prueba para actualizar los headers `Authorization` en la clase `SecurityAndIntegrationTests`.
