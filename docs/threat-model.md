# Modelo de amenazas — Tutor IA (STRIDE)

## 1. Objetivo y alcance

El objetivo de este documento es identificar y analizar las principales amenazas de seguridad asociadas al microservicio Tutor IA de AulaViva, utilizando la metodología STRIDE.

El análisis considera el procesamiento de consultas de estudiantes y docentes, la recuperación de información académica mediante Retrieval-Augmented Generation (RAG) y la comunicación con servicios externos de inteligencia artificial.

El modelo busca identificar los activos que deben protegerse, los límites de confianza, las posibles amenazas y los controles necesarios para reducir los riesgos.

**Alcance:** se considera el microservicio Tutor IA como un componente lógico independiente, aunque inicialmente pueda implementarse dentro del monolito modular de AulaViva.

## 2. Descripción del microservicio

El microservicio Tutor IA permite que estudiantes y docentes realicen consultas sobre contenidos educativos. Para responder, utiliza un mecanismo RAG que recupera información desde una base de conocimiento asociada al establecimiento, curso y contenidos curriculares.

El flujo general es:

1. El estudiante o docente envía una consulta desde la aplicación web.
2. El backend valida la identidad, los permisos y el contexto del usuario.
3. La consulta se envía al microservicio Tutor IA.
4. El microservicio consulta la base de datos vectorial para recuperar fragmentos relevantes.
5. Los fragmentos recuperados se utilizan como contexto para generar una respuesta mediante un proveedor externo de modelos de lenguaje (LLM).
6. La respuesta se devuelve al usuario.

### Componentes involucrados

| Componente | Responsabilidad |
|---|---|
| Aplicación web | Interfaz utilizada por estudiantes y docentes. |
| Backend AulaViva | Autenticación, autorización y comunicación con Tutor IA. |
| Microservicio Tutor IA | Orquestación de consultas, recuperación RAG y generación de respuestas. |
| PostgreSQL / pgvector | Almacenamiento de documentos, metadatos y vectores. |
| Proveedor LLM | Generación de respuestas a partir del contexto proporcionado. |
| Keycloak | Gestión de identidad y autenticación mediante OAuth2/OIDC. |

## 3. Activos que deben protegerse

| ID | Activo | Importancia |
|---|---|---|
| A1 | Datos personales de estudiantes y docentes | Contienen información asociada a menores de edad y usuarios del establecimiento. |
| A2 | Credenciales y tokens de autenticación | Permiten acceder a recursos y servicios de AulaViva. |
| A3 | Documentos y contenidos educativos | Constituyen la base de conocimiento utilizada por el tutor. |
| A4 | Vectores y metadatos almacenados en pgvector | Permiten recuperar información académica y deben respetar el aislamiento entre establecimientos. |
| A5 | Consultas y respuestas del Tutor IA | Pueden contener información personal, académica o confidencial. |
| A6 | Configuración y secretos del servicio | Incluyen claves de API, credenciales de base de datos y configuraciones internas. |
| A7 | Disponibilidad del servicio | Permite que estudiantes y docentes utilicen el tutor cuando lo necesiten. |

## 4. Límites de confianza

Se identifican los siguientes límites de confianza:

| ID | Límite | Descripción |
|---|---|---|
| LT1 | Usuario → Aplicación web | Las solicitudes provienen de usuarios externos y deben considerarse entradas no confiables. |
| LT2 | Backend → Tutor IA | La comunicación entre servicios requiere autenticación y autorización. |
| LT3 | Tutor IA → PostgreSQL/pgvector | El acceso debe respetar el aislamiento de datos entre tenants. |
| LT4 | Tutor IA → Proveedor LLM | Se transmiten consultas y contexto a un proveedor externo, por lo que deben controlarse los datos compartidos. |
| LT5 | Tutor IA → Configuración y secretos | El servicio debe acceder únicamente a los secretos necesarios para su funcionamiento. |

## 5. Análisis de amenazas STRIDE

Se utiliza STRIDE para clasificar las amenazas en seis categorías: suplantación de identidad, manipulación, repudio, divulgación de información, denegación de servicio y elevación de privilegios.

### 5.1. Suplantación de identidad (Spoofing)

**Amenaza:** un atacante podría hacerse pasar por un estudiante, docente o servicio autorizado para realizar consultas al Tutor IA y acceder a información que no le corresponde.

Ejemplos:
- Utilizar un token robado o manipulado.
- Enviar solicitudes directamente al microservicio sin pasar por los controles del backend.
- Suplantar la identidad de otro tenant.

