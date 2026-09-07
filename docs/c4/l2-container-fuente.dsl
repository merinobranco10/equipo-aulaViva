workspace "AulaViva" "Arquitectura C4 de AulaViva" {

    model {

        estudiante = person "Estudiante" "Utiliza AulaViva para acceder a contenidos, realizar evaluaciones y utilizar el tutor IA."
        profesor = person "Profesor" "Gestiona contenidos, cursos y evaluaciones."
        coordinador = person "Coordinador Académico" "Supervisa la actividad académica."
        apoderado = person "Apoderado" "Consulta el progreso académico del estudiante."
        sostenedor = person "Sostenedor" "Supervisa la gestión de los establecimientos."

        mineduc = softwareSystem "MINEDUC" "Proporciona información del currículo oficial chileno." "External"
        ia = softwareSystem "Servicio de IA / LLM" "Proporciona capacidades de procesamiento de lenguaje para el tutor IA." "External"
        identidad = softwareSystem "Servicio de Identidad" "Gestiona la autenticación de usuarios." "External"

        aulaViva = softwareSystem "AulaViva" "Plataforma SaaS educativa multi-tenant." {

            web = container "Aplicación Web" "Interfaz utilizada por los usuarios." "React / TypeScript"
            backend = container "Backend AulaViva" "Monolito modular con la lógica de negocio." "Java / Spring Boot"
            database = container "Base de Datos" "Almacena usuarios, tenants, cursos, contenidos, evaluaciones y progreso." "PostgreSQL"
            vectorDB = container "Base de Datos Vectorial" "Almacena embeddings para el sistema RAG." "PostgreSQL + pgvector"
            storage = container "Almacenamiento de Objetos" "Almacena documentos y archivos educativos." "Object Storage"
            broker = container "Message Broker" "Permite procesamiento asíncrono." "Kafka / RabbitMQ"
        }

        estudiante -> web "Utiliza"
        profesor -> web "Utiliza"
        coordinador -> web "Utiliza"
        apoderado -> web "Utiliza"
        sostenedor -> web "Utiliza"

        web -> backend "Realiza solicitudes" "HTTPS/REST"

        backend -> database "Lee y escribe datos"
        backend -> vectorDB "Consulta embeddings"
        backend -> storage "Gestiona archivos"
        backend -> broker "Publica y consume eventos"

        backend -> identidad "Autentica usuarios"
        backend -> mineduc "Consulta currículo oficial"
        backend -> ia "Solicita generación de respuestas"
    }

    views {

        container aulaViva "C4-Nivel2" {
            include *
            autolayout lr
            title "AulaViva - C4 Nivel 2: Contenedores"
        }

        styles {

            element "Person" {
                shape person
            }

            element "Software System" {
                shape roundedbox
            }

            element "Container" {
                shape roundedbox
            }

            element "External" {
                background #999999
                color #ffffff
            }
        }
    }
}
