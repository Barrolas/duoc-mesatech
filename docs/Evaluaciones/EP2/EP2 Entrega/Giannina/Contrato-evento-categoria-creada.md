# Contrato del evento `categoria.creada`

**Proyecto:** MesaTech Cloud · EP2  
**Equipo:** Ari · Nicolás · Giannina · Skarlett  
**Versión del contrato:** 1.0  
**Fecha:** 2026-11-02 (America/Santiago)  
**Fuentes:** `origin/feature/skarlett-consumidor` (consumidor), `docs/Evaluaciones/EP2/EP2 Entrega/Ari/Inventario_AWS_EP2.md`, `00-Distribucion-tareas-equipo.md`, PDF EP2 MesaTech Cloud.

---

## 1. Propósito

Al crear una categoría válida en Catálogo, el publicador persiste el registro en RDS Catálogo y publica un JSON por AMQP. Solicitudes consume el mensaje, guarda una copia local conservando el mismo `categoriaId` y solo entonces confirma el procesamiento. Así Solicitudes puede ofrecer categorías al crear solicitudes sin leer el RDS de Catálogo.

Alcance mínimo: solo creación. Actualizaciones y bajas del catálogo quedan fuera de este contrato (ampliación opcional).

---

## 2. Canal

Valores alineados entre inventario Ari y `application.properties` del consumidor. Sin contraseñas.

| Recurso | Valor |
| --- | --- |
| Broker | RabbitMQ 4.2-management en EC2 C (`mesatech-ep2-rabbitmq`) |
| AMQP (A/B → C) | `<IP_PRIVADA_EC2_C>:5672` (host lógico `rabbitmq-ec2c`) |
| Virtual host | `mesatech` |
| Exchange | `catalogo.exchange` · tipo **direct** · **durable** · auto-delete `false` |
| Routing key | `categoria.creada` |
| Cola | `solicitudes.catalogo.queue` · **durable** · classic |
| Binding | cola ← exchange con routing key `categoria.creada` |
| Usuario publicador | `catalogo_publicador` (inventario Ari) |
| Usuario consumidor | `solicitudes_consumidor` (inventario Ari) |
| Declara topología en app | Consumidor: `mesatech.rabbitmq.declare` (default `true`); en AWS conviene declarar en broker y dejar `false` si se acuerda |

En local el consumidor acepta `RABBITMQ_USER` / `RABBITMQ_VHOST` (defaults de desarrollo `mesatech` / `mesatech`). En AWS Academy se usan los usuarios de aplicación del inventario, no `guest`.

---

## 3. Esquema del mensaje

DTO del consumidor: record `CategoriaCreadaEvent`  
`ms-solicitudes/.../messaging/CategoriaCreadaEvent.java` · rama `origin/feature/skarlett-consumidor`.

Serialización: `JacksonJsonMessageConverter` + `JsonMapper` de Spring Boot 4.1.1 (`RabbitConfig`). Sin `@JsonFormat` ni módulo de fechas propio: `java.time.Instant` viaja como texto ISO-8601 UTC (los tests usan `2026-10-29T15:00:00Z`).

No hay campos extra en el record. La entidad local añade `recibidaEn` al persistir; **no** forma parte del payload AMQP.

| Campo | Tipo | Obligatorio | Formato | Ejemplo | Regla |
| --- | --- | --- | --- | --- | --- |
| `eventId` | `String` | sí | UUID u otro id no vacío | `"evt-1"` / UUID | Rechazo (`EventoInvalidoException`) si null/blank |
| `tipoEvento` | `String` | sí | literal fijo | `"categoria.creada"` | Debe igualar `CategoriaCreadaEvent.TIPO` |
| `fechaEvento` | `Instant` | sí | ISO-8601 UTC (`…Z`) | `"2026-10-29T15:00:00Z"` | Instant no null |
| `categoriaId` | `Long` | sí | entero > 0 típico | `7` | Misma PK que en RDS Catálogo; no autogenerar otra en la copia |
| `nombre` | `String` | sí | texto no blank | `"Hardware"` | Se hace `trim()` al guardar |

---

## 4. Ejemplo JSON

Ejemplo de payload (el mismo que usan los tests del consumidor). El formato es igual al que se publicó en AWS durante P2; el `eventId` real queda en la bitácora de Skarlett.

```json
{
  "eventId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "tipoEvento": "categoria.creada",
  "fechaEvento": "2026-10-29T15:00:00Z",
  "categoriaId": 7,
  "nombre": "Hardware"
}
```

---

## 5. Garantías

