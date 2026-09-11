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
| Message Broker            | Amazon MSK para Kafka         | AWS       | Permite utilizar Kafka como servicio gestionado para la comunicación y procesamiento asíncrono entre componentes.                                                     |
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

* Costos asociados al uso y almacenamiento.
* Dependencia del proveedor cloud.
* Se debe configurar correctamente el acceso a la base de datos y las credenciales.
* Garantizar el aislamiento de datos entre los distintos colegios.

---

## Almacenamiento de Objetos

**Componente:** Almacenamiento de Objetos
**Tecnología actual:** Object Storage
**Servicio propuesto:** Amazon S3

Amazon S3 puede utilizarse para almacenar documentos y archivos educativos de AulaViva, separando estos archivos del almacenamiento local de los servidores de aplicación.

### Beneficios

* Alta escalabilidad.
* Alta Disponibilidad.
* Integración con otros servicios AWS.
* Posibilidad de configurar políticas de acceso y ciclo de vida.

### Consideraciones

* Evitar el acceso público por defecto.
* Se deben definir correctamente los permisos de acceso.
* Es necesario proteger los archivos que contengan información sensible.
* Considerar el aislamiento de información entre tenants.
* Controlar los costos de almacenamiento y transferencia.

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

* Mayor dependencia del proveedor cloud (AWS).
* Costos asociados al servicio.
* La elección entre Kafka y RabbitMQ aún debe ser validada.
* Considerar el volumen de eventos y la demanda esperada.

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

* Se deben definir correctamente los roles y permisos.
* Garantizar que la autorización respete el aislamiento entre colegios.
* Proteger la información asociada a estudiantes menores de edad.
* Configurar adecuadamente las políticas de seguridad.

---

## Análisis complementario — Servicios AI/Data

Además de los cuatro servicios seleccionados, el C4 Nivel 2
contempla componentes específicos asociados al Tutor IA/RAG,
los cuales son analizados por el área AI/Data.

### Base de Datos Vectorial (pgvector)

El componente Base de Datos Vectorial está basado en
PostgreSQL + pgvector y deberá considerar los requerimientos
del sistema RAG.

**Alternativas consideradas:**

- **Opción A: PostgreSQL gestionado con pgvector**
  (ej. Amazon RDS o Supabase).
  - Ventaja: mayor control del esquema y costo predecible.
  - Riesgo: requiere gestionar la configuración de pgvector.

- **Opción B: Base de datos vectorial dedicada**
  (ej. Pinecone).
  - Ventaja: escalamiento automático y menor mantenimiento.
  - Riesgo: mayor costo y menor control sobre los datos.

### Servicio IA / LLM

El Tutor IA de AulaViva requiere un servicio de LLM para
generar respuestas utilizando el contexto recuperado mediante
el sistema RAG.

La selección del servicio deberá considerar aspectos como
capacidades del modelo, costos, integración con RAG,
rendimiento, privacidad de los datos y dependencia del
proveedor.

**Alternativas consideradas:**

- **Opción A: API gestionada de un proveedor**
  (ej. Anthropic u OpenAI).
  - Ventaja: no requiere administrar infraestructura propia.
  - Riesgo: costo variable y dependencia de la disponibilidad
    del proveedor externo.

- **Opción B: Modelo autoalojado.**
  - Ventaja: mayor control sobre el modelo, infraestructura
    y privacidad de los datos.
  - Riesgo: requiere infraestructura propia, recursos GPU
    y mayor mantenimiento.

Estas alternativas deberán ser evaluadas por el área AI/Data
antes de establecer una decisión definitiva para la
arquitectura de AulaViva.

---
---

## Riesgos generales

La utilización de servicios gestionados permite reducir la
carga operacional y facilita la escalabilidad, pero también
genera algunas consideraciones:

- Dependencia del proveedor cloud.
- Costos variables según el consumo.
- Posibles dificultades para migrar entre proveedores.
- Necesidad de configurar correctamente seguridad,
  permisos y acceso.
- Dependencia de la disponibilidad de servicios externos.

## Conclusión

Como propuesta inicial, AWS presenta servicios gestionados
que pueden cubrir los principales componentes de
infraestructura seleccionados para AulaViva.

La propuesta considera **Amazon RDS for PostgreSQL,
Amazon S3, Amazon MSK y Amazon Cognito** para los
componentes de base de datos, almacenamiento, mensajería
e identidad.

Esta selección es **preliminar** y deberá ser validada por
el equipo antes de considerarse una decisión definitiva.

Adicionalmente, AI/Data ha identificado alternativas para
la Base de Datos Vectorial y el servicio de LLM, las cuales
deberán evaluarse según los requerimientos del Tutor IA/RAG.
