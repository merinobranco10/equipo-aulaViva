# ADR 0006: Estrategia GitOps y herramienta de despliegue

- **Estado:** Aceptada
- **Fecha:** 22-10-2026

## 1. Contexto

AulaViva utiliza Kubernetes para desplegar su backend en dos entornos independientes: desarrollo (`equipo-aulaviva-dev`) y producción (`equipo-aulaviva-prod`). Para mantener ambos entornos consistentes y facilitar la trazabilidad de los cambios, se requiere una estrategia de despliegue que permita gestionar la configuración de infraestructura y aplicaciones mediante control de versiones.

El código fuente del backend se mantiene en el repositorio original, mientras que los manifiestos de Kubernetes y las configuraciones de Kustomize se encuentran en un repositorio GitOps independiente. Esta separación permite distinguir la construcción de la aplicación de su configuración de despliegue.

El proceso de integración continua ejecuta verificaciones de calidad, construye y publica la imagen del backend en GitHub Container Registry (GHCR) y obtiene su digest SHA-256. Posteriormente, propone actualizar la referencia de la imagen en el repositorio GitOps mediante un Pull Request, permitiendo revisar y aprobar los cambios antes de incorporarlos a la rama principal.

Se consideran dos enfoques para aplicar los cambios en Kubernetes:

- **Modelo push:** El sistema de integración continua o una herramienta externa ejecuta operaciones de despliegue directamente sobre el clúster.
- **Modelo pull:** Un controlador que opera dentro del entorno Kubernetes consulta el repositorio GitOps, detecta las diferencias entre el estado declarado y el estado real, y reconcilia los recursos.

También es necesario seleccionar una herramienta que permita implementar esta estrategia, gestionar los entornos y mantener la sincronización entre Git y Kubernetes.

## 2. Decisión

Se adopta una estrategia **GitOps basada en el modelo pull, utilizando Argo CD como herramienta de despliegue y reconciliación continua**.

### 2.1. Modelo de despliegue

Se elige el modelo pull frente al modelo push porque permite separar la integración continua de la aplicación de cambios en Kubernetes. GitHub Actions se encarga de validar el código, construir y publicar la imagen, y proponer su promoción en el repositorio GitOps, mientras que Argo CD gestiona la sincronización y reconciliación de los recursos del clúster. Esta separación reduce la necesidad de otorgar al pipeline credenciales directas para modificar Kubernetes, facilita la trazabilidad de los cambios mediante Git y permite detectar y corregir desviaciones respecto de la configuración declarada. Además, la sincronización automática en desarrollo y manual en producción permite equilibrar la rapidez de validación y el control de los despliegues.

### 2.2 Selección de la herramienta GitOps: Argo CD

Se selecciona Argo CD frente a alternativas como Flux CD debido a su interfaz gráfica, que facilita la visualización del estado de sincronización y salud de las aplicaciones, así como la supervisión de los despliegues. Además, permite gestionar de forma independiente las aplicaciones correspondientes a los entornos de desarrollo y producción, configurar políticas de sincronización específicas e integrarse con Kustomize para reutilizar configuraciones comunes y mantener las particularidades de cada entorno. Estas características se ajustan a las necesidades de AulaViva, ya que facilitan la operación, la trazabilidad y la demostración del proceso de despliegue. Si bien Flux CD también permite implementar una estrategia GitOps, se prioriza Argo CD por su adecuación a los requisitos operativos y de supervisión de AulaViva.

Argo CD será responsable de consultar el repositorio GitOps y reconciliar los recursos de Kubernetes con el estado declarado en Git. Los cambios en la infraestructura y en los manifiestos se gestionarán mediante commits y Pull Requests, manteniendo el historial de modificaciones y facilitando la recuperación de configuraciones anteriores.

El proceso de promoción de versiones seguirá este flujo:

1. Un cambio en la rama `desarrollo` del repositorio original activa el pipeline de integración continua.
2. El pipeline ejecuta las pruebas y verificaciones de calidad y construye la imagen del backend.
3. Si las verificaciones son satisfactorias, la imagen se publica en GHCR y se obtiene su digest SHA-256.
4. El pipeline propone un cambio en el repositorio GitOps mediante un Pull Request que actualiza la referencia de la imagen.
5. Una vez revisado y aprobado el Pull Request, el cambio se incorpora a la rama `main` del repositorio GitOps.
6. Argo CD detecta la nueva configuración y aplica los cambios de acuerdo con la política de sincronización del entorno.

La integración continua podrá actualizar el repositorio GitOps mediante cambios versionados, pero no realizará despliegues directos mediante comandos de Kubernetes. La aplicación de los manifiestos y la reconciliación del estado serán responsabilidad de Argo CD.

### 2.3. Política de sincronización por entorno

Se establece la siguiente política:

| Entorno | Namespace | Sincronización | Justificación |
|---|---|---|---|
| Desarrollo | `equipo-aulaviva-dev` | Automática, con `prune` y `selfHeal` | Facilita validar rápidamente los cambios integrados en Git. |
| Producción | `equipo-aulaviva-prod` | Manual | Permite revisar y autorizar la aplicación de cambios antes de desplegarlos. |

La configuración de cada entorno se mantendrá en su correspondiente overlay de Kustomize. Las imágenes se referenciarán mediante digest SHA-256 para identificar de forma inmutable el contenido publicado, evitando depender de etiquetas mutables como `latest`.

