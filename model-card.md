
# Model Card — Tutor IA AulaViva

## 1. Información general

| Campo | Descripción |
|---|---|
| Sistema | Tutor IA AulaViva |
| Tipo | Asistente educativo basado en LLM y RAG |
| Versión | Borrador inicial |
| Responsable | Equipo AI/Data |
| Estado | Diseño y preparación de integración |
| Público objetivo | Estudiantes de educación escolar |

## 2. Propósito

El Tutor IA tiene como objetivo apoyar el aprendizaje de los estudiantes mediante respuestas a preguntas académicas relacionadas con los contenidos de sus cursos.

El sistema utiliza recuperación aumentada por generación (RAG) para fundamentar sus respuestas en los apuntes asociados al curso del estudiante.

## 3. Arquitectura prevista

1. El estudiante envía una pregunta y el identificador del curso.
2. El backend valida la identidad y los permisos del estudiante.
3. El sistema identifica el tenant y el curso autorizado.
4. El componente RAG recupera fragmentos relevantes del corpus correspondiente.
5. El LLM genera una respuesta utilizando el contexto recuperado.
6. El backend entrega la respuesta y las fuentes utilizadas.

## 4. Datos de entrada

- Identificador del curso (`courseId`).
- Pregunta escrita por el estudiante.
- Fragmentos de apuntes recuperados desde el corpus del curso.

No se deben enviar datos personales innecesarios al proveedor del modelo.

## 5. Datos de salida

La respuesta de la API contempla:

- `answer`: respuesta generada por el tutor.
- `sources`: fuentes utilizadas por el componente RAG, cuando estén disponibles.

## 6. Corpus inicial

| Curso | Identificador | Cantidad de apuntes |
|---|---|---:|
| Matemática 8° básico A | matematica-8a | 5 |
| Ciencias Naturales 8° básico B | ciencias-8b | 4 |

Los apuntes son materiales de demostración creados para preparar y probar el flujo de recuperación.

Deben revisarse y validarse contra los objetivos curriculares oficiales antes de su utilización educativa en producción.

## 7. Comportamiento esperado

- Responder preguntas respaldadas por los apuntes del curso.
- Mantener las respuestas dentro del contenido curricular asignado.
- Utilizar lenguaje claro y apropiado para estudiantes.
- Evitar inventar información o fuentes.
- Reconocer cuando los apuntes no contienen información suficiente.
- Informar cuando el servicio no esté disponible.

## 8. Limitaciones

- La calidad de las respuestas depende de la calidad y cobertura del corpus.
- El sistema puede recuperar fragmentos poco relevantes.
- Un LLM puede generar respuestas incorrectas o imprecisas.
- La recuperación RAG no garantiza por sí sola que una respuesta sea correcta.
- El proveedor y modelo definitivo aún deben confirmarse.
- El endpoint actual del backend es un stub que responde HTTP 501 hasta la implementación del Día 4.

## 9. Seguridad y privacidad

- El acceso al tutor debe limitarse a estudiantes autenticados.
- El backend debe validar que el estudiante pertenece al curso consultado.
- Los datos de un colegio no deben mezclarse con los de otro.
- Las claves API deben almacenarse como secretos.
- No se deben incluir credenciales en GitHub.
- Se deben minimizar los datos personales enviados al proveedor externo.
- Las preguntas y respuestas deben tratarse como información potencialmente sensible.

## 10. Evaluación

El conjunto contempla 36 casos: 32 preguntas con respuesta en los apuntes, 1 fuera de tema, 1 de otro colegio y 2 intentos de manipulación (prompt injection).

Criterios de evaluación:

- Exactitud factual.
- Correspondencia con las fuentes recuperadas.
- Pertinencia curricular.
- Claridad de la respuesta.
- Comportamiento ante consultas sin información suficiente.
- Aislamiento de información entre cursos y colegios.
- Manejo de errores y falta de disponibilidad.

La evaluación debe ser ejecutada y registrada antes de considerar el sistema apto para producción.

## 11. Riesgos

| Riesgo | Mitigación |
|---|---|
| Respuestas incorrectas | Evaluación con casos de prueba y revisión humana |
| Información fuera del currículo | Recuperación limitada al corpus autorizado |
| Filtración entre colegios | Validación de tenant y permisos en backend |
| Exposición de claves | Secretos gestionados fuera del repositorio |
| Indisponibilidad del proveedor | Timeout, manejo de errores y monitoreo |
| Respuestas inadecuadas | Filtros, supervisión y pruebas periódicas |

## 12. Estado de implementación

Este documento describe el diseño y los criterios previstos para el Tutor IA.

No representa una integración productiva terminada. La selección definitiva del proveedor, el modelo, la base vectorial, la evaluación real y la implementación del endpoint deben completarse y validarse por el equipo.
