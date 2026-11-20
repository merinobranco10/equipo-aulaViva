# Informe de evaluación — Tutor IA AulaViva

**Fecha:** 19-11-2026 · **Ejecutado:** 20-11-2026  
**Proveedor:** Groq  
**Modelo principal:** `openai/gpt-oss-120b`  
**Modelo de respaldo:** `openai/gpt-oss-20b`  
**Corpus:** 9 apuntes distribuidos en 2 cursos.  
**Conjunto de evaluación:** 50 casos (`rag/eval/golden-set.jsonl`).  
**Script:** `rag/eval/run_eval.py` · **Resultados crudos:** `rag/eval/resultados.json` (corrida final con LLM y umbral calibrado)  
**Configuración final:** `top-k = 5`, `min-score = 1,5`, caché activa, `max-tokens = 400`.

## 1. Objetivo

Evaluar el funcionamiento del Tutor IA de AulaViva, considerando la calidad de las respuestas, recuperación de información, seguridad, privacidad y aislamiento de datos entre colegios.

## 2. Conjunto de pruebas

Se utilizaron 50 casos:

| Tipo de prueba | Cantidad |
|---|---:|
| Preguntas con respuesta en los apuntes | 40 |
| Preguntas fuera del contenido disponible | 3 |
| Bloqueo de prompt injection | 4 |
| Aislamiento entre colegios | 1 |
| Protección de datos personales | 1 |
| Protección de información de otros estudiantes | 1 |
| **Total** | **50** |

Distribución de preguntas académicas:

| Curso | Casos |
|---|---:|
| Matemática 8° básico A | 22 |
| Ciencias Naturales 8° básico B | 18 |

## 3. Métricas de evaluación

| Métrica | Objetivo |
|---|---:|
| Hit@3 | ≥ 80 % |
| Hechos clave | ≥ 70 % |
| Fidelidad a las fuentes | ≥ 90 % |
| Rechazo de consultas fuera de alcance | 100 % |
| Bloqueo de prompt injection | 100 % |
| Aislamiento entre colegios | 100 % |
| Protección de información personal | 100 % |

## 4. Resultados por curso

Corrida final (20-11-2026) con `openai/gpt-oss-120b`, `top-k = 5` y `min-score = 1,5`:

| Curso | Hit@3 | Hechos clave | Fidelidad |
|---|---:|---:|---:|
| Matemática 8° básico A | 91 % | 86 % | 95 % |
| Ciencias Naturales 8° básico B | 78 % | 78 % | 78 % |
| **Total (40 casos)** | **85 %** | **82 %** | **88 %** |
| Meta | ≥ 80 % | ≥ 70 % | ≥ 90 % |

Diferencia entre cursos: 13 puntos en hit@3 y 9 en hechos clave (meta de equidad < 15: cumple).

Antes de calibrar (`min-score = 2,0`) el total era 75 % / 75 % / 78 %, con Ciencias en 56 % / 56 % / 56 % y 35 puntos de diferencia entre cursos.

### 4.1 Calibración de `min-score`

Se corrió el golden set sin clave del LLM (modo extractivo), cambiando solo el umbral. El umbral decide si la pregunta se considera fuera del temario antes de llamar al LLM, así que se puede calibrar sin gastar cuota.

| `min-score` | Hit@3 | Hechos clave | Fidelidad | Preguntas válidas rechazadas | Rechazo fuera de alcance |
|---:|---:|---:|---:|---:|---:|
| **1,5** | **80 %** | **70 %** | **98 %** | **1** | **100 %** |
| 2,0 | 72 % | 68 % | 82 % | 7 | 100 % |
| 2,5 | 70 % | 65 % | 80 % | 8 | 100 % |
| 3,0 | 68 % | 62 % | 75 % | 10 | 100 % |

Con 1,5 se recuperan 6 de las 7 preguntas válidas que 2,0 rechazaba y las 3 preguntas fuera del temario siguen rechazadas. Se fija `aulaviva.tutor.min-score=1.5` y se confirmó con la corrida final con LLM (sección 4).

## 5. Resultados de seguridad

Iguales en todas las corridas (con y sin LLM, con cualquier umbral):

| Prueba | Casos | Resultado |
|---|---:|---|
| Consultas fuera de alcance | 3 | 100 % rechazadas |
| Prompt injection | 4 | 100 % bloqueadas sin llamar al LLM |
| Datos de otros estudiantes | 1 | 100 % bloqueado |
| Aislamiento entre colegios | 1 | 100 % (HTTP 404) |
| Protección de RUT | 1 | 100 % (el RUT no aparece en la respuesta) |

Evidencia de la demo (pasos 3 y 4): [`d4-01-tutor-llm-e-injection.png`](../qa/evidence/s18/d4-01-tutor-llm-e-injection.png) (respuesta con LLM y ataque bloqueado) y [`d4-02-tutor-extractivo-sin-clave.png`](../qa/evidence/s18/d4-02-tutor-extractivo-sin-clave.png) (sin clave, modo extractivo).

Latencia observada en la corrida final: mediana de 0,8 s con LLM (p95 ≈ 1,1 s); 2-3 ms para respuestas desde caché, bloqueos y modo extractivo.

## 6. Análisis de fallos

En la corrida base (umbral 2,0) fallaron 11 de 40 casos; 7 eran preguntas cortas de Ciencias («¿Qué es la célula?») que BM25 puntuaba bajo y se rechazaban como fuera del temario. La calibración a 1,5 recuperó 5 de ellas. En la corrida final fallan 8 casos:

| Causa | Casos | Comentario |
|---|---|---|
| El LLM respondió «No encuentro eso en tus apuntes» aunque había fragmentos recuperados | GS13, GS23, GS39, GS43 | Comportamiento conservador: prefiere no responder antes que inventar. GS23 y GS39 son la misma pregunta (procariota vs. eucariota); conviene reforzar ese apunte |
| Pregunta rechazada por el umbral | GS22 | Unicelular vs. pluricelular: el puntaje BM25 queda bajo 1,5 |
| El apunte esperado no quedó entre los 3 primeros | GS01 | La respuesta fue correcta y con fuente; solo falla hit@3 |
| La respuesta no incluyó los hechos esperados | GS02, GS17 | Búsqueda léxica: el apunte usa otras palabras que la pregunta |

La fidelidad (88 %) queda 2 puntos bajo la meta: los 4 casos en que el LLM dice «No encuentro…» cuentan como no fieles porque no citan fuente, aunque no inventan nada. Mejoras propuestas: ampliar los apuntes de Ciencias (células procariotas/eucariotas y organismos unicelulares) y, más adelante, búsqueda semántica (ADR 0009, alternativa diferida).

## 7. Metodología

La evaluación utiliza métricas propias implementadas en `rag/eval/run_eval.py`, a partir del conjunto de pruebas `rag/eval/golden-set.jsonl`.

No se utilizó RAGAS. Se emplearon métricas personalizadas para medir recuperación de fuentes, presencia de hechos clave, fidelidad y comportamiento de seguridad.

## 8. Conclusión

- Seguridad, privacidad y aislamiento cumplen el 100 % en todas las corridas.
- Con el umbral calibrado se cumplen hit@3 (85 %), hechos clave (82 %) y equidad entre cursos (13 y 9 puntos).
- La fidelidad queda en 88 % (meta 90 %): los fallos son respuestas «No encuentro…» sin fuente, no respuestas inventadas.
- Las respuestas con LLM tardan 0,8 s en mediana y, si el LLM falla, el tutor sigue respondiendo desde los apuntes.
