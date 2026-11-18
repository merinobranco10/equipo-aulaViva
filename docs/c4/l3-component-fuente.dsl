workspace "AulaViva - C4 L3" "Arquitectura de componentes del backend AulaViva" {

    !identifiers hierarchical
    !impliedRelationships false

    model {
        // Personas
        estudiante = person "Estudiante" "Realiza evaluaciones, consulta su progreso y utiliza el Tutor IA."
        docente = person "Docente" "Gestiona cursos y crea evaluaciones."
        apoderado = person "Apoderado" "Consulta el progreso académico del estudiante asociado."

        // Servicios externos
        llm = softwareSystem "Proveedor LLM" "Servicio externo que genera respuestas para el Tutor IA." "External"

        // Sistema y contenedores
        aulaviva = softwareSystem "AulaViva" "Plataforma SaaS educativa multi-tenant." {
        db = container "PostgreSQL" "Almacena datos transaccionales, datos de autorización y datos del Tutor IA (metadatos, fragmentos y embeddings), con aislamiento lógico por tenant." "PostgreSQL 16 + pgvector" "Database"

            backend = container "Backend AulaViva" "Monolito modular que expone la API y coordina los casos de uso." "Spring Boot 3.5 / Java 21" {
                ui = component "UI estática" "Entrega las vistas web por rol y consume la API REST." "HTML + CSS + JavaScript"
                identity = component "Identity" "Autentica usuarios, carga su identidad y aplica autorización basada en roles." "Spring Security"
                tenancy = component "TenantContext" "Resuelve el tenant asociado al usuario autenticado y entrega el contexto de aislamiento." "Java / Spring"
                academic = component "Gestión Académica" "Provee información de cursos y estudiantes para los casos de uso académicos." "Java / Spring"
                evaluations = component "Evaluations" "Crea evaluaciones, registra entregas, corrige respuestas y calcula calificaciones." "Java / Spring"
                progress = component "Progress" "Consulta y consolida el avance académico para el panel del apoderado." "Java / Spring"
                tutor = component "Tutor IA" "Atiende preguntas académicas y coordina la generación de respuestas con el proveedor LLM." "Java / Spring"
                outbox = component "Outbox" "Registra eventos de dominio en la misma base transaccional para su publicación posterior." "Java / Spring Data JPA"
                shared = component "Shared / API Errors" "Centraliza excepciones, respuestas ProblemDetails y manejo común de errores." "Java / Spring"
            }
        }

        // Interacción de usuarios con la UI
        estudiante -> aulaviva.backend.ui "Utiliza evaluaciones, progreso y Tutor IA" "HTTPS"
        docente -> aulaviva.backend.ui "Gestiona cursos y evaluaciones" "HTTPS"
        apoderado -> aulaviva.backend.ui "Consulta el progreso del estudiante" "HTTPS"

        // UI y seguridad
        aulaviva.backend.ui -> aulaviva.backend.identity "Solicita autenticación y consulta el usuario actual" "HTTP / REST"
        aulaviva.backend.ui -> aulaviva.backend.evaluations "Consume operaciones de evaluaciones" "HTTP / REST"
        aulaviva.backend.ui -> aulaviva.backend.progress "Consulta el progreso académico" "HTTP / REST"
        aulaviva.backend.ui -> aulaviva.backend.tutor "Envía preguntas al Tutor IA" "HTTP / REST"

        // Dependencias internas y multi-tenancy
        aulaviva.backend.identity -> aulaviva.backend.tenancy "Asocia el usuario autenticado con su tenant" "Java"
        aulaviva.backend.evaluations -> aulaviva.backend.tenancy "Obtiene el contexto del tenant para filtrar operaciones" "Java"
        aulaviva.backend.progress -> aulaviva.backend.tenancy "Obtiene el contexto del tenant para filtrar consultas" "Java"
        aulaviva.backend.tutor -> aulaviva.backend.tenancy "Obtiene el contexto del tenant para limitar el contexto académico" "Java"
        aulaviva.backend.evaluations -> aulaviva.backend.academic "Consulta cursos y datos académicos necesarios" "Java"
        aulaviva.backend.progress -> aulaviva.backend.academic "Consulta la relación entre estudiante y curso" "Java"

        // Persistencia y eventos
        aulaviva.backend.identity -> aulaviva.db "Lee usuarios, roles y asociaciones de tenant" "JPA / SQL"
        aulaviva.backend.academic -> aulaviva.db "Consulta información académica" "JPA / SQL"
        aulaviva.backend.evaluations -> aulaviva.db "Lee y escribe evaluaciones, preguntas, entregas y notas" "JPA / SQL"
        aulaviva.backend.progress -> aulaviva.db "Consulta resultados y progreso" "JPA / SQL"
        aulaviva.backend.outbox -> aulaviva.db "Persiste eventos de dominio en la tabla outbox" "JPA / SQL"
        aulaviva.backend.evaluations -> aulaviva.backend.outbox "Registra EvaluationCompleted al completar una evaluación" "Java"

        // Tutor IA
        aulaviva.backend.tutor -> llm "Solicita generación de respuesta con contexto permitido" "HTTPS / API"

        // Manejo transversal de errores
        aulaviva.backend.ui -> aulaviva.backend.shared "Interpreta respuestas de error de la API" "HTTP / ProblemDetails"
        aulaviva.backend.evaluations -> aulaviva.backend.shared "Utiliza excepciones y respuestas comunes" "Java"
        aulaviva.backend.progress -> aulaviva.backend.shared "Utiliza excepciones y respuestas comunes" "Java"
        aulaviva.backend.tutor -> aulaviva.backend.shared "Utiliza excepciones y respuestas comunes" "Java"
    }

    views {
        component aulaviva.backend "AulaViva-C4-L3" {
            include estudiante
            include docente
            include apoderado
            include *
            autoLayout lr
            title "AulaViva - C4 Nivel 3 - Componentes del Backend"
        }

        styles {
            element "Person" {
                shape Person
                background "#08427b"
                color "#ffffff"
            }
            element "Software System" {
                background "#1168bd"
                color "#ffffff"
            }
            element "Container" {
                background "#438dd5"
                color "#ffffff"
            }
            element "Component" {
                background "#85bbf0"
                color "#000000"
            }
            element "Database" {
                shape Cylinder
                background "#438dd5"
                color "#ffffff"
            }
            element "External" {
                background "#999999"
                color "#ffffff"
            }
        }
    }
}