**Controles propuestos:**
- Validar tokens JWT mediante Keycloak.
- Verificar la identidad y los permisos del usuario en cada solicitud.
- Autenticar las comunicaciones entre servicios.
- Rechazar solicitudes sin credenciales válidas.

### 5.2. Manipulación de datos (Tampering)

**Amenaza:** un atacante podría modificar consultas, documentos o metadatos para alterar el comportamiento del tutor o provocar que recupere información incorrecta.

Ejemplos:
- Modificar documentos educativos almacenados en la base de conocimiento.
- Alterar el identificador del tenant enviado en una consulta.
- Introducir instrucciones maliciosas dentro de documentos utilizados por RAG (prompt injection indirecto).

**Controles propuestos:**
- Validar y sanitizar las entradas recibidas.
- Aplicar controles de autorización para modificar documentos.
- Obtener el tenant desde el contexto autenticado y no confiar en identificadores proporcionados libremente por el usuario.
- Validar los documentos antes de incorporarlos a la base de conocimiento.
- Tratar los documentos recuperados como contenido no confiable y separar sus instrucciones del prompt del sistema.

### 5.3. Repudio (Repudiation)

**Amenaza:** un usuario podría negar haber realizado una consulta o un administrador podría negar haber modificado documentos o configuraciones del tutor.

Ejemplos:
- Un usuario niega haber realizado una consulta que generó una respuesta.
- No existe registro de quién modificó un documento educativo.
- No se puede determinar qué usuario o servicio realizó una operación.

**Controles propuestos:**
- Mantener registros de auditoría de operaciones relevantes.
- Registrar identificador de usuario, tenant, fecha, tipo de operación y resultado.
- Registrar modificaciones de documentos y configuraciones.
- Proteger los registros contra modificaciones no autorizadas.
- Evitar almacenar innecesariamente el contenido sensible de las consultas.

### 5.4. Divulgación de información (Information Disclosure)

**Amenaza:** un atacante podría obtener información personal, académica o confidencial perteneciente a otros estudiantes o establecimientos.

Ejemplos:
- Una consulta recupera documentos pertenecientes a otro tenant.
- Se exponen datos personales al proveedor LLM.
- Los registros de aplicación almacenan consultas con información sensible.
- Se filtran claves de API o credenciales del servicio.

**Controles propuestos:**
- Implementar aislamiento lógico entre tenants mediante `tenant_id` y políticas de seguridad a nivel de fila (RLS) en PostgreSQL.
- Aplicar filtros de tenant también durante la recuperación vectorial.
- Transmitir al proveedor LLM únicamente los datos estrictamente necesarios.
- Evitar incluir datos personales innecesarios en los prompts.
- Gestionar credenciales mediante secretos y variables de entorno protegidas.
- Utilizar TLS para las comunicaciones.
- Restringir el acceso a registros y aplicar políticas de retención.

### 5.5. Denegación de servicio (Denial of Service)

**Amenaza:** un atacante podría realizar una gran cantidad de consultas o solicitudes costosas para degradar o interrumpir la disponibilidad del Tutor IA.

Ejemplos:
- Enviar consultas repetitivas en intervalos muy cortos.
- Enviar prompts excesivamente extensos.
- Provocar múltiples solicitudes al proveedor LLM.
- Consumir recursos de CPU, memoria o presupuesto de tokens.

**Controles propuestos:**
- Implementar límites de solicitudes por usuario y tenant (rate limiting).
- Definir límites de tamaño para las consultas.
- Establecer timeouts y límites de reintentos.
- Aplicar cuotas de consumo de tokens por tenant.
- Monitorear latencia, errores y consumo de recursos.
- Utilizar escalabilidad horizontal cuando la demanda lo requiera.

### 5.6. Elevación de privilegios (Elevation of Privilege)

**Amenaza:** un usuario o servicio podría obtener permisos superiores a los que le corresponden y acceder a funcionalidades administrativas o información restringida.

Ejemplos:
- Un estudiante intenta ejecutar operaciones destinadas a docentes.
- Un usuario accede a documentos de otro curso modificando parámetros de una solicitud.
- El microservicio utiliza credenciales con permisos administrativos innecesarios.

**Controles propuestos:**
- Aplicar control de acceso basado en roles (RBAC).
- Verificar permisos según el rol, tenant y contexto del curso.
- Aplicar el principio de mínimo privilegio a las cuentas de servicio.
- Restringir las operaciones de administración y carga de documentos.
- Validar la autorización tanto en el backend como en el microservicio cuando corresponda.

