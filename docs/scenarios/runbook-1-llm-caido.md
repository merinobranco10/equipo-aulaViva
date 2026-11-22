# Runbook: LLM Caído (Tutor IA)
**Síntoma:** Errores 502/503 en el endpoint `/api/tutor`.
**Solución:**
1. Revisar el estado del proveedor de IA.
2. Activar la flag de configuración para usar el modelo de respaldo.
3. Si ambos fallan, devolver un mensaje amigable al usuario.
