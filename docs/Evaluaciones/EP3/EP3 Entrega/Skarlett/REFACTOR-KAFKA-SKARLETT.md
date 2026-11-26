# Refactor Kafka · Skarlett (`ms-solicitudes`)

Alineado con `docs/Evaluaciones/EP3/EP3 Entrega/equipo/INSTRUCCIONES-REFACTOR-KAFKA.md` (rama `feature/nico-ep3-kafka`) y con el código de Nico en `ms-catalogo` (`PrioridadRenombradaDTO`, `CatalogoConsumer`, `CatalogoPrioridadService`, `config/KafkaConfig`).

La lógica no cambió: mismos tópicos, group `mesatech-solicitudes-kafka`, ack manual después de guardar, idempotencia por `eventId`, DLQ `mesatech.dlq.eventos` y `operacion.jsonl`. `CategoriaCreadaListener` (Rabbit) no se tocó.

## Antes → después

| Antes | Después |
| --- | --- |
| `messaging/kafka/SolicitudCreadaEvent` | `dto/SolicitudCreadaDTO` |
| `messaging/kafka/SolicitudEstadoEvent` | `dto/SolicitudEstadoDTO` |
| `SolicitudCreadaKafkaListener` + `SolicitudEstadoKafkaListener` | `consumer/SolicitudesConsumer` (dos `@KafkaListener`) |
| `messaging/kafka/SeguimientoSolicitudService` | `service/SeguimientoSolicitudService` (creación y estado) |
| `messaging/kafka/KafkaConfig` | `config/KafkaConfig` (factory, ack manual y DLQ) |
| `messaging/kafka/SolicitudKafkaProducer` | `producer/SolicitudKafkaProducer` |
| `messaging/kafka/OperacionLogService` | `service/OperacionLogService` |
| `mesatech.kafka.topics.solicitud-creada` | `app.kafka.mistopicos.solicitud-creada` |
| `mesatech.kafka.topics.solicitud-estado` | `app.kafka.mistopicos.solicitud-estado` |
| `mesatech.kafka.topics.dlq` | `app.kafka.mistopicos.dlq` |

El consumer registra en el log la key, la partición y el offset, como el lab.

## Properties

Siguen saliendo de las mismas env, con estos defaults:

```properties
app.kafka.mistopicos.solicitud-creada=${KAFKA_TOPIC_SOLICITUD_CREADA:mesatech.solicitud.creada}
app.kafka.mistopicos.solicitud-estado=${KAFKA_TOPIC_SOLICITUD_ESTADO:mesatech.solicitud.estado}
app.kafka.mistopicos.dlq=${KAFKA_TOPIC_DLQ:mesatech.dlq.eventos}
mesatech.kafka.listener.concurrency=${KAFKA_LISTENER_CONCURRENCY:3}
spring.kafka.consumer.group-id=${KAFKA_SOLICITUDES_GROUP_ID:mesatech-solicitudes-kafka}
```

## `env.example` y la rama de Nico

El bloque final es el de Nico, byte a byte: `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` y `KAFKA_CONSUMER_GROUP_ID=mesatech-catalogo-kafka`. Las variables extra de solicitudes están más arriba (antes de la sección de `ms-catalogo`), porque pegarlas al final del mismo bloque sí choca en el merge. `git merge-tree` contra `origin/feature/nico-ep3-kafka` no deja conflictos. `origin/dev` sigue en `fb26b69`; esta rama ya lo contiene.

## Tests

`mvn -q test` en `ms-solicitudes` con JDK 17: **20 tests, 0 fallos, 0 errores**.
