# Checklist 12-Factor — AulaViva

## Objetivo

Este documento presenta la revisión de los principios de la metodología
12-Factor aplicada al proyecto AulaViva, considerando su arquitectura
SaaS multi-tenant y los componentes asociados al Tutor IA/RAG.

Debido a que el proyecto se encuentra actualmente en una etapa inicial
de planificación, los factores se evalúan considerando la arquitectura
propuesta y las acciones necesarias para su futura implementación.

---

## Factor I — Codebase

**Estado: Cumple**

AulaViva utiliza Git como sistema de control de versiones y GitHub como
repositorio central del proyecto. El código fuente se mantiene versionado
y los cambios pueden gestionarse mediante ramas y Pull Requests.

### Justificación

El código fuente de AulaViva se encuentra centralizado en un repositorio
GitHub y gestionado mediante Git, permitiendo controlar versiones,
registrar cambios y mantener una fuente única del código.

El mismo codebase será utilizado como base para los distintos despliegues
de AulaViva en los ambientes de desarrollo, pruebas y producción.

**Responsable:** Matías Díaz — Tech Lead.

---

## Factor II — Dependencies

**Estado: No cumple**

En el diseño actual todavía no se ha formalizado completamente una
estrategia para asegurar y controlar las versiones de todas las
dependencias utilizadas durante el desarrollo y despliegue.

### Justificación

Las dependencias de cada componente deben declararse explícitamente mediante
sus respectivos gestores de paquetes, evitando depender de librerías o
componentes instalados manualmente en el sistema operativo.

En el componente **AI/Data**, esto incluye las librerías necesarias para el
funcionamiento del Tutor IA/RAG, como los clientes utilizados para acceder a
PostgreSQL + pgvector, generar y consultar embeddings, y comunicarse con el
servicio externo de IA/LLM mediante SDK o cliente HTTP.

### Dependencias identificadas

- **Backend:** dependencias administradas mediante Maven o Gradle.
- **Frontend:** dependencias administradas mediante `package.json` y
  `package-lock.json`.
- **AI/Data:** dependencias administradas mediante `requirements.txt` o
  `pyproject.toml`, incluyendo:
  - Cliente para PostgreSQL + pgvector.
  - Librerías utilizadas para embeddings y recuperación de información
    del sistema RAG.
  - SDK o cliente HTTP para comunicarse con el proveedor del LLM.
  - Otras librerías necesarias para el procesamiento de datos del Tutor IA.

### Acción

- Declarar explícitamente todas las dependencias del Backend, Frontend y AI/Data.
- Mantener versiones controladas de las dependencias.
- Ejecutar su instalación automáticamente durante el proceso de build.
- Evitar dependencias implícitas del sistema operativo.
- Incorporar, como medida complementaria, análisis de vulnerabilidades de
  dependencias en el pipeline CI/CD.

**Responsable:** Matías Díaz — Tech Lead, con apoyo de Gerardo González - AI/Data.

---

## Factor III — Config

**Estado: No cumple**

La configuración de AulaViva debe mantenerse separada del código fuente de la aplicación. Esto incluye información como credenciales, URLs de servicios externos, parámetros de conexión a bases de datos, claves de APIs y configuraciones específicas de cada ambiente.

### Justificación

AulaViva contempla utilizar variables de entorno para gestionar la
configuración operacional de la aplicación y mecanismos seguros de
administración de secretos para los valores sensibles, evitando almacenar
credenciales o claves directamente en el código fuente o repositorio.

Esto incluye la configuración necesaria para los componentes de Backend,
Frontend y AI/Data, como conexiones a PostgreSQL, almacenamiento,
autenticación, servicios de mensajería y servicios utilizados por el
Tutor IA/RAG.

Debido al carácter multi-tenant de AulaViva, la configuración específica
de cada colegio deberá mantenerse separada de la configuración general
de la aplicación y gestionarse de manera que preserve el aislamiento
entre tenants.

### Acción

- Definir las variables de entorno requeridas por cada componente.
- Crear `.env.example` sin información sensible.
- Evitar almacenar credenciales y claves de API en el código fuente.
- Gestionar secretos mediante mecanismos seguros en cloud.
- Diferenciar la configuración de desarrollo, pruebas y producción.
- Definir la configuración externa necesaria para los servicios del
  Tutor IA/RAG.
- Definir un mecanismo para gestionar la configuración específica de cada
  tenant sin incorporarla directamente al código fuente.

**Responsable:** Valentina León — DevSecOps, con apoyo de Matías Díaz —
Tech Lead y Gerardo González — AI/Data para la configuración de los
servicios del Tutor IA/RAG.

