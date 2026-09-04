workspace "AulaViva" "Arquitectura C4 de AulaViva" {

    model {

        estudiante = person "Estudiante" "Utiliza AulaViva para acceder a contenidos, realizar evaluaciones y utilizar el tutor IA."
        profesor = person "Profesor" "Gestiona contenidos, cursos y evaluaciones de sus estudiantes."
        coordinador = person "Coordinador Académico" "Supervisa cursos, contenidos, evaluaciones y progreso académico."
        apoderado = person "Apoderado" "Consulta el progreso académico del estudiante."
        sostenedor = person "Sostenedor" "Supervisa la información académica y gestión de los establecimientos."

        aulaViva = softwareSystem "AulaViva" "Plataforma SaaS educativa multi-tenant que gestiona contenidos, evaluaciones, progreso y un tutor IA basado en RAG."

        mineduc = softwareSystem "MINEDUC" "Proporciona información y referencias del currículo oficial chileno." "External"
        ia = softwareSystem "Servicio de IA / LLM" "Proporciona capacidades de procesamiento de lenguaje para el tutor IA." "External"
        identidad = softwareSystem "Servicio de Identidad" "Gestiona autenticación y servicios de identidad de los usuarios." "External"

        estudiante -> aulaViva "Utiliza"
        profesor -> aulaViva "Gestiona cursos y evaluaciones"
        coordinador -> aulaViva "Supervisa la actividad académica"
        apoderado -> aulaViva "Consulta el progreso"
        sostenedor -> aulaViva "Supervisa la plataforma"

        aulaViva -> mineduc "Consulta currículo oficial"
        aulaViva -> ia "Solicita generación de respuestas"
        aulaViva -> identidad "Autentica usuarios"
    }

    views {

        systemContext aulaViva "C4-Nivel1" {
            include *
            autolayout lr
            title "AulaViva - C4 Nivel 1: Contexto del Sistema"
        }

        styles {
            element "Person" {
                shape person
            }

            element "Software System" {
                shape roundedbox
            }

            element "External" {
                background #999999
                color #ffffff
            }
        }
    }
}
