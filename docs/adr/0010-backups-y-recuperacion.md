# ADR 0010: Estrategia de Backups y Recuperación
**Estado:** Aceptado
**Contexto:** Necesitamos garantizar un RPO de 15 minutos y un RTO de 2 horas para el sistema.
**Decisión:** Implementaremos backups automatizados de PostgreSQL cada 15 minutos usando CronJobs en Kubernetes, almacenados en AWS S3 (con cifrado en reposo). Para el restore, usaremos el script `restore.sh`.
**Consecuencias:** Aumento en los costos de almacenamiento, pero cumplimiento garantizado de los SLOs y DR plan.