---

## Factor IV — Backing Services

**Estado: No cumple**

AulaViva contempla servicios externos como PostgreSQL + pgvector,
almacenamiento de objetos, sistemas de mensajería, servicios de
autenticación y servicios de IA/LLM.

Estos servicios serán tratados como recursos externos adjuntos
(*backing services*), desacoplados del código de la aplicación y accesibles
mediante configuración externa.

### Justificación

Las conexiones a los servicios externos se gestionarán mediante URLs,
endpoints, credenciales y otras configuraciones externas, de acuerdo con
lo establecido en el Factor III — Config.

Desde el componente AI/Data, PostgreSQL + pgvector y el servicio externo
de IA/LLM serán tratados como recursos externos. El Tutor IA/RAG accederá
a la información vectorial mediante una conexión configurable y consumirá
el modelo de lenguaje mediante API o SDK, manteniendo desacoplado el
proveedor utilizado.

### Servicios identificados

- PostgreSQL + pgvector: persistencia de datos, almacenamiento y búsqueda vectorial utilizada por el sistema RAG.
- Almacenamiento de objetos (MinIO/S3): almacenamiento de archivos y recursos utilizados por la plataforma.
- Servicio de mensajería: RabbitMQ o Kafka, pendiente de decisión.
- Servicio/API de LLM: Proveedor externo utilizado por el Tutor IA.
- Servicio de autenticación: Gestión de identidad y acceso.

### Acción

- Documentar todos los backing services utilizados por AulaViva.
- Parametrizar sus conexiones mediante configuración externa.
- Evitar dependencias directas entre la lógica de negocio y un proveedor
  específico cuando sea posible.
- Definir el servicio de mensajería que utilizará la arquitectura.
- Definir y documentar el mecanismo de aislamiento de datos entre tenants.
- Validar que el proveedor del LLM y las instancias de PostgreSQL + pgvector
  puedan configurarse externamente sin modificar el código de la aplicación.

**Responsable:** Valentina León — DevSecOps, con apoyo de Gerardo González —
AI/Data para PostgreSQL + pgvector y los servicios relacionados con el
Tutor IA/RAG.

---

## Factor V — Build, Release, Run

**Estado: No cumple**

La arquitectura propuesta contempla separar las etapas de construcción (Build),
preparación (Release) y ejecución (Run)de AulaViva.

### Justificación

Durante la etapa de **Build**, el código fuente deberá transformarse en
artefactos o imágenes de contenedor versionadas, incluyendo las dependencias
necesarias para su ejecución.

Durante **Release**, el artefacto generado deberá combinarse con la
configuración correspondiente al ambiente de despliegue, manteniendo las
credenciales y configuraciones externas separadas del código.

Finalmente, durante **Run**, AulaViva deberá ejecutar el artefacto generado
sin modificar su código ni reconstruirlo.

El mismo artefacto que haya sido validado en los ambientes previos deberá
poder promoverse posteriormente a producción, siguiendo el principio
**build once, deploy many**.

Actualmente este proceso todavía no se encuentra completamente implementado
y automatizado, por lo que el factor no se considera cumplido.

### Acción

- Definir un proceso de build reproducible.
- Generar imágenes o artefactos versionados.
- Identificar cada release mediante una versión única y trazable.
- Mantener la configuración de cada ambiente separada del artefacto generado.
- Promover el mismo artefacto entre desarrollo, pruebas/staging y producción,
  evitando reconstruirlo entre ambientes.
- Automatizar las etapas de Build, Release y Run mediante el pipeline CI/CD.
- Evitar modificaciones del código o del artefacto durante la ejecución.
- Documentar el procedimiento de despliegue y rollback.

**Responsable:** Valentina León — DevSecOps, con apoyo de Matías Díaz —
Tech Lead.

---

## Factor VI — Processes

**Estado: No cumple**

El diseño contempla una arquitectura compatible con procesos stateless, evitando que
las instancias del Backend o de los componentes asociados al Tutor IA/RAG
dependan de información persistente almacenada localmente.

### Justificación

Aunque la arquitectura contempla un modelo stateless, este comportamiento aún no se encuentra formalmente definido ni validado. La información persistente deberá mantenerse en servicios externos como PostgreSQL y pgvector.

Esto permitirá que distintas instancias atiendan solicitudes de forma independiente, facilitando el escalamiento horizontal durante períodos de alta demanda, como evaluaciones, y manteniendo correctamente el contexto de cada tenant.

### Acción

