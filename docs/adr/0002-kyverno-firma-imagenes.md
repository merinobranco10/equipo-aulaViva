# ADR-0002: Política de Admisión con Kyverno para Firmas de Imágenes

## Contexto
El clúster de Kubernetes de AulaViva necesita garantizar que solo se ejecuten imágenes de contenedor que hayan sido firmadas criptográficamente por nuestro pipeline de CI/CD. Esto evita que imágenes maliciosas o no verificadas se desplieguen en el namespace `equipo-aulaviva`.

## Decisión
Se implementó **Kyverno** como controlador de admisión en el clúster. Se creó una `ClusterPolicy` llamada `require-image-signature` que intercepta cualquier intento de crear un Pod en el namespace `equipo-aulaviva`.

La política tiene las siguientes características:
1. **Modo Enforce:** Bloquea activamente el despliegue si la validación falla.
2. **Verificación Keyless (OIDC):** En lugar de usar una clave pública estática, Kyverno valida que la imagen haya sido firmada por el workflow de GitHub Actions de nuestro repositorio (`ci.yml` en la rama `desarrollo`), usando el emisor de OIDC de GitHub.

## Consecuencias
- **Positivas:** Se garantiza la cadena de suministro. Ninguna imagen sin firma válida podrá ejecutarse en el namespace de producción/desarrollo.
- **Negativas:** Si el workflow de GitHub Actions falla o cambia de nombre, la política rechazará las imágenes hasta que se actualice la regla en Kyverno.

## Archivo de implementación
La política se encuentra en el repositorio en: `k8s/policies/require-signature.yaml`.
