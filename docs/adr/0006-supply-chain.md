# ADR 0006 — Política de firma y procedencia de artefactos (Supply Chain)

- **Estado:** Propuesta
- **Fecha:** 07-11-2026

## 1. Contexto

AulaViva utiliza GitHub Actions para automatizar la integración, construcción y publicación de su backend en GitHub Container Registry (GHCR).

La imagen del backend constituye un artefacto que posteriormente será utilizado por Kubernetes mediante el repositorio GitOps. Por ello, es necesario garantizar su integridad, autenticidad y trazabilidad, evitando que se desplieguen imágenes modificadas o cuya procedencia no pueda verificarse.

Para abordar estos riesgos se establece una política de firma de imágenes y generación de attestations de procedencia mediante mecanismos compatibles con SLSA (Supply-chain Levels for Software Artifacts).

## 2. Decisión

Se adopta una estrategia combinada de **GitHub Artifact Attestations, Docker Buildx y Sigstore Cosign**, utilizando los siguientes mecanismos:

1. **Provenance de GitHub:** generar una attestation SLSA firmada por GitHub mediante `actions/attest@v4`.
2. **Provenance de Buildx:** generar metadatos de construcción en formato SLSA v0.2 mediante Docker Buildx.
3. **Firma de imágenes:** utilizar Cosign en modalidad keyless, aprovechando la identidad OIDC de GitHub Actions, sin administrar claves privadas.
4. **Attestation con Cosign:** adjuntar la provenance generada por Buildx mediante `cosign attest`.
5. **Verificación:** validar la provenance de GitHub, la firma de Cosign y la attestation antes de permitir la promoción de la imagen al repositorio GitOps.

## 3. Política de firma

Se establece el uso obligatorio de Cosign con firma keyless (OIDC) para las imágenes Docker del backend de AulaViva publicadas en GitHub Container Registry (GHCR).

La firma se realizará desde GitHub Actions utilizando la identidad OIDC del workflow autorizado, sin necesidad de almacenar ni administrar claves privadas.

Se aceptarán únicamente imágenes cuya firma pueda verificarse mediante `cosign verify`, comprobando que:

- El certificado corresponda al workflow `.github/workflows/ci.yml` del repositorio `merinobranco10/equipo-aulaViva`.

- El emisor OIDC sea https://token.actions.githubusercontent.com.

- La identidad corresponda a la ejecución autorizada desde la rama desarrollo.

- La firma esté asociada al digest SHA-256 exacto de la imagen.

Regla de cumplimiento: ninguna imagen podrá avanzar al proceso de promoción GitOps si no cuenta con una firma Cosign válida que cumpla las condiciones anteriores.

## 4. Política de SLSA

Se establece como objetivo alcanzar **SLSA Build Level 2 (L2)** para las imágenes Docker del backend de AulaViva, utilizando GitHub Actions como plataforma de construcción y generación de provenance.

Para ello, se adoptan los siguientes controles:

1. **Generación de provenance:** Utilizar GitHub Artifact Attestations (`actions/attest@v4`) para generar provenance SLSA firmada por la plataforma.
2. **Identificación del artefacto:** Asociar cada attestation al digest SHA-256 de la imagen publicada en GHCR.
3. **Trazabilidad:** Identificar el repositorio, workflow y commit de origen de la construcción.
4. **Verificación:** Ejecutar `gh attestation verify` para comprobar la autenticidad de la provenance y validar que corresponda al repositorio, workflow y rama autorizados.
5. **Attestation complementaria:** Utilizar Docker Buildx para generar provenance SLSA v0.2 y Cosign para adjuntarla y verificarla mediante `cosign attest` y `cosign verify-attestation`.
6. **Control de promoción:** Impedir la promoción GitOps cuando la provenance de GitHub, la firma Cosign o la attestation complementaria no pueda verificarse correctamente.

**Regla de cumplimiento:** Ninguna imagen podrá avanzar al proceso de promoción GitOps si no dispone de provenance verificable asociada a su digest y generada por el workflow autorizado.

El cumplimiento efectivo de SLSA Build Level 2 deberá comprobarse mediante las evidencias de generación, autenticidad y verificación de provenance.

## 5. Consecuencias

**Beneficios:**

- Permite verificar la autenticidad e integridad de las imágenes.
- Reduce la necesidad de administrar claves privadas mediante firma keyless.
- Proporciona trazabilidad entre el código fuente, el workflow y la imagen construida.
- Permite detectar imágenes sin firma o con provenance no válida.
- Evita promover artefactos cuya procedencia no cumpla la política establecida.
- Integra los controles de seguridad en el pipeline CI/CD.

**Consideraciones:**

- La generación y verificación de attestations requiere conectividad con los servicios de GitHub y Sigstore.
- Los jobs necesitan permisos específicos de OIDC, GHCR y attestations.
- La verificación depende de mantener correctamente configuradas las identidades y referencias autorizadas.
- La política debe revisarse cuando cambien el repositorio, el workflow o la rama de construcción.
