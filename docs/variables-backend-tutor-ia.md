# Variables requeridas para Backend y Tutor IA — AulaViva

## 1. Backend

| Variable | Función | Tipo |
|---|---|---|
| SERVER_PORT | Puerto del servidor | Configuración |
| SPRING_PROFILES_ACTIVE | Entorno de ejecución | Configuración |
| DB_HOST | Dirección de PostgreSQL | Configuración |
| DB_PORT | Puerto de PostgreSQL | Configuración |
| DB_NAME | Nombre de la base de datos | Configuración |
| DB_USERNAME | Usuario de la base de datos | Secreto |
| DB_PASSWORD | Contraseña de la base de datos | Secreto |
| KAFKA_BOOTSTRAP_SERVERS | Dirección de los brokers Kafka | Configuración |
| AWS_REGION | Región de AWS | Configuración |

## 2. Tutor IA

| Variable | Función | Tipo |
|---|---|---|
| AI_PROVIDER | Proveedor del modelo de IA | Configuración |
| AI_API_KEY | Autenticación con el proveedor de IA | Secreto |
| AI_MODEL | Modelo de IA utilizado | Configuración |
| AI_BASE_URL | URL del servicio de IA, si corresponde | Configuración |
| AI_TIMEOUT_SECONDS | Tiempo máximo de espera | Configuración |
| EMBEDDING_MODEL | Modelo para generar embeddings | Configuración |
| VECTOR_DB_HOST | Dirección del servicio vectorial | Configuración |
| VECTOR_DB_PORT | Puerto del servicio vectorial | Configuración |
| VECTOR_DB_NAME | Nombre de la base vectorial | Configuración |
| VECTOR_DB_USERNAME | Usuario de la base vectorial | Secreto |
| VECTOR_DB_PASSWORD | Contraseña de la base vectorial | Secreto |

## 3. Gestión de secretos

- Las variables no sensibles se gestionarán mediante Kubernetes ConfigMaps.
- Las contraseñas y claves API se gestionarán mediante Kubernetes Secrets o AWS Secrets Manager.
- Se evitará almacenar credenciales reales en GitHub.
- No se incluirán claves API ni contraseñas reales en los archivos de configuración.
- Para acceder a AWS se priorizarán roles IAM y mecanismos de identidad de carga de trabajo, evitando claves estáticas.

## 4. Pendientes de confirmación

- Confirmar las variables que requiere la implementación real del backend.
- Confirmar el proveedor, modelo y API del Tutor IA.
- Confirmar la tecnología y configuración de PostgreSQL y pgvector.
- Confirmar la configuración de Kafka y AWS.
- Coordinar con el equipo la incorporación de las variables a los manifiestos de Kubernetes.

## 5. Uso del documento

Este inventario servirá como referencia para clasificar las variables, gestionar los secretos y preparar los manifiestos de despliegue de AulaViva.
