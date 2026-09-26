workspace "AulaViva" "Arquitectura C4 de AulaViva" {
    
    model {

        estudiante = person "Estudiante" "Utiliza AulaViva para acceder a contenidos, realizar evaluaciones y utilizar el tutor IA."
        
        profesor = person "Profesor" "Gestiona contenidos, cursos y evaluaciones."
        
        coordinador = person "Coordinador Académico" "Supervisa la actividad académica."
        
        apoderado = person "Apoderado" "Consulta el progreso académico del estudiante."
        
        sostenedor = person "Sostenedor" "Supervisa la gestión de los establecimientos."

        mineduc = softwareSystem "Sitio Web MINEDUC" "Sitio web externo utilizado como fuente de información y referencias del currículo oficial chileno." "External"

        ia = softwareSystem "Servicio de IA / LLM (Anthropic Claude)" "Servicio gestionado utilizado por el Tutor IA para generar respuestas a partir del contexto recuperado mediante RAG." "External"

        identidad = softwareSystem "Servicio de Identidad (Amazon Cognito)" "Servicio gestionado de AWS que gestiona la autenticación e identidad de los usuarios de AulaViva." "External"

        aulaViva = softwareSystem "AulaViva" "Plataforma SaaS educativa multi-tenant que gestiona contenidos, evaluaciones, progreso académico y un Tutor IA basado en RAG." {

            web = container "Aplicación Web" "Interfaz utilizada por estudiantes, profesores, coordinadores, apoderados y sostenedores." "React / TypeScript"

            backend = container "Backend AulaViva" "Monolito modular que implementa la lógica de negocio, gestión multi-tenant y coordinación con servicios externos." "Java / Spring Boot"

            database = container "Base de Datos" "Almacena usuarios, tenants, cursos, contenidos, evaluaciones y progreso académico, manteniendo el aislamiento de información entre colegios." "Amazon RDS for PostgreSQL"

            vectorDB = container "Base de Datos Vectorial" "Almacena embeddings del contenido curricular y permite búsquedas vectoriales utilizadas por el sistema RAG." "Amazon RDS for PostgreSQL + pgvector"

            storage = container "Almacenamiento de Objetos" "Almacena documentos, archivos y contenidos educativos utilizados por AulaViva." "Amazon S3"

            broker = container "Message Broker" "Permite procesamiento asíncrono y comunicación mediante eventos entre los componentes de AulaViva." "Amazon MSK (Apache Kafka)"
        }

        estudiante -> web "Accede a contenidos, evaluaciones y Tutor IA"

        profesor -> web "Gestiona cursos, contenidos y evaluaciones"

        coordinador -> web "Supervisa la actividad académica"

        apoderado -> web "Consulta el progreso académico"

        sostenedor -> web "Supervisa la gestión de los establecimientos"

        web -> backend "Realiza solicitudes" "HTTPS/REST"

        backend -> database "Lee y escribe información académica y de tenants" "PostgreSQL"

        backend -> vectorDB "Consulta embeddings y recupera contexto para RAG" "PostgreSQL / pgvector"

        backend -> storage "Almacena y recupera documentos educativos" "Amazon S3"

        backend -> broker "Publica y consume eventos asíncronos" "Apache Kafka"

        backend -> identidad "Autentica usuarios y gestiona identidad" "HTTPS"

        backend -> mineduc "Consulta información y referencias del currículo oficial" "HTTPS"

        backend -> ia "Envía contexto recuperado por RAG y solicita generación de respuestas" "HTTPS/API"
    }

    views {

        container aulaViva "C4-Nivel2" {
            include *
            autolayout lr
            title "AulaViva - C4 Nivel 2: Contenedores y Servicios Gestionados"
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
