### Tecnologías tentativas por contenedor

| Contenedor | Tecnología tentativa | Justificación breve |
|---|---|---|
| *Aplicación Web* | React + TypeScript | Permite construir una interfaz web modular y mantenible. |
| *Backend AulaViva* | Java + Spring Boot | Adecuado para una API robusta, seguridad, RBAC y arquitectura modular. |
| *Base de Datos* | PostgreSQL | Soporta datos relacionales y permite implementar aislamiento por tenant_id y RLS. |
| *Base de Datos Vectorial* | PostgreSQL + pgvector | Permite almacenar y consultar embeddings para el sistema RAG. |
| **Almacenamiento de Objetos** | MinIO / S3-compatible | Permite almacenar documentos, archivos educativos y otros objetos de forma independiente de la base de datos relacional. |
| *Message Broker* | RabbitMQ / Apache Kafka | Permite ejecutar tareas asíncronas y desacoplar procesos. |
| *Servicio de Identidad* | Keycloak + OAuth 2.0/OIDC | Permite gestionar autenticación, roles y federación de identidad. |
| *Servicio de IA / LLM* | API de LLM + Python/FastAPI | Facilita integrar modelos de lenguaje y desarrollar la lógica del tutor IA/RAG. |
| *MINEDUC* | API o fuente oficial disponible | Permite obtener y mantener actualizado el contexto curricular oficial. |
