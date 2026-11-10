# ADR 0006: Política de firma de imágenes y SLSA (Supply Chain)

- **Estado:** Propuesta
- **Fecha:** 10-11-2026

## 1. Contexto

AulaViva construye su backend en GitHub Actions y publica la imagen en GitHub Container Registry (GHCR). Esa imagen se promueve al repositorio GitOps por su digest SHA-256 (ver ADR 0005) y Argo CD la despliega en Kubernetes.

Hasta la S09 el pipeline de seguridad (`.github/workflows/security.yml`) revisa el código y las dependencias, pero nada demuestra que la imagen que llega al clúster es la que construyó nuestro pipeline ni a partir de qué código. Una imagen alterada en el registro, o publicada fuera del pipeline, sería indistinguible de la legítima.

La S10 extiende el workflow de seguridad existente con controles de cadena de suministro: firma de la imagen y attestation de procedencia.

## 2. Decisión

Se agrega el job `sign-and-attest` a `.github/workflows/security.yml`, es decir, **en el workflow de seguridad y no en `ci.yml`**. `ci.yml` conserva su responsabilidad (calidad, build, publicación y promoción); `security.yml` concentra todos los controles de seguridad.

El job corre en **todos los disparadores de `security.yml`** (push a `desarrollo` y Pull Request hacia `main` o `desarrollo`), junto con `sast`, `sca` y `secrets`, y solo si estos y los controles `iac-scan` y `container-scan` pasaron (`needs`). En todos los casos termina con `cosign verify` y `cosign verify-attestation`: si la imagen no está firmada y con attestation SLSA válida, el check falla.

| Evento | Qué hace |
|---|---|
| `push` a `desarrollo` | **Firma** la imagen y adjunta la attestation SLSA (si aún no existen) y las verifica. |
| `pull_request` hacia `main` o `desarrollo` | Solo **verifica**. Si el push a `desarrollo` que firma la imagen sigue en curso, espera su término. Nunca firma: desde un PR la identidad OIDC sería `refs/pull/N/merge` y no la autorizada. |

Así, nada llega a `main` sin haber pasado todos los controles de seguridad, y cada push a `desarrollo` se revisa con los mismos controles.

Detalles de funcionamiento:

1. No reconstruye la imagen. Resuelve la imagen que publica `ci.yml` para el commit (tag `sha-<commit>`) a su digest y trabaja siempre sobre `ghcr.io/merinobranco10/equipo-aulaviva-backend@sha256:...`. En un PR usa el commit `head` de la rama, no el commit de merge.
2. Si `ci.yml` no se ejecutó para ese commit (no cambió `backend/**`), usa la última imagen que `ci.yml` publicó con éxito en `desarrollo`.
3. La firma es `cosign sign --yes` en modalidad **keyless** (OIDC de GitHub Actions, sin llaves privadas que custodiar). Es idempotente: no vuelve a firmar una imagen que ya verifica.
4. La attestation se obtiene de la provenance SLSA que BuildKit adjunta a la imagen y se agrega con `cosign attest --type slsaprovenance`.

Permisos mínimos del job: `contents: read`, `packages: write` (publicar firma y attestation en GHCR), `id-token: write` (token OIDC para la firma keyless) y `actions: read` (localizar el run de CI del mismo commit).

## 3. Política de firma

Toda imagen del backend que se despliegue debe estar firmada con Cosign keyless. Una firma es válida solo si `cosign verify` confirma, simultáneamente, que:

- El certificado corresponde al workflow `.github/workflows/security.yml` del repositorio `merinobranco10/equipo-aulaViva`.
- La identidad corresponde a la ejecución sobre la rama `desarrollo` (`refs/heads/desarrollo`).
- El emisor OIDC es `https://token.actions.githubusercontent.com`.
- La firma está asociada al digest SHA-256 exacto de la imagen (nunca a un tag mutable).

La identidad firmante es `security.yml` porque es el workflow que ejecuta la firma. Cualquier política de admisión (Kyverno) debe referenciar esa misma identidad; si el workflow cambia de nombre o de rama, la política debe actualizarse en conjunto.

## 4. Política de SLSA

El objetivo es **SLSA Build Level 2**: build en un servicio hospedado (GitHub Actions) y provenance asociada al digest de la imagen y firmada.

| Control | Cómo se cumple |
|---|---|
| Build en servicio hospedado | La imagen se construye en runners hospedados de GitHub Actions (`ci.yml`). |
| Provenance generada por el build | `docker/build-push-action` adjunta a la imagen la provenance SLSA de BuildKit (repositorio, workflow, commit y materiales). |
| Provenance ligada al artefacto | La attestation se adjunta al digest exacto con `cosign attest --type slsaprovenance`. |
| Provenance firmada | `cosign attest` firma el predicate de forma keyless con la identidad OIDC del workflow. |
| Verificable | `cosign verify-attestation` comprueba tipo, identidad y emisor en cada ejecución. |

Si BuildKit emitiera provenance SLSA v1, el job usa el tipo `slsaprovenance1` en lugar de `slsaprovenance`; el job falla si la imagen no trae provenance válida.

