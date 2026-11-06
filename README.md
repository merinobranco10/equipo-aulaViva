# AulaViva

## Descripción

AulaViva es una plataforma SaaS multi-tenant orientada a colegios de la Región Metropolitana.

El objetivo del proyecto es mejorar la experiencia de aprendizaje de estudiantes y docentes mediante herramientas de gestión académica, evaluaciones y apoyo mediante inteligencia artificial.

## Objetivo del MVP

El MVP de AulaViva contempla:

- Gestión de cursos, docentes y estudiantes.
- Control de acceso basado en roles (RBAC).
- Evaluaciones auto-corregidas con feedback.
- Tutor IA basado en RAG utilizando los apuntes de cada curso.
- Panel para apoderados con seguimiento del avance.

## Equipo

| Integrante | Rol |
|---|---|
| Branco Merino | PO |
| Matías Díaz | Tech Lead |
| Valentina León | DevSecOps |
| Gerardo González | AI/Data |
| Alexander Ruiz-Tagle | QA |

## Organización del proyecto

El trabajo del equipo se gestiona mediante GitHub Issues y un tablero Kanban.

Las columnas del tablero son: Backlog, To Do, In Progress, Review, Blocked y Done.

## Documentación

- [Charter del equipo](CHARTER.md)
- ADRs: `docs/adr/`

## Comunicación

El canal principal de comunicación del equipo es Discord.

Las decisiones importantes del proyecto se documentan en GitHub mediante Issues, Pull Requests o documentos dentro del repositorio.

## Despliegue en Kubernetes (S07)

Servicio principal: `backend/` (Spring Boot 3.5, Java 21).

- **Imagen:** Dockerfile multi-stage (Maven + Temurin 21 para compilar, `distroless/java21-debian12:nonroot` como imagen final), publicada en GHCR y referenciada por digest en `k8s/deployment.yaml`.
- **Manifiestos:** `k8s/` (Deployment, Service, Ingress, ConfigMap y HPA), aplicados con `kubectl apply -k k8s/`.
- **Infraestructura como código:** `terraform/` crea el namespace `equipo-aulaviva` y un secret placeholder. El provider está en `version.tf` y el backend remoto en `backend.tf`.

### Estado del despliegue

![Estado de los recursos de Kubernetes](docs/qa/evidence/s07/kubectl-get-all.png)

### Prueba del HPA

Se realizó una prueba de carga sintética para comprobar el escalado automático del backend. El HPA aumenta las réplicas cuando se supera el umbral de CPU configurado y las reduce cuando termina la carga.

**1. Escalado: la CPU supera el 60% y el HPA sube las réplicas**

![HPA escalando](docs/qa/evidence/s07/hpa-scale-up.png)

**2. Pods y consumo con las réplicas escaladas**

![Pods escalados](docs/qa/evidence/s07/kubectl-get-pods-scaled.png)

**3. Reducción: al detener la carga vuelve a 2 réplicas**

![HPA bajando](docs/qa/evidence/s07/hpa-scale-down.png)

## CI/CD y GitOps (S08)

Durante S08 se incorporó la validación automatizada del backend y la gestión de los manifiestos Kubernetes mediante un repositorio GitOps independiente.

