# Terraform — Infraestructura de AulaViva

## Objetivo

Esta configuración prepara los recursos necesarios para trabajar con Kubernetes en AulaViva. Incluye el provider de Kubernetes, el namespace de la aplicación, un placeholder para secretos y un backend remoto en AWS S3 para almacenar el estado de Terraform.

## Requisitos previos

- Terraform 1.10.0 o superior.
- AWS CLI configurado con credenciales autorizadas.
- Un bucket S3 existente en la región configurada.
- Permisos de lectura y escritura sobre el estado y sus archivos de bloqueo.
- Un clúster de Kubernetes accesible y un archivo kubeconfig válido.
- Permisos para administrar el namespace y los recursos definidos.

## Inicialización

Desde la raíz del repositorio, revisar `backend.tf` y reemplazar los valores de ejemplo por el nombre real del bucket S3 y la región de AWS.

Después, ejecutar:

```bash
terraform -chdir=terraform init
```

## Validación

Comprobar el formato y la validez de los archivos:

```bash
terraform -chdir=terraform fmt -check
terraform -chdir=terraform validate
```

La validación requiere inicializar previamente los providers. No demuestra que el clúster esté accesible ni que los recursos puedan crearse.

## Planificación

Revisar los cambios propuestos antes de aplicarlos:

```bash
terraform -chdir=terraform plan
```

Confirmar que el contexto de Kubernetes y la cuenta de AWS corresponden al entorno autorizado.

## Aplicación

Solo después de revisar el plan y obtener la autorización correspondiente:

```bash
terraform -chdir=terraform apply
```

Revisar los cambios propuestos y confirmar únicamente si son correctos y están autorizados.

## Seguridad

- No almacenar credenciales reales en el repositorio.
- No incluir archivos de estado, como `terraform.tfstate`, en Git.
- Restringir el acceso al bucket S3 y habilitar el cifrado.
- Utilizar permisos mínimos necesarios para AWS y Kubernetes.
- El recurso Secret incluido en la configuración es únicamente un placeholder y no constituye una integración completa con AWS Secrets Manager.
- No guardar secretos reales en el estado de Terraform, ya que pueden quedar registrados allí.

## Estado de la implementación

La configuración debe validarse en un entorno autorizado. El backend requiere un bucket S3 existente y la ejecución de Terraform requiere acceso a los servicios correspondientes.
