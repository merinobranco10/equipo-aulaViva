# Runbook: LLM Caído
**Síntoma:** Errores 502/503 en endpoints que usan IA. Los logs muestran "Timeout connecting to LLM API".
**Solución:**
1. Revisar el estado del proveedor (OpenAI Status page).
2. Activar la flag de feature toggle para usar el LLM de respaldo (ej. cambiar de OpenAI a Anthropic).
3. Si ambos fallan, devolver una respuesta en caché o un mensaje amigable al usuario (Graceful Degradation).
