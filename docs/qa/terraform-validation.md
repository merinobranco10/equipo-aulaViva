# Validación QA — Infraestructura Terraform

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 13 de octubre de 2026
**Sesión:** S07

## 1. Objetivo
Validar que la configuración de Terraform para AulaViva es correcta y coherente con los manifiestos de Kubernetes.

## 2. Resultado de la validación
| Comando | Resultado |
|---|---|
| `terraform init -backend=false` | ✅ Providers inicializados |
| `terraform validate` | ✅ Configuración válida |

Evidencia: ![Terraform Validate](evidence/terraform-validate-output.png)

## 3. Hallazgo y corrección
**Hallazgo:** el namespace en `terraform/main.tf` era `aulaviva`, pero los manifiestos de Kubernetes esperan `equipo-aulaviva`.
**Corrección:** se alineó el default del namespace a `equipo-aulaviva`.

## 4. Pendientes
- Provisionar el bucket S3 real para el backend remoto.
- Ejecutar `terraform plan` y `apply` en el entorno real.

**Validado por:** Alexander Ruiz-Tagle — QA Lead
