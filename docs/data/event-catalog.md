# Catálogo de Eventos de Dominio — AulaViva

## 1. Propósito

Este documento define el catálogo de eventos de dominio utilizados para la comunicación entre los bounded contexts de AulaViva.

Los eventos representan **hechos ocurridos** dentro del dominio y permiten desacoplar los distintos contextos mediante comunicación asíncrona utilizando Kafka/MSK como broker de eventos.

Los eventos definidos consideran los siguientes bounded contexts:

* Gestión Académica
* Evaluaciones y Progreso
* Tutor IA y Contenidos
* Identidad y Acceso

---

## 2. Convenciones generales

Todos los eventos deben cumplir las siguientes convenciones:

* El nombre del evento representa un hecho que ya ocurrió.
* Los nombres utilizan PascalCase y tiempo verbal pasado.
* Cada evento posee una versión.
* La versión inicial de los eventos es `1.0`.
* Los eventos incluyen identificadores para garantizar trazabilidad.
* La entrega de eventos utiliza un modelo **at-least-once**.
* El consumidor debe implementar procesamiento idempotente utilizando `eventId`.
* `tenantId` permite identificar el tenant al que pertenece el evento.
* `occurredAt` indica cuándo ocurrió el hecho en el dominio.
* `correlationId` permite relacionar eventos pertenecientes a una misma operación o flujo de negocio.

### Campos comunes

Todos los eventos incluyen los siguientes campos:

| Campo           | Tipo     | Descripción                                                              |
| --------------- | -------- | ------------------------------------------------------------------------ |
| `eventId`       | UUID     | Identificador único del evento. Se utiliza para garantizar idempotencia. |
| `eventType`     | String   | Nombre del evento.                                                       |
| `version`       | String   | Versión del contrato del evento.                                         |
| `tenantId`      | UUID     | Identificador del tenant.                                                |
| `occurredAt`    | DateTime | Fecha y hora en que ocurrió el evento.                                   |
| `correlationId` | UUID     | Identificador para relacionar eventos pertenecientes al mismo flujo.     |

---

# 3. Catálogo de eventos

## 3.1 Gestión Académica

### 1. StudentEnrolled

**Versión:** `1.0`

**Descripción:**
Se produce cuando un estudiante es matriculado o inscrito en un curso.

**Productor:** Gestión Académica

**Consumidores:**

* Evaluaciones y Progreso
* Tutor IA y Contenidos

**Campos principales:**

* `studentId`
* `courseId`
* `enrollmentId`
* `tenantId`
* `occurredAt`

---

### 2. CourseCreated

**Versión:** `1.0`

**Descripción:**
Se produce cuando se crea un nuevo curso dentro de AulaViva.

**Productor:** Gestión Académica

**Consumidores:**

* Evaluaciones y Progreso
* Tutor IA y Contenidos

**Campos principales:**

* `courseId`
* `teacherId`
* `courseName`
* `tenantId`
* `occurredAt`

---

### 3. TeacherAssignedToCourse

**Versión:** `1.0`

**Descripción:**
Se produce cuando un docente es asignado a un curso.

**Productor:** Gestión Académica

**Consumidores:**

* Evaluaciones y Progreso

**Campos principales:**

* `teacherId`
* `courseId`
* `assignmentId`
* `tenantId`
* `occurredAt`

---

# 3.2 Evaluaciones y Progreso

### 4. EvaluationCreated

**Versión:** `1.0`

**Descripción:**
Se produce cuando se crea una nueva evaluación asociada a un curso.

**Productor:** Evaluaciones y Progreso

**Consumidores:**

* Gestión Académica
* Tutor IA y Contenidos

**Campos principales:**

* `evaluationId`
* `courseId`
* `teacherId`
* `title`
* `tenantId`
* `occurredAt`

---

### 5. EvaluationPublished

**Versión:** `1.0`

**Descripción:**
Se produce cuando una evaluación queda publicada y disponible para los estudiantes.

**Productor:** Evaluaciones y Progreso

**Consumidores:**

* Gestión Académica
* Tutor IA y Contenidos

**Campos principales:**

* `evaluationId`
* `courseId`
* `publishedAt`
* `tenantId`
* `occurredAt`

---

### 6. EvaluationSubmitted

**Versión:** `1.0`

**Descripción:**
Se produce cuando un estudiante entrega una evaluación.

**Productor:** Evaluaciones y Progreso

**Consumidores:**

* Tutor IA y Contenidos
* Evaluaciones y Progreso

**Campos principales:**

* `evaluationId`
* `submissionId`
* `studentId`
* `submittedAt`
* `tenantId`
* `occurredAt`

---

### 7. EvaluationGraded

**Versión:** `1.0`

**Descripción:**
Se produce cuando una entrega de evaluación ha sido calificada y se obtiene su resultado.

**Productor:** Evaluaciones y Progreso

**Consumidores:**

* Gestión Académica
* Evaluaciones y Progreso
* Tutor IA y Contenidos

**Campos principales:**

* `evaluationId`
* `submissionId`
* `studentId`
* `grade`
* `gradedAt`
* `tenantId`
* `occurredAt`

---

### 8. EvaluationCompleted

**Versión:** `1.0`

**Descripción:**
Se produce cuando el procesamiento completo de una evaluación entregada ha finalizado y el resultado final está disponible.

Este evento se diferencia de `EvaluationGraded`, ya que `EvaluationGraded` representa la calificación de una entrega, mientras que `EvaluationCompleted` representa el cierre del procesamiento completo de la evaluación.

