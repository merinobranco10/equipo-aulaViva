# Configuración del proveedor LLM — Tutor IA

> Decisión registrada en [ADR 0009](../adr/0009-tutor-ia-proveedor-llm.md). Actualizado el 18-11-2026: el proveedor pasa de Anthropic a **Groq**.

## 1. Decisión

| Tema | Elección |
|---|---|
| Proveedor | Groq, por API (compatible con OpenAI) |
| Modelo principal | `llama-3.3-70b-versatile` |
| Modelo de respaldo | `openai/gpt-oss-20b` (si el principal agota su cuota o falla) |
| Búsqueda en apuntes | BM25 (palabras clave) sobre `backend/src/main/resources/corpus/<courseId>/`, sin base vectorial |
| Si Groq no responde | Fallback extractivo: se devuelve el fragmento más relevante del apunte, sin LLM |

Motivos:

- Groq tiene plan gratuito sin tarjeta y responde muy rápido, suficiente para la demo y las pruebas del equipo.
- Ambos modelos son de producción en Groq (no "preview").
- La API es compatible con OpenAI: cambiar de proveedor después (por ejemplo a Claude Haiku en producción) solo requiere cambiar `LLM_BASE_URL`, `LLM_MODEL` y la clave.
- Con 9 apuntes pequeños no se necesita base vectorial; todo corre sin Docker en un PC de 8 GB.

Antes del Día 4, revisar en la consola de Groq los límites vigentes del plan gratuito (solicitudes por minuto y por día).

## 2. Variables de entorno

| Variable | Descripción | Valor |
|---|---|---|
| LLM_BASE_URL | URL base de la API | https://api.groq.com/openai/v1 |
| LLM_MODEL | Modelo principal | llama-3.3-70b-versatile |
| LLM_FALLBACK_MODEL | Modelo de respaldo | openai/gpt-oss-20b |
| LLM_API_KEY | Clave privada de Groq | **Secreto** |
| LLM_TIMEOUT_SECONDS | Tiempo máximo de espera | 8 |
| RAG_TOP_K | Fragmentos enviados al modelo | 3 |
| TUTOR_DAILY_BUDGET | Preguntas al LLM por colegio y día (después, solo fallback) | 200 |

Los apuntes se leen del classpath (`classpath:corpus/<courseId>/`), así que no hay variable de carpeta.

## 3. Configuración local

Ejemplo de `.env` local (este archivo **no** se sube a GitHub):

```env
LLM_BASE_URL=https://api.groq.com/openai/v1
LLM_MODEL=llama-3.3-70b-versatile
LLM_FALLBACK_MODEL=openai/gpt-oss-20b
LLM_API_KEY=
LLM_TIMEOUT_SECONDS=8
RAG_TOP_K=3
```

La clave se completa solo en tu computador. En PowerShell:

```powershell
$env:LLM_API_KEY = 'pega-aqui-tu-clave'
```

En bash / Codespaces:

```bash
export LLM_API_KEY='pega-aqui-tu-clave'
```

Sin `LLM_API_KEY` el tutor funciona igual, pero solo con el fallback extractivo.

## 4. Gestión de secretos

`LLM_API_KEY` es un secreto y nunca debe:

- Guardarse en archivos versionados.
- Publicarse en GitHub, Discord ni otros canales públicos.
- Escribirse en el código fuente.
- Aparecer en capturas de pantalla.

La crea quien administra la cuenta de Groq y la entrega **por privado** a quien la necesite. En GitHub se guarda como *secret* del repositorio (Settings → Secrets and variables → Actions). En Kubernetes se usa un Secret. Si se filtra, se revoca en la consola de Groq y se genera otra.

## 5. Cómo funciona el flujo

1. El estudiante envía su pregunta y el `courseId`.
2. El backend valida identidad, rol y que el estudiante esté inscrito en ese curso (otro colegio → 404).
3. Guardrails de entrada: se bloquean intentos de prompt injection y se quitan datos personales (RUT, correo, teléfono).
4. BM25 busca en `corpus/<courseId>/` los 3 fragmentos más parecidos.
5. Se envía a Groq solo la pregunta limpia y esos fragmentos, con la instrucción de responder únicamente con ese contenido.
6. El backend devuelve `answer` y `sources`.

Reglas:

- Si no hay fragmentos suficientes, el tutor responde que no tiene información del curso sobre ese tema.
- Si la pregunta intenta cambiar las reglas del tutor, se niega y queda registrado.
- Nunca se leen apuntes de un curso al que el estudiante no pertenece.
- Al proveedor no se envían nombre, RUT, correo, notas ni identificadores del estudiante.

## 6. Manejo de errores

| Caso | Qué hace el backend |
|---|---|
| Clave ausente, inválida o revocada | Fallback extractivo y log de advertencia (sin la clave) |
| Timeout (> 8 s) | Fallback extractivo |
| Límite de solicitudes de Groq (HTTP 429) | Reintenta con el modelo de respaldo; si falla, fallback extractivo |
| Groq caído (5xx) | Circuit breaker (Resilience4j) y fallback extractivo |
| Sin contexto relevante | "No tengo información sobre eso en los apuntes del curso" |

El estudiante recibe un mensaje claro y sin datos técnicos.

## 7. Validaciones antes de usarlo

- La clave es válida (una llamada de prueba responde 200).
- Los nombres exactos de los modelos funcionan.
- El timeout es de 8 s.
- No se registran preguntas con datos personales.
- Se corre [`rag/eval/golden-set.jsonl`](../../rag/eval/golden-set.jsonl) y se registran los resultados en la [model card](../ai/model-card.md).

## 8. Estado

Decisión de proveedor y modelo tomada. El endpoint `POST /v1/tutor/questions` sigue respondiendo HTTP 501 hasta la implementación del Día 4. Documentar la configuración no significa que el tutor ya esté operativo.
