# Inventario AWS — EP2 MesaTech Cloud

**Responsable:** Ari  
**Cuenta / lab:** AWS Academy Learner Lab  
**Región:** us-east-1  
**Fecha:** 2026-10-30  

> Completar sin contraseñas. Las claves van solo por canal privado del equipo.

---

## 1. EC2

| Rol | Nombre | Instance ID | Tipo | AZ | IPv4 privada | IPv4 pública | Key pair | SG (id) |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| EC2 C · RabbitMQ | mesatech-ep2-rabbitmq | i-05fc13c5d3273e566 | t3.medium | us-east-1b | 172.31.18.133 | 54.90.144.4 | mesatech-ep2 | sg-0194df0045c5f001c |
| EC2 A · Catálogo | mesatech-ep2-catalogo | i-0a7c485a5fd3c064c | t3.medium | us-east-1b | 172.31.17.69 | 98.89.1.84 | mesatech-ep2 | sg-054c453030ed5f8e8 |
| EC2 B · Solicitudes | mesatech-ep2-solicitudes | i-0b99595991c09d866 | t3.medium | us-east-1b | 172.31.23.125 | 18.207.144.251 | mesatech-ep2 | sg-0571f2e504fd01ab3 |

**SO broker:** Ubuntu 24.04 LTS  
**Docker / Compose (EC2 C):** Docker 29.9.2 · Compose v5.6.1  
**Imagen RabbitMQ:** `rabbitmq:4.2-management`  
**Compose path en EC2 C:** `~/rabbitmq/compose.yaml` (+ `.env` local, no en git)

---

## 2. RDS

| Rol | Identifier | Motor | Clase | AZ | Endpoint | Puerto | DB name | Public access | SG |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RDS Catálogo | mesatech-ep2-catalogo | PostgreSQL | db.t3.micro | us-east-1d | mesatech-ep2-catalogo.cg33a3yt7ns0.us-east-1.rds.amazonaws.com | 5432 | mesatech_catalogo | No | mesatech-ep2-rds-catalogo-sg / sg-0c718b6d89f69f687 |
| RDS Solicitudes | mesatech-ep2-solicitudes | PostgreSQL | db.t3.micro | us-east-1d | mesatech-ep2-solicitudes.cg33a3yt7ns0.us-east-1.rds.amazonaws.com | 5432 | mesatech_solicitudes | No | mesatech-ep2-rds-solicitudes-sg / sg-01ecd39653046dcb7 |

Master user (ambos): `mesatech_admin` — password solo fuera del repo.

---

## 3. RabbitMQ (recursos)

| Recurso | Valor |
| --- | --- |
| Management UI | `http://54.90.144.4:15672` |
| AMQP (desde A/B) | `172.31.18.133:5672` |
| Virtual host | `mesatech` |
| Admin UI user | `mesatech_admin` |
| Usuario publicador | `catalogo_publicador` |
| Usuario consumidor | `solicitudes_consumidor` |
| Exchange | `catalogo.exchange` (direct, durable) |
| Routing key | `categoria.creada` |
| Cola | `solicitudes.catalogo.queue` (durable, classic) |

---

## 4. Matriz de security groups (resumen)

| Destino | Puerto | Origen autorizado |
| --- | --- | --- |
| EC2 RabbitMQ | 22 | IP admin `/32` |
| EC2 RabbitMQ | 15672 | IP admin `/32` |
| EC2 RabbitMQ | 5672 | sg-054c453030ed5f8e8 (Catálogo) + sg-0571f2e504fd01ab3 (Solicitudes) |
| RDS Catálogo | 5432 | sg-054c453030ed5f8e8 (EC2 Catálogo) |
| RDS Solicitudes | 5432 | sg-0571f2e504fd01ab3 (EC2 Solicitudes) |
| EC2 Catálogo / Solicitudes | 22 | IP admin `/32` |

IP admin usada en lab: `190.114.35.100/32` (actualizar si cambia).

---

## 5. Handoff al equipo

| Para | Qué entregar (sin secretos en git) |
| --- | --- |
| Nicolás | AMQP `172.31.18.133:5672`, vhost `mesatech`, user `catalogo_publicador`, exchange `catalogo.exchange` / key `categoria.creada`, endpoint RDS Catálogo, IP privada EC2 A `172.31.17.69` |
| Skarlett | AMQP `172.31.18.133:5672`, vhost `mesatech`, user `solicitudes_consumidor`, cola `solicitudes.catalogo.queue`, endpoint RDS Solicitudes, IP privada EC2 B `172.31.23.125` |
| Giannina | Instance IDs, nombres, endpoints RDS (para diagrama) |

Contraseñas RabbitMQ app + master RDS: solo chat/privado del equipo.