## 6. Tabla consolidada de amenazas y mitigaciones

| ID | Categoría STRIDE | Amenaza identificada | Control o mitigación |
|---|---|---|---|
| T1 | Spoofing | Suplantación de estudiantes o docentes mediante tokens robados. | Validación JWT con Keycloak y autenticación entre servicios. |
| T2 | Spoofing | Acceso directo al microservicio sin autorización. | Autenticación y autorización obligatorias en las solicitudes. |
| T3 | Tampering | Modificación de documentos educativos. | Validación de documentos y permisos de modificación. |
| T4 | Tampering | Manipulación del tenant enviado en una consulta. | Obtener el tenant desde el contexto autenticado y validar su pertenencia. |
| T5 | Tampering | Instrucciones maliciosas en documentos recuperados mediante RAG. | Validación de documentos y separación entre instrucciones del sistema y contenido recuperado. |
| T6 | Repudiation | Negación de consultas o modificaciones realizadas. | Registros de auditoría con identidad, tenant, fecha y operación. |
| T7 | Information Disclosure | Recuperación de documentos de otro establecimiento. | Aislamiento por tenant, RLS y filtros en búsquedas vectoriales. |
| T8 | Information Disclosure | Exposición de datos personales al proveedor LLM. | Minimización de datos y control de información enviada al proveedor. |
| T9 | Information Disclosure | Exposición de credenciales o claves de API. | Gestión segura de secretos y restricción de accesos. |
| T10 | Denial of Service | Consultas excesivas que degradan el servicio. | Rate limiting, cuotas por tenant y límites de tamaño. |
| T11 | Denial of Service | Consumo excesivo de tokens y recursos del proveedor LLM. | Límites de tokens, timeouts, monitoreo y presupuestos por tenant. |
| T12 | Elevation of Privilege | Estudiante accede a funciones destinadas a docentes. | RBAC y validación de permisos por operación. |
| T13 | Elevation of Privilege | Servicio utiliza permisos superiores a los necesarios. | Principio de mínimo privilegio y cuentas de servicio restringidas. |

## 7. Priorización de controles

Se consideran prioritarios los siguientes controles debido al contexto de AulaViva y al tratamiento de información de menores de edad:

1. **Aislamiento entre tenants:** impedir que un establecimiento acceda a información de otro.
2. **Autenticación y autorización:** verificar la identidad, rol y permisos de cada usuario y servicio.
3. **Protección de datos personales:** minimizar la información enviada al proveedor LLM y evitar filtraciones mediante registros.
4. **Seguridad del mecanismo RAG:** validar los documentos recuperados y prevenir instrucciones maliciosas.
5. **Disponibilidad y control de costos:** limitar consultas y consumo de tokens para evitar abusos.

## 8. Relación con el pipeline de seguridad

El modelo de amenazas se complementa con los controles automatizados del pipeline de seguridad implementados en el repositorio:

| Control del pipeline | Amenazas que ayuda a mitigar |
|---|---|
| SAST con Semgrep | Errores de implementación que pueden facilitar manipulación, divulgación de información o elevación de privilegios. |
| SCA con Trivy | Vulnerabilidades conocidas en dependencias que podrían comprometer el servicio. |
| Detección de secretos con Gitleaks | Exposición accidental de credenciales y claves de API. |
| Generación de SBOM CycloneDX | Identificación y trazabilidad de componentes y dependencias del software. |

Estos controles complementan las medidas de diseño y configuración descritas anteriormente. No reemplazan los controles de autorización, aislamiento entre tenants ni protección de datos que deben implementarse en el servicio.

## 9. Conclusión

El análisis STRIDE permitió identificar amenazas relacionadas con la identidad, integridad, trazabilidad, confidencialidad y disponibilidad del microservicio Tutor IA.

Los riesgos más relevantes para AulaViva corresponden a la divulgación de información entre establecimientos, la exposición de datos personales de estudiantes, la manipulación de contenidos recuperados mediante RAG y el uso indebido de funcionalidades según los roles.

La aplicación de controles como RBAC, JWT, aislamiento mediante `tenant_id` y RLS, gestión segura de secretos, auditoría y limitación de consumo permite reducir estos riesgos.

Este modelo debe revisarse y actualizarse cuando cambie la arquitectura, se incorporen nuevos componentes o se identifiquen amenazas adicionales.
