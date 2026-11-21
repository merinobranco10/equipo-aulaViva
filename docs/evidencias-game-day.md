# Evidencias Game Day SF-D5 (Ambos)

## Simulación de Escenarios (E1, E2, E3) y RTO/RPO

| Escenario | Descripción | RTO (Recuperación) | RPO (Pérdida de datos) | Tiempo de simulación real |
| :--- | :--- | :--- | :--- | :--- |
| **E1** | Caída del LLM (Tutor IA) | 5 minutos | 0 (Sin pérdida, es stateless) | 3 minutos (Se activó el fallback) |
| **E2** | Pérdida de Base de Datos (PostgreSQL) | 2 horas | 15 minutos | 45 minutos (Restore desde backup) |
| **E3** | Caída de la Aplicación (Backend) | 10 minutos | 0 (Sin pérdida, K8s reinicia pods) | 2 minutos (Auto-healing de K8s) |

*Nota: Los tiempos de simulación real se midieron durante la ejecución del Game Day con el equipo de QA y DevSecOps.*
