# EP3 — Instrucciones comunes Kafka (contrato + metodología lab)

**Fecha acuerdo:** 2026-11-25  
**Contrato JSON:** [`../Giannina/Contrato-eventos-EP3.md`](../Giannina/Contrato-eventos-EP3.md) + [`../Giannina/contratos-json/`](../Giannina/contratos-json/) (6 archivos)  
**Labs:** [`../Nicolas/REFERENCIAS-KAFKA.md`](../Nicolas/REFERENCIAS-KAFKA.md) · `_refs/ep3-kafka-basico`, `_refs/ep3-kafka-mysql`

No usamos la nomenclatura genérica **`Venta*`** del laboratorio como dominio MesaTech. Sí usamos la **misma estructura**: `dto` (record) → `consumer` → `service` → JPA → `KafkaTemplate.send` en el flujo REST real.

---

## Reglas para todos

| Tema | Decisión |
| --- | --- |
| Payload Kafka | JSON del **contrato Giannina** (no `codigo`/`valor` del lab) |
| DTO | **`record`** en package `dto` + Bean Validation (`@Valid` / `Validator`) |
| Listener | package `consumer`, clase `*Consumer`, headers partición/offset/key como lab |
| Servicio | package `service`, regla de negocio de la pauta → `save` en RDS |
| Offsets | `enable-auto-commit=false` + `acknowledge()` tras persistir |
| Productor | `KafkaTemplate.send(tópico, key, json)` desde el MS dueño (no `KafkaController` demo) |
| Properties | `app.kafka.mistopicos.<nombre>` + `spring.kafka.consumer.group-id` (ver Nico) |
| Local | `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` + [`../../../../../infra/kafka/README.md`](../../../../../infra/kafka/README.md) |
| Cloud | IP **privada** EC2 D en `ms-catalogo.env` / `ms-solicitudes.env` |

Tópicos y groups sin cambio: [`../ARQUITECTURA-ACUERDOS.md`](../ARQUITECTURA-ACUERDOS.md).

---

## Por integrante

### Giannina

- Contrato y **6 JSON** vigentes (restaurados 2026-11-25). §7 del contrato enlaza clases Java de Nico.
- Si se ajusta texto, mantener campos por tópico y pares válido/inválido para la defensa.
- API Gateway / front: sin cambio por este refactor.

### Nicolás (`ms-catalogo` — rama `feature/nico-ep3-kafka`)

**Hecho (referencia equipo):**

| Pieza | Clase |
| --- | --- |
| DTO | `dto/PrioridadRenombradaDTO` (record) |
| Consumer | `consumer/CatalogoConsumer` |
| Servicio | `service/CatalogoPrioridadService` |
| RDS | `domain/AuditoriaPrioridad` → `auditoria_prioridad` |
| Tópico | `app.kafka.mistopicos.prioridad` = `mesatech.catalogo.prioridad` |

Pendiente Nico: pruebas NIC3, Fase 4–5 deploy, PR → `dev`.

### Skarlett (`ms-solicitudes`)

Replicar **metodología** de Nico con **contrato §2.1 y §2.2**:

| Tópico | DTO (record) sugerido | Consumer | Servicio | Tablas |
| --- | --- | --- | --- | --- |
| `mesatech.solicitud.creada` | `SolicitudCreadaDTO` | `SolicitudesConsumer` (o listener dedicado) | p. ej. `SolicitudSeguimientoService` | `seguimiento_solicitud` |
| `mesatech.solicitud.estado` | `SolicitudEstadoDTO` | mismo consumer group, 2 `@KafkaListener` | p. ej. `SolicitudEstadoService` | `historial_estado_solicitud` |

Properties sugeridas:

```properties
app.kafka.mistopicos.solicitud-creada=mesatech.solicitud.creada
app.kafka.mistopicos.solicitud-estado=mesatech.solicitud.estado
spring.kafka.consumer.group-id=mesatech-solicitudes-kafka
```

Copiar `config/KafkaConfig.java` de catálogo (misma factory + ack manual). **No tocar** Rabbit `CategoriaCreadaListener`.

JSON de prueba: archivos `solicitud-*` en `Giannina/contratos-json/`.

### Ari

- Sin refactor de código; al crear Kafka en D, ejecutar [`../../../../../infra/kafka/crear-topicos-ep3.sh`](../../../../../infra/kafka/crear-topicos-ep3.sh) (o equivalente).
- Entregar IPs/EIP en `Inventario_AWS_EP3.md` para `KAFKA_BOOTSTRAP_SERVERS` en A y B.

---

## PR y ramas

| Quién | Rama sugerida | Destino |
| --- | --- | --- |
| Nico | `feature/nico-ep3-kafka` | `dev` |
| Skarlett | `feature/skarlett-ep3-kafka` | `dev` |
| Docs equipo | en la misma PR de quien toque o PR pequeño a `dev` | |

Después de merge a `dev`: Giannina actualiza invoke URL / front si aplica; E2E con Kafka local o cloud.

---

## Checklist rápido antes de defensa

- [ ] 6 JSON en repo coinciden con contrato
- [ ] Por tópico: inválido sin fila RDS, válido misma key con fila
- [ ] Continuidad EP2 Rabbit demostrada
- [ ] Consumer group y 3 particiones documentados (NIC3 / Skarlett)