#!/usr/bin/env bash
# Idempotent bootstrap para el entorno de desarrollo de MesaTech Cloud (Cloud Agents / local).
# - Instala Maven y Docker si faltan (Node.js y Java 17+ ya vienen en la imagen base).
# - Configura Docker para VMs anidadas (storage-driver vfs).
# - Compila los tres servicios Spring Boot y las dependencias del frontend.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# --- 1. Herramientas de sistema ausentes en la imagen base: Maven + Docker ---
NEED_APT=()
command -v mvn >/dev/null 2>&1 || NEED_APT+=(maven)
command -v docker >/dev/null 2>&1 || NEED_APT+=(docker.io docker-compose-v2)
if [ ${#NEED_APT[@]} -gt 0 ]; then
  echo "==> Instalando paquetes de sistema: ${NEED_APT[*]}"
  sudo apt-get update -y
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y "${NEED_APT[@]}"
fi

# --- 2. Docker daemon: 'vfs' funciona de forma fiable en VMs anidadas de Cloud Agents ---
sudo mkdir -p /etc/docker
if [ ! -f /etc/docker/daemon.json ] || ! grep -q '"vfs"' /etc/docker/daemon.json; then
  echo "==> Escribiendo /etc/docker/daemon.json (storage-driver vfs)"
  echo '{ "storage-driver": "vfs", "features": { "containerd-snapshotter": false } }' \
    | sudo tee /etc/docker/daemon.json >/dev/null
fi

# --- 3. Backend: compilar los tres servicios Spring Boot (puebla ~/.m2) ---
for module in bff ms-solicitudes ms-catalogo; do
  echo "==> Compilando $module"
  mvn -B -q -f "$module/pom.xml" -DskipTests package
done

# --- 4. Frontend: dependencias (no hay lockfile -> npm install) ---
echo "==> Instalando dependencias del frontend"
( cd frontend && npm install )

echo "==> install.sh completado."
