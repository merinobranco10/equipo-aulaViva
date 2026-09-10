# Checklist 12-Factor — AulaViva

## Objetivo

Este documento presenta la revisión de los principios de la metodología 12-Factor aplicada al proyecto AulaViva, enfocándose en los factores relacionados con configuración, servicios externos y gestión de logs.

Debido a que el proyecto se encuentra actualmente en una etapa inicial de planificación, los factores se evalúan considerando la arquitectura propuesta y las acciones necesarias para su futura implementación.

---

## Factor III — Config

**Estado:** Pendiente de implementación

La configuración de AulaViva debe mantenerse separada del código fuente de la aplicación. Esto incluye información como credenciales, URLs de servicios externos, parámetros de conexión a bases de datos, claves de APIs y configuraciones específicas de cada ambiente.

### Propuesta

Utilizar variables de entorno para gestionar la configuración de cada ambiente y evitar almacenar credenciales o información sensible directamente en el repositorio.

Los valores sensibles deberán gestionarse mediante mecanismos seguros de administración de secretos cuando la aplicación sea desplegada en un entorno cloud.

### Acción

- Definir las variables de entorno requeridas por cada componente.
- Crear archivos de ejemplo como `.env.example`, sin información sensible.
- Evitar almacenar credenciales y claves de API dentro del código fuente.
- Diferenciar la configuración correspondiente a los ambientes de desarrollo, pruebas y producción.

**Responsable:** Valentina León — DevSecOps.

---

## Factor IV — Backing Services

**Estado:** Pendiente de formalización

AulaViva contempla distintos servicios externos y componentes de infraestructura, entre ellos PostgreSQL con pgvector, almacenamiento compatible con S3, sistemas de mensajería y servicios relacionados con inteligencia artificial.

Estos componentes deben tratarse como recursos externos desacoplados de la aplicación, de manera que puedan ser reemplazados o modificados sin realizar cambios significativos en la lógica de negocio.

### Propuesta

Definir cada servicio mediante configuración externa y establecer interfaces claras entre la aplicación y los servicios utilizados.

### Servicios identificados

- PostgreSQL + pgvector: persistencia de datos y búsqueda vectorial.
- MinIO/S3: almacenamiento de archivos y objetos.
- RabbitMQ/Kafka: comunicación y mensajería entre componentes.
- Servicio o API de LLM: funcionalidades de inteligencia artificial.
- Servicio de autenticación: gestión de identidad y acceso.

### Acción

Documentar las dependencias externas y parametrizar sus conexiones mediante configuración externa, permitiendo reemplazar estos servicios sin modificar significativamente el código de la aplicación.

**Responsable:** Valentina León — DevSecOps, con apoyo del área AI/Data para los servicios relacionados con inteligencia artificial.

---

## Factor XI — Logs

**Estado:** Pendiente de implementación

Los logs deben considerarse como un flujo de eventos de la aplicación y no como archivos que dependan del almacenamiento local del servidor o contenedor.

### Propuesta

La aplicación deberá generar logs estructurados y enviarlos a una solución centralizada de observabilidad en el entorno de despliegue.

Los logs deberían permitir identificar, entre otros elementos:

- Errores de aplicación.
- Eventos relevantes de seguridad.
- Solicitudes y respuestas importantes.
- Problemas de conexión con servicios externos.
- Eventos asociados a los servicios de inteligencia artificial.

No se recomienda depender de archivos locales dentro de los contenedores, ya que estos pueden perderse cuando una instancia sea reiniciada o reemplazada.

### Acción

- Definir un formato estándar para los logs.
- Utilizar niveles de registro como INFO, WARN y ERROR.
- Evitar registrar contraseñas, tokens u otra información sensible.
- Preparar la integración con una plataforma de observabilidad durante el despliegue cloud.

**Responsable:** Valentina León — DevSecOps.

---

## Resumen

| Factor | Estado | Acción principal | Responsable |
|---|---|---|---|
| III. Config | Pendiente | Implementar variables de entorno y gestión segura de secretos | Valentina León |
| IV. Backing Services | Pendiente | Desacoplar y parametrizar los servicios externos | Valentina León |
| XI. Logs | Pendiente | Implementar logs estructurados y centralizados | Valentina León |

## Criterio de evaluación

Los tres factores quedan registrados como pendientes de implementación debido a que AulaViva se encuentra actualmente en una etapa inicial de planificación.

Las propuestas establecidas en este documento servirán como criterios para orientar la implementación y posterior despliegue del sistema en un entorno cloud.
