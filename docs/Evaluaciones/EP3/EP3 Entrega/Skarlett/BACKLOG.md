# Backlog Skarlett — EP3 (`ms-solicitudes` + pruebas + informe)

**Tópicos:** `mesatech.solicitud.creada`, `mesatech.solicitud.estado`  
**Mantener EP2:** `CategoriaCreadaListener` Rabbit en EC2 B.

---

## S0 — Modelo datos RDS Solicitudes

1. Tablas seguimiento (nombres en `ARQUITECTURA-ACUERDOS.md`).
2. Migración o ddl-auto documentado.
3. Consultas SQL para informe.

---

## S1 — Config Kafka `ms-solicitudes`

Igual patrón que Nico: [`equipo/INSTRUCCIONES-REFACTOR-KAFKA.md`](../equipo/INSTRUCCIONES-REFACTOR-KAFKA.md) · DTOs `SolicitudCreadaDTO` / `SolicitudEstadoDTO` (record) · `SolicitudesConsumer` · properties `app.kafka.mistopicos.*` · manual ack.

---

## S2 — Productor: solicitud creada

1. Tras POST solicitud válida → evento DTO contrato.
2. Key `solicitud-{id}`; incluir nivel prioridad.
3. Log envío confirmado (`acks=all`).

---

## S3 — Productor: cambio estado

1. En transición válida (`TransicionesEstado`): `estadoAnterior`, `estadoNuevo`.
2. Misma key por solicitud.

---

## S4 — Consumers (2 listeners)

| Listener | Regla |
| --- | --- |
| Creada | Alta → preferente; resto → normal |
| Estado | RESUELTA/CERRADA → fuera atención; otros → seguimiento |

`@Valid`, rechazo documentado, ack tras persistir.

---

## S5 — Rabbit EP2

1. Deploy B con credenciales Ari.
2. Prueba continuidad antes de demo Kafka integrada.

---

## S6 — Deploy EC2 B

EIP B, puerto 8081, logs, env actualizado.

---

## S7 — Pruebas aceptación

Ejecutar y documentar en [`PRUEBAS-EP3.md`](PRUEBAS-EP3.md) (entrada / esperado / obtenido / evidencia).

- Continuidad EP2 (React + v1/v2 + 401)
- Tres flujos Kafka
- Validación inválido → válido misma key
- Concurrencia 1 vs 3
- Consumer lag (ver procedimiento)

---

## S8 — `operacion.jsonl`

Campos pauta: fecha UTC, nivel, servicio, grupo, tópico, partición, offset, key, eventId, resultado.  
Implementar en A y B o centralizar en B según diseño; sin secretos.  
Búsqueda por `eventId` documentada.

---

## S9 — Informe EP3

Integrar capturas Ari, Nico, Giannina + tablas de prueba.

---

## Rama Git

`feature/skarlett-ep3-kafka` → PR `dev`.