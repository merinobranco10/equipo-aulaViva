# ADR 0002 — Arquitectura del sistema

**Estado:** Aceptada
**Fecha:** 03-09-2026

## Contexto

AulaViva debe soportar **multi-tenancy, seguridad y privacidad de datos de menores, escalabilidad ante períodos de alta demanda y un tutor de IA basado en RAG**.

Se evaluaron dos alternativas:

1. **Monolito Modular → Microservicios**
2. **Arquitectura Orientada a Eventos**

## Decisión

Se selecciona **Monolito Modular → Microservicios**.

El MVP comenzará como un **monolito modular**, con módulos claramente separados que permitan una futura extracción a microservicios cuando las necesidades de escalabilidad, rendimiento o aislamiento lo justifiquen.

El **Tutor IA/RAG** será uno de los principales candidatos para convertirse posteriormente en un microservicio independiente.

## Comparación

| Criterio                   | **Monolito Modular → Microservicios**                                                                           | **Arquitectura Orientada a Eventos**                                                                              |
| -------------------------- | --------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| **Seguridad y privacidad** | **Alta** — Permite centralizar autenticación, RBAC y aislamiento de tenants durante el MVP.                     | **Media-Alta** — Permite seguridad distribuida, pero requiere proteger eventos, canales y consumidores.           |
| **Multi-tenancy**          | **Alta** — Facilita controlar el tenant desde un backend centralizado y aplicar aislamiento en la BD.           | **Media** — El aislamiento debe mantenerse también en eventos, consumidores y procesamiento asíncrono.            |
| **Escalabilidad**          | **Alta** — Permite escalar inicialmente el monolito y luego cada microservicio de forma independiente.          | **Muy alta** — Los eventos permiten procesamiento paralelo y escalamiento independiente de consumidores.          |
| **Complejidad inicial**    | **Media** — La estructura modular mantiene una infraestructura relativamente simple para el MVP.                | **Alta** — Requiere broker, productores, consumidores, manejo de errores y monitoreo distribuido.                 |
| **Desarrollo del MVP**     | **Simple** — Permite desarrollar las funcionalidades principales dentro de un mismo backend modular.            | **Más complejo** — Introduce comunicación asíncrona e infraestructura adicional desde el inicio.                  |
| **Evolución futura**       | **Muy alta** — Los módulos pueden convertirse progresivamente en microservicios según las necesidades.          | **Alta** — Facilita agregar nuevos consumidores y procesos, pero requiere mantener la infraestructura de eventos. |
| **Tutor IA/RAG**           | **Muy adecuado** — Permite comenzar integrado y posteriormente separarlo para escalarlo y controlar sus costos. | **Adecuado** — Los eventos ayudan en procesos como embeddings, pero agregan complejidad al flujo del tutor.       |

## Justificación de la decisión

El enfoque elegido permite mantener una **menor complejidad durante el desarrollo del MVP**, facilitando la implementación centralizada de autenticación, RBAC y aislamiento por tenant.

Además, permite evolucionar progresivamente hacia microservicios y escalar de forma independiente los componentes que tengan mayor demanda, especialmente el **Tutor IA/RAG**, que requiere gestionar LLM, embeddings, recuperación de información, guardrails y costos de IA.

## Alternativa descartada: Arquitectura Orientada a Eventos

Se descarta como arquitectura principal porque introduce **mayor complejidad desde las primeras etapas del proyecto**, especialmente en comunicación asíncrona, trazabilidad, consistencia de datos y monitoreo.

Aunque es adecuada para procesos como **procesamiento de documentos, generación de embeddings, actualización de progreso y notificaciones**, no resuelve por sí sola la organización de los módulos, el control de acceso ni el aislamiento entre tenants.


## Consecuencias

**Positivas:**

* Menor complejidad inicial.
* Desarrollo más rápido del MVP.
* Facilita seguridad y aislamiento por tenant.
* Permite escalar progresivamente.
* Facilita la futura separación del Tutor IA.

**Negativas:**

* La migración de módulos a microservicios requerirá trabajo adicional.
* Se deben mantener límites claros entre módulos desde el inicio.
* La incorporación posterior de eventos agregará cierta complejidad.