**Productor:** Evaluaciones y Progreso

**Consumidores:**

* Gestión Académica
* Tutor IA y Contenidos

**Campos principales:**

* `evaluationId`
* `submissionId`
* `studentId`
* `status`
* `completedAt`
* `tenantId`
* `occurredAt`

---

### 9. StudentProgressUpdated

**Versión:** `1.0`

**Descripción:**
Se produce cuando se actualiza el progreso académico de un estudiante como consecuencia de actividades o resultados de evaluación.

**Productor:** Evaluaciones y Progreso

**Consumidores:**

* Gestión Académica
* Tutor IA y Contenidos

**Campos principales:**

* `studentId`
* `courseId`
* `progressId`
* `progressPercentage`
* `updatedAt`
* `tenantId`
* `occurredAt`

---

# 3.3 Tutor IA y Contenidos

### 10. TutorQuestionReceived

**Versión:** `1.0`

**Descripción:**
Se produce cuando el sistema Tutor IA recibe una pregunta de un estudiante.

**Productor:** Tutor IA y Contenidos

**Consumidores:**

* Tutor IA y Contenidos

**Campos principales:**

* `questionId`
* `studentId`
* `courseId`
* `question`
* `receivedAt`
* `tenantId`
* `occurredAt`

**Nota:**
Este evento corresponde a un hecho interno del bounded context de Tutor IA y puede utilizarse para trazabilidad, auditoría y procesamiento asíncrono.

---

### 11. TutorResponseGenerated

**Versión:** `1.0`

**Descripción:**
Se produce cuando Tutor IA genera una respuesta para una pregunta realizada por un estudiante.

**Productor:** Tutor IA y Contenidos

**Consumidores:**

* Tutor IA y Contenidos

**Campos principales:**

* `questionId`
* `responseId`
* `studentId`
* `response`
* `generatedAt`
* `tenantId`
* `occurredAt`

**Nota:**
Este evento corresponde a un hecho interno del bounded context de Tutor IA y puede utilizarse para trazabilidad, auditoría y procesamiento posterior.

---

### 12. LearningDocumentIndexed

**Versión:** `1.0`

**Descripción:**
Se produce cuando un documento de aprendizaje ha sido procesado e indexado correctamente para su utilización por el Tutor IA.

**Productor:** Tutor IA y Contenidos

**Consumidores:**

* Tutor IA y Contenidos

**Campos principales:**

* `documentId`
* `courseId`
* `storageKey`
* `indexId`
* `indexedAt`
* `tenantId`
* `occurredAt`

---

# 3.4 Identidad y Acceso

### 13. UserRoleAssigned

**Versión:** `1.0`

**Descripción:**
Se produce cuando a un usuario se le asigna un rol dentro de AulaViva.

**Productor:** Identidad y Acceso

**Consumidores:**

* Gestión Académica
* Evaluaciones y Progreso
* Tutor IA y Contenidos

**Campos principales:**

* `userId`
* `roleId`
* `role`
* `assignedAt`
* `tenantId`
* `occurredAt`

---

### 14. UserAccessRevoked

**Versión:** `1.0`

**Descripción:**
Se produce cuando se revoca el acceso de un usuario a AulaViva.

**Productor:** Identidad y Acceso

**Consumidores:**

* Gestión Académica
* Evaluaciones y Progreso
* Tutor IA y Contenidos

**Campos principales:**

* `userId`
* `reason`
* `revokedAt`
* `tenantId`
* `occurredAt`

---

# 4. Idempotencia y entrega de eventos

AulaViva utilizará un modelo de entrega **at-least-once** para los eventos publicados mediante Kafka/MSK.

Este modelo garantiza que un evento no se pierda, pero permite que un mismo evento pueda ser recibido más de una vez por un consumidor.

Para evitar efectos duplicados, los consumidores deberán implementar procesamiento idempotente utilizando `eventId` como identificador único del evento.

El consumidor deberá registrar los `eventId` procesados y verificar su existencia antes de ejecutar nuevamente una operación asociada al evento.

Ejemplo:

```text
Evento recibido
      |
      v
¿eventId ya procesado?
   /          \
 Sí            No
 |              |
Ignorar       Procesar
                  |
                  v
          Registrar eventId
```

De esta forma, si Kafka entrega nuevamente el mismo evento, el consumidor podrá reconocerlo y evitar ejecutar dos veces la misma operación.

---

# 5. Trazabilidad

Para facilitar la trazabilidad distribuida, todos los eventos contienen:

* `eventId`: identifica de manera única cada evento.
* `tenantId`: identifica el tenant asociado.
* `occurredAt`: registra cuándo ocurrió el hecho.
* `correlationId`: permite relacionar diferentes eventos pertenecientes a una misma operación.

Estos identificadores permiten reconstruir el flujo de una operación entre los distintos bounded contexts.

---

# 6. Resumen

El catálogo contiene **14 eventos de dominio**, distribuidos de la siguiente manera:

| Bounded Context         | Cantidad |
| ----------------------- | -------: |
| Gestión Académica       |        3 |
| Evaluaciones y Progreso |        6 |
| Tutor IA y Contenidos   |        3 |
| Identidad y Acceso      |        2 |
| **Total**               |   **14** |

Los eventos están diseñados para mantener bajo acoplamiento entre bounded contexts y permitir comunicación asíncrona mediante Kafka/MSK.

La estrategia de entrega será **at-least-once**, utilizando `eventId` como mecanismo principal para garantizar la idempotencia de los consumidores.
