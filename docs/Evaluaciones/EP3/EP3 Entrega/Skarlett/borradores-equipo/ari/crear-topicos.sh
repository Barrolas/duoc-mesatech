#!/usr/bin/env bash
set -euo pipefail

BOOTSTRAP="${1:-localhost:9092}"
KAFKA_BIN="${KAFKA_BIN:-/opt/kafka/bin/kafka-topics.sh}"

for topic in \
  mesatech.solicitud.creada \
  mesatech.solicitud.estado \
  mesatech.catalogo.prioridad \
  mesatech.dlq.eventos
do
  "$KAFKA_BIN" --bootstrap-server "$BOOTSTRAP" \
    --create --if-not-exists \
    --topic "$topic" \
    --partitions 3 \
    --replication-factor 1
done

"$KAFKA_BIN" --bootstrap-server "$BOOTSTRAP" --list
for topic in mesatech.solicitud.creada mesatech.solicitud.estado mesatech.catalogo.prioridad mesatech.dlq.eventos; do
  "$KAFKA_BIN" --bootstrap-server "$BOOTSTRAP" --describe --topic "$topic"
done
