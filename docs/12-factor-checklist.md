## Factor 2 — Dependencies (Dependencias)

Desde el componente de IA (AI/Data), las dependencias identificadas son:

- **Base de Datos Vectorial (pgvector)**: extensión de PostgreSQL usada para
  almacenar y consultar embeddings. Se declara como dependencia explícita
  en el entorno (no se asume instalada en el sistema base).
- **Servicio de IA/LLM externo**: SDK o cliente HTTP usado para conectarse
  al proveedor del modelo de lenguaje (ej. API de Claude/OpenAI).
- Todas las dependencias se gestionan mediante archivos de manejo de
  paquetes (ej. requirements.txt / package.json), evitando dependencias
  implícitas del sistema operativo.

## Factor 4 — Backing Services (Servicios de respaldo)

Los componentes de IA se tratan como recursos externos adjuntos (attached
resources), no como parte fija del código:

- **Base de Datos Vectorial**: se accede mediante una URL de conexión
  configurable (ver Factor 3 - Config), permitiendo intercambiarla por
  otra instancia sin cambiar el código.
- **Servicio de IA/LLM externo**: se consume vía API, con su endpoint y
  credenciales también configurables por variables de entorno.
- Esto permite reemplazar el proveedor del LLM o de la base vectorial sin
  modificar el código de la aplicación.

  # Checklist 12-Factor — AulaViva

## Objetivo

Este documento presenta la revisión de los principios de la metodología 12-Factor aplicada al proyecto AulaViva, enfocándose en los factores relacionados con configuración, servicios externos, proceso de despliegue, comunicación mediante puertos y gestión de logs.

Debido a que el proyecto se encuentra actualmente en una etapa inicial de planificación, los factores se evalúan considerando la arquitectura propuesta y las acciones necesarias para su futura implementación.

---

## Factor I Codebase

**Estado: Cumple**

AulaViva utiliza Git como sistema de control de versiones y GitHub como repositorio central del proyecto. El código fuente se mantiene versionado y los cambios pueden gestionarse mediante ramas y Pull Requests.

### Justificación

El código fuente de AulaViva se encuentra centralizado en un repositorio GitHub y gestionado mediante Git, permitiendo controlar versiones, registrar cambios y mantener una fuente única del código.

**Responsable:** Matías Díaz — TechLead.

---

## Factor II Dependencies

**Estado: No cumple**

En el diseño actual todavía no se ha formalizado completamente una estrategia para asegurar y controlar las versiones de todas las dependencias utilizadas durante el desarrollo y despliegue.

### Justificación

Si bien las dependencias pueden ser declaradas mediante los gestores correspondientes, todavía se requiere formalizar su versionado y control de vulnerabilidades para asegurar una construcción reproducible.

### Acción

1. Utilizar Maven o Gradle para administrar las dependencias del
   backend.
2. Utilizar `package.json` y `package-lock.json` para el frontend.
3. Mantener versiones controladas de las dependencias.
4. Ejecutar la instalación de dependencias automáticamente durante
   el proceso de construcción.
5. Incorporar análisis de vulnerabilidades de dependencias en el
   pipeline CI/CD.

**Responsable:** Matías Díaz — TechLead.

---

## Factor III — Config

**Estado:** Pendiente de implementación

La configuración de AulaViva debe mantenerse separada del código fuente de la aplicación. Esto incluye información como credenciales, URLs de servicios externos, parámetros de conexión a bases de datos, claves de APIs y configuraciones específicas de cada ambiente.

### Propuesta

Utilizar variables de entorno para gestionar la configuración de cada ambiente y evitar almacenar credenciales o información sensible directamente en el repositorio.

Los valores sensibles deberán gestionarse mediante mecanismos seguros de administración de secretos cuando la aplicación sea desplegada en un entorno cloud.

### Acción

* Definir las variables de entorno requeridas por cada componente.
* Crear archivos de ejemplo como `.env.example`, sin información sensible.
* Evitar almacenar credenciales y claves de API dentro del código fuente.
* Diferenciar la configuración correspondiente a los ambientes de desarrollo, pruebas y producción.

**Responsable:** Valentina León — DevSecOps.

---

## Factor IV — Backing Services

**Estado:** Pendiente de formalización

AulaViva contempla distintos servicios externos y componentes de infraestructura, entre ellos PostgreSQL con pgvector, almacenamiento compatible con S3, sistemas de mensajería y servicios relacionados con inteligencia artificial.

