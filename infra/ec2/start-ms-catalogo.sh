#!/usr/bin/env bash
set -euo pipefail
ENV_FILE="${HOME}/apps/ms-catalogo.env"
JAR="${HOME}/apps/ms-catalogo.jar"
LOG="${HOME}/apps/ms-catalogo.log"
sed -i 's/\r$//' "$ENV_FILE" 2>/dev/null || true
set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a
pkill -f 'ms-catalogo.jar' 2>/dev/null || true
sleep 1
: > "$LOG"
nohup java -jar "$JAR" >> "$LOG" 2>&1 &
echo "started PID $!"
sleep 12
tail -n 50 "$LOG"
ss -tlnp | grep 8082 || true
