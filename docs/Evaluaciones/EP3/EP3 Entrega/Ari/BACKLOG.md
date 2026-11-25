# Backlog Ari — Infraestructura EP3

**Criterios pauta:** Continuidad EP2, red (C4), broker Kafka estable, soporte a lag.

---

## A0 — Preparación cuenta

1. Iniciar Learner Lab (cuenta vigente).
2. Acordar horario apagado EC2/RDS (créditos).
3. Canal privado del equipo para secretos (no Git).

---

## A1 — Elastic IPs (antes del handoff)

| Paso | Acción |
| --- | --- |
| A1.1 | EC2 → Elastic IPs → Allocate (mínimo 4). |
| A1.2 | Asociar EIP a instancia A/B/C/D al crearlas. |
| A1.3 | Registrar en `Inventario_AWS_EP3.md`: Instance ID, **EIP**, privada, AZ. |
| A1.4 | Avisar: **SSH y scripts usan EIP**, no IP efímera del panel. |

---

## A2 — EC2 (4 instancias)

| ID | Rol | Puertos |
| --- | --- | --- |
| A | Catálogo (+ BFF opcional) | 8082, 8080? |
| B | Solicitudes | 8081 |
| C | Rabbit EP2 | 5672, 15672 |
| D | Kafka | 9092, UI 8088 |

Por instancia: Ubuntu 24.04, `t3.medium` (o documentar si `small`), key `mesatech-ep2.pem`, SG según A6, `~/apps/` en A/B.

---

## A3 — RDS (2)

1. `mesatech-ep2-catalogo` → `mesatech_catalogo` (SG solo desde A).
2. `mesatech-ep2-solicitudes` → `mesatech_solicitudes` (SG solo desde B).
3. Sin acceso público; endpoints JDBC en inventario (sin passwords).

---

## A4 — Rabbit en C (continuidad EP2)

1. Docker + compose `rabbitmq:4.2-management`, volumen, hostname fijo.
2. vhost `mesatech`, usuarios app (no guest), `catalogo.exchange`, `solicitudes.catalogo.queue`, `categoria.creada`.
3. Entregar **IP privada C** para `RABBITMQ_HOST` en A y B.
4. Captura: `docker compose ps`, binding en UI (sin passwords).

---

## A5 — Kafka en D

1. `~/kafka/docker-compose.yml` desde `docs/Evaluaciones/EP3/EP3 Contexto/Trabajando con Kafka.docx` (extracto: `Trabajando-con-Kafka-extract.txt`).
2. `KAFKA_ADVERTISED_LISTENERS` EXTERNAL = `PLAINTEXT://<IP_PRIVADA_D>:9092`.
3. Kafka UI: mapear `8088:8080`; no exponer UI a 0.0.0.0 sin SG admin.
4. `docker compose up -d` → healthy.
5. Crear tópicos acordados en `ARQUITECTURA-ACUERDOS.md` con **--partitions 3 --replication-factor 1**.

```bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --create \
  --topic mesatech.solicitud.creada --partitions 3 --replication-factor 1
```

---

## A6 — Security groups

| Destino | Puerto | Origen |
| --- | --- | --- |
| A,B,C,D | 22 | IP admin `/32` |
| C | 5672 | SG A + B |
| C | 15672 | IP admin |
| D | 9092 | SG A + B |
| D | 8088 | IP admin (opcional) |
| RDS | 5432 | SG EC2 dueña |
| BFF 8080 | 8080 | Solo tráfico Gateway / SG acordado |

Actualizar IP admin cuando cambie (lección EP2).

---

## A7 — API Gateway (base AWS; rutas: Giannina)

1. HTTP API (v2) misma región.
2. Integración HTTP hacia BFF (`http://<EIP o privada>:8080`).
3. Entregar a Giannina: **API ID**, invoke URL, región.
4. No commitear stage keys.

---

## A8 — Handoff

Completar inventario y enviar:

| Para | Datos |
| --- | --- |
| Nico | EIP A, JDBC catálogo, privada C, privada D, creds Rabbit publicador |
| Skarlett | EIP B, JDBC solicitudes, mismas privadas brokers, creds Rabbit consumidor |
| Giannina | Todas EIP/privadas, API ID |
| Todos | Fecha última actualización inventario |

---

## Soporte consumer lag (con Skarlett)

- Acceso SSH a D para `kafka-consumer-groups --describe`.
- Mantener broker estable durante prueba de carga.
- Capturas UI/CLI de offsets por partición (evidencias ARI3-L01..).