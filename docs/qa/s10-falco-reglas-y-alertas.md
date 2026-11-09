# S10 — Reglas Falco propias y ruta de alertas

**Responsable principal:** Gerardo González (AI/Data)  
**Archivo de reglas:** `falco/rules-custom.yaml`

## 1. Objetivo

Falco permite detectar actividades sospechosas dentro de los contenedores de AulaViva, especialmente en los servicios relacionados con el tutor IA/RAG, que manejan información de estudiantes y credenciales del proveedor del modelo.

Las reglas se aplican a los pods `aulaviva-*` del namespace `equipo-aulaviva`.

## 2. Reglas implementadas

| Regla | Qué detecta | Prioridad | Nivel | ¿Genera incidente? |
|---|---|---|---|---|
| Shell en contenedor | Ejecución de `sh`, `bash` u otra shell dentro de un contenedor | WARNING | Medio | No, requiere revisión |
| Escritura en /etc | Escritura de archivos dentro de `/etc/` | CRITICAL | Alto | Sí |
| Conexión saliente inesperada | Conexión a una IP pública en un puerto distinto de 443 | ERROR | Alto | Sí |

La regla de conexión saliente considera el puerto 443 como permitido para las comunicaciones necesarias con el proveedor del modelo de IA.

## 3. Ruta de alertamiento

1. Falco detecta una actividad sospechosa dentro de un contenedor.
2. La alerta queda registrada en los logs de Falco.
3. Las alertas WARNING son revisadas por el responsable de DevSecOps.
4. Las alertas ERROR y CRITICAL se consideran de nivel alto y requieren registrar un incidente en GitHub con la etiqueta `incident`.
5. El equipo responsable investiga el evento, revisa el pod afectado, aplica las medidas correctivas y documenta el cierre del incidente.

**Responsables:**

- Primera respuesta: Valentina León (DevSecOps).
- Apoyo: Gerardo González (AI/Data), especialmente ante eventos relacionados con el tutor IA/RAG.
- Coordinación: Matías Díaz (Tech Lead).
- Información del incidente: Branco Merino (PO).

**Importante:** la creación del Issue se realiza mediante el procedimiento de respuesta del equipo. Actualmente no existe una integración que cree automáticamente los incidentes desde Falco.

## 4. Evidencia de incidente

Se registró el siguiente incidente en GitHub:

**Issue #48:** `[INCIDENTE] Escritura en /etc de AulaViva - 2026-11-09`

- Etiqueta: `incident`.
- Prioridad: CRITICAL.
- Evento: escritura en `/etc/prueba-falco`.
- Contenedor: `aulaviva-falco-test`.
- Namespace: `equipo-aulaviva`.

## 5. Evidencias de las pruebas

Las siguientes capturas respaldan la instalación, detección de alertas y registro del incidente:

- `docs/qa/evidence/s10/01-falco-instalado.png`
- `docs/qa/evidence/s10/02-alerta-shell.png`
- `docs/qa/evidence/s10/03-alerta-escritura-etc.png`
- `docs/qa/evidence/s10/04-alerta-conexion-saliente.png`
- `docs/qa/evidence/s10/05-issue-incidente.png`

## 6. Cómo se probaron las reglas

Se realizaron pruebas manuales utilizando un pod auxiliar `aulaviva-falco-test` basado en Alpine, dentro del namespace `equipo-aulaviva`.

- `sh -c "echo hola"` → Regla 1: Shell en contenedor.
- `sh -c "echo x >> /etc/prueba-falco"` → Regla 2: Escritura en `/etc`.
- `wget -T 5 -q -O /dev/null http://1.1.1.1` → Regla 3: Conexión saliente inesperada.

Adicionalmente, el workflow automatizado `Falco - Prueba de reglas (S10)` volvió a probar las tres reglas en un clúster temporal de Kubernetes con Kind, utilizando el pod `aulaviva-tutor-test` basado en BusyBox.

La ejecución #10 finalizó correctamente y mostró las tres alertas en el resumen de GitHub Actions.

El backend real utiliza una imagen distroless que no incluye una shell, por lo que se utilizaron pods auxiliares para realizar las pruebas.

## 7. Mejora futura

Integrar Falco con Falcosidekick y Discord para enviar notificaciones automáticas al equipo responsable.
