# ADR 0003 — Proveedor Cloud

**Estado:** Aceptado\
**Fecha:** 11-09-2026\
**Autor:** Matías Díaz Tech Lead
## Contexto

AulaViva es una plataforma SaaS educativa multi-tenant destinada a colegios de la Región Metropolitana. El sistema deberá gestionar estudiantes, docentes, coordinadores, apoderados y sostenedores, además de contenidos, evaluaciones automáticas, seguimiento académico y un Tutor IA basado en RAG.

De acuerdo con el **ADR 0002 — Estilo arquitectónico**, el MVP se plantea inicialmente como un **Monolito Modular**, con posibilidad de evolucionar progresivamente hacia microservicios cuando determinados módulos requieran mayor escalabilidad, aislamiento o evolución independiente.

El proveedor Cloud seleccionado deberá permitir implementar la arquitectura definida y contribuir al cumplimiento de los principales atributos de calidad establecidos para el sistema.

Los principales criterios considerados para la selección del proveedor son:

### Escalabilidad y ejecución

- Permitir escalamiento horizontal durante períodos de alta demanda.
- Soportar una arquitectura basada en procesos stateless.
- Permitir ejecutar procesos administrativos y tareas batch de manera independiente.
- Facilitar la evolución progresiva desde el Monolito Modular hacia microservicios.

### Despliegue y operación

- Permitir utilizar una misma imagen de aplicación entre los ambientes de desarrollo, staging y producción, modificando únicamente la configuración de cada entorno.
- Facilitar la implementación futura de CI/CD y pruebas automatizadas.
- Proporcionar mecanismos de observabilidad y health checks.

### Persistencia y servicios

- Permitir mantener los datos persistentes fuera de los contenedores.
- Proporcionar servicios administrados para bases de datos, mensajería y almacenamiento.
- Soportar los requerimientos asociados al Tutor IA/RAG.
- Facilitar la implementación del aislamiento lógico entre tenants.

La selección debe considerar además la capacidad del proveedor para acompañar el crecimiento de AulaViva sin requerir un cambio de plataforma cuando la solución evolucione desde el Monolito Modular hacia una arquitectura basada en microservicios.

## Decisión

Se selecciona **Amazon Web Services (AWS)** como proveedor Cloud objetivo para AulaViva.

La elección de AWS se fundamenta en su capacidad para cubrir los principales requisitos previstos para AulaViva relacionados con **escalabilidad, ejecución de aplicaciones contenerizadas, persistencia administrada, CI/CD, observabilidad y procesamiento asíncrono, además de permitir la integración con servicios externos como Anthropic Claude.**

También permitirá disponer de un ecosistema de servicios que pueda acompañar la evolución de AulaViva desde el MVP hacia una arquitectura con componentes independientes, sin establecer desde el inicio una arquitectura de microservicios.

Como parte de esta decisión, se utilizarán servicios gestionados de AWS
para los principales componentes definidos en el C4 Nivel 2:

- **Amazon RDS for PostgreSQL** para la Base de Datos.
- **Amazon RDS for PostgreSQL + pgvector** para la Base de Datos Vectorial.
- **Amazon S3** para el almacenamiento de objetos.
- **Amazon MSK con Apache Kafka** para el Message Broker.
- **Amazon Cognito** para la gestión de identidad y autenticación.

Para el Tutor IA/RAG se utilizará **Anthropic Claude mediante API**
como servicio LLM externo.

## Alternativas

### 1. Amazon Web Services (AWS) — Seleccionada

AWS constituye una alternativa viable para AulaViva debido a su amplio ecosistema de servicios para aplicaciones contenerizadas, bases de datos, mensajería, observabilidad, automatización e inteligencia artificial.

**Ventajas:**

- Facilita el escalamiento horizontal.
- Permite ejecutar aplicaciones contenerizadas mediante servicios administrados.
- Soporta arquitecturas basadas en procesos stateless.
- Permite externalizar la información persistente.
- Facilita la futura implementación de CI/CD y automatización de infraestructura.
- Proporciona servicios administrados para bases de datos, mensajería y observabilidad.
- Permite ejecutar procesos administrativos y tareas batch de forma independiente.
- Es compatible con la evolución progresiva desde el Monolito Modular hacia microservicios.
- Dispone de servicios para cargas de trabajo de datos e inteligencia artificial.
- Permite centralizar diferentes capacidades de infraestructura dentro de un mismo proveedor.

**Desventajas:**

- La gran cantidad de servicios disponibles puede aumentar la complejidad al momento de seleccionar y configurar la solución.
- La facturación basada en múltiples servicios y modalidades de consumo puede dificultar la estimación inicial de costos.
- Algunos servicios tienen configuraciones y conceptos propios que requieren conocimientos específicos del ecosistema AWS.
- El uso de servicios propietarios puede dificultar una migración posterior

