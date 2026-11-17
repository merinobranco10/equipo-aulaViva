
# Configuración del proveedor LLM — Tutor IA

## 1. Objetivo

Documentar la configuración necesaria para conectar el Tutor IA de AulaViva con un proveedor de modelos de lenguaje (LLM).

La configuración se realizará mediante variables de entorno para evitar almacenar credenciales dentro del código fuente.

## 2. Variables de entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| AI_PROVIDER | Proveedor del LLM | openai |
| AI_API_KEY | Clave privada de autenticación | Secreto |
| AI_MODEL | Identificador del modelo | Por definir |
| AI_BASE_URL | URL base de la API, si corresponde | Según proveedor |
| AI_TIMEOUT_SECONDS | Tiempo máximo de espera | 30 |
| EMBEDDING_MODEL | Modelo de embeddings | Por definir |
| VECTOR_DB_HOST | Host de base vectorial | Según infraestructura |
| VECTOR_DB_PORT | Puerto de base vectorial | Según infraestructura |
| VECTOR_DB_NAME | Nombre de la base vectorial | Según infraestructura |
| VECTOR_DB_USERNAME | Usuario de la base vectorial | Secreto |
| VECTOR_DB_PASSWORD | Contraseña de la base vectorial | Secreto |

Los valores definitivos deben coordinarse con el equipo de backend e infraestructura.

## 3. Selección del proveedor

El proveedor debe ser compatible con los requisitos de integración del backend.

Antes de seleccionar un proveedor se deben evaluar:

- Compatibilidad con la API requerida.
- Calidad de respuestas en español.
- Costos por tokens de entrada y salida.
- Límites de solicitudes.
- Latencia.
- Disponibilidad del servicio.
- Políticas de privacidad y tratamiento de datos.
- Condiciones de uso para aplicaciones educativas.

El proveedor y modelo definitivo quedan pendientes de confirmación por el equipo.

## 4. Configuración local

Crear las variables de entorno en el entorno local de ejecución.

Ejemplo de configuración:

```env
AI_PROVIDER=openai
AI_API_KEY=
AI_MODEL=
AI_BASE_URL=
AI_TIMEOUT_SECONDS=30
EMBEDDING_MODEL=
VECTOR_DB_HOST=
VECTOR_DB_PORT=
VECTOR_DB_NAME=
VECTOR_DB_USERNAME=
VECTOR_DB_PASSWORD=
```

Los campos vacíos deben completarse con valores confirmados por el equipo.

No subir archivos `.env` con credenciales reales al repositorio.

## 5. Gestión de secretos

La variable `AI_API_KEY` es un secreto y nunca debe:

- Guardarse en archivos versionados.
- Publicarse en GitHub.
- Compartirse en canales públicos.
- Incorporarse directamente al código fuente.
- Incluirse en capturas de pantalla.

Para producción se recomienda utilizar un gestor de secretos como AWS Secrets Manager o Kubernetes Secrets, según la infraestructura acordada.

## 6. Entrega de credenciales

La persona responsable de proporcionar la clave debe compartirla directamente con la persona autorizada para configurar el backend.

La entrega debe realizarse por un canal privado y seguro.

No incluir la clave en este documento ni en commits.

## 7. Validaciones antes de utilizar el proveedor

- Confirmar que el proveedor está habilitado.
- Confirmar el nombre exacto del modelo.
- Confirmar que la clave API es válida.
- Configurar un timeout.
- Manejar errores de autenticación.
- Manejar límites de solicitudes.
- Manejar indisponibilidad del proveedor.
- Registrar métricas de disponibilidad y costos.
- Evitar registrar preguntas que contengan datos personales innecesarios.

## 8. Integración con RAG

El flujo esperado es:

1. Recibir la pregunta del estudiante.
2. Validar el curso y el tenant.
3. Recuperar fragmentos del corpus correspondiente.
4. Enviar al modelo la pregunta junto con el contexto recuperado.
5. Generar una respuesta basada en las fuentes.
6. Devolver `answer` y `sources`.

Si no existen fragmentos suficientes para responder, el sistema debe informar que no dispone de información del curso sobre ese tema.

## 9. Manejo de errores

El backend debe contemplar:

- Error de autenticación del proveedor.
- Timeout.
- Límite de solicitudes excedido.
- Servicio externo no disponible.
- Error de recuperación vectorial.
- Ausencia de contexto relevante.

El estudiante debe recibir un mensaje claro y no información técnica sensible.

## 10. Estado

La configuración está documentada como preparación de la integración.

El endpoint `POST /v1/tutor/questions` todavía responde HTTP 501 en la implementación actual del backend. Por lo tanto, documentar las variables no significa que el Tutor IA ya esté operativo.

La integración efectiva debe realizarse y probarse en la etapa correspondiente del backend.