Estos componentes deben tratarse como recursos externos desacoplados de la aplicación, de manera que puedan ser reemplazados o modificados sin realizar cambios significativos en la lógica de negocio.

### Propuesta

Definir cada servicio mediante configuración externa y establecer interfaces claras entre la aplicación y los servicios utilizados.

### Servicios identificados

* PostgreSQL + pgvector: persistencia de datos y búsqueda vectorial.
* MinIO/S3: almacenamiento de archivos y objetos.
* RabbitMQ/Kafka: comunicación y mensajería entre componentes.
* Servicio o API de LLM: funcionalidades de inteligencia artificial.
* Servicio de autenticación: gestión de identidad y acceso.

### Acción

Documentar las dependencias externas y parametrizar sus conexiones mediante configuración externa, permitiendo reemplazar estos servicios sin modificar significativamente el código de la aplicación.

**Responsable:** Valentina León — DevSecOps, con apoyo del área AI/Data para los servicios relacionados con inteligencia artificial.

---

## Factor V — Build, Release, Run

**Estado:** Pendiente de implementación

AulaViva debe separar claramente las etapas de construcción, preparación y ejecución de la aplicación. El código fuente debe transformarse en un artefacto desplegable durante la etapa de build, mientras que la configuración específica de cada ambiente debe incorporarse durante el release. Finalmente, la aplicación debe ejecutarse utilizando el artefacto generado, sin modificar el código durante la ejecución.

### Propuesta

Utilizar un proceso automatizado de integración y despliegue que permita generar una versión reproducible de la aplicación y promoverla entre los ambientes de desarrollo, pruebas y producción.

La configuración específica de cada ambiente deberá mantenerse separada del código y gestionarse mediante variables de entorno o mecanismos seguros de configuración.

### Acción

* Definir un proceso de build reproducible.
* Generar artefactos o imágenes de contenedor versionadas.
* Separar las etapas de build, release y run.
* Evitar modificar el código fuente directamente durante la ejecución.
* Mantener separada la configuración de cada ambiente.
* Documentar el proceso de despliegue de la aplicación.

**Responsable:** Valentina León — DevSecOps.

---
## Factor VI Processes

**Estado: No cumple**

El diseño contempla una arquitectura compatible con procesos stateless mediante servicios externos como PostgreSQL y PostgreSQL + pgvector, pero el requisito todavía no está explícitamente definido.

### Justificación

La arquitectura aún no especifica explícitamente que el Backend AulaViva deba ejecutarse como un proceso stateless ni establece restricciones para evitar el almacenamiento de información persistente en memoria local o en el sistema de archivos del proceso.

### Acción

Definir explícitamente en la arquitectura que el Backend AulaViva será **stateless**. Cada instancia del backend deberá evitar almacenar información persistente de usuarios, sesiones o estado de negocio en memoria local o en el sistema de archivos. La información que deba persistir entre solicitudes deberá almacenarse en servicios externos, principalmente PostgreSQL y PostgreSQL + pgvector.

**Responsable:** Matías Díaz — TechLead.

--
## Factor VII — Port Binding

**Estado:** Pendiente de implementación

AulaViva debe permitir que sus servicios sean accesibles mediante la asignación de puertos definidos por la configuración del entorno de ejecución. La aplicación debe ser capaz de ejecutarse como un servicio independiente, sin depender de un servidor web externo instalado en el mismo entorno.

### Propuesta

Configurar la aplicación para que escuche en un puerto definido mediante una variable de entorno, permitiendo que el mismo artefacto pueda ejecutarse en distintos ambientes sin modificar su código.

En caso de utilizar contenedores, el puerto interno de la aplicación deberá ser definido y posteriormente publicado mediante la configuración del entorno de despliegue.

### Acción

* Definir el puerto de escucha de la aplicación mediante configuración externa.
* Evitar dejar el puerto fijo directamente en el código.
* Documentar los puertos utilizados por cada servicio.
* Configurar correctamente la exposición de puertos en el entorno cloud o de contenedores.
* Verificar la conectividad entre los distintos componentes de AulaViva.

**Responsable:** Valentina León — DevSecOps.

---

## Factor VIII Concurrency

**Estado: Cumple**

El diseño de AulaViva contempla el escalamiento horizontal del Backend, permitiendo ejecutar múltiples instancias de la aplicación de manera concurrente.
Esto permite aumentar la capacidad del sistema durante períodos de alta demanda, como evaluaciones, sin depender de una única instancia.

### Justificación

El diseño contempla el escalamiento horizontal y la distribución de carga entre instancias.

