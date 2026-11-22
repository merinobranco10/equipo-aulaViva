# ADR 0011 — FinOps: control del costo del Tutor IA y de la nube

**Estado:** Aceptada · **Fecha:** 21-11-2026 · **Autor:** Gerardo González (AI/Data)

## Contexto

El Tutor IA gasta plata cada vez que usa el modelo de lenguaje (se paga por "tokens", que son pedacitos de texto). La nube de AWS también cobra por uso. AulaViva es multi-colegio: la infraestructura es compartida, pero el gasto del tutor lo generan colegios distintos.

Si no se controla, un colegio puede gastar mucho y nadie lo ve hasta que llega la cuenta. Los datos medidos el Día 4 están en [mediciones.md](../finops/mediciones.md) y el costo mensual estimado está en [costos.md](../finops/costos.md).

## Decisión

Se adopta un control de costo en 4 partes:

1. **Presupuesto por colegio.** Cada colegio tiene un límite diario de tokens en la app (variable `TUTOR_DAILY_TOKEN_BUDGET`, hoy 50.000). Al pasarse, el tutor responde solo con el fragmento de los apuntes, sin usar el modelo. Hoy el conteo vive en memoria de cada copia de la app; en producción debe pasar a Redis.
2. **Caché de respuestas.** Activada (`aulaviva.tutor.cache-enabled=true`). En la medición, con preguntas repetidas bajó 91 % las llamadas y 92 % el costo, sin cambiar la calidad.
3. **Contexto acotado.** Se mantiene `top-k = 5` y `max-tokens = 400`. Se probó `top-k = 3` (18 % más barato), pero bajó 7 puntos los hechos clave y el límite aceptado es 5, así que se descartó.
4. **Medición por colegio (showback).** La métrica `tutor.tokens` guarda los tokens por colegio (`tenant`) y por tipo (`prompt` y `completion`). Con ella se calcula cuánto del costo del tutor corresponde a cada colegio. La infraestructura es compartida, así que su costo se reparte por número de estudiantes.

### Etiquetas (tags) de costo

Se usan 4 etiquetas en todos los recursos: `project`, `env`, `owner` y `cost-center`. Ya están definidas en `terraform/main.tf`. En AWS, para que sirvan en la factura, hay que activarlas en **Billing → Cost allocation tags**. Después de activarlas, el costo se puede filtrar por proyecto y por ambiente (staging o producción).

### Alerta mensual (diseño)

Se diseña una alerta con AWS Budgets: aviso al llegar al **80 %** y al **100 %** del presupuesto mensual. Todavía no está creada: queda como diseño.

## Alternativas consideradas

| Alternativa | Veredicto |
|---|---|
| Sin control de gasto | Descartada: un error o abuso puede subir la cuenta sin aviso |
| Límite global (un solo tope para todos) | Descartada: un colegio puede gastar el tope de todos los demás |
| **Presupuesto por colegio + caché + contexto acotado** | **Elegida:** barata, ya está implementada y se mide por colegio |
| Modelo propio en servidores (self-hosted) | Descartada: necesita máquinas con GPU y tiene un costo fijo alto aunque nadie use el tutor |

## Consecuencias

- (+) Un colegio no puede gastar el presupuesto de los demás.
- (+) La caché baja mucho el costo sin perder calidad.
- (+) Se puede mostrar al panel cuánto cuesta cada colegio.
- (−) El límite de 50.000 tokens alcanza para unas 80 preguntas al día sin caché. Con 600 estudiantes por colegio hay que subirlo (según los supuestos de [costos.md](../finops/costos.md), del orden de 330.000 a 670.000 tokens diarios por colegio).
- (−) Con varias copias de la app, cada una cuenta por separado, así que el límite real puede ser mayor al configurado hasta que se use Redis.
- (−) La alerta de AWS Budgets es solo diseño por ahora.
- (−) La demo usa el plan gratuito de Groq; en producción habrá que pagar. Los precios usados están en [costos.md](../finops/costos.md).

## Verificación

- Los números de ahorro están en [mediciones.md](../finops/mediciones.md) (caché y `top-k`).
- La métrica `tutor.tokens` se puede ver en `/actuator/prometheus`.
- El presupuesto por colegio lo prueban los tests del tutor (`TutorFlowTest`).
