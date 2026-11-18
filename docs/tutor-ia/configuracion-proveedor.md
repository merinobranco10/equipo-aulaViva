# Configuración del proveedor LLM — Tutor IA

## 1. Decisión

| Tema | Elección |
|---|---|
| Proveedor | Anthropic (Claude), por API |
| Modelo | `claude-haiku-5-5` |
| Modelo alternativo | `claude-sonnet-5-5`, si las respuestas de Haiku no alcanzan |
| Búsqueda en apuntes | Por palabras clave sobre los archivos de `corpus/` (sin base vectorial) |

Motivos:

- Haiku es rápido y barato, y sirve para responder con apuntes cortos.
- Con solo 9 apuntes pequeños no se necesita base vectorial, lo que permite trabajar sin Docker en un PC de 8 GB.
- Si más adelante el corpus crece, se puede cambiar a búsqueda con embeddings sin cambiar el contrato de la API.

Antes del Día 4 se debe revisar en la cuenta de la API el costo y los límites de uso vigentes.

## 2. Variables de entorno

| Variable | Descripción | Valor |
|---|---|---|
| AI_PROVIDER | Proveedor del LLM | anthropic |
| AI_API_KEY | Clave privada de la API | Secreto |
| AI_MODEL | Modelo a usar | claude-haiku-5-5 |
| AI_BASE_URL | URL base de la API | https://api.anthropic.com |
| AI_TIMEOUT_SECONDS | Tiempo máximo de espera | 30 |
| CORPUS_PATH | Carpeta con los apuntes | corpus |
| RAG_TOP_K | Cantidad de fragmentos enviados al modelo | 3 |

## 3. Configuración local

Ejemplo de `.env` local (este archivo NO se sube a GitHub):

```env
AI_PROVIDER=anthropic
AI_API_KEY=
AI_MODEL=claude-haiku-5-5
AI_BASE_URL=https://api.anthropic.com
AI_TIMEOUT_SECONDS=30
CORPUS_PATH=corpus
RAG_TOP_K=3
```

La clave se completa solo en tu computador. Para correr la app:

```bash
export AI_API_KEY='pega-aqui-tu-clave'
```

## 4. Gestión de secretos

`AI_API_KEY` es un secreto y nunca debe:

- Guardarse en archivos versionados.
- Publicarse en GitHub.
- Compartirse en canales públicos.
- Escribirse en el código fuente.
- Aparecer en capturas de pantalla.

En GitHub se guarda como *secret* del repositorio (Settings → Secrets and variables → Actions). En Kubernetes se usa un Secret.

## 5. Cómo funciona el flujo

1. El estudiante envía su pregunta y el `courseId`.
2. El backend valida identidad, rol y que el estudiante esté inscrito en ese curso.
3. El backend busca en `corpus/<courseId>/` los 3 fragmentos más parecidos a la pregunta.
4. Se envía al modelo la pregunta con esos fragmentos.
5. El modelo responde usando solo ese contenido.
6. El backend devuelve `answer` y `sources`.

Reglas:

- Si no hay fragmentos suficientes, el tutor responde que no tiene información del curso sobre ese tema.
- Si la pregunta intenta cambiar las reglas del tutor (prompt injection), el tutor se niega y queda registrado.
- Nunca se leen apuntes de un curso al que el estudiante no pertenece.

## 6. Manejo de errores

El backend debe contemplar:

- Clave inválida o vencida.
- Timeout.
- Límite de solicitudes excedido.
- Servicio no disponible.
- Ausencia de contexto relevante.

El estudiante recibe un mensaje claro y sin datos técnicos.

## 7. Validaciones antes de usarlo

- La clave API es válida.
- El nombre exacto del modelo funciona.
- El timeout está configurado.
- No se registran preguntas con datos personales innecesarios.
- Se corre `golden-set.jsonl` y se revisan los resultados.

## 8. Estado

Decisión de proveedor y modelo tomada. El endpoint `POST /v1/tutor/questions` sigue respondiendo HTTP 501 hasta la implementación del Día 4. Documentar la configuración no significa que el tutor ya esté operativo.
