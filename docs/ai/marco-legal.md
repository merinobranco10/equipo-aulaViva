# Marco legal y ético — Tutor IA AulaViva

**Fecha:** 19-11-2026 · **Actualizado:** 20-11-2026 (vigencia de la Ley 21.719 y fuentes)

Este documento identifica las principales consideraciones legales y éticas relacionadas con el tratamiento de datos personales y el uso de inteligencia artificial en AulaViva.

## 1. Normativa aplicable

| Norma | Descripción |
|---|---|
| Ley 19.628 | Protección de la vida privada y tratamiento de datos personales. |
| Ley 21.719 | Reforma el marco chileno de protección de datos personales y crea la Agencia de Protección de Datos Personales. |
| Recomendación UNESCO sobre ética de la IA (2021) | Principios de transparencia, supervisión humana y responsabilidad. |
| Política Nacional de Inteligencia Artificial de Chile | Orientaciones nacionales para el desarrollo y uso responsable de IA. |

### Vigencia de la Ley 21.719 (estado al 20-11-2026)

| Fecha | Hito |
|---|---|
| 13-12-2024 | Se publica la Ley 21.719 en el Diario Oficial, con entrada en vigencia fijada para el **1 de diciembre de 2026**. |
| 01-09-2026 | El Gobierno ingresa al Senado un proyecto (boletín 18.623-07) para postergar la vigencia. |
| 28-10-2026 | El Senado lo aprueba y fija la nueva fecha en el **15 de octubre de 2027**. |
| 17-11-2026 | La Cámara de Diputadas y Diputados lo aprueba en segundo trámite; queda despachado para ser ley. |

**Para AulaViva:** la fecha que se espera es el **15 de octubre de 2027**, pero el cambio rige solo cuando la ley modificatoria se publique en el Diario Oficial. Hasta esa publicación, la fecha legal sigue siendo el 1 de diciembre de 2026. Antes de la defensa hay que revisar en la BCN si ya se publicó.

La postergación no cambia las obligaciones de fondo (minimización, finalidad, seguridad y protección reforzada de menores). AulaViva las aplica desde ya.

## 2. Requisitos y cumplimiento en AulaViva

| Requisito | Aplicación en AulaViva | Evidencia |
|---|---|---|
| Finalidad del tratamiento | Utilizar los datos únicamente para prestar el servicio educativo. | Model Card |
| Minimización de datos | Evitar el envío de información personal innecesaria al proveedor. | `PiiMasker.java` |
| Protección de menores | Informar al estudiante y a su apoderado sobre el funcionamiento del tutor y obtener las autorizaciones que correspondan. | Documentación del proyecto |
| Seguridad | Proteger las credenciales y controlar el acceso a la información. | Configuración y secretos |
| Confidencialidad | Impedir que un estudiante consulte información de otros estudiantes. | Golden Set |
| Aislamiento de colegios | Evitar el acceso a información de otro tenant. | `TutorService.java` |
| Transparencia | Informar que las respuestas son generadas mediante inteligencia artificial y mostrar sus fuentes. | Model Card |
| Supervisión humana | Mantener al docente como responsable de las decisiones académicas. | Model Card |
| Protección de datos personales | Enmascarar RUT, correo electrónico y teléfono. | `PiiMasker.java` |
| Seguridad ante instrucciones maliciosas | Detectar y bloquear intentos de prompt injection. | `Guardrails.java` |

## 3. Protección de información personal

AulaViva contempla mecanismos para identificar y enmascarar:

- RUT.
- Correos electrónicos.
- Números telefónicos.

Los datos identificados se reemplazan por etiquetas como `[RUT]`, `[CORREO]` y `[TELEFONO]`.

El objetivo es reducir la exposición de información personal durante el procesamiento de las preguntas.

## 4. Protección de niños, niñas y adolescentes

Debido a que AulaViva está orientado a estudiantes escolares, el tratamiento de datos debe considerar su edad y las autorizaciones correspondientes.

Se deben contemplar:

- Información clara para estudiantes y apoderados.
- Consentimiento cuando corresponda.
- Protección reforzada de datos personales.
- Acceso limitado a la información académica.
- Supervisión de los docentes.
- Prohibición de utilizar información de otros estudiantes.

## 5. Uso responsable de inteligencia artificial

El Tutor IA debe utilizarse como herramienta de apoyo educativo.

No debe:

- Reemplazar al docente.
- Calificar automáticamente a los estudiantes.
- Tomar decisiones académicas relevantes sin supervisión humana.
- Inventar información que no esté respaldada por los apuntes.
- Revelar información personal.
- Utilizar contenidos pertenecientes a otros colegios.

## 6. Riesgos identificados

| Riesgo | Medida de mitigación |
|---|---|
| Exposición de datos personales | Enmascaramiento de información |
| Acceso no autorizado | Validación de identidad y permisos |
| Filtración entre colegios | Aislamiento de tenant |
| Prompt injection | Guardrails |
| Respuestas incorrectas | Evaluación y supervisión docente |
| Dependencia de proveedor externo | Fallback extractivo |
| Tratamiento de datos de menores | Información y autorizaciones correspondientes |

## 7. Pendientes de cumplimiento

1. Formalizar las condiciones de tratamiento de datos con los colegios.
2. Revisar las condiciones contractuales del proveedor Groq y el tratamiento internacional de información.
3. Definir procedimientos de atención de solicitudes de titulares.
4. Establecer un procedimiento de respuesta ante incidentes de seguridad.
5. Revisar periódicamente el cumplimiento de la normativa aplicable.

## 8. Conclusión

AulaViva incorpora medidas técnicas y organizativas para reducir los riesgos relacionados con el uso de inteligencia artificial y el tratamiento de datos personales.

Sin embargo, la implementación técnica no constituye por sí sola una garantía de cumplimiento legal. Es necesario mantener la supervisión humana, formalizar las responsabilidades de tratamiento y revisar las obligaciones legales aplicables.

## 9. Fuentes de referencia

Consultadas el 20-11-2026.

- [BCN, Balance legislativo de la Ley 21.719](https://www.bcn.cl/balance-legislativo/detalle/ficha_LEY_21719_2024-12-13): publicación (13-12-2024) y vigencia original (1-12-2026).
- [Gobierno Digital, Guía práctica de implementación de la Ley 21.719](https://wikiguias.digital.gob.cl/datos-personales/guia-practica-implementacion-nueva-ley-datos-personales): relación entre la Ley 21.719 y la Ley 19.628.
- [Cámara de Diputadas y Diputados, Ley de Protección de Datos Personales](https://camara.cl/transparencia/leydatospersonales.aspx).
- [Garrigues: el Senado aprueba la postergación al 15-10-2027](https://www.garrigues.com/es_ES/noticia/senado-chileno-aprueba-proyecto-ley-busca-postergar-15-octubre-2027-entrada-vigencia-nueva) (30-10-2026).
- [Cámara: la Comisión de Constitución aprueba la postergación](https://www.camara.cl/cms/2026/11/05/proteccion-de-datos-comision-aprobo-postergar-entrada-en-vigencia-de-la-ley/) (05-11-2026).
- [Alessandri: el Congreso aprueba la postergación](https://www.az.cl/?p=264501) (18-11-2026).
- [BCN LeyChile](https://www.bcn.cl/leychile/): texto oficial de la Ley 19.628 y de la Ley 21.719, y publicación de la ley modificatoria cuando ocurra.
- UNESCO, Recomendación sobre la ética de la inteligencia artificial (2021).
- Política Nacional de Inteligencia Artificial de Chile.
