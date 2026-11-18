# ADR 0009 — Tutor IA: proveedor LLM (Groq) y RAG léxico

**Estado:** Aceptada · **Fecha:** 18-11-2026 · **Reemplaza en parte:** la elección de LLM de ADR 0003 y `docs/arch/managed-services.md`

## Contexto

El Tutor IA responde preguntas de estudiantes usando solo los apuntes de su curso (9 apuntes en dos cursos de demo). Restricciones del MVP:

- La defensa es en días y el equipo trabaja en PCs de 8 GB o en Codespaces: no se puede correr un modelo local ni una base vectorial extra.
- No hay presupuesto: se necesita un plan gratuito sin tarjeta y que **no use los datos para entrenar**.
- Usuarios menores de edad: hay que minimizar los datos enviados al proveedor (Ley 21.719, vigencia prevista 1-dic-2026).
- El tutor no puede caerse en la demo si el proveedor falla o se agota la cuota.

La decisión original (ADR 0003) era Anthropic Claude por API, que es de pago.

## Decisión

1. **Proveedor: Groq**, por su API compatible con OpenAI (`https://api.groq.com/openai/v1`).
   - Modelo principal: `openai/gpt-oss-120b`.
   - Respaldo: `openai/gpt-oss-20b` (ante HTTP 429 o error del principal).
   - Ambos son modelos de producción en Groq, no preview.
   - Son modelos de razonamiento: se llaman con `reasoning_effort: "low"` y `max_tokens` de al menos 300. Con un límite menor, el razonamiento consume todos los tokens y la respuesta llega vacía (verificado el 18-11-2026).
2. **Recuperación: BM25 léxico** sobre `backend/src/main/resources/corpus/<courseId>/`, top-k = 3, filtrado siempre por el curso y el colegio del estudiante autenticado. Sin embeddings ni pgvector.
3. **Guardrails de entrada:** bloqueo de patrones de prompt injection ("ignora tus instrucciones", "actúa como", etiquetas falsas de sistema) y redacción de PII (RUT, correo, teléfono) antes de buscar y de llamar al LLM.
4. **Prompt de sistema acotado:** responder solo con el contexto entregado; si no está, decir "No tengo información sobre eso en los apuntes del curso".
5. **Resiliencia:** timeout de 8 s, circuit breaker y reintento al modelo de respaldo con Resilience4j. Si todo falla, **fallback extractivo** (se devuelve el fragmento más relevante, sin LLM).
6. **Presupuesto por colegio** (`TUTOR_DAILY_BUDGET`): al superarlo, solo responde el fallback.
7. **Minimización de datos:** a Groq solo viajan la pregunta limpia y los fragmentos. Nunca nombre, RUT, notas ni IDs.
8. **Configuración** por variables `LLM_BASE_URL`, `LLM_MODEL`, `LLM_FALLBACK_MODEL`, `LLM_API_KEY` (secreto) y `LLM_TIMEOUT_SECONDS`, detalladas en `docs/tutor-ia/configuracion-proveedor.md`.

## Alternativas consideradas

| Alternativa | Costo | Privacidad | Latencia / RAM | Esfuerzo MVP | Veredicto |
|---|---|---|---|---|---|
| **Groq (`openai/gpt-oss-120b`)** | Plan gratuito sin tarjeta | No entrena con datos de la API | Muy baja latencia; 0 RAM local | Bajo (API estilo OpenAI) | **Elegida** |
| Groq `llama-3.3-70b-versatile` | — | — | — | — | Descartada: Groq lo retiró del plan normal el 16-08-2026 (solo Enterprise); la prueba con la clave del equipo dio 404 |
| Google Gemini (plan gratuito) | Gratis con cuotas | En el plan gratuito los datos pueden usarse para mejorar productos | Baja; 0 RAM local | Bajo | Descartada: uso de datos de menores en el plan gratuito y cuotas reducidas |
| Anthropic Claude Haiku | De pago por token | No entrena con datos de la API | Baja; 0 RAM local | Bajo | Descartada para el MVP por costo; **opción de producción** (cambiar `LLM_*`) |
| Modelo local (Ollama, 7-8B) | Gratis | Total, nada sale del equipo | Necesita 8+ GB solo para el modelo | Medio | Descartada: no cabe en PCs de 8 GB junto al backend |
| RAG con embeddings + pgvector | Bajo | Igual que el proveedor | Base extra y modelo de embeddings | Alto | Diferida: con 9 apuntes BM25 basta |
| Fine-tuning de un modelo | Alto | Requiere enviar datos de entrenamiento | — | Muy alto | Descartada: el contenido cambia por curso; RAG es más simple |

## Consecuencias

- (+) Costo cero en la demo y sin infraestructura extra; corre en Codespaces.
- (+) Cambiar a otro proveedor compatible con OpenAI (o a Claude en producción) es solo configuración.
- (+) El fallback extractivo mantiene el tutor útil aunque Groq falle o no haya clave.
- (−) Dependencia de un proveedor externo con cuotas del plan gratuito (solicitudes por minuto y por día).
- (−) BM25 falla con sinónimos o preguntas parafraseadas; se mide con el golden set.
- (−) Los guardrails por patrones no detectan todos los ataques. Groq ofrece `llama-prompt-guard-2` (preview) como posible mejora futura.
- (−) Hay que custodiar `LLM_API_KEY`: se entrega por privado y vive solo en secrets de GitHub o Kubernetes.

## Verificación (Día 4)

- Ejecutar `rag/eval/golden-set.jsonl` (50 casos): ≥ 80 % en `answer` y 100 % en bloqueo, aislamiento entre colegios y PII. Registrar los resultados en `docs/ai/model-card.md`.
- Test de integración: estudiante de otro colegio → 404; docente → 403.
- Test con el proveedor simulado caído o lento (> 8 s): responde el fallback extractivo.
- Revisar en los logs que no aparezcan la clave ni datos personales.

## Actualización 18-11-2026

La decisión inicial nombraba `llama-3.3-70b-versatile` como modelo principal. Al probar la clave del equipo, ese modelo respondió 404: Groq lo retiró del plan normal el 16-08-2026 y recomienda `openai/gpt-oss-120b`. Se cambia el modelo principal a `openai/gpt-oss-120b`; el de respaldo sigue siendo `openai/gpt-oss-20b`, que respondió correctamente en la prueba.