- **CI:** validaciones automatizadas mediante GitHub Actions.
- **Kustomize:** organización de los manifiestos en una configuración base y overlays para desarrollo y producción.
- **Argo CD:** sincronización automática en desarrollo y sincronización manual en producción.
- **Repositorio GitOps:** [equipo-aulaviva-gitops](https://github.com/merinobranco10/equipo-aulaviva-gitops).

### Evidencias S08

**1. Aplicaciones sincronizadas y saludables en Argo CD**

![Aplicaciones de AulaViva en Argo CD](docs/qa/evidence/s08/09-argo-cd-applications-healthy.png)

**2. Validación de manifiestos con Kustomize**

![Validación de Kustomize](docs/qa/evidence/s08/08-kustomize-validation.png)

**3. Pipeline de CI exitoso**

![Pipeline de CI](docs/qa/evidence/s08/04-ci-all-jobs-success-gitops.png)

## Generación y publicación del SBOM (CycloneDX)

### Objetivo
Generar un inventario de los componentes y dependencias del backend de AulaViva mediante CycloneDX, permitiendo identificar las bibliotecas utilizadas y facilitar el análisis de seguridad de la aplicación.

### Implementación
Se incorporó el plugin **CycloneDX Maven Plugin** en el archivo `backend/pom.xml`, configurado para generar un archivo en formato JSON.

El SBOM se genera durante la ejecución del pipeline de integración continua de GitHub Actions, dentro del workflow **AulaViva - CI Quality Gates**.

**Configuración utilizada:**
- Herramienta: CycloneDX Maven Plugin.
- Versión: 2.9.3.
- Formato de salida: JSON.
- Nombre del archivo: `sbom.json`.
- Ubicación de generación: `backend/target/sbom.json`.
- Dependencias de prueba excluidas del inventario.

### Validación
Se incorporó una etapa de validación en GitHub Actions para comprobar que el archivo SBOM exista y que su estructura corresponda al formato CycloneDX.

Además, se revisó el contenido del archivo para verificar que incluya las dependencias directas y transitivas del backend.

### Publicación del artefacto
El archivo generado se publica automáticamente como artefacto de GitHub Actions mediante la acción `actions/upload-artifact@v4`.

**Nombre del artefacto:** `aulaviva-sbom`.

### Procedimiento para descargar el SBOM
1. Ingresar al repositorio de AulaViva en GitHub.
2. Acceder a la pestaña **Actions**.
3. Seleccionar una ejecución exitosa del workflow **AulaViva - CI Quality Gates**.
4. Ubicar la sección **Artifacts** al final de la ejecución.
5. Descargar el artefacto `aulaviva-sbom`.
6. Descomprimir el archivo y abrir `sbom.json`.

### Resultado esperado
Se obtiene un inventario de componentes de software en formato CycloneDX JSON, que permite mantener trazabilidad sobre las dependencias utilizadas por el backend y sirve como insumo para futuras revisiones de seguridad y análisis de vulnerabilidades.


## Estado del proyecto

## Detección de secretos con Gitleaks

### Objetivo

Incorporar un mecanismo automatizado para detectar secretos expuestos en el repositorio AulaViva, incluyendo posibles API Keys, tokens y credenciales, tanto en los archivos actuales como en el historial de Git.

### Implementación

Se integró **Gitleaks** mediante la acción `gitleaks/gitleaks-action@v2` en el workflow `AulaViva - Security Analysis`, ubicado en `.github/workflows/security.yml`.

La configuración incluye:

- Creación de un job independiente denominado `secrets`.
- Uso de `actions/checkout@v4` con `fetch-depth: 0` para obtener el historial completo.
- Ejecución automática de Gitleaks en GitHub Actions.
- Escaneo adicional del historial Git mediante Docker y Gitleaks v8.24.3.
- Uso de `--redact` para evitar mostrar valores sensibles durante el análisis.
- Configuración de códigos de salida bloqueantes para detener el pipeline cuando se detectan secretos.
- Generación y publicación del reporte SARIF como artefacto de GitHub Actions.

### Gestión de hallazgos
### Clasificación final de los hallazgos

Los dos hallazgos detectados en los archivos de ejemplos de API fueron revisados y confirmados por el Tech Lead como valores ficticios utilizados para pruebas.

Por lo tanto, se clasificaron como falsos positivos y se mantuvieron sus fingerprints específicos en `.gitleaksignore`.

No fue necesaria la rotación de credenciales, ya que se confirmó que los valores no corresponden a credenciales reales.

Durante el análisis inicial se identificaron dos coincidencias de la regla `generic-api-key` en archivos de ejemplos de API:

- `api/examples/tutor-question-error.json`
- `api/examples/submit-evaluation.json`

Se incorporaron sus fingerprints específicos al archivo `.gitleaksignore` para excluir esas coincidencias del análisis. Su origen como datos ficticios no ha sido verificado independientemente.

### Validación

En la ejecución de GitHub Actions se verificó:

- Escaneo de 219 commits del historial Git.
- Análisis aproximado de 506 KB.
- Resultado final: `no leaks found`.
- Generación del artefacto `gitleaks-results.sarif.zip`.
- Ejecución exitosa del job de detección de secretos.

### Evidencias

Los resultados pueden consultarse en:

1. Repositorio de AulaViva → pestaña **Actions**.
2. Workflow **AulaViva - Security Analysis**.
3. Job **Secrets - Deteccion de secretos**.
4. Artefactos de la ejecución → `gitleaks-results.sarif.zip`.

### Resultado esperado

Disponer de un control automatizado para identificar posibles secretos expuestos en el código y su historial, evitando que nuevos hallazgos pasen inadvertidos mediante un gate bloqueante en el pipeline de seguridad.

El proyecto cuenta con un backend contenerizado, despliegue en Kubernetes, escalado automático y gestión GitOps mediante Kustomize y Argo CD. Las evidencias documentan las validaciones realizadas en S07 y S08.

## Re-triage de hallazgos y gestión de excepciones (VEX)

### Objetivo

Establecer un procedimiento para revisar, clasificar y gestionar los hallazgos de seguridad detectados durante los análisis automatizados de AulaViva.

El procedimiento permite determinar si una vulnerabilidad afecta realmente al proyecto, definir las acciones correctivas y documentar excepciones justificadas mediante Vulnerability Exploitability eXchange (VEX).

### Herramientas involucradas

Los hallazgos pueden provenir de los siguientes controles:

| Herramienta | Tipo de análisis | Hallazgos que puede detectar |
|---|---|---|
| Semgrep | SAST | Problemas de seguridad en el código fuente Java. |
| Trivy | SCA | Vulnerabilidades conocidas en dependencias del proyecto. |
| Gitleaks | Detección de secretos | Posibles credenciales, tokens y claves expuestas. |
| CycloneDX | SBOM | Inventario de componentes y dependencias para facilitar su identificación y análisis. |

### 2. Priorización de hallazgos

Los hallazgos identificados por las herramientas se priorizan considerando su severidad, explotabilidad, exposición e impacto potencial sobre AulaViva.

#### Criterios de evaluación

| Criterio | Descripción |
|---|---|
| Severidad | Clasificación asignada por la herramienta (CRITICAL, HIGH, MEDIUM o LOW). Cuando esté disponible, también se considera la puntuación CVSS. |
| Explotabilidad | Existencia de exploits conocidos, facilidad de explotación y condiciones necesarias para aprovechar la vulnerabilidad. |
| Exposición | Accesibilidad del componente vulnerable desde Internet, usuarios autenticados u otros servicios internos. |
| Impacto | Posibles consecuencias sobre la confidencialidad, integridad y disponibilidad de la plataforma. |

#### Niveles de prioridad

| Prioridad | Condiciones orientativas | Acción requerida |
|---|---|---|
| P0 – Crítica | Vulnerabilidad CRITICAL con explotación conocida, exposición directa o consecuencias potencialmente graves. | Evaluación y mitigación inmediata. |
| P1 – Alta | Vulnerabilidad HIGH con exploit conocido, componente expuesto o impacto significativo. | Planificar y ejecutar una corrección prioritaria. |
| P2 – Media | Vulnerabilidad MEDIUM o HIGH con explotabilidad y exposición limitadas. | Registrar y programar su corrección. |
| P3 – Baja | Vulnerabilidad LOW, sin evidencia de explotación y con impacto limitado. | Resolver durante el mantenimiento periódico. |

Esta clasificación corresponde a una política interna del equipo y no reemplaza las puntuaciones CVSS ni la evaluación técnica de cada vulnerabilidad. Los hallazgos CRITICAL siempre requieren revisión prioritaria, aunque no se conozca un exploit público.

#### Aplicación en AulaViva

Se dará especial atención a los hallazgos que puedan comprometer:

- El aislamiento de datos entre colegios (multi-tenancy).
- Los datos personales y académicos de estudiantes menores de edad.
- Los mecanismos de autenticación y autorización (Keycloak y RBAC).
- El acceso a documentos y datos utilizados por el Tutor IA.
- La disponibilidad de la plataforma y los costos asociados al consumo de servicios LLM.

Los siguientes tiempos son objetivos internos propuestos para evaluar y gestionar los hallazgos:

| Prioridad | Tiempo objetivo de evaluación |
|---|---|
| P0 – Crítica | Inmediato. |
| P1 – Alta | Dentro de 24 horas. |
| P2 – Media | Dentro de 7 días. |
| P3 – Baja | Durante el siguiente ciclo de mantenimiento. |

Estos plazos corresponden a objetivos de gestión y no garantizan que la corrección completa se realice dentro del mismo período.


### Procedimiento de re-triage

Cuando una herramienta detecte un hallazgo, el equipo deberá realizar los siguientes pasos:

**1. Identificar el hallazgo**

Registrar la información disponible:

| Campo | Descripción |
|---|---|
| Herramienta de origen | Herramienta que detectó el problema (Semgrep, Trivy o Gitleaks). |
| Identificador del hallazgo | Identificador de la alerta o CVE, cuando corresponda. |
| Componente afectado | Componente, dependencia o archivo involucrado. |
| Versión | Versión del componente afectado, cuando corresponda. |
| Severidad | Severidad asignada por la herramienta (CRITICAL, HIGH, MEDIUM o LOW). |
| Fecha de detección | Fecha en que se identificó el hallazgo. |
| Workflow y ejecución | Nombre del workflow y enlace o identificador de la ejecución de GitHub Actions. |

**2. Analizar el hallazgo**

Revisar la información técnica para determinar si el problema representa un riesgo real para AulaViva.

Se deberán considerar los siguientes aspectos:

- Si el componente o código afectado está presente y se utiliza en la aplicación.

- Si la versión utilizada está afectada por la vulnerabilidad.

- Si existen exploits conocidos o evidencia de explotación activa.

- Si se cumplen las condiciones necesarias para explotar el problema.

- Si el componente está expuesto a usuarios externos o servicios internos.

- Qué impacto podría tener sobre la confidencialidad, integridad y disponibilidad de AulaViva.

**3. Clasificar el hallazgo**

Después de la revisión, se debe asignar una clasificación:

| Clasificación | Descripción | Acción |
|---|---|---|
| Vulnerabilidad confirmada | El componente o código está afectado. | Corregir o implementar una mitigación. |
| Falso positivo | La herramienta identificó un problema que no corresponde a una vulnerabilidad real. | Documentar la justificación y evidencia. |
| No afectado | El producto no está afectado por la vulnerabilidad identificada. | Documentar la justificación mediante VEX cuando corresponda. |
| En investigación | No existe información suficiente para determinar su aplicabilidad. | Continuar el análisis y asignar un responsable. |

Una vez clasificado, se deberá asignar una prioridad P0, P1, P2 o P3 considerando la severidad, explotabilidad, exposición e impacto definidos en la sección de priorización de hallazgos.

La prioridad asignada determinará el tiempo objetivo de evaluación y seguimiento.

**4. Definir la acción correctiva**

Según el resultado del análisis, se deberá tomar una de las siguientes decisiones:

- **Corregir:** actualizar la dependencia, modificar el código o eliminar la condición vulnerable.
- **Mitigar:** implementar controles que reduzcan el riesgo mientras se prepara una solución definitiva.
- **Investigar:** recopilar información adicional antes de tomar una decisión.
- **Solicitar una excepción:** cuando exista una justificación técnica para aceptar temporalmente el riesgo o determinar que el producto no está afectado.

**5. Registrar el resultado**

Cada hallazgo revisado debe quedar documentado con:

| Campo | Descripción |
|---|---|
| Identificador del hallazgo | Identificador de la alerta o CVE, cuando corresponda. |
| Herramienta de origen | Herramienta que detectó el problema. |
| Componente y versión | Componente, dependencia o archivo afectado y su versión, cuando corresponda. |
| Clasificación final | Vulnerabilidad confirmada, falso positivo, no afectado o en investigación. |
| Prioridad asignada | Prioridad P0, P1, P2 o P3. |
| Justificación técnica | Motivo de la clasificación y prioridad asignadas. |
| Evidencia de la revisión | Resultados del análisis, referencias, pruebas o documentación que respalda la decisión. |
| Acción definida | Corregir, mitigar, investigar, solicitar una excepción o documentar como no afectado. |
| Responsable | Integrante encargado del seguimiento y resolución. |
| Fecha de detección y revisión | Fechas en que se detectó y reevaluó el hallazgo. |
| Fecha de próxima revisión | Fecha programada para una nueva evaluación, si corresponde. |

Los resultados deben quedar asociados a un GitHub Issue o Pull Request para mantener la trazabilidad.

**6. Verificar la corrección y cerrar el hallazgo**

Cuando se implemente una corrección o mitigación, el equipo deberá:

- Verificar que la modificación resuelva el problema identificado.

- Volver a ejecutar la herramienta de análisis correspondiente.

- Comprobar que el hallazgo ya no se detecte o que la mitigación esté correctamente aplicada.

- Actualizar el estado del hallazgo y registrar la evidencia de resolución.

- Cerrar el GitHub Issue o actualizar el Pull Request correspondiente.

Si el problema continúa presente, el hallazgo deberá permanecer abierto y continuar su seguimiento hasta su resolución.

### Gestión de excepciones mediante VEX

VEX (Vulnerability Exploitability eXchange) permite comunicar si un producto está afectado por una vulnerabilidad conocida y cuál es su situación respecto de ella.

Cuando se determine que una vulnerabilidad no afecta a AulaViva o se requiera documentar formalmente su estado, se podrá registrar mediante un documento VEX asociado al SBOM CycloneDX correspondiente.

Se consideran los siguientes estados:

| Estado VEX | Significado |
|---|---|
| `not_affected` | El producto no está afectado por la vulnerabilidad. |
| `affected` | El producto está afectado por la vulnerabilidad. |
| `fixed` | La vulnerabilidad fue corregida en el producto. |
| `under_investigation` | La aplicabilidad de la vulnerabilidad todavía está siendo investigada. |

**Requisitos para solicitar una excepción**

La solicitud deberá incluir:

| Campo | Descripción |
|---|---|
| Identificador | Identificador del hallazgo o CVE, cuando corresponda. |
| Herramienta | Semgrep, Trivy o Gitleaks. |
| Componente afectado | Dependencia, archivo o servicio involucrado. |
| Severidad | Severidad original informada por la herramienta. |
| Prioridad | P0, P1, P2 o P3 asignada durante el análisis. |
| Justificación | Motivo por el cual se solicita la excepción. |
| Evidencia | Resultados del análisis, versiones, configuración y condiciones de uso. |
| Riesgo residual | Riesgo que permanece y sus posibles consecuencias. |
| Mitigaciones | Medidas aplicadas para reducir el riesgo, si corresponde. |
| Responsable | Integrante que solicita y mantiene el seguimiento. |
| Aprobación | Responsable técnico que revisa y aprueba o rechaza la solicitud. |
| Fecha de revisión | Fecha en que debe reevaluarse la decisión. |

**Flujo de aprobación**

1. Un integrante del equipo registra el hallazgo y su análisis.
2. Se documentan la prioridad, justificación y evidencia.
3. El responsable técnico revisa la solicitud y las mitigaciones propuestas.
4. El Tech Lead aprueba o rechaza la excepción.
5. La decisión queda registrada mediante una incidencia o Pull Request en GitHub.
6. Se establece una fecha de revisión para comprobar si la excepción sigue siendo válida.
7. Si el problema se corrige, se actualiza el registro y se cierra el seguimiento.

Una excepción no elimina la vulnerabilidad ni el riesgo asociado. Tampoco reemplaza la corrección cuando esta sea necesaria. Las credenciales o secretos potencialmente expuestos deben evaluarse para su revocación o rotación, aunque exista una solicitud de excepción.
Una excepción no elimina una vulnerabilidad confirmada ni debe utilizarse para ocultar hallazgos. Su objetivo es documentar una decisión técnica justificada y mantener la trazabilidad del riesgo aceptado.

### Relación entre SBOM y VEX

El SBOM generado mediante CycloneDX en el workflow `AulaViva - CI Quality Gates` proporciona el inventario de componentes y dependencias del backend.

Este inventario sirve como referencia para identificar los componentes relacionados con un hallazgo y documentar su aplicabilidad mediante VEX.

El SBOM y el documento VEX cumplen funciones diferentes:

- **SBOM:** identifica los componentes y dependencias que forman parte del software.
- **VEX:** comunica si una vulnerabilidad conocida afecta al producto y cuál es su estado.

Ambos documentos pueden mantenerse relacionados mediante la identificación del producto, sus componentes y la versión analizada.

### Revisión y actualización de excepciones

Las excepciones aprobadas deben revisarse cuando ocurra alguna de las siguientes situaciones:

- Se actualice la versión del componente afectado.
- Cambien las condiciones de explotación de la vulnerabilidad.
- Se incorporen nuevos controles de seguridad.
- Se publique información que modifique la evaluación inicial.
- Se alcance la fecha de revisión establecida.

Si la justificación deja de ser válida, el hallazgo deberá volver a evaluarse y se deberán definir las acciones correctivas correspondientes.

