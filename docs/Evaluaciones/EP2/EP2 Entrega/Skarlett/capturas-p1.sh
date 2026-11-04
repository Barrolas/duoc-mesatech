#!/usr/bin/env bash
# Muestra en pantalla la evidencia de P1 desde la EC2 B, para capturarla.
# Uso: bash capturas-p1.sh <IP_PUBLICA_EC2_B> [ruta-llave.pem]
# Las contraseñas se leen de ~/mesatech.env en la EC2; no se muestran.
set -euo pipefail
IP="${1:?Falta la IP pública de la EC2 B}"
KEY="${2:-$HOME/.ssh/mesatech-ep2.pem}"

ssh -t -i "$KEY" "ubuntu@$IP" '
clear
echo "===== P1-ec2-b-proceso ====="
echo "Instancia: $(T=$(curl -s -X PUT http://169.254.169.254/latest/api/token -H "X-aws-ec2-metadata-token-ttl-seconds: 60"); curl -s -H "X-aws-ec2-metadata-token: $T" http://169.254.169.254/latest/meta-data/instance-id)  ·  IP privada: $(hostname -I)"
echo
systemctl status mesatech-ms-solicitudes --no-pager -n 0
echo
journalctl -u mesatech-ms-solicitudes --no-pager -o cat | grep -E "HikariPool-1 - Start completed|Created new connection|Started SolicitudesApplication" | tail -3 | cut -c1-200
echo
read -p "Captura P1-ec2-b-proceso.png y presiona Enter... "
clear
echo "===== P1-rds-solicitudes ====="
set -a; . ~/mesatech.env; set +a
PGPASSWORD="$POSTGRES_PASSWORD" psql "host=mesatech-ep2-solicitudes.cg33a3yt7ns0.us-east-1.rds.amazonaws.com port=5432 dbname=mesatech_solicitudes user=$POSTGRES_USER sslmode=require" \
  -c "SELECT current_database(), inet_server_addr() AS ip_rds;" \
  -c "\dt" \
  -c "SELECT categoria_id, nombre, event_id, fecha_evento, recibida_en FROM categorias_locales ORDER BY recibida_en;"
read -p "Captura P1-rds-solicitudes.png y presiona Enter... "
'
