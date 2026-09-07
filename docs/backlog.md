# Backlog inicial — AulaViva

## Trazabilidad Goal → Historia
Todas las historias derivan directamente de un deliverable del
`impact-map.md`, que a su vez apunta al goal: modernizar el aprendizaje con
evaluación automática y tutor IA curricular multi-tenant.

---

## US-01 · Aislamiento multi-tenant
**Como** Sostenedor
**Quiero** que cada colegio opere en un tenant aislado lógicamente
**Para** garantizar que los datos y contenidos de un colegio nunca sean
accesibles por otro colegio

| INVEST | Cumple | Justificación |
|---|---|---|
| Independent | ✅ | No depende de RBAC ni de otras historias para implementarse a nivel de infraestructura. |
| Negotiable | ✅ | El mecanismo (schema-per-tenant, row-level security, etc.) se decide en diseño técnico. |
| Valuable | ✅ | Sin esto no existe producto multi-tenant vendible. |
| Estimable | ✅ | Alcance acotado: aislamiento de datos + pruebas de fuga entre tenants. |
| Small | ✅ | Entra en un sprint; no incluye UI de administración. |
| Testable | ✅ | Verificable con casos de acceso cruzado entre tenants. |

## US-02 · Gestión de cursos, docentes y estudiantes con RBAC
**Como** Coordinador académico
**Quiero** gestionar cursos, docentes y estudiantes con roles y permisos
definidos
**Para** asegurar que cada usuario solo acceda a la información y funciones
que le corresponden

| INVEST | Cumple | Justificación |
|---|---|---|
| Independent | ✅ | Depende solo de que exista el tenant (US-01). |
| Negotiable | ✅ | El set exacto de roles se puede ajustar con el equipo. |
| Valuable | ✅ | Habilita el control de acceso base para todo lo demás. |
| Estimable | ✅ | CRUD + matriz de permisos. |
| Small | ✅ | Se limita a la gestión y permisos, sin reportes avanzados. |
| Testable | ✅ | Casos de acceso permitido/denegado por rol. |

## US-03 · Evaluaciones auto-corregidas con feedback
**Como** Docente
**Quiero** que las evaluaciones se corrijan automáticamente y entreguen
feedback a los estudiantes
**Para** reducir el tiempo de corrección manual y dar retroalimentación
inmediata

| INVEST | Cumple | Justificación |
|---|---|---|
| Independent | ✅ | Requiere RBAC (US-02) para saber quién es docente/estudiante. |
| Negotiable | ✅ | El formato de feedback (breve/detallado) se define con el docente piloto. |
| Valuable | ✅ | Impacto medible: horas de corrección ahorradas por docente. |
| Estimable | ✅ | Motor de corrección de preguntas cerradas, alcance claro. |
| Small | ✅ | Limitado a preguntas auto-corregibles (no ensayo abierto). |
| Testable | ✅ | Casos feliz/incompleto/fuera de plazo (ver Gherkin). |

## US-04 · Tutor IA con RAG sobre apuntes del curso
**Como** Estudiante
**Quiero** consultar un tutor IA que responda basado en los apuntes de mi
curso
**Para** resolver mis dudas dentro del contexto del currículum vigente

| INVEST | Cumple | Justificación |
|---|---|---|
| Independent | ✅ | Requiere que existan cursos y apuntes (US-02). |
| Negotiable | ✅ | El proveedor de LLM y estrategia de chunking/RAG son detalles de implementación. |
| Valuable | ✅ | Es el diferenciador principal del producto. |
| Estimable | ✅ | Alcance: ingestión de apuntes + pipeline RAG + chat. |
| Small | ⚠️ | Es la historia más grande del set; podría dividirse en "ingestión de apuntes" y "chat con RAG" si excede el sprint. |
| Testable | ✅ | Casos de pregunta dentro/fuera de contexto y caída del servicio. |

## US-05 · Panel del apoderado con seguimiento de avance
**Como** Apoderado
**Quiero** ver un panel con el avance académico de mi hijo/a
**Para** poder acompañar su proceso de aprendizaje

| INVEST | Cumple | Justificación |
|---|---|---|
| Independent | ✅ | Solo requiere que existan calificaciones (US-03) para tener datos que mostrar. |
| Negotiable | ✅ | El nivel de detalle del panel (solo notas vs. detalle por pregunta) es negociable. |
| Valuable | ✅ | Cumple el requisito de negocio de involucrar al apoderado, y el consentimiento parental. |
| Estimable | ✅ | Vista de solo lectura sobre datos ya existentes. |
| Small | ✅ | No incluye mensajería ni notificaciones, solo consulta. |
| Testable | ✅ | Casos de múltiples hijos y falta de consentimiento (ver Gherkin). |

---

## Priorización MoSCoW (justificada)

| Historia | Prioridad | Justificación |
|---|---|---|
| US-01 Multi-tenant | **Must** | Bloqueante arquitectónico: sin aislamiento por tenant, ningún otro feature es viable ni seguro de lanzar. |
| US-02 RBAC | **Must** | Prerrequisito de seguridad para todas las demás historias (evaluaciones, tutor IA y panel dependen de saber quién es quién). |
| US-03 Evaluaciones auto-corregidas | **Must** | Es el dolor #1 declarado por el docente en el contexto de negocio (reducir corrección manual); genera valor medible desde el día 1. |
| US-04 Tutor IA con RAG | **Should** | Es el diferenciador del producto, pero depende de US-01/US-02 ya construidas y tiene mayor riesgo técnico (RAG + costos LLM); puede iterar una versión después del primer release. |
| US-05 Panel del apoderado | **Could** | Aporta valor pero no bloquea el uso core de docentes/estudiantes; puede lanzarse en una iteración posterior sin frenar el MVP. |