### 2. Microsoft Azure

Microsoft Azure constituye una alternativa técnicamente viable para ejecutar AulaViva mediante servicios administrados, contenedores, bases de datos, mensajería, CI/CD y herramientas de observabilidad.

**Ventajas:**

- Soporte para aplicaciones contenerizadas.
- Facilita el escalamiento horizontal.
- Amplia oferta de servicios administrados.
- Integración con herramientas de desarrollo y CI/CD.
- Servicios administrados para bases de datos, mensajería y observabilidad.
- Permite implementar aplicaciones stateless.
- Facilita una futura evolución hacia microservicios.

**Desventajas:**

- Presenta una fuerte integración con el ecosistema Microsoft, lo que puede aportar menos valor si AulaViva no utiliza tecnologías como .NET, Active Directory o herramientas empresariales de Microsoft.
- La variedad de opciones de cómputo y servicios puede dificultar la elección de la alternativa más adecuada para una arquitectura inicialmente pequeña.
- Algunos servicios y configuraciones de Azure utilizan conceptos específicos del ecosistema Microsoft, aumentando la curva de aprendizaje para equipos sin experiencia previa.
- La adopción de servicios propietarios puede generar dependencia de Azure y aumentar el esfuerzo requerido para una migración posterior.

### 3. Google Cloud Platform (GCP)

Google Cloud Platform constituye otra alternativa viable para desplegar AulaViva mediante infraestructura basada en contenedores y servicios administrados.

**Ventajas:**

- Buen soporte para despliegues contenerizados.
- Facilita el escalamiento horizontal.
- Dispone de servicios administrados para bases de datos, mensajería, observabilidad y CI/CD.
- Permite implementar aplicaciones stateless.
- Facilita la evolución hacia arquitecturas basadas en microservicios.
- Cuenta con servicios orientados a cargas de trabajo de datos e inteligencia artificial.

**Desventajas:**

- Su propuesta presenta especial fortaleza en servicios de datos, analítica e inteligencia artificial, capacidades que podrían exceder las necesidades del MVP inicial de AulaViva.
- La utilización de determinados servicios de GCP puede requerir conocimientos específicos de sus herramientas y modelos de operación.
- Para una solución cuyo núcleo inicial es un backend Java/Spring Boot con PostgreSQL, algunas capacidades diferenciadoras de GCP podrían no representar una ventaja significativa.
- La utilización de servicios propietarios puede generar dependencia del proveedor y dificultar una migración posterior.

AWS se selecciona por presentar un ecosistema suficientemente amplio para cubrir las necesidades actuales de diseño y acompañar la evolución futura de la plataforma.

## Consecuencias

### Positivas

- Se mantiene coherencia con la decisión de comenzar con un **Monolito Modular** y evolucionar posteriormente hacia microservicios.
- La arquitectura podrá disponer de capacidades para escalar horizontalmente durante períodos de alta demanda.
- Se podrá implementar una estrategia de despliegue basada en una misma imagen de aplicación entre ambientes.
- Los datos persistentes podrán mantenerse fuera de los contenedores.
- Se facilitará la futura implementación de CI/CD e Infraestructura como Código.
- Los procesos administrativos y tareas batch podrán ejecutarse independientemente de las solicitudes normales de la aplicación.
- Se podrán incorporar mecanismos de observabilidad y health checks.
- Se facilitará la implementación de pruebas automatizadas sobre los componentes críticos.
- El Tutor IA/RAG podrá integrarse con servicios de procesamiento y datos compatibles con la arquitectura definida.
- La infraestructura podrá crecer progresivamente junto con la plataforma.
- Se dispondrá de un ecosistema amplio de servicios administrados para acompañar la evolución de AulaViva.

### Negativas

- Se genera dependencia respecto de AWS como proveedor Cloud.
- La utilización de servicios específicos de AWS puede dificultar una futura migración hacia Azure, GCP u otro proveedor.
- Se requerirá conocimiento específico del ecosistema AWS.
- La utilización de múltiples servicios administrados puede incrementar la complejidad operacional.
- Los costos de infraestructura y servicios deberán ser evaluados una vez que se realice el despliegue.
- Los futuros pipelines CI/CD y mecanismos de observabilidad requerirán mantenimiento y monitoreo.
- Una futura migración de proveedor requeriría adaptar configuraciones, infraestructura como código y componentes dependientes de servicios específicos de AWS.
- La futura extracción de módulos hacia microservicios requerirá mantener contratos, interfaces y pruebas de integración adecuadas.
