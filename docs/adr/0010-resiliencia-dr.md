# ADR 0010 — Resiliencia y recuperación ante desastres (DR)

**Estado:** Aceptada · **Fecha:** 22-11-2026 · **Autores:** Valentina León (DevSecOps), Alexander Ruiz-Tagle (QA)

## Contexto

AulaViva guarda notas y entregas de estudiantes: perderlas o dejar a un colegio sin servicio durante una evaluación es el peor escenario. Además, el Tutor IA depende de un proveedor externo (Groq) que puede fallar o quedarse sin cuota. El MVP corre en staging con docker compose y la producción está diseñada sobre AWS ([ADR 0003](0003-cloud-style.md)).

## Decisión

1. **Base de datos en producción (diseño):** RDS PostgreSQL **Multi-AZ** con backups automáticos y **restauración a un punto en el tiempo (PITR)**. RPO ≤ 5 min y RTO 30 min.
2. **Staging:** respaldo lógico con `pg_dump` ([`scripts/backup.sh`](../../scripts/backup.sh)) y restauración probada con [`scripts/restore.sh`](../../scripts/restore.sh). RPO 24 h y RTO 30 min.
3. **App:** en Kubernetes, mínimo 2 réplicas con HPA y probes de liveness/readiness; en compose, `restart: unless-stopped` y el runbook.
4. **LLM:** timeout de 8 s, reintento, circuit breaker y **fallback extractivo** (responde con el fragmento del apunte). Ante 429 entra antes el modelo de respaldo `openai/gpt-oss-20b`.
5. **Operación:** SLOs en [slos.md](../sre/slos.md), [plan DR](../sre/dr-plan.md), runbooks en [docs/runbooks/](../runbooks/) y un game day por entrega con post-mortem.

## Alternativas consideradas

| Alternativa | Veredicto |
|---|---|
| Multi-región activo-activo | Descartada: duplica el costo de infraestructura y es desproporcionada para 5 colegios |
| **RDS Multi-AZ + PITR** | **Elegida para producción:** cubre la caída de una zona y la corrupción de datos a un costo moderado |
| Solo respaldos manuales | Aceptada **solo para staging**: no cumple un RPO de minutos |
| LLM: cola asíncrona con reintentos | Descartada: el estudiante necesita la respuesta al momento |
| **LLM: fallback extractivo** | **Elegida:** el tutor nunca queda sin respuesta y no inventa contenido |

## Consecuencias

- (+) Un colegio no pierde notas si cae una zona de AWS o se corrompe la base.
- (+) La caída del LLM no deja al tutor sin respuesta.
- (−) Multi-AZ duplica el costo de la base en producción ([costos.md](../finops/costos.md)).
- (−) En staging el respaldo es manual; entre respaldos se pueden perder hasta 24 h de datos.
- (−) En compose la app no se reinicia sola si se detiene a mano: depende del runbook.

## Verificación

Game day del Día 5 (E1 LLM caído, E2 pérdida de la base, E3 caída de la app bajo carga): resultados en el [post-mortem](../postmortems/2026-11-22-game-day.md).
