#!/usr/bin/env bash
# AulaViva - Recorre con curl los pasos 1, 2, 5 y 6 de la demo (S18, Dia 1).
# Requisitos: la app corriendo con perfil demo y la misma DEMO_PASSWORD.
# Uso (Git Bash o Codespaces):
#   DEMO_PASSWORD='tu-clave' ./scripts/demo-dia1.sh
#   BASE=https://<codespace>-8080.app.github.dev DEMO_PASSWORD='tu-clave' ./scripts/demo-dia1.sh
set -euo pipefail

B=${BASE:-http://localhost:8080}
P=${DEMO_PASSWORD:?Define DEMO_PASSWORD con la misma clave con que levantaste la app}
key() { echo "demo-$(date +%s)-$RANDOM"; }
paso() { printf '\n== %s\n' "$1"; }

paso "0. La app responde"
curl -sSf "$B/api/v1/status"; echo

paso "1. docente.a crea una evaluacion de 2 preguntas"
CREADA=$(curl -sS -u "docente.a:$P" -H 'Content-Type: application/json' -H "Idempotency-Key: $(key)" \
  -d '{"courseId":"matematica-8a","title":"Demo: enteros","questions":[
        {"text":"¿Cuál es el resultado de (-5) · 3?","options":["15","-15","-8"],"correctOption":1,"points":2,
         "explanation":"Signos distintos: el producto es negativo."},
        {"text":"¿Cuál es el resultado de (-20) : (-4)?","options":["5","-5","-24"],"correctOption":0,"points":2}]}' \
  "$B/v1/evaluations")
echo "$CREADA"
EVAL=$(echo "$CREADA" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
[ -n "$EVAL" ] || { echo "No se pudo crear la evaluacion"; exit 1; }

paso "2a. estudiante.a ve la evaluacion (sin respuestas correctas)"
VISTA=$(curl -sS -u "estudiante.a:$P" "$B/v1/evaluations/$EVAL")
echo "$VISTA"
Q1=$(echo "$VISTA" | grep -o '"id":"[^"]*"' | sed -n '2p' | cut -d'"' -f4)
Q2=$(echo "$VISTA" | grep -o '"id":"[^"]*"' | sed -n '3p' | cut -d'"' -f4)

paso "2b. estudiante.a rinde (1 correcta y 1 incorrecta) y recibe nota + feedback"
curl -sS -u "estudiante.a:$P" -H 'Content-Type: application/json' -H "Idempotency-Key: $(key)" \
  -d "{\"answers\":[{\"questionId\":\"$Q1\",\"selectedOption\":1},{\"questionId\":\"$Q2\",\"selectedOption\":1}]}" \
  "$B/v1/evaluations/$EVAL/submissions"; echo

paso "5. apoderado.a ve el avance de su pupilo"
curl -sS -u "apoderado.a:$P" "$B/v1/students/u-estudiante-a/progress"; echo

paso "6. docente.b (otro colegio) intenta abrir la evaluacion de A -> debe ser 404"
CODE=$(curl -s -o /dev/null -w '%{http_code}' -u "docente.b:$P" "$B/v1/evaluations/$EVAL")
echo "HTTP $CODE"
[ "$CODE" = "404" ] && echo "OK: aislamiento entre colegios" || { echo "FALLA: se esperaba 404"; exit 1; }

paso "Extra. Sin credenciales -> 401"
curl -s -o /dev/null -w 'HTTP %{http_code}\n' "$B/v1/me"

echo; echo "Demo del Dia 1 completa."
