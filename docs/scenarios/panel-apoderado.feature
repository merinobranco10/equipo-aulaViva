Feature: Panel del apoderado con seguimiento de avance
  Como Apoderado
  Quiero ver un panel con el avance académico de mi hijo/a
  Para poder acompañar su proceso de aprendizaje

  Scenario: Apoderado consulta el avance de su hijo/a
    Given un "Apoderado" autenticado y vinculado al estudiante "Juan Soto"
    When accede al panel de seguimiento
    Then el sistema muestra las calificaciones y avance de los cursos de "Juan Soto"

  Scenario: Apoderado con múltiples hijos/as en distintos colegios (borde)
    Given un "Apoderado" vinculado a estudiantes en dos colegios distintos
    When accede al panel de seguimiento
    Then el sistema muestra el avance de cada estudiante segmentado por su colegio/tenant correspondiente

  Scenario: Error de vínculo sin consentimiento parental
    Given un "Apoderado" intenta vincularse a un estudiante
    When el consentimiento parental requerido no ha sido otorgado
    Then el sistema rechaza el acceso al panel de seguimiento
    And solicita completar el proceso de consentimiento antes de continuar
