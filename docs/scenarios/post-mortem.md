# Post-Mortem: Game Day SF-D5
**Fecha:** [Fecha de hoy]
**Escenarios probados:** Caída de LLM, Pérdida de BD, Caída de App.
**Resultados:**
- El sistema se recuperó del fallo de BD en [X] minutos (RTO cumplido).
- **Lecciones aprendidas:** El script de restore tardó más de lo esperado porque faltaba un índice en la BD. 
- **Acciones correctivas:** Optimizar `restore.sh` y añadir monitoreo en Prometheus para alertar antes de que la BD se caiga.
