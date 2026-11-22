#!/usr/bin/env bash
# Restaura un respaldo hecho con scripts/backup.sh y espera a que la app quede sana.
# Uso: ./scripts/restore.sh backups/aulaviva-AAAAMMDD-HHMMSS.dump
set -euo pipefail
FILE=${1:?Indica el archivo .dump}
docker compose up -d db
until docker compose exec -T db pg_isready -U aulaviva >/dev/null; do sleep 1; done
docker compose stop app
docker compose exec -T db pg_restore -U aulaviva -d aulaviva --clean --if-exists < "$FILE"
docker compose start app
until curl -sf http://localhost:8080/actuator/health >/dev/null; do sleep 2; done
echo "Restore completo y app sana"
