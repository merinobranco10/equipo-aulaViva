# ADR 0004 — Persistencia y mensajería

**Estado:** Pendiente  
**Fecha:** 06-10-2026

## Contexto

AulaViva es una plataforma SaaS multi-tenant orientada a instituciones educacionales, cuya arquitectura considera inicialmente un monolito modular con una evolución progresiva hacia microservicios, de acuerdo con lo definido en el ADR 0002.

La solución debe soportar distintos contextos de negocio, entre ellos Gestión Académica, Evaluaciones y Progreso, Tutor IA y Contenidos e Identidad y Acceso. Estos contextos requieren persistencia de información de negocio y comunicación asíncrona para propagar eventos de dominio.

Los principales requisitos que influyen en esta decisión son:

* Mantener la integridad y consistencia de los datos transaccionales.
* Aislar adecuadamente la información asociada a cada tenant.
* Soportar crecimiento horizontal de la aplicación.
* Permitir comunicación asíncrona entre contextos.
* Mantener trazabilidad de las operaciones distribuidas.
* Permitir reintentos y procesamiento seguro de mensajes duplicados.
* Facilitar una futura evolución desde el monolito modular hacia microservicios.
* Soportar eventos de dominio que puedan ser consumidos por múltiples contextos.
* Mantener los eventos disponibles durante un período suficiente para recuperación y reprocesamiento.

## 2. Decisión

### 2.1. Persistencia por bounded context

Se utilizarán tecnologías de persistencia según las necesidades de cada contexto.

| Bounded context         | Persistencia                                                                                  | Datos principales                                                                                       |
| ----------------------- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| Gestión Académica       | Amazon RDS for PostgreSQL                                                                     | Colegios, cursos, estudiantes, profesores, apoderados y matrículas                                      |
| Evaluaciones y Progreso | Amazon RDS for PostgreSQL                                                                     | Evaluaciones, preguntas, entregas, respuestas, puntajes, calificaciones y progreso                      |
| Tutor IA y Contenidos   | Amazon RDS for PostgreSQL con pgvector y Amazon S3                                            | Metadatos de documentos, fragmentos de contenido, embeddings y referencias a archivos                   |
| Identidad y Acceso      | Amazon Cognito para autenticación y PostgreSQL para datos propios de autorización de AulaViva | Identidades autenticadas externamente y, cuando corresponda, asociaciones con tenants, roles y permisos |

**Gestión Académica:** PostgreSQL será la fuente de verdad para la estructura académica. Los demás contextos consultarán la información necesaria mediante contratos o eventos, sin modificar directamente las tablas de Gestión Académica.

**Evaluaciones y Progreso:** PostgreSQL almacenará las evaluaciones, preguntas, entregas, respuestas y resultados. Las operaciones que modifiquen el estado de una entrega y registren su resultado deberán preservar la consistencia transaccional.

**Tutor IA y Contenidos:** PostgreSQL almacenará los metadatos y la información estructurada del contenido. pgvector permitirá recuperar fragmentos semánticamente relevantes para RAG. S3 almacenará los documentos originales y archivos académicos, mientras que la base de datos conservará sus identificadores y metadatos. La eliminación o actualización de documentos deberá contemplar la sincronización de sus fragmentos y embeddings.

**Identidad y Acceso:** Amazon Cognito se utilizará para la autenticación. AulaViva mantendrá las reglas de autorización y pertenencia a tenants que necesite la aplicación. Los tokens autenticados no reemplazan las verificaciones de permisos ni el aislamiento de datos por colegio.


Los eventos seguirán una estructura común que incluye, entre otros:

* `event_id`
* `event_type`
* `event_version`
* `occurred_at`
* `aggregate_id`
* `tenant_id`
* `trace_id`
* `data`

La arquitectura de procesamiento de eventos utilizará un modelo de entrega at-least-once. Por lo tanto, un evento podrá ser procesado más de una vez debido a reintentos o fallos durante el procesamiento. Los consumidores deberán implementar mecanismos de idempotencia utilizando `event_id` .

Para preservar el orden de eventos pertenecientes al mismo agregado, se utilizará `aggregate_id` como **clave de partición de Kafka**. De esta forma, los eventos correspondientes al mismo agregado serán enviados a una misma partición y podrán procesarse secuencialmente.

### 2.2. Estrategia multi-tenant

Los datos relacionales de los colegios deberán estar aislados mediante un identificador de tenant y controles de autorización en el backend. Se evaluará el uso de políticas de seguridad a nivel de fila (Row-Level Security, RLS) en PostgreSQL como defensa adicional.

Las consultas vectoriales deberán restringirse al tenant y al curso autorizados. Los metadatos de documentos, los fragmentos y los embeddings deberán conservar los identificadores necesarios para aplicar estos filtros.

Los objetos almacenados en S3 deberán organizarse y protegerse de forma que un tenant no pueda acceder a los archivos de otro. La separación lógica deberá complementarse con políticas de acceso y validaciones de autorización.

La estrategia inicial utilizará una infraestructura de datos compartida con aislamiento lógico, sin descartar una separación física por tenant si los requisitos de seguridad o escala lo justifican en el futuro.

