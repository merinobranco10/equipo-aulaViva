# Validación Final S08 — Pipeline CI, SAST y GitOps

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 22 de octubre de 2026
**Sesión:** S08

## 1. Objetivo
Validar que el flujo de integración continua, la seguridad del código y la estrategia de despliegue GitOps definida en el ADR 0006 funcionan correctamente.

## 2. Validación del Pipeline de CI (Quality Gates)
Se verificó el flujo en GitHub Actions sobre la rama `desarrollo`:
- ✅ **Lint:** El código pasa las reglas de formato de Google (Checkstyle).
- ✅ **Pruebas:** Las pruebas unitarias se ejecutan correctamente.
- ✅ **Cobertura:** Se supera el umbral mínimo del 70% exigido por JaCoCo.
- ✅ **SAST:** El análisis estático con Semgrep no detectó vulnerabilidades críticas.

**Evidencia:** `docs/qa/evidence/s08-tests-coverage.png`

## 3. Validación de Protección de Ramas (Hallazgo QA)
Se intentó validar la protección de ramas. Se realizó la prueba de intentar un commit directo a `desarrollo`.
- ⚠️ **Hallazgo (Riesgo):** La rama `desarrollo` **permite actualmente commits directos**. No hay una regla de protección activa que obligue a usar Pull Requests o que exija la aprobación de los Quality Gates.
- **Recomendación:** Se sugiere a la administración del repositorio activar la protección de ramas en GitHub (Settings > Branches) para exigir PRs y checks en verde antes de fusionar.

## 4. Validación de la Estrategia GitOps (ADR 0006)
Se revisó el ADR 0006 y se valida la decisión técnica:
- El modelo "pull" con Argo CD asegura que el pipeline (GitHub Actions) no tenga credenciales de administrador del clúster.
- Los entornos `dev` (automático) y `prod` (manual) garantizan un equilibrio entre velocidad y control.
- El uso de digests inmutables (SHA-256) evita despliegues accidentales de versiones incorrectas.

## 5. Conclusión
El pipeline de calidad y la arquitectura GitOps están correctos. Sin embargo, queda como acción pendiente para el equipo activar la protección de la rama `desarrollo` para evitar que se suba código sin pasar por los Quality Gates.

**Validado por:** Alexander Ruiz-Tagle — QA Lead
