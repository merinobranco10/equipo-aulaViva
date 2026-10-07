# Catálogo de Eventos de Dominio — AulaViva

## 1. Propósito

Este documento define el catálogo de eventos de dominio utilizados para la comunicación entre los distintos Bounded Contexts de AulaViva.

Los eventos representan hechos que ya ocurrieron dentro del sistema y permiten desacoplar los contextos mediante comunicación asíncrona utilizando Kafka/MSK.

Los Bounded Contexts considerados son:

* Gestión Académica

* Evaluaciones y Progreso

* Tutor IA y Contenidos

* Identidad y Acceso

## 2. Convenciones generales

### 2.1 Nombre de los eventos

Los nombres siguen el formato:

`recurso.acción.pasado`

Ejemplos:

* `student.enrolled`

* `evaluation.submitted`

* `evaluation.graded`

* `tutor.question.received`

El nombre representa un hecho que ya ocurrió.

Cuando el evento involucra un sub-recurso dentro de un contexto, se permite un cuarto segmento:

`recurso.subrecurso.acción.pasado`

Ejemplos aceptados en este catálogo:
- `course.teacher.assigned`
- `student.progress.updated`
- `tutor.question.received`
- `tutor.response.generated`
- `learning.document.indexed`
- `user.role.assigned`
- `user.access.revoked`

Esto mantiene la semántica del formato `recurso.acción.pasado` cuando el recurso tiene sub-recursos naturales.

### 2.2 Versionado

Todos los eventos actualmente definidos utilizan la versión `1.0`.

El campo `event_version` permite evolucionar el esquema de un evento sin romper consumidores existentes.

### 2.3 Campos comunes

Todos los eventos utilizan los siguientes campos:

| **Campo** | **Descripción** |
|---|---|
| `event_id` | Identificador único del evento |
| `event_type` | Nombre del evento |
| `event_version` | Versión del esquema del evento |
| `occurred_at` | Fecha y hora en que ocurrió el evento |
| `aggregate_id` | Identificador del agregado relacionado |
| `tenant_id` | Identificador del tenant |
| `trace_id` | Identificador utilizado para trazabilidad |
| `data` | Datos específicos del evento |

**Diferencia entre `occurred_at` y los campos `*_at` dentro de `data`:**

- `occurred_at` es la marca de tiempo del sistema cuando el evento fue emitido al broker.
- Los campos como `enrolled_at`, `created_at` o `submitted_at` corresponden a la marca de tiempo del hecho de negocio (cuando el usuario realizó la acción).

En la mayoría de los casos coinciden, pero pueden diferir levemente por latencia de procesamiento. Se mantienen ambos para preservar la semántica del hecho de negocio y permitir auditorías precisas.

### 2.4 Esquema general

```
{
  "event_id": "uuid",
  "event_type": "evaluation.submitted",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:30:00Z",
  "aggregate_id": "uuid",
  "tenant_id": "uuid",
  "trace_id": "uuid",
  "data": {}
}

```

## 3. Catálogo de eventos

### 3.1 Gestión Académica

#### 1. `student.enrolled`

Versión: 1.0

Descripción: Se genera cuando un estudiante es matriculado en un curso.

Producer: Gestión Académica

Consumers:

* Evaluaciones y Progreso — habilita al estudiante para participar en las evaluaciones asociadas al curso.
* Tutor IA y Contenidos — habilita el contexto académico del estudiante para consultas relacionadas con el curso.

Campos principales:

* `student_id`

* `course_id`

* `enrollment_id`

* `enrolled_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "student.enrolled",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:00:00Z",
  "aggregate_id": "enrollment-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "student_id": "student-uuid",
    "course_id": "course-uuid",
    "enrollment_id": "enrollment-uuid",
    "enrolled_at": "2026-10-04T15:00:00Z"
  }
}

```

#### 2. `course.created`

Versión: 1.0

Descripción: Se genera cuando se crea un nuevo curso.

Producer: Gestión Académica

Consumers:

* Evaluaciones y Progreso — reconoce el curso como referencia válida para asociar evaluaciones.
* Tutor IA y Contenidos — reconoce el curso como ámbito para asociar contenidos y contexto utilizado por el Tutor IA.

Campos principales:

* `course_id`

* `name`

* `created_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "course.created",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:05:00Z",
  "aggregate_id": "course-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "course_id": "course-uuid",
    "name": "Programación Avanzada",
    "created_at": "2026-10-04T15:05:00Z"
  }
}

```

