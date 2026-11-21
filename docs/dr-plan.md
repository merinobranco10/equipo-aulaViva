# Plan de Recuperación ante Desastres (DR Plan)

## Objetivos
- **RTO (Recovery Time Objective):** 2 horas (Tiempo máximo para restaurar el servicio).
- **RPO (Recovery Point Objective):** 15 minutos (Pérdida máxima de datos aceptable).

## Escenarios de Desastre
1. **Caída de la Región Cloud:** Si el proveedor principal falla, se debe desplegar la infraestructura en la región secundaria usando Terraform.
2. **Corrupción de Base de Datos:** Restaurar el último backup válido en una instancia nueva, verificar integridad y apuntar la aplicación a ella.
3. **Fallo del LLM (OpenAI/Anthropic):** Conmutar el tráfico al proveedor de respaldo (Fallback) configurado en la aplicación.

## Pasos de Recuperación (Runbook General)
1. Declarar el incidente y notificar al equipo.
2. Evaluar el alcance del fallo.
3. Ejecutar el script de restauración correspondiente (`restore.sh` o Terraform apply).
4. Validar que los SLOs vuelvan a la normalidad.
