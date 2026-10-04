
# Catálogo de Eventos de Dominio — AulaViva

## 1. Objetivo

Definir los eventos de dominio que permiten comunicar hechos ocurridos entre los cuatro bounded contexts de AulaViva mediante Amazon MSK (Apache Kafka).

## 2. Estructura común

Todos los eventos deben incluir:

- `eventId`: identificador único del evento.
- `eventType`: nombre del evento.
- `eventVersion`: versión del evento.
- `tenantId`: identificador del colegio.
- `occurredAt`: fecha y hora del hecho ocurrido.
- `correlationId`: identificador para relacionar operaciones.

Versión inicial de los eventos: `1.0`.

## 3. Eventos de Gestión Académica

### 3.1 StudentEnrolled
- **Descripción:** un estudiante queda matriculado en un curso.
- **Productor:** Gestión Académica.
- **Consumidores:** Evaluaciones y Progreso; Tutor IA y Contenidos.
- **Campos:** `studentId`, `courseId`, `enrollmentId`.

### 3.2 CourseCreated
- **Descripción:** se crea un curso.
- **Productor:** Gestión Académica.
- **Consumidores:** Evaluaciones y Progreso; Tutor IA y Contenidos.
- **Campos:** `courseId`, `courseName`, `teacherId`.

### 3.3 TeacherAssignedToCourse
- **Descripción:** se asigna un profesor a un curso.
- **Productor:** Gestión Académica.
- **Consumidores:** Evaluaciones y Progreso.
- **Campos:** `teacherId`, `courseId`.

## 4. Eventos de Evaluaciones y Progreso

### 4.1 EvaluationCreated
- **Descripción:** se crea una evaluación.
- **Productor:** Evaluaciones y Progreso.
- **Consumidores:** Gestión Académica; Tutor IA y Contenidos.
- **Campos:** `evaluationId`, `courseId`, `title`, `status`.

### 4.2 EvaluationPublished
- **Descripción:** una evaluación queda disponible para los estudiantes.
- **Productor:** Evaluaciones y Progreso.
- **Consumidores:** Gestión Académica.
- **Campos:** `evaluationId`, `courseId`, `publishedAt`.

### 4.3 EvaluationSubmitted
- **Descripción:** un estudiante entrega sus respuestas.
- **Productor:** Evaluaciones y Progreso.
- **Consumidores:** Tutor IA y Contenidos.
- **Campos:** `submissionId`, `evaluationId`, `studentId`, `submittedAt`.

### 4.4 EvaluationGraded
- **Descripción:** se registra la calificación de una entrega.
- **Productor:** Evaluaciones y Progreso.
- **Consumidores:** Gestión Académica; Tutor IA y Contenidos.
- **Campos:** `submissionId`, `studentId`, `grade`, `obtainedScore`, `maximumScore`.

### 4.5 EvaluationCompleted
- **Descripción:** termina el procesamiento de una evaluación entregada y se obtiene su resultado.
- **Productor:** Evaluaciones y Progreso.
- **Consumidores:** Gestión Académica; Tutor IA y Contenidos.
- **Campos:** `evaluationId`, `studentId`, `grade`, `submissionId`.

### 4.6 StudentProgressUpdated
- **Descripción:** se actualiza el progreso académico de un estudiante.
- **Productor:** Evaluaciones y Progreso.
- **Consumidores:** Gestión Académica; Tutor IA y Contenidos.
- **Campos:** `studentId`, `courseId`, `completedEvaluations`, `averageGrade`.

## 5. Eventos de Tutor IA y Contenidos

### 5.1 TutorQuestionReceived
- **Descripción:** el Tutor IA recibe una consulta académica.
- **Productor:** Tutor IA y Contenidos.
- **Consumidores:** Tutor IA y Contenidos.
- **Campos:** `queryId`, `studentId`, `courseId`, `question`.

### 5.2 TutorResponseGenerated
- **Descripción:** el Tutor IA genera una respuesta contextualizada mediante RAG.
- **Productor:** Tutor IA y Contenidos.
- **Consumidores:** Tutor IA y Contenidos.
- **Campos:** `queryId`, `responseId`, `sourceDocumentIds`, `generatedAt`.

### 5.3 LearningDocumentIndexed
- **Descripción:** un documento educativo queda indexado para su recuperación mediante RAG.
- **Productor:** Tutor IA y Contenidos.
- **Consumidores:** Tutor IA y Contenidos.
- **Campos:** `documentId`, `courseId`, `indexId`, `indexedAt`.

## 6. Eventos de Identidad y Acceso

### 6.1 UserRoleAssigned
- **Descripción:** se asigna un rol a un usuario dentro de un tenant.
- **Productor:** Identidad y Acceso.
- **Consumidores:** Gestión Académica; Evaluaciones y Progreso; Tutor IA y Contenidos.
- **Campos:** `userId`, `roleId`, `assignedAt`.

### 6.2 UserAccessRevoked
- **Descripción:** se revoca el acceso de un usuario.
- **Productor:** Identidad y Acceso.
- **Consumidores:** Gestión Académica; Evaluaciones y Progreso; Tutor IA y Contenidos.
- **Campos:** `userId`, `scope`, `revokedAt`.

## 7. Entrega e idempotencia

- Amazon MSK (Apache Kafka) se considera el broker de eventos.
- Se contempla entrega at-least-once, por lo que un evento puede recibirse más de una vez.
- Los consumidores deberán identificar eventos previamente procesados mediante `eventId`.
- El mecanismo de deduplicación y la persistencia de eventos procesados quedan pendientes de validación por QA.
- Los eventos deben respetar el aislamiento por `tenantId`.
- No se deben publicar contraseñas, tokens ni secretos.

## 8. Compatibilidad y validación

El evento `EvaluationCompleted` debe revisarse frente al esquema `EvaluationCompletedEvent` definido en `api/openapi.yaml`.

Los campos comunes de trazabilidad propuestos en este catálogo aún deben contrastarse con el contrato existente.

Los productores, consumidores y esquemas se consideran una propuesta inicial y deberán validarse antes de su implementación definitiva.
