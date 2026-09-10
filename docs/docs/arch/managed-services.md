# Servicios Gestionados — AulaViva

## Objetivo

Este documento presenta una propuesta de servicios gestionados para los principales componentes de infraestructura definidos en el C4 Nivel 2 de AulaViva.

El proveedor cloud considerado como propuesta es **AWS**. Esta selección es preliminar y deberá ser validada por el equipo antes de establecerla como decisión definitiva de arquitectura.

La selección se enfoca principalmente en los componentes relacionados con DevSecOps, mientras que los servicios específicos de inteligencia artificial y datos serán complementados por el área AI/Data.

---

## Propuesta de servicios gestionados

| Componente C4 L2          | Servicio gestionado propuesto | Proveedor | Justificación                                                                                                                                                         |
| ------------------------- | ----------------------------- | --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Base de Datos             | Amazon RDS for PostgreSQL     | AWS       | Permite utilizar PostgreSQL como servicio gestionado, reduciendo la administración de infraestructura y facilitando tareas como backups, actualizaciones y monitoreo. |
| Almacenamiento de Objetos | Amazon S3                     | AWS       | Proporciona almacenamiento de objetos escalable para documentos y archivos educativos de AulaViva.                                                                    |
| Message Broker            | Amazon MSK                    | AWS       | Permite utilizar Kafka como servicio gestionado para la comunicación y procesamiento asíncrono entre componentes.                                                     |
| Servicio de Identidad     | Amazon Cognito                | AWS       | Permite gestionar autenticación e identidad de usuarios sin implementar toda la infraestructura de identidad directamente en la aplicación.                           |

---

## Base de Datos

**Componente:** Base de Datos
**Tecnología actual:** PostgreSQL
**Servicio propuesto:** Amazon RDS for PostgreSQL

Amazon RDS permitiría utilizar PostgreSQL como un servicio administrado, reduciendo la necesidad de gestionar directamente servidores, actualizaciones y tareas de mantenimiento de la base de datos.

### Beneficios

* Administración simplificada de PostgreSQL.
* Respaldos automatizados.
* Opciones de alta disponibilidad.
* Monitoreo del servicio.
* Integración con otros servicios de AWS.

### Consideraciones

* Dependencia del proveedor cloud.
* Costos asociados al uso y almacenamiento.
* Se debe configurar correctamente el acceso a la base de datos y las credenciales.

---

## Almacenamiento de Objetos

**Componente:** Almacenamiento de Objetos
**Tecnología actual:** Object Storage
**Servicio propuesto:** Amazon S3

Amazon S3 puede utilizarse para almacenar documentos y archivos educativos de AulaViva, separando estos archivos del almacenamiento local de los servidores de aplicación.

### Beneficios

* Alta escalabilidad.
* Disponibilidad del almacenamiento.
* Integración con aplicaciones y servicios cloud.
* Posibilidad de configurar políticas de acceso y ciclo de vida.

### Consideraciones

* Se deben definir correctamente los permisos de acceso.
* Es necesario proteger los archivos que contengan información sensible.
* Los costos dependen del almacenamiento y transferencia utilizados.

---

## Message Broker

**Componente:** Message Broker
**Tecnología actual:** Kafka / RabbitMQ
**Servicio propuesto:** Amazon MSK para Kafka

Amazon MSK permitiría utilizar Kafka como servicio gestionado para implementar la comunicación asíncrona definida en el C4 L2.

### Beneficios

* Reduce la administración de los servidores Kafka.
* Facilita la escalabilidad del sistema de mensajería.
* Permite mantener comunicación asíncrona entre componentes.
* Integración con el ecosistema AWS.

### Consideraciones

* Mayor dependencia de AWS.
* Costos asociados a la infraestructura utilizada.
* La elección entre Kafka y RabbitMQ debe validarse según los requerimientos funcionales del sistema.

---

## Servicio de Identidad

**Componente:** Servicio de Identidad
**Servicio propuesto:** Amazon Cognito

Amazon Cognito puede utilizarse para gestionar la autenticación de los usuarios de AulaViva y reducir la necesidad de implementar directamente toda la infraestructura de identidad.

### Beneficios

* Gestión centralizada de usuarios.
* Integración con aplicaciones web.
* Soporte para autenticación y control de acceso.
* Reduce la infraestructura que debe administrar el equipo.

### Consideraciones

* Dependencia del proveedor.
* Se deben definir correctamente los roles y permisos.
* La configuración debe cumplir con los requisitos de seguridad y privacidad de AulaViva.

---

## Servicios relacionados con AI/Data

Los componentes relacionados con inteligencia artificial y datos serán complementados por el área AI/Data.

En particular, el componente **Base de Datos Vectorial**, basado en PostgreSQL + pgvector, deberá ser analizado considerando los requerimientos del sistema RAG y las alternativas de servicios gestionados disponibles.

El servicio de **LLM** también será evaluado por el área AI/Data, considerando aspectos como capacidades del modelo, costos, integración, rendimiento y dependencia del proveedor.

---

## Riesgos generales

La utilización de servicios gestionados permite reducir la carga operacional y facilita la escalabilidad, pero también genera algunas consideraciones:

* Dependencia del proveedor cloud.
* Costos variables según el consumo.
* Posibles dificultades para migrar entre proveedores.
* Necesidad de configurar correctamente seguridad, permisos y acceso.
* Dependencia de la disponibilidad de los servicios externos.

---

## Conclusión

Como propuesta inicial, AWS presenta servicios gestionados que pueden cubrir los principales componentes de infraestructura definidos para AulaViva.

La propuesta considera **Amazon RDS for PostgreSQL, Amazon S3, Amazon MSK y Amazon Cognito** para los componentes relacionados con base de datos, almacenamiento, mensajería e identidad.

Esta selección es **preliminar** y deberá ser validada por el equipo antes de considerarse una decisión definitiva. Los componentes específicos de inteligencia artificial y datos serán complementados por el área AI/Data.
