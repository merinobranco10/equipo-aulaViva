// Actualizado 18-11-2026: Tutor IA con Groq + BM25 + guardrails + fallback (ADR 0009), corpus en el
// classpath, sin pgvector; coordinador y revisión de entregas (Día 2).
workspace "AulaViva - C4 L3" "Arquitectura de componentes del backend AulaViva" {

    !identifiers hierarchical
    !impliedRelationships false

    model {
        // Personas
        estudiante = person "Estudiante" "Realiza evaluaciones, consulta su progreso y utiliza el Tutor IA."
        docente = person "Docente" "Gestiona cursos y crea evaluaciones."
        apoderado = person "Apoderado" "Consulta el progreso académico del estudiante asociado."
        coordinador = person "Coordinador Académico" "Consulta el progreso de cualquier estudiante de su colegio."

        // Servicios externos
        llm = softwareSystem "Proveedor LLM (Groq)" "openai/gpt-oss-120b (respaldo openai/gpt-oss-20b), API compatible con OpenAI." "External"

        // Sistema y contenedores
        aulaviva = softwareSystem "AulaViva" "Plataforma SaaS educativa multi-tenant." {
            db = container "PostgreSQL" "Datos transaccionales, usuarios y roles, notas y tabla outbox_events, con aislamiento lógico por tenant_id. Sin embeddings (pgvector diferido)." "PostgreSQL 16 / H2 en local" "Database"

            backend = container "Backend AulaViva" "Monolito modular que expone la API y coordina los casos de uso." "Spring Boot 3.5 / Java 21" {
                ui = component "UI estática" "Entrega las vistas web por rol y consume la API REST." "HTML + CSS + JavaScript"
                identity = component "Identity" "Autentica usuarios, carga su identidad y aplica autorización basada en roles." "Spring Security"
                tenancy = component "TenantContext" "Resuelve el tenant asociado al usuario autenticado y entrega el contexto de aislamiento." "Java / Spring"
                academic = component "Gestión Académica" "Provee información de cursos y estudiantes para los casos de uso académicos." "Java / Spring"
                evaluations = component "Evaluations" "Crea evaluaciones, registra entregas con Idempotency-Key, corrige, calcula la nota (1,0-7,0, 60 %) y entrega la revisión de la propia entrega." "Java / Spring"
                progress = component "Progress" "Consolida el avance académico y decide quién puede ver a cada estudiante (estudiante, apoderado, docente, coordinador)." "Java / Spring"
                tutor = component "Tutor IA" "Guardrails (injection y PII), búsqueda BM25 top-3 en los apuntes del curso, llamada al LLM con timeout de 8 s y fallback extractivo (Resilience4j)." "Java / Spring"
                corpus = component "Corpus de apuntes" "Apuntes Markdown por curso con su OA (classpath:corpus/<courseId>/)." "Recursos del JAR"
                outbox = component "Outbox" "Registra eventos de dominio en la misma base transaccional para su publicación posterior." "Java / Spring Data JPA"
                shared = component "Shared / API Errors" "Centraliza excepciones, respuestas ProblemDetails y manejo común de errores." "Java / Spring"
            }
        }

        // Interacción de usuarios con la UI
        estudiante -> aulaviva.backend.ui "Utiliza evaluaciones, progreso y Tutor IA" "HTTPS"
        docente -> aulaviva.backend.ui "Gestiona cursos y evaluaciones" "HTTPS"
        apoderado -> aulaviva.backend.ui "Consulta el progreso del estudiante" "HTTPS"
        coordinador -> aulaviva.backend.progress "Consulta el progreso de estudiantes de su colegio" "HTTPS / REST"

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
        aulaviva.backend.tutor -> aulaviva.backend.corpus "Busca los 3 fragmentos más relevantes del curso autorizado" "BM25"
        aulaviva.backend.tutor -> llm "Envía pregunta sin PII + fragmentos; timeout 8 s" "HTTPS / API OpenAI-compatible"

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
            include coordinador
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
