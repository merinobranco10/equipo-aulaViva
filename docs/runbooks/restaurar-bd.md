# Runbook: restaurar la base de datos

**Aplica a:** staging (docker compose / Codespace) · **Dueño:** Valentina León

## Síntoma
La app responde 500 en todas las rutas con datos, o `/actuator/health` aparece `DOWN` por la base de datos.

## Diagnóstico (2 min)
```bash
docker compose ps
docker compose logs --tail=50 db
docker compose logs --tail=50 app
```

## Mitigación
```bash
ls -t backups/ | head -1                      # último respaldo
./scripts/restore.sh backups/<último>.dump
```
Si el volumen se perdió (`docker volume rm equipo-aulaviva_pgdata`), primero `docker compose up -d` y después el restore.

## Verificación
```bash
curl -s localhost:8080/actuator/health                                   # {"status":"UP",...}
curl -s -o /dev/null -w "%{http_code}\n" -u docente.a:$DEMO_PASSWORD \
  localhost:8080/v1/evaluations/<id-conocido>                              # 200
```

## Escalamiento
Si el restore falla 2 veces: avisar a Tech Lead (Matías) y DevSecOps (Valentina) por Discord `#incidentes` y registrar el error en el post-mortem.
