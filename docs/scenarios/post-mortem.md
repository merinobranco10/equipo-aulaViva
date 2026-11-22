# Post-Mortem: Game Day SF-D5
**Fecha:** 21 de Noviembre de 2026
**Escenarios probados:** Caída de LLM, Pérdida de BD.

## Resultados
- El sistema se recuperó del fallo de BD en 45 minutos (RTO cumplido).
- El fallback del LLM funcionó correctamente.

## Lecciones aprendidas
- El script de `restore.sh` tardó más de lo esperado por el volumen de datos.

## Acciones correctivas
- Optimizar `restore.sh` y ajustar las alertas de Prometheus.
