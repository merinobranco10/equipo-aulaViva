# Inventario de secretos

**Responsable:** Valentina León (DevSecOps) · **Actualizado:** 19-11-2026

Regla del equipo: ningún secreto se escribe en el repo, en Discord ni en capturas. Las claves se entregan solo por mensaje privado. `gitleaks` (job "Secrets" de `security.yml`) bloquea cualquier commit que traiga uno.

## 1. Dónde vive cada secreto

| Secreto | Para qué | Dónde vive | Quién lo usa | Quién lo crea y rota |
|---|---|---|---|---|
| `LLM_API_KEY` | Clave de Groq para el Tutor IA | GitHub → Settings → Secrets → **Actions** (cargado el 18-11) y **Codespaces**; en Kubernetes, Secret `aulaviva-app-secrets` creado por Terraform (`var.llm_api_key`, `sensitive = true`) | Backend (`aulaviva.llm.api-key`) | Branco (dueño de la cuenta Groq) |
| `DEMO_PASSWORD` | Clave de los 6 usuarios de demo | Variable de entorno local, **Codespaces** secret y `.env` local (no versionado) | `DemoDataLoader` (perfil `demo`) | Cada integrante define la suya |
| `DB_PASSWORD` | Clave de PostgreSQL | Codespaces secret y `.env` local; en producción, Secret de Kubernetes | `docker-compose.yml` (servicio `db` y `app`) | Valentina |
| `GITOPS_TOKEN` | PR automático al repo `equipo-aulaviva-gitops` (promoción por digest) | GitHub → Secrets → Actions | `ci.yml`, job "Promote - GitOps" | Branco (token de grano fino, solo ese repo) |
| `GITHUB_TOKEN` | Publicar en GHCR y firmar con Cosign keyless | Lo genera GitHub en cada run (no se guarda) | `ci.yml` y `security.yml` | GitHub (expira al terminar el job) |
| Firma de imágenes | Cosign **keyless** (OIDC de GitHub + Sigstore) | No existe llave privada que guardar (ADR 0006) | `security.yml`, job "Supply chain" | — |

En los tests no hay secretos: la clave de los usuarios de prueba es el valor ficticio `solo-para-tests` de `backend/src/test/resources/application-test.properties`.

## 2. Cómo se usan

- **Local / Codespaces:** `docker compose up` exige `DB_PASSWORD` y `DEMO_PASSWORD` (falla con un mensaje claro si faltan). `LLM_API_KEY` es opcional: sin ella el tutor usa el modo extractivo. En Codespaces llegan como variables de entorno al declararlas en `.devcontainer/devcontainer.json` (`secrets`).
- **Kubernetes:** `terraform apply -var="llm_api_key=$LLM_API_KEY"` crea el Secret. El valor queda en el estado de Terraform, por eso el estado nunca se sube al repo (está en `.gitignore`) y en producción se reemplaza por AWS Secrets Manager con External Secrets.
- **Logs:** la clave no se imprime en ningún log; el Día 4 lo verifica en la revisión de logs del tutor.

## 3. Rotación

| Secreto | Cuándo | Cómo |
|---|---|---|
| `LLM_API_KEY` | Si se filtra, si alguien deja el equipo o después de la defensa | console.groq.com → API Keys → revocar y crear otra → actualizar el secret en Actions y Codespaces |
| `GITOPS_TOKEN` | Al vencer (90 días) o si se filtra | GitHub → Developer settings → token de grano fino nuevo → actualizar el secret |
| `DEMO_PASSWORD` / `DB_PASSWORD` | Cuando cada uno quiera | Cambiar la variable y recrear los contenedores (`docker compose down -v` borra los datos de demo) |

## 4. Qué responder en la defensa

"¿Dónde vive un secret y quién lo rota?": la clave de Groq vive en los secrets de GitHub (Actions y Codespaces) y entra al clúster como Secret de Kubernetes vía Terraform con una variable `sensitive`. La rota Branco desde la consola de Groq, y gitleaks impide que llegue al repo.
