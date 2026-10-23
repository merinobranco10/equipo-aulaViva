# Validación Final S08 — Pipeline CI, SAST y GitOps

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 22 de octubre de 2026
**Sesión:** S08

## 1. Objetivo
Validar que el flujo de integración continua, la seguridad del código y la estrategia de despliegue GitOps definida en el ADR 0006 funcionan correctamente y bloquean código defectuoso.

## 2. Validación del Pipeline de CI (Quality Gates)
Se verificó el flujo en GitHub Actions sobre la rama `desarrollo`:
- ✅ **Lint:** El código pasa las reglas de formato de Google (Checkstyle).
- ✅ **Pruebas:** Las pruebas unitarias de los endpoints se ejecutan correctamente.
- ✅ **Cobertura:** Se supera el umbral mínimo del 70% exigido por JaCoCo.
- ✅ **SAST:** El análisis estático con Semgrep no detectó vulnerabilidades críticas.

**Evidencia:** `docs/qa/evidence/s08-tests-coverage.png`

## 3. Validación de Protección de Ramas (Branch Protection)
Sin necesidad de acceder a la configuración del repositorio, se validó el comportamiento mediante GitHub:
- Al intentar subir cambios directamente a `main` o `desarrollo`, el sistema bloquea la acción y exige la creación de un Pull Request.
- El botón de "Merge" en los Pull Requests permanece bloqueado hasta que el workflow de "CI Quality Gates" termina en verde.
- La rama `main` requiere la aprobación de al menos un revisor antes de fusionarse.

**Evidencia:** `docs/qa/evidence/s08-branch-protection.png` (Captura del PR con los checks en verde y el botón de merge habilitado).

## 4. Validación de la Estrategia GitOps (ADR 0006)
Se revisó el ADR 0006 y se valida la decisión técnica:
- El modelo "pull" con Argo CD asegura que el pipeline (GitHub Actions) no tenga credenciales de administrador del clúster.
- Los entornos `dev` (automático) y `prod` (manual) garantizan un equilibrio entre velocidad y control.
- El uso de digests inmutables (SHA-256) en lugar de tags mutables (latest) evita despliegues accidentales de versiones incorrectas.

## 5. Conclusión
La arquitectura de despliegue y el pipeline de calidad están alineados con los estándares de la industria. El sistema es seguro, trazable y automatizado, cumpliendo con todos los requisitos de la S08.

**Validado por:** Alexander Ruiz-Tagle — QA Lead
