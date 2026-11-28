# Kafka local — EP3 MesaTech

Compose basado en el material **Trabajando con Kafka** (Confluent 7.9.0). Alineado a [`ARQUITECTURA-ACUERDOS.md`](../../docs/Evaluaciones/EP3/EP3%20Entrega/ARQUITECTURA-ACUERDOS.md).

## Arranque

Desde la raíz del repo:

```powershell
docker compose -f infra/kafka/compose.yaml up -d
.\infra\kafka\crear-topicos-ep3.ps1
```

| Servicio | Puerto host | Uso |
| --- | --- | --- |
| Broker | `9092` | `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` en MS |
| Kafka UI | `8088` | Solo lab (sin auth; no exponer en cloud) |

Postgres y Rabbit siguen en [`../docker-compose.yml`](../docker-compose.yml).

## Tópicos EP3

| Tópico | Particiones |
| --- | --- |
| `mesatech.solicitud.creada` | 3 |
| `mesatech.solicitud.estado` | 3 |
| `mesatech.catalogo.prioridad` | 3 |
| `mesatech.dlq.eventos` | 3 (opcional DLQ) |

## Cloud (EC2 D)

- En `KAFKA_ADVERTISED_LISTENERS`, reemplazar `localhost:9092` por la **IP pública o privada** acordada con Ari.
- UI en **8088** en servidor (no 8080: BFF).
- Script Linux: `bash infra/kafka/crear-topicos-ep3.sh`

## Detener

```powershell
docker compose -f infra/kafka/compose.yaml down
```

Volúmenes: `docker compose -f infra/kafka/compose.yaml down -v` (borra datos del broker).