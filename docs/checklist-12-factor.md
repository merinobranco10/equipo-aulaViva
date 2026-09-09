## Factor 2 — Dependencies (Dependencias)

Desde el componente de IA (AI/Data), las dependencias identificadas son:

- **Base de Datos Vectorial (pgvector)**: extensión de PostgreSQL usada para
  almacenar y consultar embeddings. Se declara como dependencia explícita
  en el entorno (no se asume instalada en el sistema base).
- **Servicio de IA/LLM externo**: SDK o cliente HTTP usado para conectarse
  al proveedor del modelo de lenguaje (ej. API de Claude/OpenAI).
- Todas las dependencias se gestionan mediante archivos de manejo de
  paquetes (ej. requirements.txt / package.json), evitando dependencias
  implícitas del sistema operativo.

## Factor 4 — Backing Services (Servicios de respaldo)

Los componentes de IA se tratan como recursos externos adjuntos (attached
resources), no como parte fija del código:

- **Base de Datos Vectorial**: se accede mediante una URL de conexión
  configurable (ver Factor 3 - Config), permitiendo intercambiarla por
  otra instancia sin cambiar el código.
- **Servicio de IA/LLM externo**: se consume vía API, con su endpoint y
  credenciales también configurables por variables de entorno.
- Esto permite reemplazar el proveedor del LLM o de la base vectorial sin
  modificar el código de la aplicación.