**Responsable:** Matías Díaz — TechLead.

---

## Factor IX Disposability


**Estado: No cumple**

El backend está diseñado para ejecutarse en instancias reemplazables, pero todavía no se han formalizado mecanismos para comprobar su disponibilidad y realizar una terminación segura.

### Justificación

Actualmente no se encuentran implementados todos los mecanismos necesarios para garantizar el inicio, detención y reemplazo seguro de las instancias.

### Acción 

Incorporar health checks y graceful shutdown, además de asegurar que las instancias no almacenen información persistente localmente. De esta manera, una instancia podrá ser reemplazada sin pérdida de información.

**Responsable:** Matías Díaz — TechLead.

---

## Factor XI — Logs

**Estado:** Pendiente de implementación

Los logs deben considerarse como un flujo de eventos de la aplicación y no como archivos que dependan del almacenamiento local del servidor o contenedor.

### Propuesta

La aplicación deberá generar logs estructurados y enviarlos a una solución centralizada de observabilidad en el entorno de despliegue.

Los logs deberían permitir identificar, entre otros elementos:

* Errores de aplicación.
* Eventos relevantes de seguridad.
* Solicitudes y respuestas importantes.
* Problemas de conexión con servicios externos.
* Eventos asociados a los servicios de inteligencia artificial.

No se recomienda depender de archivos locales dentro de los contenedores, ya que estos pueden perderse cuando una instancia sea reiniciada o reemplazada.


---

## Factor X — Dev/Prod Parity

**Estado:** Pendiente de implementación

AulaViva debe mantener dev, staging y producción lo más similares posible. Al ser SaaS multi-tenant con Tutor IA/RAG sobre PostgreSQL + pgvector, la paridad es crítica: si el esquema vectorial, la versión del LLM o el broker difieren entre ambientes, el comportamiento del tutor puede cambiar sin que las pruebas lo detecten.

### Propuesta

- Desplegar la misma imagen de contenedor versionada (backend Spring Boot + frontend React) en dev, staging y prod, cambiando solo variables de entorno.
- Gestionar infraestructura con IaC (Terraform sobre AWS) para replicar RDS PostgreSQL + pgvector, MSK, S3 y Cognito.
- Usar versiones idénticas de PostgreSQL, pgvector, Kafka y endpoint del LLM en cada ambiente.
- Documentar diferencias permitidas solo de capacidad (tamaño de instancia, réplicas), nunca de comportamiento.
- Para desarrollo local: docker-compose con PostgreSQL + pgvector, Kafka y MinIO (S3-compatible).

### Acción

* Definir matriz de servicios por ambiente (dev / staging / prod).
* Versionar infraestructura con Terraform.
* Promover la misma imagen entre ambientes (build once, deploy many) vía CI/CD.
* Smoke tests post-despliegue en cada ambiente.
* Validar flujo completo del Tutor IA/RAG en staging antes de promover a producción.

**Responsable:** Alexander Ruiz-Tagle — QA Lead, con apoyo de Valentina León (DevSecOps).

---

## Factor XII — Admin Processes

**Estado:** Pendiente de implementación

AulaViva requiere tareas administrativas fuera del flujo normal: migraciones de esquema, reindexación de embeddings del Tutor IA cuando se actualiza el currículo MINEDUC, seeds por tenant, backfills académicos y limpieza de datos. Deben ejecutarse como procesos one-off en el mismo entorno que la app, con el mismo código y credenciales.

### Propuesta

- Ejecutar procesos administrativos como Jobs efímeros (Kubernetes Jobs en EKS) o tareas del pipeline CI/CD, reutilizando la imagen del backend.
- Migraciones con Flyway o Liquibase, como paso previo al deploy del backend.
- Reindexación de embeddings y regeneración de vectores como Jobs independientes contra la misma base vectorial.
- NO ejecutar migraciones ni seeds automáticos al arrancar la aplicación.
- Registrar cada ejecución en logs centralizados: tenant, operación, usuario responsable, resultado.

### Acción

* Definir migraciones con Flyway/Liquibase como paso previo al deploy.
* Implementar Jobs one-off para tareas IA/Data (reindexación, backfill).
* Evitar migraciones/seeds automáticos al inicio del backend.
* Usar mismas credenciales y variables de entorno que la app.
* Registrar cada ejecución en logs con trazabilidad.
* Documentar catálogo de procesos administrativos.

**Responsable:** Alexander Ruiz-Tagle — QA Lead, con apoyo de Gerardo González (AI/Data) y Matías Díaz (Tech Lead).





