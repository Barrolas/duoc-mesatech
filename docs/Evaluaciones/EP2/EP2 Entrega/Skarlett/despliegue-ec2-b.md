# Despliegue de ms-solicitudes en la EC2 B

Inventario de Ari (2026-10-30): `../Ari/Inventario_AWS_EP2.md`.

| Dato | Valor |
| --- | --- |
| EC2 | `mesatech-ep2-solicitudes` · `i-0b99595991c09d866` |
| IP privada | `172.31.23.125` |
| IP pública del inventario | `18.207.144.251` (revalidar si el lab se detuvo) |
| RDS | `mesatech-ep2-solicitudes.cg33a3yt7ns0.us-east-1.rds.amazonaws.com:5432` |
| Base | `mesatech_solicitudes` |
| RabbitMQ | `172.31.18.133:5672` · vhost `mesatech` · usuario `solicitudes_consumidor` |
| Cola | `solicitudes.catalogo.queue` |

El JAR escucha en el puerto 8081. Hibernate crea o actualiza `categorias_locales` al arrancar (`ddl-auto=update`). La clave primaria es `categoria_id`, el mismo id que publica Catálogo.

## 1. Compilar

En `ms-solicitudes/`:

```text
mvn -DskipTests package
```

El artefacto queda en `ms-solicitudes/target/ms-solicitudes-0.0.1-SNAPSHOT.jar`.

## 2. Preparar la EC2 B

Solo la primera vez. La EC2 B es una Ubuntu nueva: no trae Java ni la carpeta del JAR. El cliente `psql` sirve para la consulta del paso 4.

```text
ssh -i mesatech-ep2.pem ubuntu@<IP_PUBLICA_EC2_B>
sudo apt-get update
sudo apt-get install -y openjdk-17-jre-headless postgresql-client
java -version
mkdir -p ~/apps
```

## 3. Subir y arrancar

La plantilla de variables es `mesatech.env.example`. En la EC2 el archivo real es `/home/ubuntu/mesatech.env`. El unit `infra/scripts/systemd/mesatech-ms-solicitudes.service` ya lo carga.

```text
scp -i mesatech-ep2.pem ms-solicitudes/target/ms-solicitudes-0.0.1-SNAPSHOT.jar ubuntu@<IP_PUBLICA_EC2_B>:~/apps/ms-solicitudes.jar
scp -i mesatech-ep2.pem infra/scripts/systemd/mesatech-ms-solicitudes.service ubuntu@<IP_PUBLICA_EC2_B>:~/
```

En la EC2, después de copiar `mesatech.env` a `/home/ubuntu/mesatech.env`:

```text
sudo mv ~/mesatech-ms-solicitudes.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now mesatech-ms-solicitudes
sudo systemctl status mesatech-ms-solicitudes
journalctl -u mesatech-ms-solicitudes -n 50 --no-pager
```

`RABBITMQ_DECLARE=false` es obligatorio en esta EC2: Ari ya creó el vhost, el exchange, la cola y el binding. El usuario `solicitudes_consumidor` no debe configurar el broker.

## 4. Comprobar la copia local

```text
psql "host=mesatech-ep2-solicitudes.cg33a3yt7ns0.us-east-1.rds.amazonaws.com port=5432 dbname=mesatech_solicitudes user=mesatech_admin sslmode=require" -c "SELECT categoria_id, nombre, event_id, fecha_evento, recibida_en FROM categorias_locales ORDER BY recibida_en;"
```

Esa consulta se hace desde la EC2 B (la RDS no es pública). El log de un consumo correcto trae el `categoria_id`, el nombre y el `event_id`, y aparece solo después del `basicAck`.

## 5. Lo que todavía bloquea el arranque

- Contraseñas de RDS y de `solicitudes_consumidor` (Ari, fuera de git).
- Clave `mesatech-ep2.pem` y lab de AWS Academy encendido.
- El security group de la EC2 B hoy solo abre el SSH desde la IP de administración. El BFF tiene que poder llegar a `172.31.23.125:8081`.
- Nicolás aún no publica `categoria.creada` desde `ms-catalogo`. Sin ese mensaje no hay fila que mostrar en P2.
- Ninna debe exponer `GET /v1/solicitudes/categorias` en API Gateway, igual que el resto de `/v1/solicitudes`.
