#!/usr/bin/env bash
set -euo pipefail

echo "=== 1. Contenedores ==="
docker compose -f docker-compose.kafka-ec2d.yml ps

echo "=== 2. Topicos (3 particiones cada uno) ==="
docker exec mesatech-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --describe

echo "=== 3. Grupos de consumo (lag) ==="
for grp in mesatech-solicitudes-kafka mesatech-catalogo-kafka; do
  echo "--- Grupo: $grp ---"
  docker exec mesatech-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
    --bootstrap-server localhost:9092 --group "$grp" --describe || true
done
