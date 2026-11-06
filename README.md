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

### Cómo re-triar un hallazgo

Cuando el workflow **AulaViva - Security Analysis** detecte un hallazgo, el equipo deberá seguir estos pasos:

1. **Abrir el resultado:** en **Actions**, ingresar a la ejecución del workflow y revisar el job que falló:
   - **SAST:** descargar `semgrep-sast-results` y revisar el reporte SARIF.
   - **SCA:** revisar la tabla de Trivy en el log de ejecución.
   - **Secretos:** revisar el resumen del job y el reporte `gitleaks-results.sarif`.

2. **Priorizar:** evaluar la explotabilidad (por ejemplo, si el componente es accesible desde la red pública), la exposición y la criticidad de los datos involucrados (datos de menores y aislamiento entre colegios), no solamente el CVSS.

3. **Decidir** una de las siguientes acciones:
   - **Verdadero positivo:** corregir el problema, actualizando la dependencia, modificando el código o rotando el secreto.
   - **Falso positivo de SAST:** abrir un PR para ajustar la regla correspondiente en `.semgrep/`. No se aceptan supresiones silenciosas mediante `nosem` o `nosec`.
   - **Falso positivo de secretos:** confirmar con el Tech Lead que el valor es ficticio y agregar el fingerprint exacto a `.gitleaksignore`. Si el secreto era real, revocarlo o rotarlo inmediatamente, aunque se elimine del historial.
   - **Vulnerabilidad que no afecta al producto:** documentar la justificación mediante una declaración VEX, cuando corresponda.

4. **SLA de remediación:** los plazos objetivos para corregir los hallazgos son:

   | Severidad | Plazo de remediación |
   |---|---|
   | Critical | Menos de 7 días calendario. |
   | High | Menos de 30 días calendario. |
   | Medium | Menos de 90 días calendario. |

   Los plazos se contabilizan desde la detección y registro del hallazgo. Ante explotación activa o riesgo inminente, se deben aplicar medidas de contención inmediatamente.

Los hallazgos High o Critical deberán incluir en el PR un plan de remediación o, cuando corresponda, una justificación VEX respaldada por evidencia.

### Cómo solicitar una excepción (VEX)

VEX (*Vulnerability Exploitability eXchange*) permite comunicar si una vulnerabilidad conocida en un componente de software afecta o no a un producto. En AulaViva se utilizará para documentar la aplicabilidad de vulnerabilidades detectadas durante el análisis SCA.

Una declaración VEX no debe utilizarse para ocultar vulnerabilidades reales ni reemplazar su corrección.

1. **Preparar la declaración:** crear o actualizar el archivo `.vex/aulaviva.openvex.json` con la información de la vulnerabilidad, el producto, el componente afectado, el estado VEX y la justificación técnica respaldada por evidencia.

2. **Abrir un PR:** incorporar la declaración y especificar en su descripción:
   - CVE y biblioteca afectada.
   - Versión del componente.
   - Justificación técnica y evidencia.
   - Responsable de la revisión (Tech Lead o DevSecOps).

3. **Revisar y aprobar:** el Tech Lead o responsable de DevSecOps deberá verificar la evidencia y aprobar la declaración mediante el PR. Si no existe evidencia suficiente, la solicitud deberá permanecer en investigación o ser rechazada.

4. **Mantener la declaración:** establecer una fecha de revisión y reevaluar las excepciones en cada release mayor o cuando cambien las dependencias, versiones o condiciones de ejecución.

**Ejemplo de declaración OpenVEX:**

```json
{
  "@context": "https://openvex.dev/ns/v0.2.0",
  "@id": "https://github.com/ORGANIZACION/REPOSITORIO/vex/1",
  "author": "Equipo AulaViva",
  "timestamp": "2026-11-05T00:00:00Z",
  "version": 1,
  "statements": [
    {
      "vulnerability": {
        "name": "CVE-XXXX-XXXXX"
      },
      "products": [
        {
          "@id": "pkg:maven/cl.aulaviva/aulaviva-backend@0.1.0"
        }
      ],
      "subcomponents": [
        {
          "@id": "pkg:maven/GRUPO/ARTEFACTO@VERSION"
        }
      ],
      "status": "not_affected",
      "justification": "vulnerable_code_not_in_execute_path",
      "impact_statement": "Explicar con evidencia por qué el código vulnerable no es alcanzable en AulaViva."
    }
  ]
}
```

Si aparecen hallazgos High, el PR debe incluir un plan de remediación o la justificación VEX en su descripción.
