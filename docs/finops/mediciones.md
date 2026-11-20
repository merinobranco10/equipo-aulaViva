# Mediciones FinOps — Tutor IA AulaViva

**Fecha:** 19-11-2026

## 1. Objetivo

Evaluar el consumo de recursos y los costos asociados al funcionamiento del Tutor IA, comparando diferentes configuraciones de caché, recuperación de información y límite de tokens.

## 2. Modelo y proveedor

| Parámetro | Configuración |
|---|---|
| Proveedor | Groq |
| Modelo principal | `openai/gpt-oss-120b` |
| Modelo de respaldo | `openai/gpt-oss-20b` |
| Límite de tokens de salida base | 800 |
| Conjunto de evaluación | 50 casos |
| Script | `rag/eval/run_eval.py` |

## 3. Escenarios evaluados

### Escenario 1: Configuración base

- Caché deshabilitada.
- `top-k = 5`.
- `max-tokens = 800`.
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

| Escenario | Ejecución | Llamadas LLM | Tokens entrada | Tokens salida | Costo equivalente USD | Hit@3 | Hechos clave |
|---|---|---:|---:|---:|---:|---:|---:|
| Base | 1 | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente |
| Base | 2 | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente |
| Caché | 1 | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente |
| Caché | 2 | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente |
| Optimizado | 1 | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente |
| Optimizado | 2 | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente | Pendiente |

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

Las mediciones permitirán identificar la configuración más eficiente para el Tutor IA, equilibrando calidad de respuesta y consumo de recursos.

La conclusión definitiva queda pendiente de completar después de ejecutar los tres escenarios y registrar los datos reales.

**Fuentes de precios (consultadas el 20-11-2026):**

- [Groq Pricing (octubre 2026), resumen de la documentación de GroqCloud](https://www.morphllm.com/groq-pricing)
- [Precios de GPT-OSS-120B en Groq (computeprices.com)](https://computeprices.com/providers/groq/models/gpt-oss-120b)
- [Anuncio original de Groq (agosto 2025)](https://groq.com/blog/day-zero-support-for-openai-open-models)
