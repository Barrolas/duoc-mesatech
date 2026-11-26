# Avance EP3 · Skarlett (ms-solicitudes + Kafka)

Rama: `feature/skarlett-ep3-kafka` (desde `dev`, commit fb26b69). Disertación: miércoles 2-dic-2026, 20:15.

## 1. Mi parte, ya aplicada en esta rama (ms-solicitudes)
- Dependencias `spring-boot-starter-kafka` y validation en `ms-solicitudes/pom.xml`.
- **2 productores** (`producer/SolicitudKafkaProducer`): `mesatech.solicitud.creada` y `mesatech.solicitud.estado`, key `solicitud-{id}`.
- **2 `@KafkaListener`** en `consumer/SolicitudesConsumer`, group `mesatech-solicitudes-kafka`, con `@Valid`, **ack manual** después de guardar e **idempotencia por `eventId`**. DTOs: `dto/SolicitudCreadaDTO` y `dto/SolicitudEstadoDTO`. Properties: `app.kafka.mistopicos.*`.
- Tablas `seguimiento_solicitud` e `historial_estado_solicitud` (entidades + repos).
- **DLQ** `mesatech.dlq.eventos` con `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` (evento inválido no bloquea la partición).
- Concurrencia controlada solo con `KAFKA_LISTENER_CONCURRENCY` (default 3) para la prueba de lag 1 vs 3.
- Log de operación `operacion.jsonl` (`OperacionLogService`): fecha UTC, grupo, tópico, partición, offset, key, eventId, resultado.
- `infra/docker-compose.kafka.yml` (KRaft apache/kafka:3.9.0) y `infra/kafka/crear-topicos.sh` (4 tópicos × 3 particiones).
- Continuidad RabbitMQ EP2 (`categoria.creada`) intacta.
- Tests: **20 tests, 0 fallos** (`mvn -q test`, JDK 17), también después del refactor de paquetes.

### Probado en local (Docker: Kafka KRaft + Postgres 16 + RabbitMQ)
- 4 tópicos con 3 particiones.
- Evento inválido → DLQ; el válido con la misma key se procesa.
- Duplicados ignorados por idempotencia.
- Lag 0 → 4 por partición con el consumidor detenido → 0 al reanudar.
- Concurrencia 1 = 1 consumidor por listener; 3 = 3.
- Los 6 JSON de contrato de Giannina (rama `feature/giannina-ep3`) pasan la validación; los inválidos fallan solo por la regla de negocio.

> Nota: `env.example` queda alineado con `feature/nico-ep3-kafka`: el bloque final es el de Nico (`KAFKA_BOOTSTRAP_SERVERS` y `KAFKA_CONSUMER_GROUP_ID=mesatech-catalogo-kafka`). Las variables de solicitudes (`KAFKA_SOLICITUDES_GROUP_ID`, `KAFKA_LISTENER_CONCURRENCY`, `KAFKA_TOPIC_*`, `OPERACION_LOG_PATH`) van antes, para que el merge no choque. Detalle en `REFACTOR-KAFKA-SKARLETT.md`.

## 2. Borradores para el equipo (propuestas, NO aplicadas)
Carpeta `docs/Evaluaciones/EP3/EP3 Entrega/Skarlett/borradores-equipo/`. Cada uno revisa y aplica en su propia rama.
- **Ari** (`ari/`): instalar Docker, compose Kafka KRaft para EC2 D (advertised = IP privada), crear tópicos, verificar, reglas de Security Group (9092 solo desde SG de EC2 A y B) y procedimiento de lag 1 vs 3.
- **Nicolás** (`nicolas/ms-catalogo-kafka.diff`): productor y consumidor de `mesatech.catalogo.prioridad`, DLQ, idempotencia, `auditoria_prioridad`. 12 tests OK.
- **Frontend** (`frontend/frontend-prioridad-nivel.diff`): enviar `prioridadNivel` al crear solicitud. `npm run build` OK.
- Los tres parches aplican juntos con el mío sin conflictos.

## 3. Lo que falta (equipo)
- **Ari:** levantar Kafka en EC2 D, crear tópicos, pasar IP privadas/EIP; prueba de lag conmigo.
- **Nicolás:** integrar Kafka en `ms-catalogo` (puede usar el borrador).
- **Frontend:** enviar `prioridadNivel`.
- **Giannina:** EIP en el diagrama; merge de `feature/giannina-ep3`.
- **Todos:** despliegue AWS (sáb 28), flujos punta a punta, Gateway v1/v2 + 401, RabbitMQ EP2, `operacion.jsonl` real, informe y ensayo.

## 4. Capturas necesarias (9)
1. Tópicos `--describe` · 2. Solicitud creada + fila RDS · 3. Cambio de estado + historial · 4. Inválido a DLQ y válido procesado · 5. Lag antes/durante/después con concurrencia 1 y 3 · 6. Extracto `operacion.jsonl` · 7. RabbitMQ EP2 · 8. Gateway v1/v2 y 401 · 9. Rama y PR.

## Calendario
- Mié 25: rama + push + PR · Jue 26–Vie 27: Kafka en EC2 D e IPs · Sáb 28: despliegue AWS y capturas · Dom 29: lag 1 vs 3 · Lun 30–Mar 1: informe y ensayo · **Mié 2-dic 20:15: disertación**.
