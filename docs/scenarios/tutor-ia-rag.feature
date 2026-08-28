Feature: Tutor IA con RAG sobre apuntes del curso
  Como Estudiante
  Quiero consultar un tutor IA que responda basado en los apuntes de mi curso
  Para resolver mis dudas dentro del contexto del currículum vigente

  Scenario: Estudiante realiza una consulta cubierta por los apuntes del curso
    Given un "Estudiante" matriculado en el curso "Matemática 8vo básico"
    When pregunta al tutor IA sobre un tema incluido en los apuntes del curso
    Then el tutor IA responde usando como fuente los apuntes del curso
    And la respuesta se mantiene dentro del alcance del currículum MINEDUC del curso

  Scenario: Consulta fuera del contexto del curso (borde)
    Given un "Estudiante" matriculado en el curso "Matemática 8vo básico"
    When pregunta al tutor IA sobre un tema no incluido en los apuntes del curso
    Then el tutor IA indica que no cuenta con información del curso sobre ese tema
    And evita responder con contenido fuera del currículum asignado

  Scenario: Error de disponibilidad del servicio de tutor IA
    Given un "Estudiante" realiza una consulta al tutor IA
    When el servicio de LLM no está disponible
    Then el sistema informa al estudiante que el tutor IA no está disponible temporalmente
    And registra el incidente para monitoreo de disponibilidad y costos
