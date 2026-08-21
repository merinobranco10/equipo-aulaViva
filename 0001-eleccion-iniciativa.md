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
