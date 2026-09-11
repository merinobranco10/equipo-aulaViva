# Uso de herramientas de IA — AulaViva
Se utilizó herramienta de IA (Claude) como apoyo durante la construcción del backlog inicial,
en las siguientes etapas:

## Arquitectura multi-tenant
- Consulta de alternativas de arquitectura para el aislamiento de datos
  (base de datos separada por tenant, schema separado, row-level security,
  aislamiento a nivel de aplicación).

## Priorización MoSCoW
- Apoyo para justificar la clasificación de cada historia, distinguiendo
  entre "lo más visible para el negocio" (tutor IA) y "lo más urgente para
  que el producto sea viable" (multi-tenancy y RBAC).

## Alcance de la revisión humana
El contenido de este backlog fue definido y redactado inicialmente por el equipo, a partir del conocimiento del contexto real del proyecto. La IA se utilizó posteriormente como apoyo para:
Pulir la redacción final de los documentos, buscando claridad y un tono profesional para que sean comprensibles por todo el equipo (técnico y no técnico).

Todo el contenido de este backlog (impact map, historias, escenarios y priorización) fue revisado y verificado por el equipo antes de su inclusión final en el PR.

## Aporte AI/Data — S04 (12-Factor y estilo cloud)

Se utilizó IA generativa como apoyo para:

- Redactar el análisis de los factores 2 (Dependencies) y 4 (Backing services)
  del checklist 12-Factor, enfocados en los componentes de IA del sistema
  (Base de Datos Vectorial y Servicio de IA/LLM externo).
- Proponer alternativas de servicios gestionados para pgvector y para el LLM,
  comparando ventajas y riesgos de cada una.

El contenido fue revisado por el responsable de AI/Data antes de subirlo, y
se ajustó a los contenedores reales definidos en el C4 L2 del proyecto.


---

## Aporte QA — Alexander Ruiz-Tagle

**Fecha:** 10 de septiembre de 2026
**Rol:** QA Lead

**Resumen del uso de IA:**

Durante la S04 se utilizó ChatGPT como herramienta de apoyo para auditar los factores X (Dev/Prod Parity) y XII (Admin Processes) del manifiesto 12-Factor contra la arquitectura real de AulaViva. Se usó para identificar acciones concretas para cada factor no cumplido, redactar el borrador del aporte QA al ADR 0003 y revisar coherencia con los NFR priorizados (seguridad/privacidad, escalabilidad y fiabilidad del Tutor IA/RAG) y con las decisiones del ADR 0002.

La IA se empleó únicamente como fuente de información y generación de borradores. Las decisiones finales, la adaptación al contexto de AulaViva y la validación técnica fueron realizadas por el responsable.

**Firma:**
Alexander Ruiz-Tagle — QA Lead
