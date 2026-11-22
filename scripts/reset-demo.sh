#!/usr/bin/env bash
# AulaViva - Deja la demo con los datos originales de la semilla (Codespace / docker compose).
# Borra todo lo creado en los ensayos (evaluaciones nuevas, entregas, notas) y vuelve a cargar
# los 2 colegios, 6 usuarios y 4 evaluaciones de DemoDataLoader. Termina con el smoke test.
# Uso: DEMO_PASSWORD='<clave>' ./scripts/reset-demo.sh   (en el Codespace la variable ya existe)
set -euo pipefail
echo "Borrando la base de datos de la demo..."
docker compose down -v
docker compose up -d
echo "Esperando a que la app esté arriba..."
until curl -sf http://localhost:8080/actuator/health >/dev/null; do sleep 3; done
echo "Datos de la semilla cargados."
"$(dirname "$0")/smoke.sh"
