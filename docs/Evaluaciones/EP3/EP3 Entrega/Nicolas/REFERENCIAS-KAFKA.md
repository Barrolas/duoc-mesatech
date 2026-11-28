# Referencias Kafka — metodología lab + contrato MesaTech

| Repo lab | `_refs/ep3-kafka-basico`, `_refs/ep3-kafka-mysql` |
| Contrato JSON | [`../Giannina/Contrato-eventos-EP3.md`](../Giannina/Contrato-eventos-EP3.md) |

## Metodología (como el curso)

| Lab | `ms-catalogo` |
| --- | --- |
| Package `consumer` + clase `*Consumer` | **`consumer/CatalogoConsumer`** |
| Package `service` + `registrarVenta` | **`service/CatalogoPrioridadService.registrarCambioPrioridad`** |
| `KafkaTemplate.send(topic, key, json)` | **`CatalogoController`** (PUT prioridad real) |
| Listener + partición / offset / key | Igual en `CatalogoConsumer` |
| `enable-auto-commit=false` + ack manual | `KafkaConfig` + `Acknowledgment` |

## Negocio (rúbrica / Giannina)

| Lab | MesaTech |
| --- | --- |
| `Venta` genérico | **`record PrioridadRenombradaDTO`** + tabla **`auditoria_prioridad`** |
| Tópico `ventas` | **`mesatech.catalogo.prioridad`** (`app.kafka.mistopicos.prioridad`) |

Skarlett (misma metodología, otros DTO):

- `SolicitudCreadaDTO` + **`SolicitudesConsumer`** (sugerido)
- `SolicitudEstadoDTO` + mismo MS / segundo listener

## Properties

```properties
app.kafka.mistopicos.prioridad=mesatech.catalogo.prioridad
spring.kafka.consumer.group-id=mesatech-catalogo-kafka
```