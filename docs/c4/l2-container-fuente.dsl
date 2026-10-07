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
            description "Supervisa la gestión general de los establecimientos asociados a la plataforma."
        }


        // =====================================================
        // SISTEMAS EXTERNOS
        // =====================================================

        mineduc = softwareSystem "Sitio Web MINEDUC" {
            description "Fuente externa de información y referencias del currículo oficial chileno utilizadas por AulaViva."
            tags "External"
        }

        claude = softwareSystem "Servicio de IA / LLM (Anthropic Claude)" {
            description "Servicio externo utilizado por el Tutor IA para generar respuestas a partir del contexto académico recuperado mediante RAG."
            tags "External"
        }

        cognito = softwareSystem "Servicio de Identidad (Amazon Cognito)" {
            description "Servicio gestionado de AWS utilizado para autenticar y gestionar la identidad de los usuarios de AulaViva."
            tags "External"
        }


        // =====================================================
        // SISTEMA AULAVIVA
        // =====================================================

        aulaViva = softwareSystem "AulaViva" {
            description "Plataforma SaaS educativa multi-tenant para la gestión académica, evaluaciones, seguimiento del progreso y asistencia mediante Tutor IA."

            web = container "Aplicación Web" {
                technology "React / TypeScript"
                description "Interfaz web utilizada por estudiantes, profesores, coordinadores académicos, apoderados y sostenedores para acceder a las funcionalidades de AulaViva."
            }

            backend = container "Backend AulaViva" {
                technology "Java / Spring Boot"
                description "Monolito modular que implementa la lógica de negocio, autorización multi-tenant, gestión académica, evaluaciones, Tutor IA y coordinación con servicios externos."
            }

            db = container "Amazon RDS for PostgreSQL" {
                technology "PostgreSQL"
                description "Persistencia relacional para Gestión Académica, Evaluaciones y Progreso, y datos propios de autorización de AulaViva."
                tags "Database"
            }

            vectorDb = container "PostgreSQL + pgvector" {
                technology "Amazon RDS for PostgreSQL + pgvector"
                description "Almacena metadatos, fragmentos y embeddings utilizados para recuperar contexto semántico en el proceso RAG del Tutor IA."
                tags "Database"
            }

            storage = container "Amazon S3" {
                technology "Amazon S3"
                description "Almacena documentos originales, archivos y contenidos educativos utilizados por AulaViva y el Tutor IA."
                tags "Storage"
            }

            broker = container "Amazon MSK (Apache Kafka)" {
                technology "Amazon MSK / Apache Kafka"
                description "Broker de eventos utilizado para la comunicación asíncrona y propagación de eventos de dominio entre los módulos de AulaViva."
                tags "Broker"
            }
        }


        // =====================================================
        // RELACIONES DE LOS USUARIOS
        // =====================================================

        estudiante -> aulaViva.web "Accede a contenidos, evaluaciones, progreso académico y Tutor IA" "HTTPS"

        profesor -> aulaViva.web "Gestiona cursos, contenidos, evaluaciones y seguimiento académico" "HTTPS"

        coordinador -> aulaViva.web "Supervisa la actividad académica, evaluaciones y progreso" "HTTPS"

        apoderado -> aulaViva.web "Consulta el progreso académico y resultados del estudiante" "HTTPS"

        sostenedor -> aulaViva.web "Supervisa la gestión de los establecimientos" "HTTPS"


        // =====================================================
        // APLICACIÓN
        // =====================================================

        aulaViva.web -> aulaViva.backend "Realiza solicitudes a la API de AulaViva" "HTTPS/REST"


        // =====================================================
        // PERSISTENCIA
        // =====================================================

        aulaViva.backend -> aulaViva.db "Lee y escribe datos académicos, evaluaciones y autorización" "PostgreSQL"

        aulaViva.backend -> aulaViva.vectorDb "Almacena embeddings y recupera contexto semántico para RAG" "PostgreSQL/pgvector"

        aulaViva.backend -> aulaViva.storage "Almacena y recupera documentos y contenidos educativos" "Amazon S3 API"


        // =====================================================
        // EVENTOS
        // =====================================================

        aulaViva.backend -> aulaViva.broker "Publica eventos de dominio" "Apache Kafka"

        aulaViva.broker -> aulaViva.backend "Entrega eventos de dominio a los consumidores internos" "Apache Kafka"


        // =====================================================
        // SERVICIOS EXTERNOS
        // =====================================================

        aulaViva.backend -> cognito "Solicita autenticación y validación de identidad" "HTTPS"

        aulaViva.backend -> claude "Envía contexto RAG y solicita generación de respuestas" "HTTPS/API"

        aulaViva.backend -> mineduc "Consulta información y referencias del currículo oficial" "HTTPS"
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
            include claude
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
        }
    }
}
