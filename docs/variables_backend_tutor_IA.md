# Inventario de variables — Backend y Tutor IA de AulaViva

## 1. Objetivo

Listar las variables de entorno necesarias para ejecutar el backend de AulaViva y el Tutor IA, clasificarlas y definir cómo se guardan los secretos.

Este documento sirve de base para los manifiestos de Kubernetes y para los secrets de GitHub.

Última revisión: 17-nov-2026, contra `application.properties` y `application-pg.properties`.

## 2. Variables del backend (se usan hoy en el código)

| Variable | Función | Valor por defecto | Clasificación |
|---|---|---|---|
| SERVER_PORT | Puerto del servidor | 8080 | Configuración |
| SPRING_PROFILES_ACTIVE | Perfiles activos (`h2`, `pg`, `demo`) | h2 en local | Configuración |
| DB_URL | Dirección de PostgreSQL (perfil `pg`) | jdbc:postgresql://db:5432/aulaviva | Configuración |
| DB_USER | Usuario de la base de datos (perfil `pg`) | aulaviva | Sensible |
| DB_PASSWORD | Contraseña de la base de datos (perfil `pg`) | Sin valor | Secreto |
| DEMO_PASSWORD | Contraseña de los usuarios de demo (perfil `demo`) | Sin valor | Secreto |

Notas:

- En local se usa H2 en memoria y no hacen falta las variables `DB_*`.
- `DEMO_PASSWORD` nunca se escribe en el repositorio. En staging se guarda como secret de GitHub.
- Si `DEMO_PASSWORD` no existe, el perfil `demo` falla con un mensaje claro.

## 3. Variables del Tutor IA (se usarán desde el Día 4)

| Variable | Función | Valor | Clasificación |
|---|---|---|---|
| LLM_BASE_URL | Dirección de la API (Groq, compatible con OpenAI) | https://api.groq.com/openai/v1 | Configuración |
| LLM_MODEL | Modelo principal | llama-3.3-70b-versatile | Configuración |
| LLM_FALLBACK_MODEL | Modelo de respaldo | openai/gpt-oss-20b | Configuración |
| LLM_API_KEY | Clave de la API de Groq | Sin valor | Secreto |
| LLM_TIMEOUT_SECONDS | Tiempo máximo de espera | 8 | Configuración |
| RAG_TOP_K | Fragmentos enviados al modelo | 3 | Configuración |
| TUTOR_DAILY_BUDGET | Preguntas al LLM por colegio y día | 200 | Configuración |

Los apuntes se leen del classpath (`backend/src/main/resources/corpus/<courseId>/`), por eso ya no existe `CORPUS_PATH`. Sin `LLM_API_KEY` el tutor responde solo con el fallback extractivo.

Detalle en `docs/tutor-ia/configuracion-proveedor.md` y en el ADR 0009.

## 4. Variables descartadas o diferidas

| Variable | Motivo |
|---|---|
| EMBEDDING_MODEL | No se usa: la búsqueda en apuntes es por palabras clave (BM25) |
| AI_PROVIDER, AI_API_KEY, AI_MODEL, AI_BASE_URL, AI_TIMEOUT_SECONDS, CORPUS_PATH | Reemplazadas por las variables `LLM_*` al cambiar de Anthropic a Groq (ADR 0009) |
| VECTOR_DB_HOST, VECTOR_DB_PORT, VECTOR_DB_NAME, VECTOR_DB_USERNAME, VECTOR_DB_PASSWORD | No se usa base vectorial con 9 apuntes |
| KAFKA_BOOTSTRAP_SERVERS | Kafka no se usa: el outbox solo guarda los eventos en la base de datos (publicación diferida, ver ADR 0004) |
| AWS_REGION | El código no la lee hoy. Se agrega si se usan servicios de AWS |
| LOG_LEVEL | El código no la lee hoy. Spring permite ajustar logs con `logging.level.*` si se necesita |

## 5. Infraestructura y seguridad

| Elemento | Propósito | Tratamiento |
|---|---|---|
| AWS IAM Role | Acceso autorizado a servicios AWS | Identidad y permisos |
| Terraform state bucket | Guardar el estado remoto de Terraform en S3 | Configuración de infraestructura |
| Terraform state locking | Evitar cambios simultáneos del estado | Configuración de infraestructura |

## 6. Gestión de secretos

- Las variables de configuración van en ConfigMaps.
- Las contraseñas y claves (`DB_PASSWORD`, `DEMO_PASSWORD`, `LLM_API_KEY`) van en Kubernetes Secrets o en los secrets de GitHub Actions.
- No se guardan credenciales reales en el repositorio ni en los manifiestos.
- Los archivos `.env` con valores reales no se suben a GitHub.
- Se usan permisos mínimos y se separan los entornos de desarrollo y producción.

## 7. Pendiente

- [ ] Agregar las variables del tutor a `k8s/configmap.yaml` (las de configuración) y a un Secret (`LLM_API_KEY`).
- [ ] Crear la cuenta de Groq, generar la clave y entregarla por privado; crear el secret `LLM_API_KEY` en GitHub antes del Día 4.
- [ ] Confirmar con Matías cómo se inyectan los secrets en staging.
