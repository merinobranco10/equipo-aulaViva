# ADR 0007 — Estrategia multi-tenant

**Estado:** Aceptada · **Fecha:** 17-11-2026

## Contexto

Cada colegio es un tenant. La plataforma maneja datos de estudiantes menores de edad (Ley 21.719) y el aislamiento entre colegios es el NFR de mayor prioridad (ver `docs/arch/atributos-calidad.md`).

## Decisión

- Columna `tenant_id` en las tablas de negocio con propietario directo: `users`, `courses`, `evaluations`, `submissions` y `outbox_events`. 
`questions`, `options`, `answers` y `grades` heredan el tenant de su agregado.
- Filtro obligatorio por tenant en los repositorios (`findByIdAndTenantId`, `findByTenantIdAnd...`).
- El tenant se obtiene **siempre** del usuario autenticado (`TenantContext`), nunca de un parámetro, header o body del request.
- Un recurso de otro colegio responde **404** (no 403), para no revelar que existe.
- La idempotencia es por tenant: `UNIQUE (tenant_id, idempotency_key)`.

## Alternativas consideradas

| Alternativa | Aislamiento | Costo/operación | Complejidad MVP | Veredicto |
|---|---|---|---|---|
| BD por tenant | Máximo | Alto (N bases) | Alta | Descartada: costo y migraciones x N |
| Schema por tenant | Alto | Medio | Media | Descartada: migraciones por schema y pool de conexiones |
| RLS de PostgreSQL | Alto (en la BD) | Bajo | Media | Diferida: siguiente paso |
| `tenant_id` + filtro en app | Medio | Bajo | Baja | **Elegida** |

## Consecuencias

- (+) Simple, barata y testeable; una sola base y un solo juego de migraciones Flyway.
- (−) Depende de la disciplina del código: un repositorio sin filtro filtraría datos. Se mitiga con tests cross-tenant.
- (−) `JpaRepository` expone `findById` sin filtro: no se usa para entidades de negocio; revisar en cada PR.
- (−) `AcademicDirectory` usa SQL directo (JDBC) sobre `enrollments` y `student_guardians`, que no tienen `tenant_id`; hoy es seguro porque siempre se consulta después de cargar el recurso filtrado por tenant. Si se reutiliza de forma aislada, hay que filtrar a mano.
- Camino de evolución: agregar RLS en PostgreSQL como defensa en profundidad cuando el staging use PostgreSQL de forma permanente.

## Verificación

Tests en `SeguridadTest` (fallan si se quita el filtro por tenant):

- `docenteDeOtroColegioRecibe404`
- `estudianteDeOtroColegioNoPuedeRendir`
- `docenteDeOtroColegioNoCreaEnCursoAjeno`
- `coordinadorDeOtroColegioRecibe404`
- `progresoSegunRol` (incluye estudiante de otro colegio → 404)

Escenarios de negocio: `docs/scenarios/multy-tenancy.feature`.
