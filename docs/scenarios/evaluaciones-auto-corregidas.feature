Feature: Evaluaciones auto-corregidas con feedback
  Como Docente
  Quiero que las evaluaciones se corrijan automáticamente y entreguen feedback a los estudiantes
  Para reducir el tiempo de corrección manual y dar retroalimentación inmediata

  Scenario: Estudiante envía una evaluación completa a tiempo
    Given un "Estudiante" con una evaluación de opción múltiple asignada
    When el estudiante responde todas las preguntas y envía la evaluación antes del plazo
    Then el sistema corrige automáticamente la evaluación
    And muestra al estudiante su puntaje y feedback por pregunta

  Scenario: Estudiante envía una evaluación incompleta (borde)
    Given un "Estudiante" con una evaluación asignada
    When el estudiante envía la evaluación dejando preguntas sin responder
    Then el sistema corrige solo las preguntas respondidas
    And marca las preguntas no respondidas como incorrectas con feedback indicando que no fueron contestadas

  Scenario: Error al enviar evaluación fuera de plazo
    Given un "Estudiante" con una evaluación cuyo plazo de entrega ya venció
    When el estudiante intenta enviar sus respuestas
    Then el sistema rechaza el envío
    And notifica al estudiante que el plazo de la evaluación ha expirado
