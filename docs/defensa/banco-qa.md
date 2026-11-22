# Banco de preguntas del panel — AulaViva S18

**Regla de respuesta:** cada respuesta incluye un dato verificable, un enlace de evidencia y una limitación/pendiente cuando aplique. Responde quien construyó esa parte. No improvisar cifras ni afirmar funcionalidades diferidas como implementadas.

| # | Pregunta del panel | Responde | Dato | Evidencia | Qué no hicimos / límite |
|---:|---|---|---|---|---|
| 1 | ¿Por qué event-driven vs. sincrónico? | Matías | Outbox en tabla. | [ADR 0004 datos y eventos](../adr/0004-datos-y-eventos.md) | Kafka/MSK no está desplegado en el MVP. |
| 2 | ¿Qué alternativas descartaron en el ADR 0004? | Matías | Outbox en tabla. | [ADR 0004 datos y eventos](../adr/0004-datos-y-eventos.md) | No afirmar que Kafka está operativo; abrir la tabla del ADR durante la respuesta. |
| 3 | ¿Cómo evoluciona a 100× tráfico? | Matías | 	HPA configurado; Tutor IA identificado como primer servicio a separar. | [C4](../c4/) · [ADR 0002](../adr/0002-estilo-arquitectonico.md) · [HPA](../../k8s/hpa.yaml) | No se ha demostrado una prueba de carga de 100×; presentar como estrategia de evolución, no como capacidad validada. |
| 4 | Muéstrame un test que falle si borro esta función. | Alexander | Mostrar el test de aislamiento entre colegios y el resultado/captura del Día 3; el caso de docente de otro colegio debe recibir 404. | [Trazabilidad QA](../qa/trazabilidad.md) · [`SeguridadTest`](../../backend/src/test/java/cl/aulaviva/evaluations/SeguridadTest.java) · [`TutorSeguridadTest`](../../backend/src/test/java/cl/aulaviva/tutor/TutorSeguridadTest.java) | No reemplazar el test real por una explicación; abrir el caso concreto en el código/CI. |
| 5 | ¿Dónde está el mayor riesgo técnico hoy? | Matías | El aislamiento se aplica en la capa de aplicación, en el roadmap. | [ADR 0007](../adr/0007-estrategia-multi-tenant.md) · [Threat model](../threat-model.md) | RLS no está implementado; no afirmar aislamiento a nivel de base de datos. |
| 6 | ¿Cómo miden que ayuda al usuario? | Branco | El Impact Map define impactos observables: tiempo de corrección ahorrado, uso del tutor y porcentaje de respuestas con fuente. | [Impact Map](../impact-map.md) | No afirmar mejora cuantificada si aún no existe medición de usuarios reales. |
| 7 | ¿Costo por usuario / tenant? | Branco | 	Escenario base: 3.000 estudiantes (5 colegios × 600). | [Costos](../finops/costos.md) · [Mediciones](../finops/mediciones.md) | Usar el cálculo final del documento FinOps; no inventar un precio ni confundir plan gratuito de demo con precio de pago del escenario. |
| 8 | ¿Qué feature da más valor por esfuerzo? | Branco | Corrección automática con feedback inmediato. | [Impact Map](../impact-map.md) | El ahorro real debe medirse con docentes; no presentarlo como resultado ya demostrado. |
| 9 | Si tuvieran dos semanas más, ¿qué construirían? | Branco | Keycloak/OIDC; RLS; pgvector. | [Roadmap README](../../README.md#roadmap) | Son prioridades futuras. |
| 10 | ¿Break-even estimado? | Branco |  Fórmula de break-even con precio por estudiante supuesto y costo variable por colegio. | [Costos](../finops/costos.md) | El precio por estudiante es un supuesto de negocio, no un precio comercial validado. |
| 11 | ¿Cómo aplica la Ley 21.719? | Gerardo | Explicar las medidas de minimización, protección y tratamiento descritas en el marco legal. | [Marco legal](../ai/marco-legal.md) | No dar asesoría legal ni afirmar cumplimiento/certificación; describir controles y brechas. |
| 12 | Muéstrame un secret: ¿dónde vive y quién lo rota? | Valentina | 	GitHub Secrets / secret manager, según el entorno. Explicar responsable y procedimiento de rotación documentado. | [Secrets](../security/secrets.md) · GitHub Actions | Nunca mostrar el valor de un secreto ni copiarlo a logs o presentación. |
| 13 | ¿Qué hace el sistema frente a un prompt injection? | Gerardo | Guardrails. 4/4 casos de prompt injection bloqueados en el golden set; `GuardrailsTest` y `TutorSeguridadTest` lo verifican en cada CI. | [Guardrails](../../backend/src/main/java/cl/aulaviva/tutor/Guardrails.java) · [Eval report](../ai/eval-report.md) · [Model card](../ai/model-card.md) | La muestra es de cuatro casos del golden set; no equivale a garantía frente a todo ataque posible. |
| 14 | ¿Cómo demuestran fairness a un regulador? | Gerardo | Diferencia entre cursos: Hit@3 13 pp; hechos clave 9 pp. | [Eval report](../ai/eval-report.md) | Es un chequeo básico con dos cursos y 50 casos, no una auditoría integral de equidad. |
| 15 | Enséñame el último post-mortem. | Alexander | Abrir el último post-mortem y recorrer impacto, causa, recuperación y acciones. | [Post-mortem del game day](../postmortems/2026-11-22-game-day.md) · [Runbooks](../runbooks/) | Confirmar que el archivo existe y está actualizado antes de la defensa. |
| 16 | ¿Qué no alcanzaron? | Matías | Pendientes documentados en «Lo que no alcanzamos». | [Qué no se alcanzó README ](../../README.md#lo-que-no-alcanzamos-y-por-qué) | No ocultar brechas ni convertir el roadmap en funcionalidades ya entregadas. |

## Pautas para responder
1. Respuesta inicial de 20–40 segundos; ampliar solo si el panel lo pide.
2. Mostrar el dato primero, después abrir la evidencia.
3. Si el dato no está medido, decirlo explícitamente.
4. Distinguir MVP implementado, diseño aprobado y roadmap.
5. Nunca mostrar credenciales, datos reales de menores ni conversaciones completas del tutor.
