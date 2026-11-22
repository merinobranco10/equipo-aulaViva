#!/usr/bin/env bash
# Respaldo de la base de PostgreSQL que corre en docker compose (staging / Codespace).
# Uso: ./scripts/backup.sh
set -euo pipefail
TS=$(date +%Y%m%d-%H%M%S)
mkdir -p backups
docker compose exec -T db pg_dump -U aulaviva -Fc aulaviva > "backups/aulaviva-$TS.dump"
echo "Backup listo: backups/aulaviva-$TS.dump ($(du -h "backups/aulaviva-$TS.dump" | cut -f1))"
