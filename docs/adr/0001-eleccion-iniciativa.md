# ADR-0001: Selección de iniciativa del equipo

**Contexto**
El equipo debe seleccionar una de las tres iniciativas propuestas para desarrollar durante el proyecto:

* **MediTriage** — Triage clínico asistido por IA.
* **CrediScore** — Scoring crediticio y detección de fraude.
* **AulaViva** — Plataforma SaaS multi-tenant con tutor IA.

**Decisión**
El equipo selecciona desarrollar **AulaViva**, una plataforma educativa SaaS para colegios, orientada a mejorar la experiencia de aprendizaje de estudiantes y docentes y facilitar el seguimiento académico por parte de los apoderados.

La iniciativa fue seleccionada porque aborda una necesidad concreta del entorno educativo: centralizar contenidos, evaluaciones y apoyo al aprendizaje en una plataforma adaptada al contexto curricular chileno.

Además, el modelo SaaS multi-tenant permite que la solución pueda ser utilizada por distintos colegios, manteniendo la separación de su información y permitiendo personalización por institución.

La propuesta también presenta una oportunidad de diferenciación al incorporar un tutor IA que responde considerando los contenidos del curso y el contexto curricular del MINEDUC, en lugar de funcionar como un chatbot educativo genérico.

**Alternativas consideradas**

| Iniciativa | Valor para el usuario | Riesgo regulatorio y de datos | Complejidad técnica para el curso | Uso de IA | Veredicto |
|---|---|---|---|---|---|
| MediTriage | Alto (apoyo a triage clínico) | Muy alto: datos de salud y decisiones clínicas con impacto directo en pacientes | Alta: requiere validación médica que el equipo no puede obtener | Clasificación de urgencia | Descartada |
| CrediScore | Medio-alto (scoring y fraude) | Alto: datos financieros, sesgo y explicabilidad exigidos para decisiones de crédito | Alta: necesita datos históricos reales para entrenar y validar | Modelos de scoring | Descartada |
| AulaViva | Alto (estudiantes, docentes, apoderados y colegios) | Medio: datos de menores, mitigables con aislamiento por colegio y minimización | Media: CRUD + multi-tenant + tutor RAG acotado al currículo | Tutor con RAG sobre apuntes del curso | **Elegida** |

> Tabla agregada el 18-11-2026 para dejar explícitas las alternativas; los criterios resumen la discusión original del equipo y deben ser revisados por el equipo.

**Consecuencias**

**Positivas:**

* Existe un problema y usuarios objetivo claramente definidos.
* La propuesta puede entregar valor a múltiples actores del sistema educativo.
* El modelo SaaS permite proyectar la solución a múltiples colegios.
* El MVP puede validarse progresivamente con usuarios reales.
* Existe potencial de evolución hacia funcionalidades de analítica, personalización y apoyo docente.

**Riesgos:**

* La adopción dependerá de la disposición de colegios y docentes a incorporar una nueva plataforma.
* El tratamiento de datos de menores exige altos estándares de privacidad y seguridad.
* La calidad del tutor IA será crítica para la confianza del producto.
* Los costos variables asociados al uso de IA deben ser controlados.

**Fecha**
21 de agosto de 2026

**Autores**

1. Branco Merino — Product Owner
2. Matías Díaz — Tech Lead
3. Valentina Leon — DevSecOps Lead
4. Gerardo González — AI/Data Lead
5. Alexander Ruiz-Tagle — QA Lead

**Estado**
Aceptado
