# Uso de IA en la sesión S04

**Responsable:** Alexander Ruiz-Tagle — QA Lead

Durante la S04 se utilizó ChatGPT como apoyo para:

- Auditar los factores X (Dev/Prod Parity) y XII (Admin Processes) del manifiesto 12-Factor contra la arquitectura real de AulaViva (SaaS multi-tenant, monolito modular Spring Boot, PostgreSQL + pgvector, MSK, Tutor IA/RAG con LLM externo, AWS como cloud provider).
- Identificar acciones concretas para cada factor no cumplido, considerando multi-tenancy, currículo MINEDUC y protección de datos de menores.
- Redactar borradores para el ADR 0003 (sección QA) y para `managed-services.md` (justificación desde pruebas).
- Revisar coherencia con los NFR priorizados (seguridad/privacidad, escalabilidad, fiabilidad del tutor IA) y con el ADR 0002 (Monolito Modular → Microservicios).

La IA se usó como fuente de información y generación de borradores. Las decisiones finales y la adaptación al contexto de AulaViva fueron validadas por el responsable y el equipo.
