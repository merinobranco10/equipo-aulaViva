Feature: Aislamiento de datos multi-tenant
  Como Sostenedor
  Quiero que cada colegio opere en un tenant aislado lógicamente
  Para garantizar que los datos y contenidos de un colegio nunca sean accesibles por otro

  Scenario: Acceso exitoso a los datos del propio tenant
    Given un usuario "Docente" autenticado en el tenant "Colegio NN"
    When el usuario solicita la lista de cursos de su colegio
    Then el sistema retorna únicamente los cursos pertenecientes al tenant "Colegio NN"

  Scenario: Intento de acceso cruzado entre tenants (borde)
    Given un usuario "Coordinador académico" autenticado en el tenant "Colegio NN"
    When el usuario intenta acceder a un curso cuyo identificador pertenece al tenant "Colegio XX"
    Then el sistema deniega el acceso
    And retorna un error de recurso no encontrado sin revelar la existencia del recurso en otro tenant

  Scenario: Error de configuración de tenant inexistente
    Given un usuario intenta autenticarse indicando el tenant "Colegio Fantasma"
    When el tenant "Colegio Fantasma" no existe en el sistema
    Then el sistema rechaza el inicio de sesión
    And registra el intento en el log de auditoría de seguridad
