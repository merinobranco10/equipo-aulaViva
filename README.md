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

El proyecto cuenta con un backend contenerizado, despliegue en Kubernetes, escalado automático y gestión GitOps mediante Kustomize y Argo CD. Las evidencias documentan las validaciones realizadas en S07 y S08.
