# Atributos de Calidad (NFR)

## Objetivo

Identificar y priorizar los atributos de calidad no funcionales más relevantes
para AulaViva, considerando su arquitectura SaaS multi-tenant, el tratamiento
de datos de menores, los picos de demanda y el uso de un tutor IA basado en RAG.

## NFR prioritarios

| Prioridad | Atributo | Justificación |
|---|---|---|
| 1 | Seguridad y privacidad | El sistema maneja datos de estudiantes menores de edad y debe garantizar el aislamiento entre tenants, control de acceso y protección de datos. |
| 2 | Escalabilidad y rendimiento | La plataforma debe soportar incrementos bruscos de usuarios durante períodos de pruebas sin degradar significativamente el servicio. |
| 3 | Fiabilidad y precisión del tutor IA | Las respuestas del tutor deben utilizar el contexto curricular correspondiente y minimizar respuestas incorrectas o fuera del programa MINEDUC. |

## 1. Seguridad y privacidad

**Prioridad:** Crítica

AulaViva debe proteger los datos personales y académicos de todos los estudiantes,
especialmente por tratarse potencialmente de menores de edad. Además, cada
colegio debe permanecer aislado lógicamente de los demás tenants.

**Escenario medible:**

- El 100% de las solicitudes debe validar autenticación y autorización según
  el rol del usuario.
- Un usuario de un tenant no debe poder consultar ni modificar información
  perteneciente a otro tenant.
- Los datos sensibles deben transmitirse mediante canales cifrados.
- El acceso a información de estudiantes debe quedar registrado mediante
  auditoría (audit logging).

**Indicadores:**

- 0 accesos cross-tenant autorizados.
- 100% de endpoints protegidos mediante autenticación/autorización.
- Registro de eventos de acceso a información sensible.

## 2. Escalabilidad y rendimiento

**Prioridad:** Alta

AulaViva puede experimentar picos importantes de tráfico durante períodos de
pruebas. La arquitectura debe permitir aumentar horizontalmente los recursos
sin rediseñar el sistema.

**Escenario medible:**

Ante un incremento significativo de usuarios concurrentes durante un período
de evaluación, el sistema debe mantener tiempos de respuesta aceptables y
permitir aumentar las instancias de los servicios.

**Indicadores:**

- Tiempo de respuesta del 95% de las solicitudes inferior a 3 segundos,
  excluyendo operaciones de generación de respuestas IA.
- Capacidad de escalar horizontalmente los servicios stateless.
- Sin pérdida de solicitudes durante aumentos de carga.
- Monitoreo de CPU, memoria, latencia y tasa de errores.

## 3. Fiabilidad y precisión del tutor IA

**Prioridad:** Alta

El tutor IA debe generar respuestas utilizando información proveniente de
fuentes educativas autorizadas, como las bases curriculares, libros escolares
del MINEDUC y material proporcionado por los docentes. El uso de RAG permite
recuperar el contenido relevante antes de generar la respuesta.

**Escenario medible:**

Ante una consulta de un estudiante, el tutor debe recuperar información
relevante de las fuentes autorizadas correspondientes a su tenant, curso y
asignatura. La respuesta debe basarse en dicho contexto y, cuando no exista
información suficiente, indicar que no dispone de evidencia suficiente para
responder.

**Indicadores:**

- ≥ 90% de respuestas evaluadas deben estar respaldadas por el contexto
  recuperado.
- 0 respuestas deben utilizar contenido perteneciente a otro tenant.
- Las respuestas deben poder asociarse al contenido utilizado como contexto.
- Las consultas sin evidencia suficiente deben ser identificadas como tales.

## Priorización

La prioridad se establece según el impacto que tendría una falla:

1. **Seguridad y privacidad:** una vulneración puede comprometer información
   de menores y datos de múltiples colegios.
2. **Escalabilidad y rendimiento:** una caída de rendimiento durante períodos
   de evaluación afecta directamente la continuidad del servicio.
3. **Fiabilidad y precisión del tutor IA:** respuestas incorrectas o fuera de la
   malla currícular reducen la utilidad educativa y pueden inducir a los
   estudiantes a aprender información errónea.

Estos NFR deben utilizarse posteriormente como criterios para evaluar las
decisiones arquitectónicas y las tecnologías seleccionadas.
