# Model Card — Tutor IA AulaViva

## 1. Información general

| Campo | Descripción |
|---|---|
| Sistema | Tutor IA AulaViva |
| Tipo | Asistente educativo basado en LLM y RAG |
| Versión | 0.3 (tutor implementado, 20-11-2026) |
| Responsable | Equipo AI/Data |
| Estado | Implementado, probado con tests automatizados y evaluado con el golden set (20-11-2026) |
| Público objetivo | Estudiantes de educación escolar |
| Proveedor | Groq (API compatible con OpenAI), plan gratuito: 1.000 solicitudes/día y 8.000 tokens/min por modelo |
| Modelo principal | `openai/gpt-oss-120b` |
| Modelo de respaldo | `openai/gpt-oss-20b` |
| Decisión | [ADR 0009](../adr/0009-tutor-ia-proveedor-llm.md) |

## 2. Propósito

El Tutor IA tiene como objetivo apoyar el aprendizaje de los estudiantes mediante respuestas a preguntas académicas relacionadas con los contenidos de sus cursos.

El sistema utiliza recuperación aumentada por generación (RAG) para fundamentar sus respuestas en los apuntes asociados al curso del estudiante.

## 3. Funcionamiento

1. El estudiante envía una pregunta y el identificador del curso.
2. El backend valida la identidad y los permisos del estudiante.
3. El sistema identifica el tenant y el curso autorizado.
4. Los guardrails de entrada bloquean intentos de prompt injection y quitan datos personales (RUT, correos, teléfonos) antes de seguir.
5. El componente RAG busca con BM25 (palabras clave) los fragmentos más relevantes en `backend/src/main/resources/corpus/<courseId>/` (top-k configurable, 5 por defecto). Si el mejor puntaje no llega a `min-score` (1,5, calibrado con el golden set), responde que no encuentra eso en los apuntes sin llamar al LLM.
6. El LLM (Groq, `openai/gpt-oss-120b`, `reasoning_effort: low`) genera una respuesta usando solo el contexto recuperado, con timeout de 8 s. Ante un 429 reintenta con `openai/gpt-oss-20b`.
7. Si Groq falla, demora más de 8 s o la respuesta no cita una fuente recuperada, se responde con un **fallback extractivo** (el fragmento más relevante, sin LLM). Si se agota el presupuesto diario del colegio, responde 429.
8. El backend entrega la respuesta y las fuentes utilizadas.

## 4. Datos de entrada

- Identificador del curso (`courseId`).
- Pregunta escrita por el estudiante.
- Fragmentos de apuntes recuperados desde el corpus del curso.

Al proveedor solo se envían la pregunta (sin datos personales) y los fragmentos recuperados. No se envían nombre, RUT, correo, notas ni identificadores del estudiante.

## 5. Datos de salida

La respuesta de la API contempla:

- `answer`: respuesta generada por el tutor.
- `sources`: archivos de apuntes usados.
- `mode`: `llm`, `cache`, `extractivo` o `bloqueado`.
- `guardrail`: `injection` o `fuera_alcance`, solo si se activó.

## 6. Corpus inicial

| Curso | Identificador | Cantidad de apuntes |
|---|---|---:|
| Matemática 8° básico A | matematica-8a | 5 |
| Ciencias Naturales 8° básico B | ciencias-8b | 4 |

Los apuntes viven en `backend/src/main/resources/corpus/<courseId>/` (se empaquetan en el JAR). Cada uno indica su OA y la fuente (Programa de Estudio MINEDUC, curriculumnacional.cl). Son materiales de demostración redactados por el equipo.

OA verificados en curriculumnacional.cl el 18-11-2026:

| Curso | OA cubiertos |
|---|---|
| matematica-8a | MA07 OA 01 (enteros, base), MA08 OA 01 (multiplicación y división de enteros), MA08 OA 03 (potencias; el apunte lo amplía a base entera) |
| ciencias-8b | CN08 OA 02 (célula y sus partes, organelos), CN08 OA 07 (fotosíntesis y respiración celular) |

## 7. Comportamiento esperado

- Responder preguntas respaldadas por los apuntes del curso.
- Mantener las respuestas dentro del contenido curricular asignado.
- Utilizar lenguaje claro y apropiado para estudiantes.
- Evitar inventar información o fuentes.
- Reconocer cuando los apuntes no contienen información suficiente.
- Informar cuando el servicio no esté disponible.

## 8. Limitaciones

- La calidad de las respuestas depende de la calidad y cobertura del corpus.
- El sistema puede recuperar fragmentos poco relevantes.
- Un LLM puede generar respuestas incorrectas o imprecisas.
- La recuperación RAG no garantiza por sí sola que una respuesta sea correcta.
- El proveedor es Groq con `openai/gpt-oss-120b`. Límites del plan gratuito, verificados el 18-11-2026 con la clave del equipo: **1.000 solicitudes por día** y **8.000 tokens por minuto** por modelo (igual para `openai/gpt-oss-20b`). Una pregunta del tutor usa unos 160 tokens en una prueba simple; con 3 fragmentos de contexto se estiman 600–900. Al superar el límite se usa el modelo de respaldo y, si también se agota, el fallback extractivo.
- La búsqueda por palabras clave (BM25) puede fallar si la pregunta usa palabras distintas a las del apunte (no hay embeddings; pgvector queda diferido).
- La caché y el presupuesto diario viven en memoria: se pierden al reiniciar y no se comparten entre réplicas.
- Búsqueda léxica (BM25): las preguntas cortas o con palabras distintas a las del apunte pueden no encontrar el fragmento correcto. Ciencias rinde menos que Matemática (ver la sección 10).

