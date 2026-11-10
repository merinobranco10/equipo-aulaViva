# S10 — Reglas Falco propias y ruta de alertas

**Responsable principal:** Gerardo González — AI/Data
**Sesión:** S10
**Archivos:** `falco/rules-custom.yaml`, `falco/values.yaml`

## 1. Instalación

Falco y Falcosidekick se instalan con Helm en el namespace `falco` (comandos en la sección 6). Las reglas se cargan desde `falco/rules-custom.yaml`; las credenciales de alertamiento viven en el Secret `falcosidekick-secrets`, nunca en el repositorio.

## 2. Reglas

| Regla | Qué detecta | Prioridad | Severidad |
|---|---|---|---|
| `AulaViva - Shell en contenedor` | Una shell (`sh`, `bash`…) en cualquier pod de los namespaces `equipo-aulaviva*`. Las imágenes son distroless: no debería haber shells. | CRITICAL | High |
| `AulaViva - Escritura en /etc` | Apertura para escritura de archivos bajo `/etc` en un contenedor de AulaViva. | ERROR | High |
| `AulaViva - Conexión saliente inesperada del Tutor IA` | Conexión del Tutor IA/RAG hacia un puerto fuera de la lista permitida (DNS, 443, 5432, 8080, Kafka). **Adaptada al Tutor IA**: protege consultas, embeddings y datos de estudiantes. | CRITICAL | High |

Los outputs no incluyen la línea de comandos para no enviar tokens ni datos de estudiantes a Slack o PagerDuty.

## 3. Severidad

Falco no tiene nivel "High"; se define así:

| Prioridad Falco | Severidad AulaViva | Acción |
|---|---|---|
| EMERGENCY, ALERT, CRITICAL, ERROR | **High** | Notificación en Slack **y incidente en PagerDuty** |
| WARNING | Medium | Solo Slack |
| NOTICE o menor | Low | Solo logs de Falco |

## 4. Ruta de alertas

```
Falco (DaemonSet) -> Falcosidekick -> Slack  #aulaviva-seguridad   (Warning o superior)
                                   -> PagerDuty, servicio "AulaViva - Falco" (Error o superior)
                                         -> incidente [High] -> persona de turno
```

| Destino | Quién lo recibe | Qué ocurre |
|---|---|---|
| Slack `#aulaviva-seguridad` | Todo el equipo | Mensaje con regla, namespace, pod, contenedor e imagen. |
| PagerDuty (servicio "AulaViva - Falco") | Primera respuesta: Gerardo González (AI/Data, Tutor IA). Apoyo: Valentina León (DevSecOps). | **Se abre un incidente automáticamente** por cada alerta High. |

*(Confirmar con el equipo el canal, las personas de turno y el escalamiento.)*

## 5. Respuesta al incidente

1. **Reconocer** el incidente en PagerDuty (objetivo: 15 minutos).
2. **Preservar evidencia** antes de borrar nada: `kubectl logs` y `kubectl describe pod` del pod afectado.
3. **Contener**: aislar o eliminar el pod; el Deployment crea uno limpio.
4. **Evaluar**: si la regla fue la de salida de red o shell en el pod del backend, rotar `AI_API_KEY`, `DB_PASSWORD` y `VECTOR_DB_PASSWORD` y revisar si hubo acceso a datos de estudiantes.
5. **Cerrar** el incidente con causa y acción tomada; ajustar la regla si fue un falso positivo.

## 6. Instalación y pruebas

```bash
# validar sintaxis de las reglas
docker run --rm -v "${PWD}/falco:/rules:ro" falcosecurity/falco-no-driver:latest falco -V /rules/rules-custom.yaml

# instalar
helm repo add falcosecurity https://falcosecurity.github.io/charts && helm repo update
kubectl create namespace falco
kubectl -n falco create secret generic falcosidekick-secrets \
  --from-literal=SLACK_WEBHOOKURL='<webhook de Slack>' \
  --from-literal=PAGERDUTY_ROUTINGKEY='<integration key de PagerDuty>'
helm upgrade --install falco falcosecurity/falco -n falco -f falco/values.yaml \
  --set-file "customRules.rules-custom\.yaml=falco/rules-custom.yaml"

# pod de prueba (imita al Tutor IA por su nombre)
kubectl -n equipo-aulaviva run aulaviva-tutor-ia-falco-test --image=busybox:1.36 --restart=Never -- sleep 3600

# disparar cada regla
kubectl -n equipo-aulaviva exec aulaviva-tutor-ia-falco-test -- sh -c "echo prueba"
kubectl -n equipo-aulaviva exec aulaviva-tutor-ia-falco-test -- touch /etc/falco-test
kubectl -n equipo-aulaviva exec aulaviva-tutor-ia-falco-test -- wget -T 3 -q -O /dev/null http://example.com

# ver las alertas
kubectl -n falco logs -l app.kubernetes.io/name=falco -c falco --since=10m | grep AulaViva

# limpiar
kubectl -n equipo-aulaviva delete pod aulaviva-tutor-ia-falco-test
```

## 7. Resultados

Completar con lo observado.

| Regla | Comando | Alerta en Falco | Slack | Incidente PagerDuty | Evidencia |
|---|---|---|---|---|---|
| Shell en contenedor | `exec … sh -c` | _pendiente_ | _pendiente_ | _pendiente_ | `docs/qa/evidence/s10/falco-01-shell.png` |
| Escritura en /etc | `exec … touch /etc/…` | _pendiente_ | _pendiente_ | _pendiente_ | `docs/qa/evidence/s10/falco-02-etc.png` |
| Conexión saliente | `exec … wget` | _pendiente_ | _pendiente_ | _pendiente_ | `docs/qa/evidence/s10/falco-03-egress.png` |

## 8. Limitaciones

- La regla de salida filtra por **puerto**, no por dominio: una exfiltración por el puerto 443 no se detecta.
- El Tutor IA hoy corre dentro del pod `aulaviva-backend`; la regla cubre ese pod y los que se llamen `aulaviva-tutor-ia*`.
- La escritura en `/etc` solo se registra si la apertura tiene éxito; con `readOnlyRootFilesystem` el intento falla y Falco no lo ve.