- Definir explícitamente que el Backend AulaViva será **stateless**.
- Evitar almacenar sesiones o información persistente localmente.
- Mantener el estado persistente en backing services externos.
- Garantizar que cada solicitud identifique correctamente su tenant.
- Validar que distintas instancias puedan atender solicitudes sin depender de estado local previo.

**Responsable:** Matías Díaz — Tech Lead, con apoyo de Gerardo González —
AI/Data para los procesos asociados al Tutor IA/RAG.

---

## Factor VII — Port Binding

**Estado: No cumple**

AulaViva debe permitir que sus servicios sean accesibles mediante la asignación de puertos definidos por la configuración del entorno de ejecución. La aplicación debe ser capaz de ejecutarse como un servicio independiente con un despliegue en distintos entornos.


### Justificación

La arquitectura contempla que los servicios utilicen puertos definidos mediante configuración externa, evitando valores fijos en el código. En entornos con contenedores o cloud, la publicación y enrutamiento de estos puertos será gestionada por la infraestructura de despliegue.

Actualmente esta configuración aún no se encuentra implementada ni validada.

### Acción

- Definir y documentar los puertos utilizados por cada servicio.
- Configurar los puertos mediante variables de entorno cuando corresponda.
- Evitar puertos fijos directamente en el código.
- Configurar su exposición en contenedores y entorno cloud.
- Validar la conectividad entre los servicios.

**Responsable:** Valentina León — DevSecOps.

---

## Factor VIII — Concurrency

**Estado: No cumple**

El diseño de AulaViva contempla el escalamiento horizontal del Backend, permitiendo ejecutar múltiples instancias de la aplicación de manera concurrente. Esto permite aumentar la capacidad del sistema durante períodos de alta demanda, como evaluaciones, sin depender de una única instancia.

### Justificación
El diseño contempla el escalamiento horizontal y la distribución de carga entre instancias, pero actualmente esta capacidad todavía no se encuentra implementada ni validada mediante pruebas de carga.

### Acción

- Implementar escalamiento horizontal del Backend.
- Incorporar un mecanismo de distribución de carga.
- Definir una estimación de usuarios concurrentes esperados.
- Realizar pruebas de carga en períodos de alta demanda.
- Validar que nuevas instancias puedan incorporarse sin afectar el funcionamiento del sistema.

**Responsable:** Matías Díaz — Tech Lead, con apoyo de Alexander Ruiz-Tagle — QA Lead.

---

## Factor IX — Disposability

**Estado: No cumple**

El backend está diseñado para ejecutarse en instancias reemplazables, pero todavía no se han formalizado mecanismos para comprobar su disponibilidad y realizar una terminación segura.

### Justificación

Actualmente no se encuentran implementados todos los mecanismos necesarios para garantizar el inicio, detención y reemplazo seguro de las instancias.

### Acción

- Implementar health checks para verificar el estado de las instancias.
- Incorporar graceful shutdown para finalizar solicitudes en curso antes de detener una instancia.
- Evitar almacenar información persistente localmente.
- Validar que una instancia pueda ser reemplazada sin pérdida de información.
- Reducir el tiempo de inicio de nuevas instancias.

**Responsable:** Matías Díaz — Tech Lead, con apoyo de Valentina León — DevSecOps.

---

## Factor X — Dev/Prod Parity

**Estado: No cumple**

AulaViva debe mantener dev, staging y producción lo más similares posible. Al ser SaaS multi-tenant con Tutor IA/RAG sobre PostgreSQL + pgvector, la paridad es crítica: si el esquema vectorial, la versión del LLM o el broker difieren entre ambientes, el comportamiento del tutor puede cambiar sin que las pruebas lo detecten.

### Justificación

Este principio es especialmente importante para AulaViva debido al Tutor IA/RAG, ya que diferencias en PostgreSQL, pgvector, servicios de mensajería o configuración del LLM podrían generar comportamientos distintos entre los ambientes.

Actualmente la paridad entre ambientes se encuentra contemplada en el diseño, pero todavía no existe evidencia de que estos ambientes estén implementados y homologados.

### Acción

- Definir matriz de servicios por ambiente (dev / staging / prod).
- Versionar infraestructura con Terraform o una herramienta de Infrastructure as Code (IaC) equivalente.
- Mantener versiones equivalentes de PostgreSQL, pgvector y demás servicios críticos entre ambientes.
- Promover la misma imagen entre ambientes (build once, deploy many) vía CI/CD.
- Ejecutar Smoke tests post-despliegue en cada ambiente.
- Validar el flujo completo del Tutor IA/RAG en staging antes de promover a producción.

