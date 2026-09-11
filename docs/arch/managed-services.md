# Servicios Gestionados — AulaViva

## Objetivo

Este documento presenta la selección de servicios gestionados para los principales componentes de infraestructura definidos en el C4 Nivel 2 de AulaViva.

El proveedor cloud seleccionado como referencia es **AWS**. La selección considera las necesidades de escalabilidad, seguridad, disponibilidad y reducción de carga operacional de la plataforma.

La selección se enfoca principalmente en los componentes relacionados con DevSecOps, mientras que los servicios específicos de inteligencia artificial y datos son complementados por el área AI/Data.

---

## Propuesta de servicios gestionados

| Componente C4 L2 | Tecnología / Servicio seleccionado | Proveedor | Justificación |
|---|---|---|---|
| Base de Datos | Amazon RDS for PostgreSQL | AWS | Permite utilizar PostgreSQL como servicio gestionado, reduciendo la administración de infraestructura y facilitando tareas como backups, actualizaciones y monitoreo. |
| Almacenamiento de Objetos | Amazon S3 | AWS | Proporciona almacenamiento de objetos escalable para documentos y archivos educativos de AulaViva. |
| Message Broker | Amazon MSK con Apache Kafka | AWS | Se selecciona Apache Kafka como tecnología de mensajería y Amazon MSK como servicio gestionado para la comunicación y procesamiento asíncrono. |
| Servicio de Identidad | Amazon Cognito | AWS | Permite gestionar autenticación e identidad de usuarios sin implementar toda la infraestructura de identidad directamente en la aplicación. |
| Base de Datos Vectorial | Amazon RDS for PostgreSQL + pgvector | AWS | Permite almacenar embeddings y realizar búsquedas vectoriales para el sistema RAG manteniendo PostgreSQL como tecnología base. |

### Servicio externo de IA seleccionado

Para el componente externo de IA definido en el C4 Nivel 2 se selecciona **Anthropic Claude mediante API** como servicio LLM para el Tutor IA/RAG.

---

## Base de Datos

**Componente:** Base de Datos  
**Tecnología:** PostgreSQL  
**Servicio seleccionado:** Amazon RDS for PostgreSQL

Amazon RDS permitirá utilizar PostgreSQL como un servicio administrado, reduciendo la necesidad de gestionar directamente servidores, actualizaciones y tareas de mantenimiento de la base de datos.

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
**Tecnología:** Object Storage  
**Servicio seleccionado:** Amazon S3

Amazon S3 se utilizará para almacenar documentos y archivos educativos de AulaViva, separando estos archivos del almacenamiento local de los servidores de aplicación.

### Beneficios

* Alta escalabilidad.
* Alta disponibilidad.
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
**Tecnología seleccionada:** Apache Kafka  
**Servicio gestionado seleccionado:** Amazon MSK

Se selecciona **Apache Kafka** como tecnología de mensajería de AulaViva y **Amazon MSK** como servicio gestionado para su ejecución.

Amazon MSK permitirá implementar la comunicación y procesamiento asíncrono definido en el C4 Nivel 2, reduciendo la necesidad de administrar directamente la infraestructura de Kafka.

### Beneficios

* Reduce la administración de los servidores Kafka.
* Facilita la escalabilidad del sistema de mensajería.
* Permite mantener comunicación asíncrona entre componentes.
* Integración con el ecosistema AWS.

### Consideraciones

* Mayor dependencia del proveedor cloud (AWS).
* Costos asociados al servicio.
* Considerar el volumen de eventos y la demanda esperada.
* Definir correctamente los tópicos y eventos utilizados por la plataforma.

### Alternativa evaluada

También se consideró **RabbitMQ** como alternativa para implementar la mensajería asíncrona de AulaViva.

**Decisión final:** se selecciona **Apache Kafka mediante Amazon MSK** para la arquitectura de AulaViva.

---

## Servicio de Identidad

**Componente:** Servicio de Identidad  
**Servicio seleccionado:** Amazon Cognito

Amazon Cognito se utilizará para gestionar la autenticación de los usuarios de AulaViva y reducir la necesidad de implementar directamente toda la infraestructura de identidad.

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

Además de los servicios gestionados seleccionados, el C4 Nivel 2 contempla componentes específicos asociados al Tutor IA/RAG, los cuales fueron analizados por el área AI/Data.

### Base de Datos Vectorial (pgvector)

El componente Base de Datos Vectorial está basado en PostgreSQL + pgvector y debe considerar los requerimientos específicos del sistema RAG.

**Alternativas consideradas:**

