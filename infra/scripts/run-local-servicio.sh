#!/usr/bin/env bash
# Arranca un servicio del stack local en primer plano, con las variables de .env (raíz del repo).
# Uso: bash infra/scripts/run-local-servicio.sh ms-solicitudes|ms-catalogo|bff
# Detener: Ctrl+C en su terminal (sirve para P3: consumidor detenido).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SVC="${1:?Indica el servicio: ms-solicitudes, ms-catalogo o bff}"
JAR="$ROOT/infra/out/$SVC.jar"
[ -f "$ROOT/.env" ] || { echo "Falta $ROOT/.env (copiar desde env.example)"; exit 1; }
[ -f "$JAR" ] || { echo "Falta $JAR: ejecuta bash infra/scripts/build-jars.sh"; exit 1; }

set -a
# shellcheck disable=SC1091
. <(tr -d '\r' < "$ROOT/.env")
set +a

cd "$ROOT"
exec java -jar "$JAR"