### 2.3. Uso de Kafka como broker de eventos

Se utilizará Amazon MSK, servicio administrado de Apache Kafka, como broker para transportar eventos de dominio entre los componentes de AulaViva.

Los eventos deberán incluir, como mínimo:
* `event_id`: identificador único del evento.
* `event_type`: nombre del evento.
* `event_version`: versión del esquema.
* `occurred_at`: fecha y hora de ocurrencia.
* `aggregate_id`: identificador del agregado de dominio.
* `tenant_id`: identificador del tenant afectado.
* `trace_id`: identificador para trazabilidad distribuida.
* `data`: datos específicos del evento.
* Identificadores de los recursos relacionados y del tenant cuando sean necesarios para el procesamiento seguro.

Los productores publicarán eventos en Kafka y los consumidores reaccionarán de forma independiente. Los consumidores deberán tolerar la entrega repetida de mensajes mediante mecanismos de idempotencia y deduplicación.

Para preservar el orden de los eventos pertenecientes a un mismo agregado, se utilizará `aggregate_id` como clave de partición de Kafka. De esta forma, los eventos asociados al mismo agregado serán enviados a la misma partición y podrán procesarse secuencialmente. Kafka garantiza el orden dentro de una partición, pero no un orden global entre todas las particiones

Los eventos no reemplazarán automáticamente la persistencia transaccional. PostgreSQL seguirá siendo la fuente de verdad de los datos relacionales de cada contexto; Kafka facilitará su comunicación asíncrona.

La retención inicial de los eventos en Kafka será de 7 días, permitiendo recuperación y reprocesamiento durante dicho período. La duración podrá revisarse posteriormente de acuerdo con los requisitos de recuperación, auditoría, volumen de eventos y costos operacionales.

No se incluirán contraseñas, tokens, documentos completos ni otros datos sensibles innecesarios en los mensajes. Los consumidores deberán validar la autorización y el ámbito del tenant antes de utilizar los datos recibidos.

### 2.4. Patrón Outbox

**Decisión: utilizar Outbox para publicar eventos derivados de cambios persistidos en PostgreSQL.**

El patrón Outbox permitirá registrar un cambio de dominio y el evento correspondiente dentro de una misma transacción de base de datos. Un proceso independiente publicará posteriormente los eventos pendientes en Kafka.

Por ejemplo, al corregir una entrega, el sistema podrá actualizar el resultado e insertar el evento `evaluation.graded` en la tabla Outbox dentro de la misma transacción. Si la transacción falla, no se persistirá ninguno de los dos cambios.

La publicación en Kafka será asíncrona. Debido a que pueden producirse reintentos o duplicados, los consumidores deberán ser idempotentes.

En el MVP se podrá implementar Outbox mediante una tabla PostgreSQL y un publicador de eventos. Si el volumen o los requisitos operacionales lo justifican, se podrá evaluar un mecanismo basado en captura de cambios de datos (CDC).

El patrón se aplicará en los contextos que necesiten publicar eventos como consecuencia de una modificación transaccional. No será obligatorio para operaciones que no produzcan eventos de dominio persistentes.

## Consecuencias

### Consecuencias positivas

* PostgreSQL proporciona un modelo relacional adecuado para las entidades transaccionales de AulaViva.
* PostgreSQL permite implementar mecanismos de aislamiento de datos por tenant, incluyendo `tenant_id` y políticas de seguridad a nivel de fila cuando corresponda.
* pgvector permite integrar las capacidades vectoriales requeridas por el Tutor IA con la persistencia de metadatos de los documentos.
* S3 separa los documentos originales de los datos transaccionales.
* Kafka desacopla productores y consumidores y permite que múltiples contextos reaccionen ante los mismos eventos.
* La retención de eventos permite disponer de un historial temporal para recuperación y reprocesamiento.
* El uso de `aggregate_id` como clave de partición permite preservar el orden de los eventos de un mismo agregado.
* El modelo at-least-once permite utilizar reintentos sin asumir que cada mensaje será entregado exactamente una vez.
* La idempotencia basada en `event_id` permite procesar de forma segura eventos duplicados.
* Outbox reduce el riesgo de inconsistencias entre los cambios realizados en PostgreSQL y los eventos publicados en Kafka.
* Kafka resulta adecuado para escenarios futuros donde aumente el volumen de eventos o sea necesario contar con múltiples consumidores independientes.

### Consecuencias negativas

* Kafka introduce una mayor complejidad operacional que una solución de mensajería basada únicamente en colas.
* Se requiere administrar tópicos, particiones, consumidores, offsets, retención y políticas de reintento.
* Los consumidores deben implementar explícitamente mecanismos de idempotencia debido al modelo at-least-once.
* El patrón Outbox agrega una estructura adicional de persistencia y un proceso encargado de publicar los eventos.
* El equipo debe monitorear el estado de los consumidores, el lag de las particiones y los errores de procesamiento.
* La utilización de Amazon MSK genera dependencia de servicios y costos asociados al proveedor cloud.
* La adopción de Kafka desde el MVP requiere mayor conocimiento y configuración inicial que una solución de mensajería más simple.

