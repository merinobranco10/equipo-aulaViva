# Validación QA — Supply Chain S10

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 09 de noviembre de 2026

## 1. Objetivo
Validar que la cadena de suministro de AulaViva está protegida, asegurando que solo se despliegue código firmado y que el pipeline detecte vulnerabilidades dinámicas, de infraestructura y en tiempo de ejecución.

## 2. Validaciones Realizadas en el Pipeline
Como QA, certifico que los siguientes controles están operativos en GitHub Actions:
- **DAST:** OWASP ZAP analiza la aplicación en ejecución para detectar fallas como XSS o falta de cabeceras de seguridad.
- **IaC Scan:** Checkov analiza el código de `terraform/` en busca de configuraciones inseguras.
- **Container Scan:** Trivy escanea la imagen final de Docker y bloquea si hay vulnerabilidades HIGH o CRITICAL.
- **Firma y SLSA:** El pipeline firma la imagen con Cosign y adjunta la attestation de procedencia.

## 3. Validaciones en el Clúster (Kubernetes)
- **Admission Control (Kyverno):** La política `require-image-signature` asegura que Kubernetes rechace cualquier imagen no firmada por nuestro workflow de CI/CD.
- **Runtime Security (Falco):** Se validaron las 3 reglas de detección en tiempo real. Para más detalle sobre las pruebas y la ruta de incidentes, revisar el documento técnico `docs/qa/s10-falco-reglas-y-alertas.md` elaborado por el equipo de DevSecOps.

## 4. Conclusión
El pipeline de CI/CD y el clúster de Kubernetes cuentan con múltiples capas de seguridad (Prevención, Detección y Reacción). Cualquier intento de inyectar código no firmado o vulnerar los contenedores será bloqueado y registrado como incidente.

**Validado por:** Alexander Ruiz-Tagle — QA Lead