## 9. Seguridad y privacidad

- El acceso al tutor debe limitarse a estudiantes autenticados.
- El backend debe validar que el estudiante pertenece al curso consultado.
- Los datos de un colegio no deben mezclarse con los de otro.
- La clave `LLM_API_KEY` se guarda como secreto (GitHub Actions / Kubernetes Secret) y se entrega por privado; nunca en el repo, Discord ni capturas.
- No se deben incluir credenciales en GitHub.
- Se deben minimizar los datos personales enviados al proveedor externo.
- Las preguntas y respuestas deben tratarse como información potencialmente sensible.

## 10. Evaluación

Se usa [`rag/eval/golden-set.jsonl`](../../rag/eval/golden-set.jsonl) (formato en [`rag/eval/README.md`](../../rag/eval/README.md)). Cada caso indica el `usuario` demo que pregunta.

El conjunto contempla 50 casos: 40 con respuesta en los apuntes (22 de Matemática y 18 de Ciencias), 3 fuera de tema, 1 de otro colegio (estudiante.b consultando `matematica-8a`, debe dar 404), 4 intentos de prompt injection, 1 con datos personales (RUT) y 1 pidiendo datos de otro estudiante.

El script es [`rag/eval/run_eval.py`](../../rag/eval/run_eval.py) y los resultados se registran en [`eval-report.md`](eval-report.md).

| Métrica | Meta |
|---|---:|
| Hit@3 (la fuente correcta entre las 3 primeras) | ≥ 80 % |
| Hechos clave presentes en la respuesta | ≥ 70 % |
| Fidelidad (cita una fuente recuperada) | ≥ 90 % |
| Bloqueo de prompt injection | 100 % |
| Rechazo fuera de alcance | 100 % |
| Aislamiento entre colegios (404) | 100 % |
| Datos personales tapados | 100 % |

Criterios de evaluación:

- Exactitud factual.
- Correspondencia con las fuentes recuperadas.
- Pertinencia curricular.
- Claridad de la respuesta.
- Comportamiento ante consultas sin información suficiente.
- Aislamiento de información entre cursos y colegios.
- Manejo de errores y falta de disponibilidad.

La evaluación debe ser ejecutada y registrada antes de considerar el sistema apto para producción.

## 11. Uso fuera de alcance

El Tutor IA:

- No reemplaza al docente.
- No debe utilizarse para calificar estudiantes.
- No toma decisiones académicas automatizadas.
- No entrega información personal de otros estudiantes.
- No responde con información ajena al corpus autorizado.

## 12. Riesgos

| Riesgo | Mitigación |
|---|---|
| Respuestas incorrectas | Evaluación con casos de prueba y revisión humana |
| Información fuera del currículo | Recuperación limitada al corpus autorizado |
| Filtración entre colegios | Validación de tenant y permisos en backend |
| Exposición de claves | Secretos gestionados fuera del repositorio |
| Indisponibilidad o cuota agotada en Groq | Timeout de 8 s, Resilience4j y fallback extractivo |
| Prompt injection | Guardrails de entrada y prompt de sistema que limita al contexto |
| Datos personales enviados al proveedor | Redacción de PII antes de llamar al LLM |
| Respuestas inadecuadas | Filtros, supervisión y pruebas periódicas |

## 13. Estado de implementación (20-11-2026)

| Componente | Estado |
|---|---|
| BM25 por curso, prompt, guardrails, PII, cliente Groq con respaldo, fallback extractivo, caché, presupuesto y métricas | Implementado |
| Tests automatizados (`TutorFlowTest`, `GuardrailsTest`, `PiiMaskerTest`, `TutorPromptTest`, contrato y seguridad) | En verde |
| Evaluación con los 50 casos del golden set | Hecha: [`eval-report.md`](eval-report.md) |
| Calibración de `min-score` | Hecha: 1,5 |
| Mediciones FinOps | Hechas: [`../finops/mediciones.md`](../finops/mediciones.md) |

Resultados principales:

| Métrica | Meta | Resultado |
|---|---:|---:|
| Hit@3 | ≥ 80 % | 85 % |
| Hechos clave | ≥ 70 % | 82 % |
| Fidelidad | ≥ 90 % | 88 % |
| Seguridad, privacidad y aislamiento | 100 % | 100 % |
| Equidad entre cursos | < 15 pts | 13 pts (hit@3) y 9 pts (hechos) |

Matemática cumple todas las metas. La fidelidad total queda 2 puntos bajo la meta porque en 4 casos el modelo prefirió responder «No encuentro eso en tus apuntes» sin citar fuente; no se detectaron respuestas inventadas.
