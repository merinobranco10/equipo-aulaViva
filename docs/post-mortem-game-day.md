# Post-mortem del Game Day (S18) — Análisis de QA

**Fecha:** 20 de noviembre de 2026
**Participantes:** Alexander Ruiz-Tagle (QA Lead), Valentina León (DevSecOps)

## 1. Resumen Ejecutivo
Se realizó un "Game Day" simulando 3 fallas críticas para poner a prueba la resiliencia de AulaViva. El objetivo fue validar que los tiempos de recuperación (RTO/RPO) definidos en el Plan de Recuperación ante Desastres (DR Plan) se cumplieran y que los SLOs se mantuvieran estables.

## 2. Resultados de los Escenarios vs. Objetivos (DR Plan)

| Escenario | Descripción | RTO Objetivo | RTO Real (Medido) | RPO Objetivo | RPO Real | ¿Cumple SLO? |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **E1** | Caída del LLM (Tutor IA) | 5 minutos | **3 minutos** (Fallback activo) | 0 | **0** | ✅ Sí |
| **E2** | Pérdida de Base de Datos | 2 horas | **45 minutos** (Restore) | 15 min | **15 min** (Backup) | ✅ Sí |
| **E3** | Caída del Backend (K8s) | 10 minutos | **2 minutos** (Auto-healing) | 0 | **0** | ✅ Sí |

**Conclusión del QA:** Todos los escenarios de falla se recuperaron en tiempos menores a los definidos como límite (RTO). El sistema demostró alta resiliencia, especialmente en el auto-healing de Kubernetes (E3) y el fallback del LLM (E1).

## 3. Análisis de SLOs (Service Level Objectives)
Durante las pruebas, se validó que el sistema puede mantener los siguientes SLOs incluso bajo estrés:
- **Disponibilidad (99.9%):** El auto-healing de K8s evitó caídas prolongadas.
- **Latencia (p95 < 300ms):** El modo fallback del LLM (búsqueda BM25 local) respondió en menos de 300ms cuando Groq falló.
- **Tasa de Errores (< 1%):** Durante la caída del LLM, los usuarios no recibieron errores 500, sino respuestas de fallback.

## 4. Lecciones Aprendidas y Acciones de Mejora
- **Lo que funcionó bien:** El Circuit Breaker y el fallback del LLM operaron perfectamente. El script de restauración de base de datos (`restore.sh`) fue efectivo.
- **Oportunidad de Mejora (QA):** Aunque la recuperación de la BD (45 min) cumplió el objetivo de 2 horas, es un tiempo alto para el usuario final. Se recomienda automatizar la restauración de la BD para reducir el tiempo a < 15 minutos en futuras iteraciones.
- **Observabilidad:** El equipo detectó las fallas rápidamente gracias a los logs y métricas expuestas por el backend.

## 5. Evidencia
Las capturas de las fallas y la recuperación están documentadas en `docs/evidencias-game-day.md`.
