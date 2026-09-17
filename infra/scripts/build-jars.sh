#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$(cd "$(dirname "$0")/.." && pwd)/out"
mkdir -p "$OUT"

for m in bff ms-solicitudes ms-catalogo; do
  echo ">>> mvn package -DskipTests ($m)"
  (cd "$ROOT/$m" && mvn -q -DskipTests package)
  jar=$(ls "$ROOT/$m/target/"*.jar 2>/dev/null | grep -v sources | grep -v javadoc | head -1)
  cp "$jar" "$OUT/$m.jar"
done
echo "Listo: $OUT"
