# Validación QA — DAST y política de admisión (S10)

**Fecha:** 10 de noviembre de 2026
**Sesión:** S10
**Alcance:** job `dast` de `.github/workflows/security.yml` y política Kyverno `require-image-signature`

## 1. Ambiente de staging

Staging es la imagen del backend **firmada y verificada** por el job `sign-and-attest`, levantada por el propio job `dast` en el runner:

- Imagen: `ghcr.io/merinobranco10/equipo-aulaviva-backend@sha256:<digest verificado>`
- URL: `http://localhost:8080` (el job espera `/actuator/health/readiness` antes de escanear).
- El ambiente se destruye al terminar el job. No hay datos persistentes.

## 2. Cuenta de prueba y datos ficticios

- Cuenta de prueba: `qa-dast@aulaviva.invalid` (dominio reservado, no existe).
- Datos: sintéticos. El staging se crea desde cero en cada ejecución; no se conecta a ninguna base ni usa datos de estudiantes reales.
- Estado actual del backend: no tiene autenticación ni base de datos (solo `/`, `/api/v1/status` y `/actuator/health`), por lo que ZAP escanea sin iniciar sesión. Cuando el backend incorpore login, esta cuenta será la usada por ZAP.

## 3. Job `dast`

| Parámetro | Valor |
|---|---|
| Herramienta | `zaproxy/action-baseline@v0.14.0` |
| Objetivo | `http://localhost:8080` |
| Requisito previo | `sign-and-attest` (firma y attestation verificadas) |
| Timeout | 9 min (`timeout-minutes: 9`) + `-T 5` en ZAP |
| Falla en | riesgo **Medium o superior** (paso "Gate DAST" sobre `report_json.json`) |
| Reporte | artefacto `zap-baseline-report` |
| Falsos positivos | `.zap/rules.tsv` |

## 4. Hallazgos y falsos positivos

Resultado de la primera ejecución del job `dast` (ZAP baseline sobre `http://localhost:8080`, 2 endpoints):

| Riesgo | Alertas |
|---|---|
| High | 0 |
| Medium | 0 |
| Low | 2 |
| Informational | 1 |

El gate (Medium o superior) **pasa**: no hay hallazgos que bloqueen.

| Regla ZAP | Hallazgo | Riesgo | Clasificación | Acción |
|---|---|---|---|---|
| 90004 | Cross-Origin-Resource-Policy Header Missing or Invalid | Low | Hallazgo real, bajo el umbral | Se acepta; no bloquea |
| 10021 | X-Content-Type-Options Header Missing | Low | Hallazgo real, bajo el umbral | Se acepta; no bloquea |
| 10049 | Storable and Cacheable Content | Informational | Informativo | Se acepta; no bloquea |

**Falsos positivos:** ninguno. No se agregó ninguna regla `IGNORE` a `.zap/rules.tsv`.

Un falso positivo solo se agrega a `.zap/rules.tsv` (`<id>  IGNORE  <motivo>`) después de registrarlo en esta tabla.

## 5. Política de admisión (Kyverno)

Política: `k8s/policies/require-signature.yaml` (`ClusterPolicy`, modo `Enforce`).

**Prueba negativa — imagen sin firma (rechazada)**

```bash
kubectl run prueba-sin-firma -n equipo-aulaviva \
  --image=ghcr.io/merinobranco10/equipo-aulaviva-backend@sha256:3f15892977d5569c9dc6822e99bb91cc9908e40b43e0aceda8d2d65e7984efbd
```

Resultado: el webhook de Kyverno rechazó el Pod (`verify-cosign-keyless-signature ... no signatures found`).
Evidencia: `docs/qa/evidence/s10/03-unsigned-image-test.png`

**Prueba positiva — imagen firmada (admitida)**

```bash
kubectl run prueba-firmada -n equipo-aulaviva \
  --image=ghcr.io/merinobranco10/equipo-aulaviva-backend@sha256:6d6837dbc116912742525f44eee0c0132fe6fb442d56634a59203996765921a6
```

Resultado: `pod/prueba-firmada created` (admitido).
Evidencia: `docs/qa/evidence/s10/04-signed-image-test.png`

## 6. Evidencias

- `docs/qa/evidence/s10/01-kubectl-pods-kyberno.png`
- `docs/qa/evidence/s10/02-kubectl-cluster-policy.png`
- `docs/qa/evidence/s10/03-unsigned-image-test.png`
- `docs/qa/evidence/s10/04-signed-image-test.png`
- `docs/qa/evidence/s10/05-pipeline-extend.png`
- `docs/qa/evidence/s10/06-zap-report.png`
