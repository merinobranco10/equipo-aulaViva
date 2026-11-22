# SLOs de AulaViva

**Responsable:** Valentina León (DevSecOps) · **Actualizado:** 22-11-2026

| SLO | SLI (cómo se mide) | Objetivo | Error budget (30 días) |
|---|---|---|---|
| Disponibilidad de la API | % de requests sin respuesta 5xx (`http_server_requests_seconds_count`) | 99,5 % | 3,6 h de falla o 0,5 % de los requests |
| Latencia de evaluaciones | p95 de `GET/POST /v1/evaluations*` | < 500 ms | 5 % de los requests sobre el umbral |
| Latencia del tutor | p95 de `tutor.latency` | < 6 s | 5 % de las respuestas sobre el umbral |
| Calidad del tutor | % de respuestas con fuente citada (fidelidad del [eval-report](../ai/eval-report.md)) | ≥ 90 % | — |

## Dónde se miden

- Métricas en `/actuator/prometheus`, restringido al rol **coordinador** (`coordinador.b` en la demo).
- Latencia de evaluaciones: prueba de carga con k6, [`scripts/k6-slo.js`](../../scripts/k6-slo.js) (10 usuarios virtuales, 2 minutos).
- Calidad del tutor: golden set de 50 casos ([eval-report](../ai/eval-report.md)).

## Resultados medidos

| SLO | Resultado | Fuente | Estado |
|---|---|---|---|
| Latencia de evaluaciones | p95 = **356 ms**, 0 % de errores (4.702 solicitudes, 10 usuarios, 2 min). Un primer intento con `EVAL_ID` vacío no cuenta | [k6-resultado.txt](k6-resultado.txt) · [captura](../qa/evidence/s18/gd-04b-k6-carga-umbrales.png) | Cumple |
| Latencia del tutor | Mediana 0,8 s, p95 ≈ 1,1 s con LLM | [eval-report](../ai/eval-report.md) | Cumple |
| Calidad del tutor | Fidelidad 88 % | [eval-report](../ai/eval-report.md) | No cumple (−2 pts) |
| Disponibilidad | Sin medición de 30 días. En el game day: tutor sin errores con el LLM caído (E1), base restaurada en 21 s (E2) y app de vuelta en 38 s tras un `kill` (E3) | [post-mortem](../postmortems/2026-11-22-game-day.md) | Parcial: falta una ventana de 30 días |

Medición preliminar (21-11-2026): k6 contra `/actuator/health` con 10 usuarios dio p95 = 39,61 ms y 0 % de errores ([captura](../qa/evidence/s18/d5-01-k6-preliminar-health.png)). No reemplaza la medición de evaluaciones porque el health no toca la base de datos.