- **Opción A: PostgreSQL gestionado con pgvector**
  (ej. Amazon RDS o Supabase).
  - Ventaja: mayor control del esquema y costo predecible.
  - Riesgo: requiere gestionar la configuración de pgvector.

- **Opción B: Base de datos vectorial dedicada**
  (ej. Pinecone).
  - Ventaja: escalamiento automático y menor mantenimiento.
  - Riesgo: mayor costo y menor control sobre los datos.

**Decisión final:** se selecciona **Amazon RDS for PostgreSQL + pgvector** para la Base de Datos Vectorial.

Esta decisión permite mantener PostgreSQL como tecnología base e incorporar las capacidades vectoriales necesarias para almacenar y consultar embeddings utilizados por el sistema RAG.

Además, permite mantener una mayor consistencia con la infraestructura de datos principal de AulaViva y evita incorporar inicialmente una base de datos vectorial completamente independiente.

### Consideraciones

* Evaluar el rendimiento de las consultas vectoriales.
* Gestionar correctamente la configuración de pgvector.
* Garantizar el aislamiento de los embeddings entre tenants.
* Controlar el crecimiento del almacenamiento de embeddings.

---

### Servicio IA / LLM

El Tutor IA de AulaViva requiere un servicio LLM para generar respuestas utilizando el contexto recuperado mediante el sistema RAG.

La selección considera aspectos como capacidades del modelo, costos, integración con RAG, rendimiento, privacidad de los datos y dependencia del proveedor.

**Alternativas consideradas:**

- **Opción A: API gestionada de un proveedor**
  (ej. Anthropic u OpenAI).
  - Ventaja: cero mantenimiento de infraestructura.
  - Riesgo: costo variable y dependencia de disponibilidad externa.

- **Opción B: Modelo autoalojado.**
  - Ventaja: mayor control y privacidad de datos.
  - Riesgo: requiere infraestructura propia (GPU) y mantenimiento.

**Decisión final:** se selecciona **Anthropic Claude mediante API** como servicio LLM para el Tutor IA de AulaViva.

Esta alternativa permite integrar el LLM con el flujo RAG de la plataforma sin necesidad de administrar infraestructura especializada para la ejecución del modelo.

### Consideraciones

* Costos variables según el consumo de la API.
* Dependencia de la disponibilidad del proveedor externo.
* Evitar enviar información personal o sensible innecesaria al modelo.
* Controlar qué información es incorporada como contexto mediante RAG.
* Considerar límites de uso y rendimiento de la API.

---

## Fuente curricular — MINEDUC

El sitio web de **MINEDUC** será utilizado como fuente externa
para obtener libros y documentos asociados al currículo oficial
chileno.

---

## Riesgos generales

La utilización de servicios gestionados permite reducir la carga operacional y facilita la escalabilidad, pero también genera algunas consideraciones:

* Dependencia del proveedor cloud y de servicios externos.
* Costos variables según el consumo.
* Posibles dificultades para migrar entre proveedores.
* Necesidad de configurar correctamente seguridad, permisos y acceso.
* Dependencia de la disponibilidad de los servicios externos.
* Necesidad de proteger adecuadamente los datos asociados a estudiantes menores de edad.

---

## Conclusión

Como resultado del análisis realizado, se seleccionaron servicios gestionados para los principales componentes de infraestructura definidos para AulaViva.

La arquitectura establece **Amazon RDS for PostgreSQL, Amazon S3, Amazon MSK con Apache Kafka y Amazon Cognito** para los componentes de base de datos, almacenamiento, mensajería e identidad.

Para los componentes asociados a AI/Data, se selecciona **Amazon RDS for PostgreSQL + pgvector** como Base de Datos Vectorial para el sistema RAG y **Anthropic Claude mediante API** como servicio LLM para el Tutor IA.

Por lo tanto, las principales decisiones establecidas son:

- **PostgreSQL → Amazon RDS for PostgreSQL.**
- **Object Storage → Amazon S3.**
- **Message Broker → Apache Kafka mediante Amazon MSK.**
- **Identidad → Amazon Cognito.**
- **Base de Datos Vectorial → Amazon RDS for PostgreSQL + pgvector.**
- **LLM → Anthropic Claude mediante API.**

El sitio web de **MINEDUC** se mantiene como una fuente externa de información y referencias del currículo oficial chileno.

Las alternativas previamente analizadas, como **RabbitMQ, Supabase, Pinecone, OpenAI y un modelo LLM autoalojado**, se mantienen documentadas como evidencia del proceso de evaluación realizado por el equipo y como referencia ante posibles cambios futuros en la arquitectura.
