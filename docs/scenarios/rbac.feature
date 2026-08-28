Feature: Gestión de cursos, docentes y estudiantes con RBAC
  Como Coordinador académico
  Quiero gestionar cursos, docentes y estudiantes con roles y permisos definidos
  Para asegurar que cada usuario solo acceda a la información y funciones que le corresponden

  Scenario: Coordinador crea un curso y asigna un docente
    Given un "Coordinador académico" autenticado en su tenant
    When crea un nuevo curso y asigna al docente "Ana Pérez" como responsable
    Then el curso queda visible para el docente "Ana Pérez"
    And el curso no es visible para docentes que no fueron asignados

  Scenario: Estudiante intenta acceder a funciones de docente (borde)
    Given un "Estudiante" autenticado en su curso
    When intenta acceder al panel de calificación de evaluaciones
    Then el sistema deniega el acceso por permisos insuficientes
    And muestra un mensaje indicando que la acción requiere rol de Docente

  Scenario: Error al asignar un rol inexistente
    Given un "Coordinador académico" intenta asignar el rol "SuperAdminGlobal" a un docente
    When dicho rol no existe en el catálogo de roles del tenant
    Then el sistema rechaza la operación
    And retorna un mensaje de error indicando que el rol no es válido
