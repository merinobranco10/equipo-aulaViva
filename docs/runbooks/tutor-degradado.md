# Runbook: Tutor IA degradado (LLM caído o sin cuota)

**Dueño:** Valentina León · **Decisión:** [ADR 0009](../adr/0009-tutor-ia-proveedor-llm.md) y [ADR 0010](../adr/0010-resiliencia-dr.md)

## Síntoma
Las respuestas del tutor llegan con `"mode":"extractivo"` (fragmento del apunte sin redacción del LLM), o aumenta la latencia del tutor.

## Diagnóstico (2 min)
```bash
# Estado del circuit breaker "llm" (CLOSED, OPEN o HALF_OPEN). Requiere rol coordinador.
curl -s -u coordinador.b:$DEMO_PASSWORD localhost:8080/actuator/circuitbreakers
# Errores del proveedor en el log (401 = clave inválida, 429 = cuota agotada)
docker compose logs --tail=100 app | grep -iE "llm|groq|401|429"
```
- **401:** la clave `LLM_API_KEY` es inválida o fue revocada.
- **429:** se agotó la cuota de Groq (1.000 solicitudes al día o 8.000 tokens por minuto). Primero entra el modelo de respaldo `openai/gpt-oss-20b`; si también falla, el modo extractivo.
- **HTTP 429 del propio tutor:** el colegio agotó su presupuesto diario (`TUTOR_DAILY_TOKEN_BUDGET`).

## Mitigación
1. No hay que hacer nada para que el servicio siga: el modo extractivo responde con los apuntes.
2. Clave inválida: rotar la clave (Branco, en console.groq.com), actualizar el secret `LLM_API_KEY` en Codespaces/Actions y recrear la app: `docker compose up -d app`.
3. Cuota agotada: esperar el reinicio diario de Groq o subir de plan; el circuit breaker reintenta solo cada 30 s (estado `HALF_OPEN`).

## Verificación
```bash
curl -s -u estudiante.a:$DEMO_PASSWORD -H 'Content-Type: application/json' \
  -d '{"courseId":"matematica-8a","question":"¿Qué es el valor absoluto?"}' localhost:8080/v1/tutor/questions
```
Debe volver `"mode":"llm"` (o `"cache"`) y el circuit breaker en `CLOSED`.

## Comunicación
Aviso en la plataforma: «El tutor está respondiendo con fragmentos de tus apuntes mientras se restablece el servicio».
