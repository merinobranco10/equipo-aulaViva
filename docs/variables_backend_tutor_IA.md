# Inventario de variables — Backend y Tutor IA de AulaViva

## 1. Objetivo

Identificar las variables de entorno necesarias para ejecutar el backend de AulaViva y preparar la integración del Tutor IA.

Este documento servirá para clasificar las variables, gestionar los secretos y preparar los manifiestos de Kubernetes.

**Importante:** las variables propuestas deben verificarse con la implementación real antes de utilizarlas.

## 2. Variables del backend

| Variable | Función | Clasificación inicial |
|---|---|---|
| SERVER_PORT | Puerto del servidor backend | Configuración |
| SPRING_PROFILES_ACTIVE | Entorno de ejecución | Configuración |
| LOG_LEVEL | Nivel de registro de eventos | Configuración |
| DB_HOST | Dirección del servidor PostgreSQL | Configuración |
| DB_PORT | Puerto de PostgreSQL | Configuración |
| DB_NAME | Nombre de la base de datos | Configuración |
| DB_USERNAME | Usuario de la base de datos | Sensible |
| DB_PASSWORD | Contraseña de la base de datos | Secreto |
| KAFKA_BOOTSTRAP_SERVERS | Direcciones de los brokers Kafka | Configuración |
| AWS_REGION | Región de los servicios AWS | Configuración |

## 3. Variables del Tutor IA

| Variable | Función | Clasificación inicial |
|---|---|---|
| AI_PROVIDER | Proveedor del modelo de inteligencia artificial | Configuración |
| AI_API_KEY | Clave de autenticación del proveedor | Secreto |
| AI_MODEL | Modelo de inteligencia artificial utilizado | Configuración |
| AI_BASE_URL | Dirección del servicio de IA, si corresponde | Configuración |
| AI_TIMEOUT_SECONDS | Tiempo máximo de espera de una solicitud | Configuración |
| EMBEDDING_MODEL | Modelo utilizado para generar embeddings | Configuración |
| VECTOR_DB_HOST | Dirección del almacenamiento vectorial | Configuración |
| VECTOR_DB_PORT | Puerto del almacenamiento vectorial | Configuración |
| VECTOR_DB_NAME | Nombre de la base vectorial | Configuración |
| VECTOR_DB_USERNAME | Usuario del almacenamiento vectorial | Sensible |
| VECTOR_DB_PASSWORD | Contraseña del almacenamiento vectorial | Secreto |

## 4. Infraestructura y seguridad

| Elemento | Propósito | Tratamiento |
|---|---|---|
| AWS IAM Role | Permitir el acceso autorizado a servicios AWS | Identidad y permisos |
| Terraform state bucket | Almacenar el estado remoto de Terraform, si se utiliza S3 | Configuración de infraestructura |
| Terraform state locking | Evitar modificaciones simultáneas del estado | Configuración de infraestructura |

## 5. Gestión propuesta de secretos

- Utilizar ConfigMaps para las variables de configuración no confidenciales.
- Utilizar Kubernetes Secrets o AWS Secrets Manager para contraseñas y claves API.
- No almacenar credenciales reales en el repositorio GitHub.
- No incluir contraseñas ni claves API reales en los manifiestos.
- Priorizar roles IAM y mecanismos de identidad de carga de trabajo para acceder a AWS.
- Definir permisos de acceso mínimos y separar los entornos de desarrollo y producción.

## 6. Validaciones pendientes

- Confirmar las variables que realmente utiliza el backend.
- Confirmar la integración y el proveedor del Tutor IA.
- Confirmar PostgreSQL y la configuración de pgvector.
- Confirmar la configuración de Kafka.
- Determinar cómo se inyectarán los secretos en Kubernetes.
- Coordinar con el equipo la incorporación de las variables a los manifiestos.

## 7. Entregable

Este inventario se utilizará como base para la clasificación de variables, la propuesta de gestión de secretos y la posterior configuración de los manifiestos de Kubernetes de AulaViva.
