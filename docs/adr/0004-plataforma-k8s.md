# ADR-0004 — Plataforma Kubernetes e Infraestructura como Código

**Estado:** Aceptado  
**Fecha:** 15-10-2026

## Contexto

AulaViva es una plataforma SaaS multi-tenant orientada a instituciones educativas. Su arquitectura contempla un backend desarrollado con Java y Spring Boot, con requisitos de seguridad, aislamiento entre instituciones, escalabilidad horizontal y disponibilidad.

Se necesita definir una plataforma de orquestación de contenedores y una estrategia de infraestructura como código (IaC) que permitan desplegar, configurar y evolucionar la aplicación de forma reproducible entre los entornos de desarrollo, pruebas y producción.

La solución debe facilitar las siguientes capacidades:

- Ejecutar contenedores con configuración declarativa y mecanismos de recuperación.
- Escalar las réplicas de la aplicación según la demanda.
- Gestionar la configuración y los recursos de Kubernetes de forma versionada.
- Aplicar medidas de seguridad, como ejecución sin privilegios, sondas de salud y gestión separada de secretos.
- Reproducir los despliegues y validar los cambios antes de aplicarlos.
- Mantener flexibilidad para seleccionar posteriormente una plataforma de producción, sin acoplar prematuramente la arquitectura a un proveedor cloud.

## Decisión

Se adopta **Kubernetes como plataforma de orquestación**, utilizando Kind para el desarrollo y las pruebas locales. Para la gestión declarativa de los recursos de la aplicación se utilizará Kustomize, mientras que Terraform se utilizará para gestionar los recursos de infraestructura que se definan explícitamente bajo su responsabilidad.

La decisión se concreta en los siguientes puntos:

### 1. Distribución de Kubernetes

Se utilizará **Kind (Kubernetes in Docker)** como distribución de Kubernetes para el entorno local de desarrollo y pruebas. Kind permite ejecutar un clúster Kubernetes sobre contenedores, facilitando la validación de manifiestos y el comportamiento básico de los despliegues sin requerir infraestructura cloud.

Esta elección no determina la distribución de Kubernetes para producción. La selección futura deberá considerar disponibilidad, operación, seguridad, costes, escalabilidad y compatibilidad con los servicios requeridos por AulaViva.

### 2. Gestión de recursos de Kubernetes

Se utilizará Kustomize para organizar y aplicar los manifiestos de Kubernetes de la aplicación. Los recursos se mantendrán versionados en el repositorio y se desplegarán mediante `kubectl apply -k`.

Se elige porque permite agrupar recursos, reutilizar configuraciones y mantener los despliegues versionados en el repositorio sin duplicar innecesariamente los archivos YAML.

Los manifiestos contemplarán, según las necesidades de cada entorno, recursos como Deployments, Services, ConfigMaps, Ingress y HorizontalPodAutoscalers. El namespace y el secret placeholder los gestiona Terraform.

La configuración se mantendrá separada del código de la aplicación. Las credenciales reales no se incorporarán al repositorio y deberán gestionarse mediante mecanismos de secretos adecuados al entorno.

### 3. Infraestructura como Código

Se utilizará Terraform para gestionar recursos de infraestructura cuya creación y ciclo de vida se hayan asignado explícitamente a Terraform. Actualmente gestiona el namespace `equipo-aulaviva` y un secret placeholder (`aulaviva-app-secrets`), que por ese motivo no forman parte de los manifiestos de Kustomize. Terraform se aplica antes que `kubectl apply -k`.

Se elige porque permite declarar infraestructura mediante código, revisar los cambios con plan antes de aplicarlos y mantener un estado que facilita el seguimiento de los recursos administrados.

Antes de incorporar nuevos recursos al estado de Terraform, se verificará si ya existen y si están siendo gestionados por otros mecanismos. Se evitará que Terraform y los manifiestos aplicados directamente mediante Kustomize administren simultáneamente el mismo recurso.

El estado de Terraform se mantendrá fuera del control de versiones y se almacenará en un **backend remoto de tipo `kubernetes`**, es decir, en un Secret del propio clúster (namespace `default`), con bloqueo mediante Lease. Se elige porque el equipo no dispone de una cuenta cloud ni de un bucket S3, y funciona con cualquier clúster local sin credenciales adicionales. Su limitación es que el estado se pierde si el clúster se elimina, lo que es aceptable en un laboratorio pero no en producción. Para un entorno cloud, se evaluará migrar a un backend con almacenamiento protegido y bloqueo (por ejemplo, S3 con `use_lockfile`) mediante `terraform init -migrate-state`.

