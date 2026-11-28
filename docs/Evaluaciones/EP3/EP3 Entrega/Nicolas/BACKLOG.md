# Backlog Nicolás — EP3 (`ms-catalogo` + EC2 A)

**Tópico dueño:** `mesatech.catalogo.prioridad` (key `prioridad-{id}`)  
**Mantener EP2:** publicador Rabbit `categoria.creada` sin regresiones.

**Orden de trabajo:** ver [`PLANIFICACION.md`](PLANIFICACION.md) (fases 1→5).

---

## N0 — Estudio repos

1. Clonar repos en `REFERENCIAS-KAFKA.md`.
2. Ejecutar lab básico local si hace falta.
3. Metodología lab + dominio: `PrioridadRenombradaDTO`, `CatalogoConsumer`, `CatalogoPrioridadService` (ver `REFERENCIAS-KAFKA.md`).

---

## N1 — Dependencias y config

| Paso | Acción |
| --- | --- |
| N1.1 | `pom.xml`: `spring-kafka`. |
| N1.2 | `application.properties`: bootstrap, serializers, `acks=all`. |
| N1.3 | Consumer: group-id, manual ack. |
| N1.4 | `env.example` + `ms-catalogo.env`: `KAFKA_BOOTSTRAP_SERVERS` (privada D cloud, `localhost:9092` local). |

---

## N2 — Kafka local

1. `infra/docker-compose.kafka.yml` (compose curso) o profile en compose.
2. Crear tópico prioridad con 3 particiones.
3. Probar producer/consumer contra localhost antes de cloud.

---

## N3 — Productor cambio nombre prioridad

| Paso | Acción |
| --- | --- |
| N3.1 | Hook en operación de actualizar `Prioridad` (REST existente). |
| N3.2 | DTO según contrato Giannina. |
| N3.3 | Regla publicación: solo formato vs revisión sustantiva. |
| N3.4 | `KafkaTemplate.send(topic, key, json)`. |
| N3.5 | Log estructurado (eventId, key, ack/fallo). |
| N3.6 | **No** romper flujo categoría Rabbit EP2. |

---

## N4 — Consumer + RDS Catálogo

| Paso | Acción |
| --- | --- |
| N4.1 | `@KafkaListener` + DTO `@Valid`. |
| N4.2 | Rechazo: log campo/motivo, sin escritura negocio. |
| N4.3 | Éxito: persistir en `auditoria_prioridad` (RDS Catálogo; ver `ARQUITECTURA-ACUERDOS.md`). |
| N4.4 | Commit offset tras save. |
| N4.5 | Test unitario regla (2 ramas). |

---

## N5 — Prueba pauta validación

1. Inyectar JSON **inválido** del archivo Giannina → sin fila negocio.
2. Mismo key, JSON **válido** → fila en RDS.
3. Capturas NIC3-K03, NIC3-K04.

---

## N6 — Deploy EC2 A

1. Inventario Ari: **EIP A** (no IP efímera).
2. Script deploy EP3: scp JAR + `ms-catalogo.env`.
3. Verificar: RDS, Rabbit privada C, Kafka privada D, `:8082`.
4. Si EIP asociada y mismo instance ID: **restart** JAR, no rebuild obligatorio.

---

## N7 — Evidencias y apoyo

Ver `Evidencias/CHECKLIST-NICO-EP3.md`.  
Apoyo opcional a Skarlett en demo lag (solo si acuerdan).

---

## Rama Git sugerida

`feature/nico-ep3-kafka` → PR `dev`.