# Informe de evaluación — Tutor IA AulaViva

**Fecha:** 19-11-2026 · **Ejecutado:** 20-11-2026  
**Proveedor:** Groq  
**Modelo principal:** `openai/gpt-oss-120b`  
**Modelo de respaldo:** `openai/gpt-oss-20b`  
**Corpus:** 9 apuntes distribuidos en 2 cursos.  
**Conjunto de evaluación:** 50 casos (`rag/eval/golden-set.jsonl`).  
**Script:** `rag/eval/run_eval.py` · **Resultados crudos:** `rag/eval/resultados.json` (corrida base con LLM)  
**Configuración evaluada:** `top-k = 5`, `min-score = 2,0` (corrida con LLM); calibración de `min-score` entre 1,5 y 3,0 sin LLM. Valor elegido: **1,5**.

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

Corrida con `openai/gpt-oss-120b`, `top-k = 5`, `min-score = 2,0` (valor previo a la calibración), 50 casos:

| Curso | Hit@3 | Hechos clave | Fidelidad |
|---|---:|---:|---:|
| Matemática 8° básico A | 91 % | 91 % | 95 % |
| Ciencias Naturales 8° básico B | 56 % | 56 % | 56 % |
| **Total (40 casos)** | **75 %** | **75 %** | **78 %** |

Diferencia entre cursos: 35 puntos en hit@3 y en hechos clave (meta de equidad: < 15). La segunda corrida dio los mismos números.

### 4.1 Calibración de `min-score`

Se corrió el golden set sin clave del LLM (modo extractivo), cambiando solo el umbral. El umbral decide si la pregunta se considera fuera del temario antes de llamar al LLM, así que se puede calibrar sin gastar cuota.

| `min-score` | Hit@3 | Hechos clave | Fidelidad | Preguntas válidas rechazadas | Rechazo fuera de alcance |
|---:|---:|---:|---:|---:|---:|
| **1,5** | **80 %** | **70 %** | **98 %** | **1** | **100 %** |
| 2,0 | 72 % | 68 % | 82 % | 7 | 100 % |
| 2,5 | 70 % | 65 % | 80 % | 8 | 100 % |
| 3,0 | 68 % | 62 % | 75 % | 10 | 100 % |

Con 1,5 se recuperan 6 de las 7 preguntas válidas que 2,0 rechazaba y las 3 preguntas fuera del temario siguen rechazadas. Se fija `aulaviva.tutor.min-score=1.5`. Con ese valor, Ciencias sube a 72 % de hit@3 y la diferencia entre cursos baja a 14 puntos en hit@3 (cumple) y 26 en hechos clave (no cumple).

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

Latencia observada: mediana de 0,6 s con LLM (p95 ≈ 1,5 s); 2-3 ms para respuestas desde caché, bloqueos y modo extractivo.

## 6. Análisis de fallos

Fallaron 11 de los 40 casos académicos en la corrida con LLM:

| Causa | Casos | Qué se hizo |
|---|---|---|
| Umbral `min-score` demasiado alto: preguntas cortas y generales («¿Qué es la célula?») obtienen poco puntaje BM25 y se tratan como fuera del temario | GS21, GS22, GS23, GS26, GS37, GS39, GS44 | Calibración a 1,5: se recuperan todos salvo GS22 |
| El LLM respondió «No encuentro eso en tus apuntes» aunque había fragmentos recuperados | GS13, GS43 | Se mantiene: es el comportamiento conservador esperado; revisar los apuntes de operaciones y respiración celular |
| Recuperación: el apunte esperado no quedó entre los 3 primeros | GS01 | La respuesta fue correcta y con fuente; el caso solo falla hit@3 |
| La respuesta no incluyó los hechos esperados | GS02 | Búsqueda léxica: el apunte usa otras palabras; candidato para ampliar el corpus |

Ciencias concentra los fallos porque sus apuntes usan términos que se repiten en muchas secciones (célula, energía), lo que baja el puntaje BM25 de preguntas cortas. Mejoras propuestas: ampliar los apuntes de Ciencias y, más adelante, búsqueda semántica (ADR 0009, alternativa diferida).

Pendiente recomendado: repetir la corrida con LLM usando `min-score = 1,5` para confirmar las metas de calidad con el umbral final.

## 7. Metodología

La evaluación utiliza métricas propias implementadas en `rag/eval/run_eval.py`, a partir del conjunto de pruebas `rag/eval/golden-set.jsonl`.

No se utilizó RAGAS. Se emplearon métricas personalizadas para medir recuperación de fuentes, presencia de hechos clave, fidelidad y comportamiento de seguridad.

## 8. Conclusión

- Seguridad, privacidad y aislamiento cumplen el 100 % en todas las corridas.
- Matemática cumple todas las metas de calidad. Ciencias no las cumple con el umbral inicial; la calibración a `min-score = 1,5` deja el total en hit@3 80 %, hechos clave 70 % y fidelidad 98 % (medido en modo extractivo).
- La equidad entre cursos cumple en hit@3 (14 pts) y no en hechos clave (26 pts): se declara como limitación y como trabajo futuro sobre el corpus de Ciencias.
- Las respuestas con LLM se generan en menos de un segundo (mediana) y, si el LLM falla, el tutor sigue respondiendo desde los apuntes.
