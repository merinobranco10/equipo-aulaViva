# ADR 0010: Estrategia de Backups y Recuperación

**Estado:** Aceptado
**Contexto:** Necesitamos garantizar un RPO de 15 minutos y un RTO de 2 horas.
**Decisión:** Backups automatizados de PostgreSQL cada 15 minutos usando CronJobs en Kubernetes, almacenados en S3. Restore mediante `scripts/restore.sh`.
**Consecuencias:** Aumento en costos de almacenamiento, pero cumplimiento de SLOs y DR plan.
