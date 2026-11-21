# Runbook: Pérdida de Base de Datos
**Síntoma:** La app no puede conectar a la BD (Connection Refused). Pods en CrashLoopBackOff.
**Solución:**
1. Levantar una nueva instancia de base de datos (RDS/Cloud SQL).
2. Ejecutar `./restore.sh backup_mas_reciente.sql.gz`.
3. Actualizar el Secret de K8s con la nueva IP/Endpoint de la BD.
4. Reiniciar los pods de la aplicación.