La incorporación de cambios a la rama `main` actualiza la configuración declarada en el repositorio GitOps. En desarrollo, Argo CD sincroniza automáticamente los cambios según la política configurada. En producción, la aplicación efectiva de los cambios requiere una revisión del estado de la aplicación y la ejecución de la sincronización manual en Argo CD.
## 3. Consecuencias

### 3.1. Consecuencias positivas

- **Trazabilidad:** Los cambios de despliegue quedan registrados en Git y pueden asociarse a commits y Pull Requests.
- **Separación de responsabilidades:** El pipeline construye y publica imágenes, mientras que Argo CD gestiona su aplicación en Kubernetes.
- **Mayor control de producción:** La sincronización manual permite revisar los cambios antes de aplicarlos.
- **Recuperación de versiones:** La reversión de un commit en el repositorio GitOps permite restaurar una configuración anterior, siempre que la imagen referenciada siga disponible.
- **Consistencia entre entornos:** Kustomize permite reutilizar recursos comunes y definir diferencias explícitas entre desarrollo y producción.
- **Detección de desviaciones:** `selfHeal` permite que Argo CD corrija cambios manuales en los recursos gestionados del entorno de desarrollo.
- **Menor exposición de credenciales de despliegue:** El pipeline no necesita credenciales directas para modificar los recursos del clúster.

### 3.2. Consecuencias negativas y limitaciones

- **Dependencia del repositorio GitOps:** Los cambios declarativos no se aplicarán mientras Argo CD no pueda acceder al repositorio o no pueda reconciliar los recursos.
- **Complejidad operativa adicional:** Es necesario mantener Argo CD, sus permisos, sus aplicaciones y las políticas de acceso al repositorio.
- **Latencia de sincronización:** Los cambios no necesariamente se aplican de forma instantánea; dependen de la detección de cambios y del estado del controlador.
- **Mayor control manual en producción:** La aprobación y sincronización manual agregan pasos al proceso de publicación.
- **Riesgo de cambios no autorizados:** El modelo depende de proteger la rama principal, revisar los Pull Requests y restringir quién puede modificar la configuración.
- **Limitaciones de la recuperación:** Revertir los manifiestos no garantiza por sí solo la recuperación de datos ni la compatibilidad con cambios irreversibles de bases de datos.
- **Disponibilidad de imágenes:** Los digests permiten identificar el contenido exacto de una imagen, pero no garantizan que esta permanezca disponible en el registro.

### 3.3. Consideraciones de seguridad y operación

Se protegerá la rama `main` del repositorio GitOps mediante revisión de cambios y controles de acceso. Los tokens utilizados por la integración continua tendrán los permisos mínimos necesarios para publicar imágenes y crear Pull Requests.

El acceso de GitHub Actions al repositorio GitOps se limitará a los permisos necesarios para proponer los cambios de configuración, mientras que las credenciales utilizadas por Argo CD para acceder al repositorio y administrar los recursos del clúster se gestionarán de forma independiente y con privilegios mínimos.

Argo CD dispondrá únicamente de los permisos requeridos para administrar los recursos de los entornos correspondientes. Los secretos de aplicación no se almacenarán en texto plano en el repositorio GitOps; se utilizarán mecanismos de gestión de secretos apropiados para el entorno.

La estrategia de despliegue se complementará con comprobaciones de salud de los pods, pruebas de funcionamiento y procedimientos documentados de rollback.

## 4. Alternativas consideradas

| Alternativa | Ventajas | Motivo de descarte |
|---|---|---|
| Despliegue push desde CI/CD | Permite ejecutar despliegues directamente desde el pipeline. | Descartado porque requiere credenciales de acceso al clúster en el pipeline y no proporciona, por sí solo, reconciliación continua del estado declarado en Git. |
| Flux CD con modelo pull | Permite implementar GitOps mediante controladores y reconciliación declarativa. | No seleccionado porque se priorizan la visibilidad del estado de las aplicaciones y la facilidad de operación de Argo CD para los objetivos del proyecto. |


## 5. Referencias

- Repositorio GitOps: https://github.com/merinobranco10/equipo-aulaviva-gitops
- Configuración de aplicaciones: `apps/aulaviva-backend/overlays/dev` y `apps/aulaviva-backend/overlays/prod`.
- Documentación oficial de Argo CD: https://argo-cd.readthedocs.io/
- Documentación oficial de Flux CD: https://fluxcd.io/flux/
- Documentación de Kustomize: https://kubectl.docs.kubernetes.io/references/kustomize/


## 6. Validación de Calidad (QA)

Desde el área de QA se revisó esta estrategia para asegurar que el proceso sea auditable y seguro:

- **Trazabilidad y auditoría:** El uso de Pull Requests en el repositorio GitOps garantiza que cada cambio en producción quede registrado con un autor, una fecha y una justificación.
- **Quality Gates:** Se validó que GitHub Actions bloquee la promoción de imágenes si las pruebas unitarias, la cobertura (mínimo 70%) o el análisis estático de seguridad (SAST) fallan.
- **Verificación de protección:** Se confirmó que la rama `main` y `desarrollo` del repositorio de código fuente están protegidas, exigiendo Pull Requests y la aprobación de los checks de CI para poder fusionar cambios.
- **Rollback:** La estrategia de usar digests inmutables (SHA-256) y mantener el historial en Git permite revertir un despliegue fallido simplemente revirtiendo un commit en el repositorio GitOps, sin necesidad de reconstruir la imagen.

**Responsable:** Alexander Ruiz-Tagle — QA Lead.
