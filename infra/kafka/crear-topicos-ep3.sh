#!/usr/bin/env bash
# Uso en EC2 D (Linux): bash infra/kafka/crear-topicos-ep3.sh
# Ajustar CONTAINER y BOOTSTRAP si el compose en cloud usa otros nombres.

set -euo pipefail

CONTAINER="${KAFKA_CONTAINER:-mesatech-kafka}"
BOOTSTRAP="${KAFKA_BOOTSTRAP_INTERNAL:-localhost:29092}"

topics=(
  mesatech.solicitud.creada
  mesatech.solicitud.estado
  mesatech.catalogo.prioridad
  mesatech.dlq.eventos
)

if ! docker inspect -f '{{.State.Running}}' "$CONTAINER" 2>/dev/null | grep -q true; then
  echo "Contenedor $CONTAINER no está en ejecución." >&2
  exit 1
fi

for name in "${topics[@]}"; do
  echo "Tópico: $name (3 particiones)"
  docker exec "$CONTAINER" kafka-topics \
    --bootstrap-server "$BOOTSTRAP" \
    --create --if-not-exists \
    --topic "$name" \
    --partitions 3 \
    --replication-factor 1
done

echo ""
docker exec "$CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP" --list