## Alternativas consideradas

### 1. RabbitMQ

Se evaluó RabbitMQ como broker de mensajería para la comunicación asíncrona.

**Ventajas:**

* Menor complejidad inicial para escenarios de colas y distribución de tareas.
* Modelo de mensajería adecuado para procesamiento asíncrono.
* Configuración relativamente sencilla para un MVP.

**Desventajas:**

* Su modelo está más orientado a mensajería y colas que a un log de eventos retenido.
* El reprocesamiento histórico de eventos no constituye su modelo principal de operación.
* Requiere diseñar mecanismos adicionales si AulaViva necesita conservar y reutilizar un historial de eventos para múltiples consumidores.
* Resulta menos alineado con la estrategia definida de eventos de dominio, particionado por agregado y retención.

**Razón para descartarlo:**
Su modelo está principalmente orientado a mensajería y distribución mediante colas, mientras que Kafka proporciona un modelo de log de eventos persistente y particionado más alineado con los requisitos definidos para AulaViva.

### 2. Base de datos independiente por contexto

Se evaluó utilizar una base de datos independiente para cada bounded context.

**Ventajas:**

* Mayor aislamiento de los datos.
* Independencia tecnológica y de evolución entre contextos.
* Facilita una futura separación física de los microservicios.

**Desventajas:**

* Mayor complejidad operacional.
* Mayor cantidad de conexiones, migraciones y mecanismos de administración.
* Introduce complejidad innecesaria mientras AulaViva permanezca como monolito modular.
* Las consultas y transacciones que involucren información de varios contextos requieren mecanismos adicionales.

**Razón para descartarlo en el MVP:**
Se prioriza PostgreSQL como persistencia principal, manteniendo la separación lógica de los módulos y dejando la separación física para una futura evolución a microservicios.

### 3. MongoDB como persistencia principal

Se evaluó una base de datos documental como alternativa a PostgreSQL.

**Ventajas:**

* Modelo flexible para estructuras documentales.
* Escalabilidad horizontal.
* Adecuado para determinados modelos de datos con estructuras variables.

**Desventajas:**

* Menor alineación con las relaciones entre estudiantes, cursos, evaluaciones, preguntas, entregas y calificaciones.
* No aporta una ventaja suficiente frente al modelo relacional requerido por AulaViva.
* Introduciría una tecnología adicional sin una necesidad concreta del MVP.

**Razón para descartarlo:**
Se prioriza PostgreSQL por la naturaleza relacional del dominio y por las necesidades transaccionales de los contextos académicos y de evaluación.

### 4. Kafka sin Transactional Outbox

Se evaluó publicar directamente los eventos en Kafka después de modificar los datos de negocio.

**Ventaja:**

* Menor complejidad de implementación inicial.

**Desventaja:**

* Existe riesgo de inconsistencia si la transacción de negocio se confirma pero la publicación del evento falla, o viceversa.

**Razón para descartarlo:**
Se utilizará Transactional Outbox en las operaciones críticas para garantizar una relación consistente entre la persistencia del cambio de negocio y la publicación posterior del evento.

### 5. Adoptar CQRS desde el MVP

Se evaluó separar desde el inicio los modelos de lectura y escritura.

**Ventaja:**

* Permite optimizar independientemente las operaciones de lectura y escritura.

**Desventajas:**

* Mayor complejidad arquitectónica.
* Requiere mantener modelos y mecanismos de sincronización adicionales.
* No existe todavía una necesidad que justifique dicha complejidad.

**Razón para descartarlo:**
Durante el MVP se utilizará PostgreSQL para las operaciones de lectura y escritura. CQRS podrá incorporarse si las necesidades de escalabilidad o complejidad de consultas lo justifican.

### 6. Adoptar Saga desde el MVP

Se evaluó utilizar Saga para coordinar operaciones entre los distintos contextos.

**Ventaja:**

* Permite coordinar transacciones distribuidas mediante pasos y compensaciones.

**Desventajas:**

* Incrementa significativamente la complejidad del sistema.
* Requiere diseñar compensaciones y gestionar estados intermedios.
* No es necesario mientras los módulos permanezcan dentro del monolito modular.

**Razón para descartarlo:**
Se mantendrán transacciones locales durante el MVP y se evaluará Saga cuando la evolución a microservicios genere una necesidad real de coordinación distribuida.

## Criterios de revisión

La decisión podrá revisarse si se presenta alguna de las siguientes condiciones:

* El volumen de eventos requiere modificar la estrategia de particionamiento.
* Se requiere un período de retención significativamente superior.
* Se necesita reprocesamiento histórico frecuente.
* Aumenta significativamente el número de consumidores.
* Las consultas requieren modelos de lectura especializados.
* La evolución a microservicios genera transacciones distribuidas que requieran Saga.
* Los costos u operación de Amazon MSK dejan de ser adecuados para las necesidades de AulaViva.
* Los requisitos de aislamiento o cumplimiento requieran bases de datos independientes por tenant.
* Los requisitos de disponibilidad o recuperación exijan garantías adicionales sobre la publicación y retención de eventos.
