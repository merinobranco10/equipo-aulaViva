# ADR 0002 — Estilo arquitectónico

**Estado:** Aceptada 
**Fecha:** 04-09-2026

## Contexto

AulaViva es una plataforma SaaS educativa multi-tenant destinada a colegios de la Región Metropolitana. El sistema debe gestionar estudiantes, docentes, coordinadores, apoderados y sostenedores, además de contenidos, evaluaciones automáticas, seguimiento académico y un tutor IA basado en RAG.

La arquitectura debe responder principalmente a los siguientes NFR prioritarios:

| Prioridad | Atributo | Justificación |
|---|---|---|
| 1 | **Seguridad y privacidad** | El sistema maneja datos de estudiantes menores de edad y debe garantizar el aislamiento entre tenants, control de acceso y protección de datos. |
| 2 | **Escalabilidad y rendimiento** | La plataforma debe soportar incrementos bruscos de usuarios durante períodos de pruebas sin degradar significativamente el servicio. |
| 3 | **Fiabilidad y precisión del tutor IA** | Las respuestas del tutor deben utilizar el contexto curricular correspondiente y minimizar respuestas incorrectas o fuera del programa MINEDUC. |

Se evaluaron dos estilos arquitectónicos candidatos:

1. **Monolito Modular → Microservicios**
2. **Arquitectura Orientada a Eventos**

## Decisión

Se selecciona **Monolito Modular → Microservicios** como estilo arquitectónico para AulaViva.

El MVP se implementará inicialmente como un **monolito modular**, manteniendo una separación clara entre los principales módulos de negocio. Posteriormente, aquellos módulos que requieran escalabilidad, aislamiento o evolución independiente podrán extraerse progresivamente como microservicios.

## Alternativas consideradas

### 1. Monolito Modular → Microservicios

Permite comenzar con una arquitectura de menor complejidad, manteniendo los módulos de negocio separados dentro de un mismo backend. Esta estrategia facilita el desarrollo del MVP y permite evolucionar progresivamente hacia microservicios cuando las necesidades de escalabilidad o independencia lo justifiquen.

**Relación con los NFR:**

- **Seguridad y privacidad:** Alta. Facilita centralizar autenticación, RBAC y políticas de aislamiento multi-tenant.
- **Escalabilidad y rendimiento:** Alta. El sistema puede escalar horizontalmente inicialmente y posteriormente escalar módulos específicos como el Tutor IA.
- **Fiabilidad y precisión del Tutor IA:** Alta. El módulo RAG puede mantener un flujo controlado entre los contenidos del curso, la recuperación de contexto y el LLM.

### 2. Arquitectura Orientada a Eventos

Utiliza eventos y procesamiento asíncrono para desacoplar los componentes del sistema. Tecnologías como RabbitMQ o Apache Kafka permitirían distribuir tareas y escalar consumidores independientemente.

**Relación con los NFR:**

- **Seguridad y privacidad:** Media-Alta. Requiere gestionar adecuadamente la seguridad de eventos, consumidores y datos asociados a cada tenant.
- **Escalabilidad y rendimiento:** Muy Alta. El procesamiento asíncrono permite distribuir carga y escalar consumidores independientemente.
- **Fiabilidad y precisión del Tutor IA:** Adecuada. Puede ser útil para procesos como generación de embeddings y procesamiento de documentos, pero agrega complejidad al flujo principal del tutor.

## Comparación

| Criterio | Monolito Modular → Microservicios | Orientada a Eventos |
|---|---|---|
| **Seguridad y privacidad** | **Alta** — Facilita centralizar autenticación, RBAC y aislamiento de tenants. | **Media-Alta** — Requiere proteger eventos, canales y consumidores. |
| **Multi-tenancy** | **Alta** — Facilita controlar el tenant desde un backend centralizado. | **Media** — El aislamiento debe mantenerse también en eventos y consumidores. |
| **Escalabilidad** | **Alta** — Permite escalar el sistema y posteriormente módulos específicos. | **Muy alta** — Permite procesamiento paralelo y consumidores independientes. |
| **Complejidad inicial** | **Media** — Adecuado para comenzar el MVP con una infraestructura controlada. | **Alta** — Requiere broker, productores, consumidores y manejo de eventos. |
| **Desarrollo del MVP** | **Simple** — Facilita implementar las funcionalidades principales rápidamente. | **Más complejo** — Introduce comunicación asíncrona desde el inicio. |
| **Evolución futura** | **Muy alta** — Permite extraer módulos progresivamente como microservicios. | **Alta** — Facilita incorporar nuevos consumidores y procesos. |
| **Tutor IA/RAG** | **Muy adecuado** — Permite desarrollar el tutor de forma integrada y separarlo posteriormente. | **Adecuado** — Los eventos son útiles para procesos auxiliares, pero aumentan la complejidad. |

## Justificación de la decisión

La elección de **Monolito Modular → Microservicios** permite equilibrar los NFR prioritarios con la complejidad del MVP.

En particular, la estrategia permite:

- Priorizar **seguridad y privacidad** mediante una gestión centralizada de autenticación, autorización, RBAC y aislamiento de tenants.
- Alcanzar una **escalabilidad adecuada** mediante despliegue horizontal y, posteriormente, extracción de módulos que requieran escalamiento independiente.
- Mantener un flujo controlado para el **Tutor IA/RAG**, facilitando la recuperación de información de los apuntes del curso y la incorporación del contexto curricular.
- Reducir la **complejidad inicial** frente a una arquitectura completamente orientada a eventos.
- Permitir una **evolución progresiva** hacia microservicios sin realizar una migración completa desde el comienzo.

La arquitectura orientada a eventos se descarta como estilo principal debido a que su mayor complejidad inicial no se justifica para el MVP. Sin embargo, algunos de sus mecanismos podrán incorporarse de forma complementaria para tareas asíncronas, como procesamiento de documentos, generación de embeddings, notificaciones y actualización de información académica.

## Consecuencias

### Positivas

- Menor complejidad para desarrollar y desplegar el MVP.
- Facilita mantener límites claros entre módulos.
- Permite evolucionar progresivamente hacia microservicios.
- Facilita la aplicación centralizada de políticas de seguridad y multi-tenancy.
- Permite escalar posteriormente componentes con mayor demanda, como el Tutor IA.
- Reduce el riesgo técnico de adoptar una arquitectura distribuida demasiado pronto.

### Negativas

- Inicialmente existe un único backend que puede convertirse en un punto de concentración de carga.
- La extracción posterior de módulos a microservicios requerirá trabajo adicional.
- Será necesario mantener una arquitectura modular estricta para evitar un monolito difícil de separar.
- La incorporación de un Message Broker introduce cierta complejidad de infraestructura, aunque no convierte al sistema en una arquitectura orientada a eventos.
