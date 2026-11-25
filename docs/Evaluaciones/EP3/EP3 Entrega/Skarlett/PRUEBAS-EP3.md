# Pruebas de aceptación EP3 — bitácora

Completar una fila por prueba ejecutada.

## Continuidad EP2

| # | Entrada | Esperado | Obtenido | Evidencia |
| --- | --- | --- | --- | --- |
| C1 | POST categoría vía app | Copia en RDS Solicitudes (Rabbit) | | SKA3-01 |
| C2 | Crear solicitud con categoría | HTTP 2xx | | SKA3-02 |
| C3 | v1 vs v2 | Respuesta según versión | | SKA3-03 |
| C4 | Sin token en Gateway | 401 | | SKA3-04 |

## Tres flujos Kafka

| Tópico | Evento | Key | Partición | RDS | Log |
| --- | --- | --- | --- | --- | --- |
| solicitud.creada | | | | | SKA3-10 |
| solicitud.estado | | | | | SKA3-11 |
| catalogo.prioridad | (evidencia Nico) | | | | SKA3-12 |

## Validación DTO (por tópico)

| Tópico | Inválido (motivo) | Válido misma key | Evidencia |
| --- | --- | --- | --- |
| | | | SKA3-20.. |

## Concurrencia

| Run | concurrency | Volumen lote | Tiempo recuperación | Evidencia |
| --- | --- | --- | --- | --- |
| 1 | 1 | | | SKA3-30 |
| 2 | 3 | | | SKA3-31 |

## Lag

Ver [`PROCEDIMIENTO-CONSUMER-LAG.md`](PROCEDIMIENTO-CONSUMER-LAG.md). Evidencias SKA3-40..42.