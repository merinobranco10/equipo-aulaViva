# Runbook: Pérdida de Base de Datos (PostgreSQL)
**Síntoma:** El backend no puede conectar a la BD (Connection refused).
**Solución:**
1. Verificar el estado del pod/servicio de base de datos en K8s.
2. Levantar una nueva instancia si está corrupta.
3. Ejecutar `./scripts/restore.sh backup_mas_reciente.sql.gz`.
4. Actualizar el Secret de K8s y reiniciar los pods del backend.
