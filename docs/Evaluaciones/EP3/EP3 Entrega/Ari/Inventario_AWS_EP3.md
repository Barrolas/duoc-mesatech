# Inventario AWS — EP3 MesaTech

**Responsable:** Ari  
**Cuenta / lab:** AWS Academy Learner Lab (misma cuenta que EP2, reutilizada el 2026-11-27 por créditos restantes: `4329-7071-7799`)  
**Región:** us-east-1  
**VPC:** `vpc-003265ec261b137e2`  
**AZ EC2:** us-east-1b  
**AZ RDS:** us-east-1d (misma VPC; se conectan sin problema)  
**Última actualización:** 2026-11-28 (America/Santiago)

> Sin contraseñas en este archivo. Tras cada reinicio del lab, confirmar que las **EIP** siguen asociadas (ver [§ 8. Operación del lab](#8-operación-del-lab)).

## 1. EC2

| Rol | Nombre | Instance ID | Tipo | AZ | IPv4 privada | **Elastic IP** | SG |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A · Catálogo | mesatech-ep2-catalogo | i-0a7c485a5fd3c064c | t3.medium | us-east-1b | 172.31.17.69 | **184.196.102.98** | sg-054c453030ed5f8e8 |
| B · Solicitudes | mesatech-ep2-solicitudes | i-0b99595991c09d866 | t3.medium | us-east-1b | 172.31.23.125 | **3.221.105.184** | sg-0571f2e504fd01ab3 |
| C · Rabbit | mesatech-ep2-rabbitmq | i-05fc13c5d3273e566 | t3.medium | us-east-1b | 172.31.18.133 | **32.199.222.166** | sg-0194df0045c5f001c |
| D · Kafka | mesatech-ep3-kafka | i-06e5ec5c80e7d4101 | t3.medium | us-east-1b | 172.31.19.10 | **52.207.167.121** | sg-075e1b51598ab0923 |

**SO:** Ubuntu 24.04 LTS (todas).  
**SSH:** `ubuntu@<EIP>` con el par de claves `mesatech-ep2` (formato `.ppk` en PuTTY; se puede exportar a OpenSSH con PuTTYgen).

**SG (entrada):**

- A: 22 desde IPs del equipo; 8080 `0.0.0.0/0` (BFF / API Gateway); 8082 desde IP de Ari.
- B: 22 desde IPs del equipo; 8081 desde SG A.
- C: 22 desde IPs del equipo; 5672 desde SG A y B; 15672 desde IPs de Ari y Nico.
- D: 22 desde IPs del equipo; 9092 desde SG A y B; 8088 desde IP de Ari.

**Corrección SG (2026-11-28):** la regla 15672 del SG de Rabbit (`sg-0194df0045c5f001c`) no había quedado guardada para la IP de Ari, y la UI daba timeout. Se volvió a agregar y guardar; la UI ya entra.

### Estado de servicios (2026-11-28)

| EC2 | Estado |
| --- | --- |
| A · Catálogo | Solo `~/apps/ms-catalogo.jar` de la EP2 (4 nov), sin systemd y sin procesos en 8080/8082. BFF y JAR con Kafka: deploy del sábado con Nico. |
| B · Solicitudes | No se revisó el runtime en esta ronda. Deploy pendiente con Skarlett. |
| C · Rabbit | Arriba (reutilizado de EP2). Ver [§ 3](#3-rabbit-c). |
| D · Kafka | Arriba, tópicos creados. Ver [§ 4](#4-kafka-d). |

## 2. RDS

| Rol | Identifier | Endpoint | DB | AZ | Public | SG |
| --- | --- | --- | --- | --- | --- | --- |
| Catálogo | mesatech-ep2-catalogo | `mesatech-ep2-catalogo.cg33a3yt7ns0.us-east-1.rds.amazonaws.com` | mesatech_catalogo | us-east-1d | No | sg-0c718b6d89f69f687 |
| Solicitudes | mesatech-ep2-solicitudes | `mesatech-ep2-solicitudes.cg33a3yt7ns0.us-east-1.rds.amazonaws.com` | mesatech_solicitudes | us-east-1d | No | sg-01ecd39653046dcb7 |

- 5432 de catálogo: solo desde SG A (`sg-054c453030ed5f8e8`).
- 5432 de solicitudes: solo desde SG B (`sg-0571f2e504fd01ab3`).
- Acceso público = No (capturas en `Evidencia EP3 - Ari.docx`, 2026-11-28). Sin passwords en este archivo.

## 3. Rabbit (C)

| Recurso | Valor |
| --- | --- |
| AMQP (desde A/B) | `172.31.18.133:5672` |
| Management UI | `http://32.199.222.166:15672` |
| vhost | `mesatech` |
| Compose | `~/rabbitmq/compose.yaml` |
| Imagen / versiones | `rabbitmq:4.2-management` · Rabbit 4.2.9 · Erlang 27.3.4.19 |
| Contenedor | `rabbitmq-rabbitmq-1` |
| Binding verificado | `catalogo.exchange` → `solicitudes.catalogo.queue` · routing key `categoria.creada` |

Credenciales (admin de la UI y usuarios de aplicación) solo por canal privado, no en Git.

## 4. Kafka (D)

| Recurso | Valor |
| --- | --- |
| Bootstrap (desde A/B) | `172.31.19.10:9092` |
| UI (opcional) | `http://52.207.167.121:8088` |
| Tópicos | `mesatech.solicitud.creada`, `mesatech.solicitud.estado`, `mesatech.catalogo.prioridad` (3 particiones, RF=1) |

- Docker 29.10.1 · Compose v5.8.1.
- Compose: `~/kafka/compose.yaml` — `apache/kafka:3.8.0` (KRaft) + `provectuslabs/kafka-ui` (`8088:8080`).
- `KAFKA_ADVERTISED_LISTENERS`: `PLAINTEXT://172.31.19.10:9092`.
- Las apps deben usar la **IP privada**, no la EIP, en `KAFKA_BOOTSTRAP_SERVERS`.

## 5. API Gateway

| Campo | Valor |
| --- | --- |
| Nombre | mesatech-ep3-api |
| API ID | `v2khybjs2j` |
| Invoke URL | `https://v2khybjs2j.execute-api.us-east-1.amazonaws.com` |
| Región | us-east-1 |
| Integración BFF | HTTP `ANY` `http://184.196.102.98:8080/{proxy}` |
| Ruta | `ANY /{proxy+}` |
| Etapa | `$default` (implementación automática) |
| CORS | Origin `http://localhost:5173`; métodos GET, POST, PUT, PATCH, DELETE, OPTIONS; headers `authorization`, `content-type` |

Responde **503** hasta que el BFF esté levantado en A:8080. Las rutas v1/v2 las ajusta Giannina.

## 6. IP admin SSH/SG

Vistas en las reglas de los SG (2026-11-27/28):

- `190.114.35.100/32` (Ari; actualizar si cambia)
- `143.255.105.60/32`
- `181.42.180.35/32`

## 7. Handoff rápido

Enviado al equipo el 2026-11-28 (America/Santiago).

| Integrante | Variables clave |
| --- | --- |
| Nico | EIP A `184.196.102.98`; privada A `172.31.17.69`; `RABBITMQ_HOST=172.31.18.133`; `KAFKA_BOOTSTRAP_SERVERS=172.31.19.10:9092`; `CATALOGO_DATASOURCE_URL=jdbc:postgresql://mesatech-ep2-catalogo.cg33a3yt7ns0.us-east-1.rds.amazonaws.com:5432/mesatech_catalogo`; creds Rabbit publicador por canal privado |
| Skarlett | EIP B `3.221.105.184`; privada B `172.31.23.125`; mismos brokers (`RABBITMQ_HOST=172.31.18.133`, `KAFKA_BOOTSTRAP_SERVERS=172.31.19.10:9092`); `SOLICITUDES_DATASOURCE_URL=jdbc:postgresql://mesatech-ep2-solicitudes.cg33a3yt7ns0.us-east-1.rds.amazonaws.com:5432/mesatech_solicitudes`; creds Rabbit consumidor por canal privado |
| Giannina | EIP A `184.196.102.98`, B `3.221.105.184`, C `32.199.222.166`, D `52.207.167.121`; privadas A/B/C/D arriba; API ID `v2khybjs2j`; invoke URL `https://v2khybjs2j.execute-api.us-east-1.amazonaws.com`; región `us-east-1` |

## 8. Operación del lab

- Al **apagar o prender** el Learner Lab o las EC2, las Elastic IP y las IP privadas **se mantienen**. Solo cambian si se termina (borra) la instancia.
- Después de un reinicio, confirmar en este archivo la **Última actualización** y que cada EIP sigue asociada a su instancia.
- Si cambia la IP de casa, actualizar la regla `/32` en los SG (lección de la EP2).
- Los RDS y la VPC no cambian al reiniciar las EC2.

## 9. Comandos de verificación

En C (Rabbit):

```bash
cd ~/rabbitmq && docker compose ps
docker exec rabbitmq-rabbitmq-1 rabbitmqctl list_bindings -p mesatech
```

En D (Kafka):

```bash
docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group <nombre-grupo>
```

Grupos acordados: `mesatech-catalogo-kafka` y `mesatech-solicitudes-kafka` (el de solicitudes se usa en la prueba de lag).
