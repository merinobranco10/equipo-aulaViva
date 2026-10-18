
# Terraform — AulaViva

## Objetivo

Gestionar mediante infraestructura como código los recursos de Kubernetes utilizados por AulaViva.

## Requisitos

- Terraform >= 1.10.0.
- Acceso a un clúster Kubernetes.
- Archivo kubeconfig válido.
- Backend S3 configurado y accesible.

## Inicializar Terraform

Desde la raíz del repositorio:

```powershell
terraform -chdir=terraform init
```

## Formatear y validar

```powershell
terraform -chdir=terraform fmt -recursive
terraform -chdir=terraform validate
```

## Revisar los cambios

```powershell
terraform -chdir=terraform plan
```

## Aplicar los cambios

```powershell
terraform -chdir=terraform apply
```

Revisar siempre el plan antes de aplicar cambios.

## Seguridad

- No subir credenciales AWS ni secretos reales.
- No versionar archivos de estado.
- Mantener el acceso al backend S3 restringido.
- No aplicar cambios sin revisar el plan.
