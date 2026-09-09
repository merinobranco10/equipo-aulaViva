## Alternativas de servicios gestionados — Componentes de IA

**Para la Base de Datos Vectorial (pgvector):**
- Opción A: PostgreSQL gestionado con extensión pgvector (ej. Amazon RDS,
  Supabase). Ventaja: control total del esquema y costo predecible.
  Riesgo: mantenimiento propio de la configuración de pgvector.
- Opción B: Servicio de vector DB dedicado (ej. Pinecone). Ventaja: escala
  automáticamente y menos mantenimiento. Riesgo: mayor costo y menor
  control sobre los datos.

**Para el LLM:**
- Opción A: API gestionada de un proveedor (ej. Anthropic, OpenAI).
  Ventaja: cero mantenimiento de infraestructura. Riesgo: costo variable
  y dependencia de disponibilidad externa.
- Opción B: Modelo autoalojado. Ventaja: más control y privacidad de
  datos. Riesgo: requiere infraestructura propia (GPU) y mantenimiento.
