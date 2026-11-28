# Backlog Ari — Infraestructura EP3

**Criterios pauta:** Continuidad EP2, red, broker Kafka estable, soporte a lag.  
**Estado (2026-11-28, America/Santiago):** A0–A8 **hechos**. Inventario enviado al equipo.

---

## A0 — Preparación de la cuenta — Hecho (2026-11-27)

Se reutilizó la **misma cuenta Learner Lab de la EP2** (decisión del 27-nov por créditos restantes). Horario de apagado y canal privado para secretos, acordados con el equipo.

---

## A1 — Elastic IPs — Hecho (2026-11-27)

Se asignaron 4 EIP y se asociaron a A, B, C y D. SSH y scripts usan la EIP, no la IP efímera. Registradas en [`Inventario_AWS_EP3.md`](Inventario_AWS_EP3.md).

---

## A2 — EC2 (4 instancias) — Hecho (2026-11-27)

| ID | Rol | Puertos |
| --- | --- | --- |
| A | Catálogo + BFF (acordado) | 8082, 8080 |
| B | Solicitudes | 8081 |
| C | Rabbit (reutilizado de EP2) | 5672, 15672 |
| D | Kafka (nueva: `mesatech-ep3-kafka`) | 9092, UI 8088 |

Todas: Ubuntu 24.04, `t3.medium`, AZ `us-east-1b`, par de claves `mesatech-ep2` (formato `.ppk` en PuTTY).

---

## A3 — RDS (2) — Hecho (2026-11-27/28)

Se reutilizaron `mesatech-ep2-catalogo` (`mesatech_catalogo`) y `mesatech-ep2-solicitudes` (`mesatech_solicitudes`). Acceso público = No. SG 5432 solo desde la EC2 dueña. Endpoints JDBC en el inventario, sin passwords. AZ de los RDS: `us-east-1d` (misma VPC que las EC2).

---

## A4 — Rabbit en C — Hecho (2026-11-28)

Broker de la EP2 **reutilizado y verificado**: `~/rabbitmq/compose.yaml`, imagen `rabbitmq:4.2-management` (4.2.9, Erlang 27.3.4.19), contenedor `rabbitmq-rabbitmq-1`. Vhost `mesatech`, binding `catalogo.exchange` → `solicitudes.catalogo.queue` con `categoria.creada`. Evidencia ARI3-03 en el Word (terminal + UI). Credenciales solo por canal privado.

---

## A5 — Kafka en D — Hecho (2026-11-27/28)

Compose en `~/kafka/compose.yaml`: `apache/kafka:3.8.0` (KRaft) + Kafka UI en 8088. `KAFKA_ADVERTISED_LISTENERS`: `PLAINTEXT://172.31.19.10:9092`. Tópicos `mesatech.solicitud.creada`, `mesatech.solicitud.estado` y `mesatech.catalogo.prioridad` con 3 particiones y RF=1.

---

## A6 — Security groups — Hecho (2026-11-27/28)

Matriz según inventario. El 28-nov se corrigió la regla 15672 del SG de Rabbit (no había quedado guardada para la IP de Ari); la UI ya entra. Actualizar la IP admin `/32` si cambia.

---

## A7 — API Gateway (base AWS) — Hecho (2026-11-28)

API HTTP `mesatech-ep3-api`, ID `v2khybjs2j`, invoke URL y CORS listos. Integración `ANY` hacia `http://184.196.102.98:8080/{proxy}`. Las rutas v1/v2 las ajusta Giannina. Hoy responde **503** hasta que el BFF esté en A.

---

## A8 — Handoff — Hecho (2026-11-28)

Inventario completado (EIP, privadas, JDBC, brokers, API ID) y **enviado al equipo**. Credenciales de Rabbit solo por canal privado.

---

## Pendientes

| Qué | Con quién | Nota |
| --- | --- | --- |
| Deploy BFF + JAR de catálogo con Kafka en A | Nico (sábado) | Hasta entonces el Gateway sigue en 503 |
| Soporte consumer lag (ARI3-L01) | Skarlett | `kafka-consumer-groups --describe` en D, antes / durante / después; concurrencia 1 y 3 |
| Aporte al informe y defensa | Equipo | Versiones, EC2, dueño de cada RDS; EIP vs IP privada; key → partición → offset |
| Revisar PR #22 | Skarlett | Opcional |
