# Plan de recuperación ante desastres (DR)

**Responsable:** Valentina León (DevSecOps) · **Actualizado:** 22-11-2026 · Decisión en el [ADR 0010](../adr/0010-resiliencia-dr.md)

## 1. Alcance

Datos de evaluaciones, entregas y progreso en PostgreSQL, y la disponibilidad de la API y del Tutor IA. Los apuntes del tutor viajan dentro de la imagen, así que no necesitan respaldo aparte.

## 2. Objetivos

| | Staging (docker compose / Codespace) | Producción (diseño, ADR 0003) |
|---|---|---|
| **RPO** (datos que se pueden perder) | 24 h: `pg_dump` diario con `scripts/backup.sh` (manual en staging) | ≤ 5 min: backups automáticos de RDS con restauración a un punto en el tiempo (PITR) |
| **RTO** (tiempo para volver a operar) | 30 min | 30 min: falla de zona cubierta por RDS Multi-AZ; pods reiniciados por Kubernetes (HPA mínimo 2) |

## 3. Estrategia

- **Staging:** respaldo lógico con [`scripts/backup.sh`](../../scripts/backup.sh) y restauración con [`scripts/restore.sh`](../../scripts/restore.sh). Los respaldos quedan en `backups/` (no se versionan).
- **Producción (diseño):** RDS PostgreSQL Multi-AZ con backups automáticos de 7 días y PITR. No está desplegado: el costo está en [costos.md](../finops/costos.md).
- **LLM caído o sin cuota:** el tutor sigue respondiendo en modo extractivo (timeout de 8 s, reintento, circuit breaker y respaldo `gpt-oss-20b` ante 429).

## 4. Roles

| Rol | Quién | Qué hace |
|---|---|---|
| Comandante del incidente | Valentina (DevSecOps) | Declara el incidente, sigue el runbook y avisa por Discord `#incidentes` |
| Escriba | Alexander (QA) | Anota la hora de cada evento y escribe el post-mortem |
| Escalamiento | Matías (Tech Lead) | Decide si se escala o se revierte un despliegue |

## 5. Procedimientos

- Base de datos perdida o corrupta: [runbook restaurar-bd](../runbooks/restaurar-bd.md).
- Tutor degradado (LLM caído o sin cuota): [runbook tutor-degradado](../runbooks/tutor-degradado.md).
- App caída: `docker compose ps` → `docker compose start app` → esperar `/actuator/health` en `UP`.

## 6. Calendario de pruebas

| Prueba | Frecuencia | Última ejecución |
|---|---|---|
| Restauración de un respaldo (E2 del game day) | Antes de cada entrega y una vez al mes | Ver [post-mortem](../postmortems/2026-11-22-game-day.md) |
| Game day completo (E1, E2, E3) | Una vez por entrega | Ver [post-mortem](../postmortems/2026-11-22-game-day.md) |