### 4. Despliegue y validación

Los cambios de infraestructura se revisarán antes de su aplicación mediante los comandos de formato, validación y planificación de Terraform. Los manifiestos de Kubernetes se validarán antes de desplegarse.

Los cambios se integrarán mediante el flujo de trabajo del repositorio, con revisión previa a su incorporación a la rama principal.

### 5. Seguridad

Los contenedores se ejecutarán con privilegios mínimos siempre que la aplicación y la imagen lo permitan. Se conservarán las sondas de salud y las restricciones de recursos configuradas para el backend.

Los Secrets de Kubernetes no se considerarán seguros únicamente por su codificación en Base64. Se restringirá el acceso mediante permisos adecuados y, para producción, se evaluará un gestor de secretos administrado o una solución equivalente.

La configuración de producción deberá incorporar controles adicionales de acceso, observabilidad, protección de red y gestión de credenciales según la plataforma finalmente seleccionada.

## Consecuencias

### Positivas

- El entorno local permite validar los manifiestos sin depender de un proveedor cloud.
- Kustomize facilita la organización y reutilización de configuraciones de Kubernetes.
- Terraform permite declarar y revisar cambios de infraestructura de forma reproducible.
- La separación de responsabilidades entre aplicación e infraestructura facilita el mantenimiento.
- La estrategia mantiene abierta la selección de una plataforma de producción.
- La configuración declarativa facilita la revisión de cambios y la automatización futura del despliegue.

### Negativas y compromisos

- Kind no reproduce todas las características operativas de un clúster de producción.
- El equipo deberá mantener y validar los manifiestos y módulos de infraestructura.
- Terraform requiere gestionar cuidadosamente el estado y evitar conflictos con recursos creados por otros mecanismos.
- El estado de Terraform reside en el mismo clúster que gestiona, por lo que se pierde si el clúster se elimina.
- La gestión de secretos, el escalado y la observabilidad requieren configuración adicional; no quedan resueltos únicamente por adoptar Kubernetes.
- La operación de Kubernetes en producción implica costes y responsabilidades de seguridad, actualización y monitorización.

## Alternativas consideradas

### Kubernetes administrado por un proveedor cloud

**Ventajas:** reduce parte de la carga operativa del plano de control y puede ofrecer integración con identidad, redes, monitorización y servicios administrados.

**Desventajas:** introduce costes recurrentes, dependencia del proveedor y decisiones adicionales sobre redes, permisos, almacenamiento y servicios asociados.

**Motivo de descarte en esta etapa:** no se selecciona todavía una plataforma administrada porque el proveedor cloud de producción no está confirmado.

### Minikube

**Ventajas:** permite ejecutar Kubernetes localmente y dispone de herramientas para desarrollo y pruebas.

**Desventajas:** suele requerir una máquina virtual o un driver adicional, con mayor consumo de recursos y arranque más lento, y no aporta una ventaja suficiente para justificar mantener dos entornos locales equivalentes.

**Motivo de descarte:** Kind solo requiere Docker, que el equipo ya utiliza para construir la imagen, por lo que se mantiene como opción local para evitar duplicar herramientas y procedimientos.

### Gestionar todos los recursos únicamente con manifiestos de Kubernetes

**Ventajas:** reduce inicialmente el número de herramientas y permite gestionar los recursos directamente con `kubectl` y Kustomize.

**Desventajas:** no proporciona por sí solo el mismo flujo de planificación y gestión de estado de infraestructura que Terraform, especialmente cuando se incorporen recursos externos a Kubernetes.

**Motivo de descarte:** se adopta Terraform para los recursos de infraestructura que requieran un ciclo de vida gestionado y trazable, manteniendo Kustomize para los recursos de aplicación asignados a este mecanismo.

## Estado y revisión futura

Esta decisión define la estrategia inicial de desarrollo y gestión declarativa de infraestructura; no implica que la plataforma de producción, un backend remoto externo para Terraform (como S3) o la integración con un gestor externo de secretos ya estén implementados.

La decisión se revisará cuando se seleccione la plataforma cloud de producción, se definan los requisitos de disponibilidad y operación, o se identifiquen necesidades que justifiquen cambiar la distribución de Kubernetes o el alcance de Terraform.