#### 3. `course.teacher.assigned`

Versión: 1.0

Descripción: Se genera cuando un docente es asignado a un curso.

Producer: Gestión Académica

Consumers:

* Evaluaciones y Progreso — actualiza la referencia del docente responsable del curso para las operaciones relacionadas con evaluaciones.

Campos principales:

* `course_id`

* `teacher_id`

* `assigned_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "course.teacher.assigned",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:10:00Z",
  "aggregate_id": "course-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "course_id": "course-uuid",
    "teacher_id": "teacher-uuid",
    "assigned_at": "2026-10-04T15:10:00Z"
  }
}

```

### 3.2 Evaluaciones y Progreso

#### 4. `evaluation.created`

Versión: 1.0

Descripción: Se genera cuando se crea una evaluación.

Producer: Evaluaciones y Progreso

Consumers:

* Ninguno externo

Campos principales:

* `evaluation_id`

* `course_id`

* `title`

* `created_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "evaluation.created",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:15:00Z",
  "aggregate_id": "evaluation-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "evaluation_id": "evaluation-uuid",
    "course_id": "course-uuid",
    "title": "Evaluación Unidad 1",
    "created_at": "2026-10-04T15:15:00Z"
  }
}

```

#### 5. `evaluation.published`

Versión: 1.0

Descripción: Se genera cuando una evaluación queda publicada y disponible para los estudiantes.

Producer: Evaluaciones y Progreso

Consumers:

* Ninguno externo

Campos principales:

* `evaluation_id`

* `course_id`

* `published_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "evaluation.published",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:20:00Z",
  "aggregate_id": "evaluation-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "evaluation_id": "evaluation-uuid",
    "course_id": "course-uuid",
    "published_at": "2026-10-04T15:20:00Z"
  }
}

```

#### 6. `evaluation.submitted`

Versión: 1.0

Descripción: Se genera cuando un estudiante entrega una evaluación.

Producer: Evaluaciones y Progreso

Consumers:

* Ninguno externo

Campos principales:

* `evaluation_id`

* `submission_id`

* `student_id`

* `submitted_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "evaluation.submitted",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:30:00Z",
  "aggregate_id": "submission-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "evaluation_id": "evaluation-uuid",
    "submission_id": "submission-uuid",
    "student_id": "student-uuid",
    "submitted_at": "2026-10-04T15:29:50Z"
  }
}
```
#### 7. `evaluation.graded`

Versión: 1.0

Descripción: Se genera cuando una entrega es calificada.

Producer: Evaluaciones y Progreso

Consumers:

* Tutor IA y Contenidos — actualiza el contexto académico disponible para personalizar la asistencia del Tutor IA según los resultados del estudiante.

Campos principales:

* `evaluation_id`

* `submission_id`

* `student_id`

* `grade`

* `graded_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "evaluation.graded",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:59:50Z",
  "aggregate_id": "submission-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "evaluation_id": "evaluation-uuid",
    "submission_id": "submission-uuid",
    "student_id": "student-uuid",
    "grade": 6.5,
    "graded_at": "2026-10-04T15:59:50Z"
  }
}

```

#### 8. `evaluation.completed`

Versión: 1.0

Descripción: Se genera cuando el ciclo de una evaluación ha finalizado y sus resultados se encuentran disponibles.

Producer: Evaluaciones y Progreso

Consumers:

* Ninguno externo

Campos principales:

* `evaluation_id`

* `course_id`

* `completed_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "evaluation.completed",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T16:00:00Z",
  "aggregate_id": "evaluation-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "evaluation_id": "evaluation-uuid",
    "course_id": "course-uuid",
    "completed_at": "2026-10-04T16:00:00Z"
  }
}

```

#### 9. `student.progress.updated`

Versión: 1.0

Descripción: Se genera cuando cambia el progreso académico de un estudiante.

Producer: Evaluaciones y Progreso

Consumers:

* Gestión Académica — actualiza la información resumida de progreso utilizada para el seguimiento académico del estudiante dentro del curso.
* Tutor IA y Contenidos — actualiza el contexto académico utilizado para adaptar la asistencia del Tutor IA al progreso del estudiante.

Campos principales:

* `student_id`

* `course_id`

* `progress_id`

* `completedEvaluations`
  
* `pendingEvaluations`
  
* `averageGrade`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "student.progress.updated",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T16:05:00Z",
  "aggregate_id": "progress-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "student_id": "student-uuid",
    "course_id": "course-uuid",
    "progress_id": "progress-uuid",
    "completedEvaluations": 8,
    "pendingEvaluations": 2,
    "averageGrade": 6.1
  }
}

