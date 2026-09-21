#!/usr/bin/env bash
# Arranque por-boot: levanta el Docker daemon y PostgreSQL 16.
# init.sql crea las bases mesatech_solicitudes y mesatech_catalogo.
# Idempotente: tolera reinicios y no duplica procesos.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# --- 1. Asegurar que el Docker daemon esté corriendo ---
if ! sudo docker info >/dev/null 2>&1; then
  echo "==> Iniciando dockerd"
  sudo bash -c 'nohup dockerd >/tmp/dockerd.log 2>&1 &'
  for _ in $(seq 1 30); do
    sudo docker info >/dev/null 2>&1 && break
    sleep 2
  done
fi
if ! sudo docker info >/dev/null 2>&1; then
  echo "ERROR: dockerd no arrancó." >&2
  tail -n 20 /tmp/dockerd.log 2>/dev/null || true
  exit 1
fi

# --- 2. PostgreSQL vía docker compose (idempotente) ---
echo "==> Levantando PostgreSQL (infra/docker-compose.yml)"
sudo docker compose -f infra/docker-compose.yml up -d

# --- 3. Esperar hasta que PostgreSQL esté healthy ---
for _ in $(seq 1 40); do
  st="$(sudo docker inspect -f '{{.State.Health.Status}}' mesatech-postgres 2>/dev/null || true)"
  if [ "$st" = "healthy" ]; then
    echo "==> PostgreSQL healthy."
    break
  fi
  sleep 3
done

echo "==> start.sh completado."
