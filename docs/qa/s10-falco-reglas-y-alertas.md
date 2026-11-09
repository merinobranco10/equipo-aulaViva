# S10 — Reglas Falco propias y ruta de alertas

**Responsable principal:** Gerardo González (AI/Data)
**Archivo de reglas:** `falco/rules-custom.yaml`

## Reglas

| Regla | Qué detecta | Prioridad | Nivel | ¿Abre incidente? |
|---|---|---|---|---|
| Shell en contenedor | Se abre `sh`/`bash` en un pod de AulaViva | WARNING | Medio | No |
| Escritura en /etc | Un proceso escribe en `/etc/` | CRITICAL | High | Sí |
| Conexión saliente inesperada | Conexión a IP pública en puerto distinto de 443 | ERROR | High | Sí |

Las reglas vigilan los pods `aulaviva-*` del namespace `equipo-aulaviva`, por lo
que cubren al tutor IA/RAG (datos de estudiantes y clave del LLM). Se permite el
puerto 443 porque el tutor debe llamar al proveedor del modelo.

## Ruta de alertas

1. Falco detecta el evento en el nodo del clúster.
2. La alerta se escribe en los logs de Falco (`kubectl logs -n falco`).
3. Si es High (ERROR o CRITICAL), se abre un Issue en GitHub con la etiqueta `incident`.
4. Primera respuesta: DevSecOps (Valentina León). También se avisa a Gerardo González (dueño del tutor) y a Matías Díaz (Tech Lead).

Mejora futura: enviar las alertas a Discord con Falcosidekick.

## Evidencias

- `docs/qa/evidence/s10/01-falco-instalado.png`
- `docs/qa/evidence/s10/02-alerta-shell.png`
- `docs/qa/evidence/s10/03-alerta-escritura-etc.png`
- `docs/qa/evidence/s10/04-alerta-conexion-saliente.png`
- `docs/qa/evidence/s10/05-issue-incidente.png`

## Cómo se probó

Pod auxiliar `aulaviva-falco-test` (alpine) en `equipo-aulaviva`:
- `sh -c "echo hola"` → regla 1
- `sh -c "echo x >> /etc/prueba-falco"` → regla 2
- `wget -T 5 -q -O /dev/null http://1.1.1.1` → regla 3

El backend real usa imagen distroless (sin shell), por eso se probó con un pod auxiliar.