**Responsable:** Alexander Ruiz-Tagle — QA Lead, con apoyo de
Valentina León — DevSecOps.

---

## Factor XI — Logs

**Estado: No cumple**

Los logs deben considerarse como un flujo de eventos de la aplicación y no como archivos que dependan del almacenamiento local del servidor o contenedor.

### Justificación

La arquitectura contempla logs estructurados y centralizados para facilitar el monitoreo y diagnóstico
de los servicios.

Debido a que AulaViva trabaja con datos de menores, los logs no deberán almacenar credenciales, tokens, datos personales sensibles ni conversaciones completas del Tutor IA.

Actualmente este mecanismo aún no está implementado.

### Acción
La aplicación deberá generar logs estructurados y enviarlos a una solución centralizada de observabilidad en el entorno de despliegue.

Los logs deberían permitir identificar, entre otros elementos:

- Errores de aplicación.
- Eventos relevantes de seguridad.
- Solicitudes y respuestas importantes.
- Problemas de conexión con servicios externos.
- Eventos asociados a los servicios de inteligencia artificial.

**Responsable:** Valentina León — DevSecOps.

---

## Factor XII — Admin Processes

**Estado: No cumple**

AulaViva requiere tareas administrativas fuera del flujo normal: migraciones de esquema, reindexación de embeddings del Tutor IA cuando se actualiza el currículo MINEDUC, seeds por tenant, backfills académicos y limpieza de datos. Deben ejecutarse como procesos one-off en el mismo entorno que la app, con el mismo código y credenciales.

### Justificación

Estas operaciones serán ejecutadas como procesos one-off utilizando el
mismo código y entorno que la aplicación, evitando incorporar estas tareas
al funcionamiento normal del Backend.

Esto es especialmente relevante para el Tutor IA/RAG, ya que una
actualización del contenido curricular puede requerir regenerar embeddings
o reindexar información.

### Acción

- Definir migraciones mediante Flyway/Liquibase.
- Implementar Jobs one-off para reindexación y backfills.
- Evitar migraciones y seeds automáticos durante el inicio del Backend.
- Utilizar credenciales con los privilegios mínimos necesarios.
- Registrar las ejecuciones con trazabilidad.
- Identificar el tenant afectado cuando corresponda.

**Responsable:** Alexander Ruiz-Tagle — QA Lead, con apoyo de Gerardo González — AI/Data y
Matías Díaz — Tech Lead.

## Resumen de evaluación 12-Factor — AulaViva

| N.º | Factor | Estado | Justificación resumida |
|---|---|---|---|
| I | **Codebase** | ✅ Cumple | El código ya se gestiona mediante Git y GitHub como fuente central versionada. |
| II | **Dependencies** | ❌ No cumple | Falta formalizar y versionar las dependencias de Backend, Frontend y AI/Data. |
| III | **Config** | ❌ No cumple | La configuración externa y la gestión segura de secretos están definidas, pero aún pendientes de implementación. |
| IV | **Backing Services** | ❌ No cumple | Los servicios externos están identificados, pero falta formalizar su configuración y desacoplamiento. |
| V | **Build, Release, Run** | ❌ No cumple | La separación de etapas está definida, pero aún no existe un proceso automatizado y reproducible. |
| VI | **Processes** | ❌ No cumple | El modelo stateless está contemplado, pero todavía no se encuentra formalizado ni validado. |
| VII | **Port Binding** | ❌ No cumple | Los puertos configurables están contemplados, pero aún no se encuentran implementados ni validados. |
| VIII | **Concurrency** | ❌ No cumple | Se contempla escalamiento horizontal, pero aún falta implementarlo y validarlo mediante pruebas de carga. |
| IX | **Disposability** | ❌ No cumple | Faltan health checks, graceful shutdown y validación del reemplazo seguro de instancias. |
| X | **Dev/Prod Parity** | ❌ No cumple | La paridad está definida en el diseño, pero los ambientes aún no están implementados y homologados. |
| XI | **Logs** | ❌ No cumple | Los logs estructurados y centralizados están definidos, pero todavía no se encuentran implementados. |
| XII | **Admin Processes** | ❌ No cumple | Los procesos administrativos están identificados, pero aún no se encuentran implementados. |

>**Nota:** La evaluación considera el estado actual de AulaViva. Los principios
> definidos únicamente a nivel de diseño o planificación se clasifican como
> **No cumple** mientras no exista evidencia suficiente de su formalización o
> implementación. Esto permite identificar claramente las brechas que deberán
> abordarse durante el desarrollo del proyecto.

**Revisado y aprobado por:** Branco Merino — Product Owner.
