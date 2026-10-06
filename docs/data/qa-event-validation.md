# Validación QA — Eventos de Dominio AulaViva

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 5 de octubre de 2026
**Sesión:** S06
**Basado en:** `docs/data/event-catalog.md` (14 eventos) y `docs/data/bounded-contexts.md`

## 1. Objetivo

Registrar las decisiones y hallazgos obtenidos durante la revisión del catálogo de eventos desde la perspectiva de calidad: nombres, estructura, versionado, trazabilidad, duplicados e idempotencia.

## 2. Metodología

Se revisaron los 14 eventos uno por uno verificando:

- Nombres en pasado y formato.
- Estructura común (event_id, event_type, event_version, occurred_at, aggregate_id, tenant_id, trace_id, data).
- Versión declarada.
- Productor y consumidores.
- Trazabilidad (identificadores y fecha/hora).
- Posibles duplicados o solapamientos.
- Estrategia de idempotencia y delivery at-least-once.

## 3. Resultado general

| Criterio | Resultado |
|---|---|
| Nombres en pasado | ✅ Cumple en los 14 eventos |
| Formato `recurso.acción.pasado` | ✅ Cumple (se aclara el uso de 4 segmentos) |
| Estructura común | ✅ Consistente |
| Versión 1.0 | ✅ Consistente |
| Productor definido | ✅ En todos |
| Consumidores definidos | ✅ En todos (algunos sin consumidores externos, ver sección 6) |
| Trazabilidad (event_id, tenant_id, occurred_at, trace_id, aggregate_id) | ✅ Presente en todos |
| Eventos duplicados | ✅ No se detectaron |
| Idempotencia | ✅ Declarada, se refuerza con reglas del productor |
| Delivery at-least-once | ✅ Declarado |

## 4. Nombres de eventos

Se validó que todos los nombres representan hechos ocurridos en pasado. Se detectó que algunos eventos usan 4 segmentos (`recurso.subrecurso.acción.pasado`) mientras la convención original describía 3 segmentos. Se acordó permitir explícitamente 4 segmentos cuando el recurso tiene sub-recursos naturales, y se agregó esta aclaración en la sección 2.1 del catálogo.

## 5. Trazabilidad

Todos los eventos incluyen los siguientes campos de trazabilidad:

| Campo | Propósito |
|---|---|
| `event_id` | Identifica de forma única cada evento |
| `trace_id` | Permite seguir una operación distribuida |
| `tenant_id` | Identifica el tenant afectado |
| `aggregate_id` | Identifica el agregado de dominio |
| `occurred_at` | Marca de tiempo de emisión del evento |

Se decidió mantener también los campos `*_at` dentro de `data` (por ejemplo `submitted_at`, `graded_at`) porque representan el momento del hecho de negocio y no siempre coinciden con `occurred_at` (latencia de procesamiento).

## 6. Productor y consumidores

| Evento | Productor | Consumidores | Observación |
|---|---|---|---|
| student.enrolled | Gestión Académica | Evaluaciones y Progreso, Tutor IA | ✅ |
| course.created | Gestión Académica | Evaluaciones y Progreso, Tutor IA | ✅ |
| course.teacher.assigned | Gestión Académica | Evaluaciones y Progreso | ✅ |
| evaluation.created | Evaluaciones y Progreso | Gestión Académica | ✅ |
| evaluation.published | Evaluaciones y Progreso | Gestión Académica | ✅ |
| evaluation.submitted | Evaluaciones y Progreso | Gestión Académica, Tutor IA | ✅ |
| evaluation.graded | Evaluaciones y Progreso | Gestión Académica, Tutor IA | ✅ |
| evaluation.completed | Evaluaciones y Progreso | Gestión Académica, Tutor IA | ✅ |
| student.progress.updated | Evaluaciones y Progreso | Gestión Académica, Tutor IA | ✅ |
| tutor.question.received | Tutor IA y Contenidos | Ninguno externo | Ver sección 6.1 |
| tutor.response.generated | Tutor IA y Contenidos | Evaluaciones y Progreso | ✅ |
| learning.document.indexed | Tutor IA y Contenidos | Ninguno externo | Ver sección 6.1 |
| user.role.assigned | Identidad y Acceso | Los 3 contextos restantes | ✅ |
| user.access.revoked | Identidad y Acceso | Los 3 contextos restantes | ✅ |

### 6.1 Eventos sin consumidores externos

Los eventos `tutor.question.received` y `learning.document.indexed` se mantienen en el catálogo aunque no tengan consumidores externos, porque:

- Alimentan proyecciones internas del contexto Tutor IA (dashboards de uso).
- Sirven para auditoría y observabilidad.
- Facilitan la incorporación de consumidores futuros sin modificar el contrato del evento.

## 7. Idempotencia y delivery at-least-once

El broker Kafka/MSK utiliza **at-least-once delivery**, lo que significa que un evento puede entregarse más de una vez. Para asegurar el procesamiento correcto se definieron las siguientes reglas:

### 7.1 Lado del consumidor

1. Al recibir un evento, el consumidor consulta si el `event_id` ya fue procesado.
2. Si ya fue procesado, descarta el evento y registra `duplicate_ignored` en el log.
3. Si no fue procesado, ejecuta la operación de negocio.
4. Registra el `event_id` como procesado en la tabla de deduplicación.

### 7.2 Lado del productor

1. El `event_id` se genera una sola vez.
2. En caso de reintento, se reutiliza el mismo `event_id` y la misma `occurred_at`.

### 7.3 Retención

Para el MVP se establece una retención inicial de **7 días** para los `event_id` procesados, con el objetivo de detectar y evitar el procesamiento de eventos duplicados.

## 8. Orden de eventos

Para preservar el orden de eventos de un mismo agregado, se decidió usar `aggregate_id` como **clave de partición** en Kafka. De este modo, todos los eventos que pertenecen al mismo agregado se procesan en orden secuencial dentro de la misma partición.

## 9. Eventos duplicados

Se revisó el catálogo completo y **no se detectaron eventos duplicados**. Los eventos con nombres similares (`evaluation.submitted` y `evaluation.graded`, o `evaluation.graded` y `evaluation.completed`) representan momentos distintos del ciclo de vida de la evaluación y no se solapan.

## 10. Decisiones incorporadas en `event-catalog.md`

1. Aclaración sobre el uso de 4 segmentos en nombres de eventos.
2. Distinción entre `occurred_at` (evento) y `*_at` (hecho de negocio).
3. Idempotencia del productor documentada.
4. Retención de `event_id` procesados (7 días).
5. Uso de `aggregate_id` como clave de partición.
6. Justificación de eventos sin consumidores externos.

## 11. Pendientes

- Evaluar la incorporación de `correlation_id` y `causation_id` para trazabilidad más profunda (fuera del alcance del MVP).
- Definir la herramienta de deduplicación concreta (Redis vs tabla PostgreSQL) durante la implementación.

**Validado por:** Alexander Ruiz-Tagle — QA Lead
