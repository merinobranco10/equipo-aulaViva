# Informe de evaluación — Tutor IA AulaViva

**Fecha:** 19-11-2026  
**Proveedor:** Groq  
**Modelo principal:** `openai/gpt-oss-120b`  
**Modelo de respaldo:** `openai/gpt-oss-20b`  
**Corpus:** 9 apuntes distribuidos en 2 cursos.  
**Conjunto de evaluación:** 50 casos (`rag/eval/golden-set.jsonl`).  
**Script:** `rag/eval/run_eval.py`

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

**Pendiente de ejecución del script de evaluación.**

| Curso | Hit@3 | Hechos clave | Fidelidad |
|---|---:|---:|---:|
| Matemática 8° básico A | Pendiente | Pendiente | Pendiente |
| Ciencias Naturales 8° básico B | Pendiente | Pendiente | Pendiente |

## 5. Resultados de seguridad

| Prueba | Resultado |
|---|---|
| Consultas fuera de alcance | Pendiente |
| Prompt injection | Pendiente |
| Aislamiento entre colegios | Pendiente |
| Protección de RUT | Pendiente |
| Protección de datos de otros estudiantes | Pendiente |

## 6. Análisis de fallos

Pendiente de completar después de ejecutar las 50 pruebas. Se identificarán los casos fallidos, los problemas de recuperación de información y las respuestas que no cumplan los criterios establecidos.

## 7. Metodología

La evaluación utiliza métricas propias implementadas en `rag/eval/run_eval.py`, a partir del conjunto de pruebas `rag/eval/golden-set.jsonl`.

No se utilizó RAGAS. Se emplearon métricas personalizadas para medir recuperación de fuentes, presencia de hechos clave, fidelidad y comportamiento de seguridad.

## 8. Conclusión

El Tutor IA dispone de un conjunto de evaluación de 50 casos que permite revisar tanto la calidad de las respuestas como los controles de seguridad y privacidad.

La aprobación final del sistema queda condicionada a la ejecución de las pruebas y al cumplimiento de los objetivos definidos en este informe.