```

### 3.3 Tutor IA y Contenidos

#### 10. `tutor.question.received`

Versión: 1.0

Descripción: Se genera cuando el Tutor IA recibe una pregunta de un estudiante.

Producer: Tutor IA y Contenidos

Consumers:

Ninguno externo

Campos principales:

* `question_id`

* `student_id`

* `course_id`

* `question`

* `received_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "tutor.question.received",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T16:14:50Z",
  "aggregate_id": "question-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "question_id": "question-uuid",
    "student_id": "student-uuid",
    "course_id": "course-uuid",
    "question": "¿Cómo se resuelve este ejercicio?",
    "received_at": "2026-10-04T16:14:50Z"
  }
}

```

#### 11. `tutor.response.generated`

Versión: 1.0

Descripción: Se genera cuando el Tutor IA genera una respuesta para una pregunta recibida.

Producer: Tutor IA y Contenidos

Consumers:

* Ninguno externo

Campos principales:

* `question_id`

* `response_id`

* `student_id`

* `response`

* `generated_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "tutor.response.generated",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T16:15:50Z",
  "aggregate_id": "response-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "question_id": "question-uuid",
    "response_id": "response-uuid",
    "student_id": "student-uuid",
    "response": "La respuesta se obtiene aplicando...",
    "generated_at": "2026-10-04T16:15:50Z"
  }
}

```

#### 12. `learning.document.indexed`

Versión: 1.0

Descripción: Se genera cuando un documento de aprendizaje ha sido procesado e indexado para ser utilizado por el Tutor IA/RAG.

Producer: Tutor IA y Contenidos

Consumers:

Ninguno externo

Campos principales:

* `document_id`

* `course_id`

* `storage_key`

* `index_id`

* `indexed_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "learning.document.indexed",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T16:19:50Z",
  "aggregate_id": "document-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "document_id": "document-uuid",
    "course_id": "course-uuid",
    "storage_key": "documents/course/document.pdf",
    "index_id": "index-uuid",
    "indexed_at": "2026-10-04T16:19:50Z"
  }
}

```

### 3.4 Identidad y Acceso

#### 13. `user.role.assigned`

Versión: 1.0

Descripción: Se genera cuando se asigna un rol a un usuario.

Producer: Identidad y Acceso

Consumers:

* Gestión Académica — actualiza la información necesaria para aplicar permisos sobre cursos y relaciones académicas.
* Evaluaciones y Progreso — actualiza la información necesaria para autorizar operaciones sobre evaluaciones y resultados.
* Tutor IA y Contenidos — actualiza la información necesaria para autorizar el acceso al Tutor IA y a contenidos académicos.

Campos principales:

* `user_id`

* `role_id`

* `role`

* `assigned_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "user.role.assigned",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T16:24:50Z",
  "aggregate_id": "user-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "user_id": "user-uuid",
    "role_id": "role-uuid",
    "role": "teacher",
    "assigned_at": "2026-10-04T16:24:50Z"
  }
}

```

#### 14. `user.access.revoked`

Versión: 1.0

Descripción: Se genera cuando se revoca el acceso de un usuario al sistema.

Producer: Identidad y Acceso

Consumers:

* Gestión Académica — invalida el acceso del usuario a operaciones de gestión académica.
* Evaluaciones y Progreso — impide que el usuario continúe realizando operaciones sobre evaluaciones y resultados.
* Tutor IA y Contenidos — impide el acceso del usuario al Tutor IA y a contenidos protegidos.

Campos principales:

* `user_id`

* `reason`

* `revoked_at`

Schema:

```
{
  "event_id": "uuid",
  "event_type": "user.access.revoked",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T16:29:50Z",
  "aggregate_id": "user-uuid",
  "tenant_id": "tenant-uuid",
  "trace_id": "trace-uuid",
  "data": {
    "user_id": "user-uuid",
    "reason": "Cuenta deshabilitada",
    "revoked_at": "2026-10-04T16:29:50Z"
  }
}