| Garantía | Estado |
| --- | --- |
| Publicación persistente (`deliveryMode` persistent) | **En código** — `CategoriaEventPublisher` en `ms-catalogo` publica mensajes persistentes |
| Publisher confirms + returned messages | **En código** — `publisher-confirm-type=correlated` y `publisher-returns=true` en `ms-catalogo` (N2, Nicolás) |
| Ack del consumidor tras persistir | **En código** — `acknowledge-mode=manual`; `basicAck` solo después de `CategoriaCopiaService.guardar` (`CategoriaCreadaListener`) |
| Evento inválido | `basicNack(..., requeue=false)` |
| Fallo al persistir (p. ej. RDS) | `basicNack(..., requeue=true)` |
| Idempotencia por `eventId` | **No implementada** como deduplicación. El consumidor hace upsert por `categoriaId` y sobrescribe `eventId`/`nombre`/`fechaEvento`. El PDF no exige idempotencia avanzada; no se afirma exactly-once |
| Conservación de `categoriaId` | **En código** — PK de `categorias_locales.categoria_id` = `evento.categoriaId()` |

---

## 6. Manejo de errores

### Publicador / P7 (broker caído)

El PDF exige: con RabbitMQ detenido, al crear categoría mostrar **respuesta HTTP controlada**, consultar RDS Catálogo (el registro puede haber quedado guardado) y explicar la inconsistencia sin recuperación automática.

**Estado:** implementado. Con RabbitMQ detenido, `ms-catalogo` responde HTTP 503 controlado; la categoría puede quedar guardada en RDS Catálogo sin evento (inconsistencia explicada, sin outbox). Evidencia en P7 de la bitácora de Skarlett.

### Consumidor

- `missing-queues-fatal=false`: la app no aborta el arranque si la cola aún no existe.
- Mensaje mal formado / campos faltantes / `tipoEvento` distinto: nack sin reencolar.
- Error de persistencia: nack con reencolar.
- No hay DLQ ni outbox en el código actual.

---

## 7. Versionado del contrato

| Versión | Cambio |
| --- | --- |
| **1.0** | Cinco campos mínimos del PDF; `tipoEvento` = `categoria.creada`; fecha = `Instant` ISO-8601 UTC; canal según inventario Ari |

Cambio incompatible (renombrar campo, cambiar tipo de `categoriaId`, otro formato de fecha, otro `tipoEvento`): subir versión menor/mayor del contrato, alinear publicador y consumidor, y actualizar binding/routing solo si el equipo lo acuerda. Ampliaciones opcionales (update/delete de categoría) = nuevos `tipoEvento` o contrato aparte.

---

## 8. Estado de implementación

| Ítem | Estado |
| --- | --- |
| Vhost / exchange / cola / binding / usuarios app | Documentado en inventario Ari (broker). Declaración también en consumidor si `declare=true` |
| DTO `CategoriaCreadaEvent` | **Código** · `dev` · `ms-solicitudes/.../messaging/CategoriaCreadaEvent.java` |
| `RabbitConfig` + Jackson converter | **Código** · misma rama · `.../messaging/RabbitConfig.java` |
| Listener + ack manual | **Código** · `.../messaging/CategoriaCreadaListener.java` + `application.properties` |
| Persistencia copia + `categoriaId` | **Código** · `CategoriaCopiaService` + entidad `CategoriaLocal` / tabla `categorias_locales` |
| Tests consumidor (ack / nack / conserva id) | **Código** · `.../messaging/*Test.java` |
| Dependencia AMQP en `ms-solicitudes` | **Código** · `pom.xml` (`spring-boot-starter-amqp`) |
| Publicador en `ms-catalogo` (DTO, template, confirms, returns, mensaje persistente, error P7) | **Código** · `.../messaging/CategoriaEventPublisher.java` + `RabbitConfig.java` (Nicolás) |
| Ejemplo JSON capturado en management UI / logs | **Evidencia** · P2 de la bitácora de Skarlett (mismo `event_id` publicado y consumido) |
| Idempotencia estricta por `eventId` | **Pendiente / no exigida** · hoy upsert por `categoriaId` |
| Merge del consumidor a `dev` | **Hecho** · integrado en `dev` |

---

## Referencias rápidas (solo lectura git)

- Consumidor: `git show dev:ms-solicitudes/src/main/java/cl/duoc/mesatech/solicitudes/messaging/...`
- Propiedades: `.../ms-solicitudes/src/main/resources/application.properties`
- Inventario: `docs/Evaluaciones/EP2/EP2 Entrega/Ari/Inventario_AWS_EP2.md`
