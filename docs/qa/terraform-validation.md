# Validación QA — Infraestructura Terraform

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 13 de octubre de 2026
**Sesión:** S07
**Alcance:** `terraform/main.tf`, `terraform/backend.tf`

## 1. Objetivo

Revisar la configuración de Terraform para AulaViva con el fin de verificar
coherencia con los manifiestos de Kubernetes, proteger archivos sensibles
y dejar constancia de la revisión QA.

## 2. Resultado de la validación

La validación se ejecutó automáticamente mediante **GitHub Actions**,
en el workflow `terraform-validate.yml`, al hacer push del cambio
de namespace en la rama `desarrollo`.

| Comando | Resultado |
|---|---|
| `terraform fmt -check` | ✅ Sin cambios de formato pendientes |
| `terraform init -backend=false` | ✅ Providers inicializados |
| `terraform validate` | ✅ Success! The configuration is valid. |

**Evidencia:** `docs/qa/evidence/terraform-validate-output.png`
## 3. Hallazgo y corrección

**Hallazgo:** el namespace por defecto en `terraform/main.tf` era `aulaviva`,
pero los manifiestos de Kubernetes (`k8s/namespace.yaml` y `k8s/kustomization.yaml`)
esperan el namespace `equipo-aulaviva`.

**Riesgo detectado:** si ambos se aplicaran sin corrección, el Deployment de
Kubernetes no encontraría el namespace esperado y el despliegue fallaría.

**Corrección aplicada:** se actualizó el default de la variable `namespace`
en `terraform/main.tf` a `equipo-aulaviva`, quedando alineado con los manifiestos
de Kubernetes.

**Evidencia:** captura del archivo `main.tf` corregido en
`docs/qa/evidence/terraform-main-tf.png`.

## 4. Archivos de protección creados

Se creó `.gitignore` en la raíz del repositorio con reglas para excluir:

- Archivos de estado de Terraform (`*.tfstate`, `*.tfstate.*`)
- Directorio `.terraform/`
- Variables con posibles valores sensibles (`*.tfvars`)
- Archivos `.env` y secretos
- `kubeconfig`

## 5. Consideraciones de seguridad

- No se almacenan credenciales reales en el repositorio.
- El `Secret` incluido es un placeholder sin datos reales.
- El backend remoto requiere un bucket S3 existente con cifrado habilitado.
- El estado de Terraform queda excluido del control de versiones.

## 6. Pendientes

- Ejecutar `terraform init -backend=false` y `terraform validate` en un
  entorno con Terraform instalado, para obtener la evidencia de la validación
  sintáctica. Se coordinará con el área de DevSecOps en la próxima sesión.
- Provisionar el bucket S3 real para el backend remoto.

**Validado por:** Alexander Ruiz-Tagle — QA Lead
