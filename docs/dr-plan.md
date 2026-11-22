# Plan de Recuperación ante Desastres (DR Plan)

## Objetivos
- **RTO (Recovery Time Objective):** 2 horas.
- **RPO (Recovery Point Objective):** 15 minutos.

## Escenarios de Desastre
1. **Caída de la Región Cloud:** Desplegar la infraestructura en la región secundaria usando Terraform.
2. **Corrupción de Base de Datos:** Restaurar el último backup válido usando `scripts/restore.sh`.
3. **Fallo del LLM (RAG):** Conmutar el tráfico a un proveedor de respaldo o devolver respuestas cacheadas.

## Pasos de Recuperación
1. Declarar el incidente y notificar al equipo.
2. Evaluar el alcance del fallo.
3. Ejecutar el script de restauración correspondiente.
4. Validar que los SLOs vuelvan a la normalidad.