```

## 4. Entrega y procesamiento de eventos

Los eventos serán publicados mediante Kafka/MSK utilizando un modelo de entrega at-least-once.

Esto significa que un mismo evento podría ser recibido más de una vez por un consumidor.

Para evitar efectos duplicados, los consumidores deben implementar procesamiento idempotente utilizando el campo `event_id`.

Un consumidor debe:

1. Recibir el evento.

2. Verificar si el `event_id` ya fue procesado.

3. Si ya fue procesado, ignorar el evento.

4. Si no fue procesado, ejecutar la operación correspondiente.

5. Registrar el `event_id` como procesado.

Ejemplo conceptual:

```
Evento publicado
        |
        v
      Kafka
        |
        v
    Consumidor
        |
        v
¿event_id ya procesado?
      /       \
    Sí         No
    |           |
 Ignorar     Procesar
                |
                v
       Registrar event_id

```

### 4.1 Idempotencia del productor

El productor debe:

1. Generar el `event_id` una sola vez.
2. Reutilizar el mismo `event_id` si necesita reintentar la publicación.
3. Publicar el evento con la misma `occurred_at` en reintentos.

Esto permite que el consumidor detecte duplicados incluso si el evento se publica varias veces por errores de red.

### 4.2 Retención de `event_id` procesados

Cada consumidor debe mantener un registro de los `event_id` ya procesados para permitir la detección de eventos duplicados.

Para el MVP se establece una ventana inicial de retención de **7 días** para los `event_id` procesados, utilizando una tabla de deduplicación o un almacén clave-valor. Este período podrá ajustarse posteriormente según las necesidades operacionales y la configuración de reintentos del sistema.

Cuando un consumidor detecta un `event_id` ya procesado:
- Descarta el evento sin ejecutar la operación de negocio.
- Registra el evento en el log de observabilidad con la marca `duplicate_ignored`.

### 4.3 Orden de eventos

Kafka garantiza el orden dentro de una partición. Para preservar el orden de eventos por agregado, se utilizará `aggregate_id` como **clave de partición**. De este modo, todos los eventos de un mismo agregado se procesan en orden secuencial.

### 4.4 Eventos sin consumidores externos

Algunos eventos del catálogo no poseen consumidores externos en la versión actual de la arquitectura.

Esto ocurre cuando el evento representa un hecho relevante para auditoría, observabilidad o evolución futura, pero ningún otro Bounded Context necesita ejecutar actualmente una acción como consecuencia directa del evento.

Los eventos sin consumidores externos son:

- `evaluation.created`
- `evaluation.published`
- `evaluation.submitted`
- `evaluation.completed`
- `tutor.question.received`
- `tutor.response.generated`
- `learning.document.indexed`

Estos eventos pueden utilizarse para auditoría, observabilidad, métricas o futuros consumidores sin introducir dependencias innecesarias entre los Bounded Contexts.

## 5. Trazabilidad

Los eventos contienen identificadores que permiten realizar seguimiento de una operación a través de los distintos Bounded Contexts.

Los principales campos de trazabilidad son:

* `event_id`: identifica de forma única el evento.

* `trace_id`: permite seguir una operación distribuida.

* `tenant_id`: identifica el tenant al que pertenece la operación.

* `aggregate_id`: identifica el agregado relacionado.

* `occurred_at`: indica cuándo ocurrió el evento.

Esto permite relacionar eventos producidos por distintos servicios y facilitar la observabilidad y diagnóstico de errores.

## 6. Coherencia con los Bounded Contexts

| **Bounded Context** | **Eventos** |
|---|---|
| Gestión Académica | `student.enrolled`, `course.created`, `course.teacher.assigned` |
| Evaluaciones y Progreso | `evaluation.created`, `evaluation.published`, `evaluation.submitted`, `evaluation.graded`, `evaluation.completed`, `student.progress.updated` |
| Tutor IA y Contenidos | `tutor.question.received`, `tutor.response.generated`, `learning.document.indexed` |
| Identidad y Acceso | `user.role.assigned`, `user.access.revoked` |

Los eventos permiten comunicación asíncrona entre contextos sin acoplar directamente sus implementaciones internas.

## 7. Resumen

El catálogo contiene 14 eventos de dominio distribuidos entre los Bounded Contexts de AulaViva:

* 3 eventos de Gestión Académica.

* 6 eventos de Evaluaciones y Progreso.

* 3 eventos de Tutor IA y Contenidos.

* 2 eventos de Identidad y Acceso.

Los eventos siguen el formato de nombres `recurso.acción.pasado`, cuentan con versión `1.0`, productor, consumidores, campos principales y schema JSON.

La comunicación se plantea de forma asíncrona mediante Kafka/MSK, utilizando entrega at-least-once, identificadores de trazabilidad e idempotencia basada en `event_id`.