**Límite reconocido:** la provenance la genera BuildKit en el job de build y la firma el job `sign-and-attest`, ambos dentro del mismo repositorio. Es suficiente para L2 (procedencia firmada y verificable), pero no ofrece la garantía de L3 (builder aislado y provenance no falsificable por el propio workflow de build).

## 5. Escaneos bloqueantes, excepciones y admisión en el clúster

### 5.1. Escaneos bloqueantes

Antes de firmar, `security.yml` ejecuta dos controles bloqueantes que `sign-and-attest` exige como requisito (`needs`):

| Job | Herramienta | Qué revisa | Cuándo bloquea |
|---|---|---|---|
| `iac-scan` | Checkov | Código Terraform de `terraform/` | Cualquier check fallido (`soft_fail: false`) |
| `container-scan` | Trivy | Imagen construida del backend (OS y librerías) | Vulnerabilidades HIGH/CRITICAL con parche disponible (`exit-code: 1`, `ignore-unfixed`) |

### 5.2. Política de excepciones (`skip_check`)

1. **Ningún `skip_check` sin registro en este ADR.** Cada check omitido (parámetro `skip_check` de la acción o comentario `#checkov:skip=` en el `.tf`) se agrega en un Pull Request que incluya su fila en el registro de abajo.
2. **Alcance mínimo.** Se omite el check específico (por ejemplo `CKV_K8S_35`), nunca una familia ni el job completo, y si es posible solo para el recurso afectado.
3. **Contenido obligatorio:** ID del check, recurso afectado, justificación técnica, riesgo residual, responsable, fecha y fecha de revisión.
4. **Revisión.** Se reevalúan en cada release mayor; una excepción vencida sin renovar se elimina.
5. **Aprobación.** El PR lo revisa el Tech Lead o DevSecOps. Está prohibido usar `soft_fail: true` como atajo.

**Estado actual:** el análisis de `terraform/` con Checkov 3.4.3 arrojó 1 check aprobado, 0 fallidos y 0 omitidos, por lo que no hay ningún `skip_check` vigente.

| ID | Check | Recurso | Justificación | Riesgo residual | Responsable | Fecha | Revisión |
|---|---|---|---|---|---|---|---|
| — | — | — | Sin excepciones vigentes | — | — | — | — |

Checkov solo analiza Terraform: los manifiestos de `k8s/` y el repositorio GitOps no están cubiertos por este job.

### 5.3. Admisión en el clúster (Kyverno)

La política `require-image-signature` (`k8s/policies/require-signature.yaml`) es una `ClusterPolicy` en modo `Enforce` que rechaza los Pods del backend (`ghcr.io/merinobranco10/equipo-aulaviva-backend*`) cuya imagen no tenga una firma Cosign keyless válida de `security.yml` en la rama `desarrollo`, con emisor `https://token.actions.githubusercontent.com`. Aplica en los namespaces `equipo-aulaviva`, `equipo-aulaviva-dev` y `equipo-aulaviva-prod`. Es el punto de control final: aunque se cree el PR de promoción GitOps, una imagen sin firma no llega a ejecutarse.

## 6. Consecuencias

**Positivas**

- Se puede comprobar la autenticidad e integridad de la imagen y trazarla hasta el repositorio, workflow y commit de origen.
- No hay llaves privadas que custodiar ni rotar.
- Los controles de seguridad quedan centralizados en `security.yml` y se ejecutan solo si los análisis previos pasan.

**Negativas y consideraciones**

- **La firma no bloquea la creación del PR de promoción GitOps.** `ci.yml` y `security.yml` corren en paralelo, de modo que ese PR puede crearse antes de que la imagen esté firmada. Los controles efectivos son el check `sign-and-attest` (que debe marcarse como obligatorio en la protección de `main`), la revisión humana del PR de promoción y la admisión al clúster, donde Kyverno rechaza imágenes sin firma válida.
- La verificación depende de la disponibilidad de GitHub y de Sigstore (Fulcio, Rekor).
- La identidad esperada está acoplada al nombre del workflow y a la rama; renombrarlos exige actualizar esta política y la política de admisión.
- Un commit que no toca `backend/**` no genera imagen nueva; el job firma o verifica la última imagen publicada en `desarrollo`.

## 7. Verificación manual

```bash
cosign verify \
  --certificate-identity "https://github.com/merinobranco10/equipo-aulaViva/.github/workflows/security.yml@refs/heads/desarrollo" \
  --certificate-oidc-issuer "https://token.actions.githubusercontent.com" \
  ghcr.io/merinobranco10/equipo-aulaviva-backend@sha256:<digest>

cosign verify-attestation --type slsaprovenance \
  --certificate-identity "https://github.com/merinobranco10/equipo-aulaViva/.github/workflows/security.yml@refs/heads/desarrollo" \
  --certificate-oidc-issuer "https://token.actions.githubusercontent.com" \
  ghcr.io/merinobranco10/equipo-aulaviva-backend@sha256:<digest>
```

## 8. Referencias

- Workflow: `.github/workflows/security.yml`, job `sign-and-attest`.
- ADR 0005: estrategia GitOps (promoción por digest).
- Sigstore Cosign: https://docs.sigstore.dev/cosign/
- SLSA: https://slsa.dev/spec/v1.0/levels
