// Actualizado 18-11-2026 (estado del MVP): LLM = Groq (ADR 0009); identidad OIDC y sostenedor diferidos
// (ADR 0008); MINEDUC solo como referencia curricular, sin integración.
workspace "AulaViva" "Arquitectura C4 de AulaViva" {

    model {

        estudiante = person "Estudiante" "Utiliza AulaViva para acceder a contenidos, realizar evaluaciones y utilizar el tutor IA."
        profesor = person "Profesor" "Gestiona contenidos, cursos y evaluaciones de sus estudiantes."
        coordinador = person "Coordinador Académico" "Supervisa cursos, contenidos, evaluaciones y progreso académico."
        apoderado = person "Apoderado" "Consulta el progreso académico del estudiante."
        sostenedor = person "Sostenedor" "Supervisará varios establecimientos. Sin vista ni rol en el MVP." "Diferido"

        aulaViva = softwareSystem "AulaViva" "Plataforma SaaS educativa multi-tenant que gestiona contenidos, evaluaciones, progreso y un tutor IA basado en RAG."

        mineduc = softwareSystem "MINEDUC (curriculumnacional.cl)" "Referencia del currículo oficial chileno: los OA de los apuntes se toman de sus programas de estudio. Sin integración por API." "External"
        ia = softwareSystem "Proveedor LLM (Groq)" "API compatible con OpenAI. Modelo openai/gpt-oss-120b (respaldo openai/gpt-oss-20b). Recibe solo la pregunta sin datos personales y fragmentos de apuntes." "External"
        identidad = softwareSystem "Proveedor de identidad OIDC" "Objetivo de producción (ADR 0008). En el MVP la autenticación es HTTP Basic dentro de AulaViva." "External,Diferido"

        estudiante -> aulaViva "Rinde evaluaciones, revisa su nota y usa el tutor IA"
        profesor -> aulaViva "Gestiona cursos y evaluaciones"
        coordinador -> aulaViva "Consulta el progreso de los estudiantes de su colegio"
        apoderado -> aulaViva "Consulta el progreso"
        sostenedor -> aulaViva "Supervisará la plataforma (futuro)" {
            tags "Diferido"
        }

        aulaViva -> mineduc "Los apuntes se alinean a sus OA (referencia, sin API)" {
            tags "Referencia"
        }
        aulaViva -> ia "Envía pregunta + 3 fragmentos y recibe la respuesta del tutor" "HTTPS"
        aulaViva -> identidad "Autenticará usuarios con OIDC (futuro)" {
            tags "Diferido"
        }
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

            element "Diferido" {
                background #dddddd
                color #555555
                border dashed
            }

            relationship "Diferido" {
                style dashed
                color #999999
            }

            relationship "Referencia" {
                style dotted
            }
        }
    }
}
