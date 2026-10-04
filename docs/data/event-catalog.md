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

* Evaluaciones y Progreso

* Tutor IA y Contenidos

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

* Evaluaciones y Progreso

* Tutor IA y Contenidos

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

* Evaluaciones y Progreso

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

* Gestión Académica

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

* Gestión Académica

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

* Gestión Académica

* Tutor IA y Contenidos

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

* Gestión Académica

* Tutor IA y Contenidos

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

* Gestión Académica

* Tutor IA y Contenidos

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

* Gestión Académica

* Tutor IA y Contenidos

Campos principales:

* `student_id`

* `course_id`

* `progress_id`

* `progress_percentage`

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
    "progress_percentage": 75
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

* Evaluaciones y Progreso

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

* Gestión Académica

* Evaluaciones y Progreso

* Tutor IA y Contenidos

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

* Gestión Académica

* Evaluaciones y Progreso

* Tutor IA y Contenidos

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
