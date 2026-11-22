// Actualizado 18-11-2026 (estado del MVP) según ADR 0004 (Outbox en tabla, Kafka diferido), ADR 0008 (HTTP Basic,
// OIDC futuro) y ADR 0009 (Groq + RAG léxico BM25). Los elementos "Diferido" son objetivo de
// producción y no existen en el MVP.
workspace "AulaViva" "Arquitectura C4 Nivel 2 - Contenedores" {

    !identifiers hierarchical
    !impliedRelationships false

    model {

        // =====================================================
        // PERSONAS
        // =====================================================

        estudiante = person "Estudiante" {
            description "Utiliza AulaViva para acceder a contenidos, realizar evaluaciones, consultar su progreso y utilizar el Tutor IA."
        }

        profesor = person "Profesor" {
            description "Gestiona cursos, contenidos y evaluaciones, y realiza seguimiento del progreso académico de sus estudiantes."
        }

        coordinador = person "Coordinador Académico" {
            description "Supervisa la actividad académica, las evaluaciones y el progreso de los estudiantes."
        }

        apoderado = person "Apoderado" {
            description "Consulta el progreso académico y los resultados del estudiante asociado."
        }

        sostenedor = person "Sostenedor" {
            description "Supervisará varios establecimientos. Sin vista ni rol en el MVP."
            tags "Diferido"
        }


        // =====================================================
        // SISTEMAS EXTERNOS
        // =====================================================

        mineduc = softwareSystem "MINEDUC (curriculumnacional.cl)" {
            description "Referencia del currículo oficial: los OA de los apuntes se toman de sus programas de estudio. Sin integración por API."
            tags "External"
        }

        llm = softwareSystem "Proveedor LLM (Groq)" {
            description "API compatible con OpenAI. Modelo openai/gpt-oss-120b, respaldo openai/gpt-oss-20b. Recibe solo la pregunta sin datos personales y los fragmentos recuperados."
            tags "External"
        }

        cognito = softwareSystem "Proveedor de identidad OIDC (Cognito / Keycloak)" {
            description "Objetivo de producción (ADR 0008). En el MVP la autenticación es HTTP Basic con usuarios en la base de datos."
            tags "External" "Diferido"
        }


        // =====================================================
        // SISTEMA AULAVIVA
        // =====================================================

        aulaViva = softwareSystem "AulaViva" {
            description "Plataforma SaaS educativa multi-tenant para la gestión académica, evaluaciones, seguimiento del progreso y asistencia mediante Tutor IA."

            web = container "Interfaz web" {
                technology "HTML / CSS / JavaScript estático"
                description "Páginas por rol (docente, estudiante, apoderado) servidas por el mismo backend desde /static. Sin framework ni build aparte; protegidas con CSP estricta."
            }

            backend = container "Backend AulaViva" {
                technology "Java / Spring Boot"
                description "Monolito modular: lógica de negocio, HTTP Basic + RBAC, aislamiento por tenant_id, evaluaciones, Outbox y Tutor IA (BM25 + guardrails + fallback). Incluye los apuntes del curso en el classpath."
            }

            db = container "Base de datos relacional" {
                technology "PostgreSQL 16 (Amazon RDS en producción) / H2 en local"
                description "Datos académicos, evaluaciones, notas, usuarios y la tabla outbox_events (Transactional Outbox). Toda tabla de negocio lleva tenant_id."
                tags "Database"
            }

            vectorDb = container "PostgreSQL + pgvector" {
                technology "Amazon RDS for PostgreSQL + pgvector"
                description "Diferido (ADR 0009): solo si el corpus crece y BM25 deja de alcanzar."
                tags "Database" "Diferido"
            }

            storage = container "Amazon S3" {
                technology "Amazon S3"
                description "Diferido: documentos subidos por docentes. En el MVP los apuntes van en el classpath."
                tags "Storage" "Diferido"
            }

            broker = container "Amazon MSK (Apache Kafka)" {
                technology "Amazon MSK / Apache Kafka"
                description "Diferido (ADR 0004): se incorpora cuando exista más de un consumidor real. Hoy los eventos quedan en la tabla outbox_events."
                tags "Broker" "Diferido"
            }
        }


        // =====================================================
        // RELACIONES DE LOS USUARIOS
        // =====================================================

        estudiante -> aulaViva.web "Rinde evaluaciones, revisa su nota y entrega corregida, y usa el Tutor IA" "HTTPS"

        profesor -> aulaViva.web "Crea evaluaciones y revisa el avance de sus cursos" "HTTPS"

        coordinador -> aulaViva.backend "Consulta el progreso de estudiantes de su colegio (API; sin vista web en el MVP)" "HTTPS/REST"

        apoderado -> aulaViva.web "Consulta el progreso y las notas de su pupilo" "HTTPS"

        sostenedor -> aulaViva.web "Supervisará los establecimientos (futuro)" "HTTPS" {
            tags "Diferido"
        }


        // =====================================================
        // APLICACIÓN
        // =====================================================

        aulaViva.web -> aulaViva.backend "Llama a la API /v1 con HTTP Basic (mismo origen)" "HTTPS/REST + JSON"


        // =====================================================
        // PERSISTENCIA
        // =====================================================

        aulaViva.backend -> aulaViva.db "Lee y escribe datos académicos y guarda eventos en outbox_events en la misma transacción" "JDBC"

        aulaViva.backend -> aulaViva.vectorDb "Búsqueda semántica (futuro)" "PostgreSQL/pgvector" {
            tags "Diferido"
        }

        aulaViva.backend -> aulaViva.storage "Guarda documentos subidos (futuro)" "Amazon S3 API" {
            tags "Diferido"
        }


        // =====================================================
        // EVENTOS
        // =====================================================

        aulaViva.backend -> aulaViva.broker "Publicará los eventos del outbox (futuro)" "Apache Kafka" {
            tags "Diferido"
        }


        // =====================================================
        // SERVICIOS EXTERNOS
        // =====================================================

        aulaViva.backend -> cognito "Validará tokens OIDC (futuro)" "HTTPS" {
            tags "Diferido"
        }

        aulaViva.backend -> llm "Envía pregunta sin PII + 3 fragmentos; timeout 8 s, fallback extractivo" "HTTPS / API OpenAI-compatible"

        aulaViva.backend -> mineduc "Los apuntes se alinean a sus OA (referencia, sin API)" {
            tags "Referencia"
        }
    }


    // =========================================================
    // VISTA C4 NIVEL 2
    // =========================================================

    views {

        container aulaViva "AulaViva-C4-L2" {

            include estudiante
            include profesor
            include coordinador
            include apoderado
            include sostenedor

            include aulaViva.web
            include aulaViva.backend

            include aulaViva.db
            include aulaViva.vectorDb
            include aulaViva.storage
            include aulaViva.broker

            include mineduc
            include llm
            include cognito

            autoLayout tb 300 300

            title "AulaViva - C4 Nivel 2 - Diagrama de Contenedores"
        }


        // =====================================================
        // ESTILOS
        // =====================================================

        styles {

            element "Person" {
                shape Person
            }

            element "Software System" {
                background "#1168bd"
                color "#ffffff"
            }

            element "Container" {
                background "#438dd5"
                color "#ffffff"
            }

            element "Database" {
                shape Cylinder
                background "#438dd5"
                color "#ffffff"
            }

            element "Storage" {
                shape Folder
                background "#438dd5"
                color "#ffffff"
            }

            element "Broker" {
                shape Hexagon
                background "#438dd5"
                color "#ffffff"
            }

            element "External" {
                background "#999999"
                color "#ffffff"
            }

            element "Diferido" {
                background "#dddddd"
                color "#555555"
                border dashed
            }

            relationship "Diferido" {
                style dashed
                color "#999999"
            }

            relationship "Referencia" {
                style dotted
            }
        }
    }
}
