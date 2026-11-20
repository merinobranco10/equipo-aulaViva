# Mediciones FinOps — Tutor IA AulaViva

**Fecha:** 19-11-2026 · **Medido:** 20-11-2026 (Groq, plan gratuito, `min-score = 2,0`)

## 1. Objetivo

Evaluar el consumo de recursos y los costos asociados al funcionamiento del Tutor IA, comparando diferentes configuraciones de caché, recuperación de información y límite de tokens.

## 2. Modelo y proveedor

| Parámetro | Configuración |
|---|---|
| Proveedor | Groq |
| Modelo principal | `openai/gpt-oss-120b` |
| Modelo de respaldo | `openai/gpt-oss-20b` |
| Límite de tokens de salida | 400 por defecto; los escenarios 1 y 2 usan 800 como línea base (`LLM_MAX_TOKENS=800`) |
| Conjunto de evaluación | 50 casos |
| Script | `rag/eval/run_eval.py` |

## 3. Escenarios evaluados

### Escenario 1: Configuración base

- Caché deshabilitada.
- `top-k = 5`.
- `max-tokens = 800` (levantar la app con `LLM_MAX_TOKENS=800`).
- Ejecutar dos veces consecutivas el conjunto de evaluación.

### Escenario 2: Caché habilitada

- Caché habilitada.
- Mantener `top-k = 5`.
- Mantener `max-tokens = 800`.
- Ejecutar dos veces consecutivas el conjunto de evaluación.
- Comparar las llamadas al proveedor entre ambas ejecuciones.

### Escenario 3: Optimización de consumo

- Caché habilitada.
- `top-k = 3`.
- `max-tokens = 400` (no menos de 300: gpt-oss usa parte de los tokens para razonar y con menos la respuesta puede llegar vacía).
- Ejecutar dos veces consecutivas el conjunto de evaluación.
- Comprobar que la calidad no disminuya más de cinco puntos porcentuales respecto de la configuración base.

## 4. Tabla de mediciones

Cada ejecución recorre los 50 casos del golden set (40 llegan al LLM o a la caché; el resto se bloquea antes). Llamadas y tokens se tomaron de `/actuator/prometheus` y de los logs del tutor.

| Escenario | Ejecución | Llamadas LLM | Tokens entrada | Tokens salida | Costo equivalente USD | Hit@3 | Hechos clave |
|---|---|---:|---:|---:|---:|---:|---:|
| Base | 1 | 34 | 18.290 | 2.375 | 0,00417 | 75 % | 75 % |
| Base | 2 | 34 | 18.290 | 2.359 | 0,00416 | 75 % | 75 % |
| Caché | 1 | 33 | 17.627 | 2.317 | 0,00403 | 75 % | 75 % |
| Caché | 2 | **3** | **1.388** | **184** | **0,00032** | 75 % | 75 % |
| Optimizado | 1 | 33 | 13.833 | 2.247 | 0,00342 | 72 % | 68 % |
| Optimizado | 2 | 5 | 1.927 | 324 | 0,00048 | 72 % | 68 % |

Promedio por respuesta con LLM: ≈ 540 tokens de entrada y ≈ 70 de salida; costo equivalente ≈ USD 0,12 por cada 1.000 preguntas sin caché.

### Resultado de cada palanca

| Palanca | Ahorro | Efecto en calidad | Decisión |
|---|---|---|---|
| **1. Caché** (segunda ejecución vs. base) | −91 % llamadas, −92 % tokens de entrada, −92 % costo | Ninguno (75 % / 75 %) | **Se adopta** (`cache-enabled=true`) |
| **2. Contexto acotado** (`top-k = 3`, `max-tokens = 400`; primera ejecución vs. base) | −24 % tokens de entrada, −5 % de salida, −18 % costo | Hit@3 −3 pts, hechos clave **−7 pts** | **No se adopta `top-k = 3`**: supera el límite de 5 puntos. Se mantiene `top-k = 5` |

`max-tokens = 400` se mantiene como tope por defecto: las respuestas usan ≈ 70 tokens de salida, así que bajar de 800 a 400 no cambia el costo ni la calidad, y sigue sobre el mínimo de 300 que necesita gpt-oss para razonar.

## 5. Fórmula de costo

Se utiliza la siguiente fórmula de costo equivalente:

`(tokens entrada × 0,15 + tokens salida × 0,60) / 1.000.000`

Los valores son el precio de pago de Groq para `openai/gpt-oss-120b`: USD 0,15 por millón de tokens de entrada y USD 0,60 por millón de salida. El modelo de respaldo `openai/gpt-oss-20b` cuesta USD 0,075 y USD 0,30. Groq anunció el modelo en agosto de 2025 con USD 0,75 por millón de salida; los precios vigentes en 2026 son los de arriba. Antes de presentar, confirmar en la página de precios de GroqCloud y anotar la fecha.

**Importante:** el resultado representa un costo equivalente de referencia y no necesariamente un cobro efectivo del plan gratuito de Groq.

## 6. Comparación de escenarios

La comparación debe considerar:

- Cantidad de llamadas realizadas al proveedor.
- Tokens consumidos en entrada.
- Tokens consumidos en salida.
- Costo equivalente.
- Calidad de recuperación.
- Cantidad de hechos clave identificados.

Se espera que el caché reduzca las llamadas al proveedor cuando se repiten preguntas y que la reducción de `top-k` y `max-tokens` disminuya el consumo.

## 7. Criterios de aceptación

- El caché debe reducir las llamadas al LLM en consultas repetidas.
- La configuración optimizada no debe reducir Hit@3 ni hechos clave en más de cinco puntos porcentuales respecto de la base.
- La reducción de consumo no debe comprometer los controles de seguridad.
- Los resultados deben ser registrados y comparados con la configuración inicial.

## 8. Conclusión

- La caché es la palanca efectiva: con preguntas repetidas elimina más del 90 % de las llamadas y del costo sin tocar la calidad.
- Reducir `top-k` ahorra un 18 %, pero baja los hechos clave 7 puntos: no compensa con un corpus tan pequeño.
- Configuración final: caché activa, `top-k = 5`, `max-tokens = 400`, `min-score = 1,5` (calibrado en `docs/ai/eval-report.md`). En la corrida final con esa configuración: 39 llamadas, 20.686 tokens de entrada, 2.767 de salida, USD 0,00476 por ejecución del golden set.
- Con el presupuesto diario de 50.000 tokens por colegio caben ≈ 80 preguntas sin caché (≈ 610 tokens cada una), y muchas más con caché.

**Fuentes de precios (consultadas el 20-11-2026):**

- [Groq Pricing (octubre 2026), resumen de la documentación de GroqCloud](https://www.morphllm.com/groq-pricing)
- [Precios de GPT-OSS-120B en Groq (computeprices.com)](https://computeprices.com/providers/groq/models/gpt-oss-120b)
- [Anuncio original de Groq (agosto 2025)](https://groq.com/blog/day-zero-support-for-openai-open-models)
