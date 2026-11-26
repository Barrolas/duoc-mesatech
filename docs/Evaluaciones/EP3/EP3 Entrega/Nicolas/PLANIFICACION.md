# Planificación de ejecución — Nicolás EP3

**Alcance:** `ms-catalogo` en EC2 **A** · tópico `mesatech.catalogo.prioridad` · group `mesatech-catalogo-kafka`  
**Fuera de alcance (otros):** tópicos solicitud (Skarlett), Gateway (Giannina), Kafka EC2 D + EIP (Ari).  
**Contrato:** [`../Giannina/Contrato-eventos-EP3.md`](../Giannina/Contrato-eventos-EP3.md)  
**Checklist evidencias:** [`Evidencias/CHECKLIST-NICO-EP3.md`](Evidencias/CHECKLIST-NICO-EP3.md)  
**Backlog detalle:** [`BACKLOG.md`](BACKLOG.md)

**Rama:** `feature/nico-ep3-kafka` → PR a `dev` (revisión: Skarlett o Ari en Kafka/JWT).

---

## Objetivo de cierre

Al terminar esta planificación en código + cloud:

1. `PUT /v1/catalogo/prioridades/{id}` publica evento con key `prioridad-{id}` y `acks=all`.
2. El listener del mismo MS valida DTO, aplica regla formato/sustantivo y escribe en `auditoria_prioridad`.
3. JSON inválido/válido de Giannina reproducen NIC3-K03 y NIC3-K04.
4. POST categoría + Rabbit EP2 sigue operativo (NIC3-K05).
5. Catálogo desplegado en **A** con Kafka (privada **D**) y Rabbit (privada **C**).

---

## Dependencias del equipo

| Necesito | Estado | Impacto si falta |
| --- | --- | --- |
| Contrato + 6 JSON | Listo en `dev` | — |
| Arquitectura alt. 1 | Cerrada | — |
| Kafka local (compose) | **Por crear** (Fase 1) | No bloquea cloud, sí bloquea desarrollo |
| Inventario EP3 + EIP A + IP privada Kafka | Ari → `Inventario_AWS_EP3.md` | Solo Fase 5 (deploy) |
| BFF en A + Gateway | Giannina / Ari | No bloquea prueba directa `:8082` |

**Trabajo en paralelo:** Fases 1–4 **sin cloud**; Fase 5 cuando Ari publique bootstrap Kafka y EIP.

---

## Fases (orden de ejecución)

### Fase 1 — Entorno local Kafka (N0, N2)

| # | Tarea | Entregable |
| --- | --- | --- |
| 1.1 | Crear rama `feature/nico-ep3-kafka` desde `dev` | Rama remota |
| 1.2 | `infra/kafka/compose.yaml` (imagen curso / KRaft o Zookeeper según material) | Broker `localhost:9092` |
| 1.3 | Script `infra/kafka/crear-topicos-ep3.ps1` (o `.sh`): `mesatech.catalogo.prioridad` **3 particiones**, RF=1 local | NIC3-K01 preparado |
| 1.4 | Smoke: productor/consola o `_refs/ep3-kafka-basico` contra el broker | Confianza en red local |

**Criterio de hecho:** `docker compose up` + tópico listado con 3 particiones.

**Commit sugerido:** `[ CHORE ]: Compose Kafka local y script tópicos EP3`

---

### Fase 2 — Config Spring Kafka (N1)

| # | Tarea | Entregable |
| --- | --- | --- |
| 2.1 | `ms-catalogo/pom.xml`: `spring-boot-starter-kafka` (+ `validation` si falta para `@Valid`) | Build OK |
| 2.2 | `application.properties`: bootstrap, serializers String, `acks=all`, consumer manual ack | Arranque sin broker = fail rápido configurable |
| 2.3 | `KafkaTopicsProperties` o constantes: nombre tópico, group-id | Una fuente de verdad |
| 2.4 | `KafkaConsumerConfig` + `KafkaProducerConfig` (patrón `_refs/ep3-kafka-basico`) | Beans listos |
| 2.5 | Variables: `KAFKA_BOOTSTRAP_SERVERS` en `env.example` raíz / doc Nico | Local + cloud |
| 2.6 | `run-local-stack.ps1`: default `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` si no existe | Stack completo |

**Criterio de hecho:** `mvn -q -pl ms-catalogo test` + app levanta con compose Postgres + Rabbit + Kafka.

**Commit sugerido:** `[ FEAT ]: Dependencias y configuración Kafka en ms-catalogo`

---

### Fase 3 — Dominio, productor y consumidor (N3, N4)

#### Artefactos en `ms-catalogo` — metodología lab + contrato Giannina

Ver [`REFERENCIAS-KAFKA.md`](REFERENCIAS-KAFKA.md).

```text
dto/
  PrioridadRenombradaDTO.java    # record + @Valid (JSON Giannina)
consumer/
  CatalogoConsumer.java          # estilo VentaConsumer del lab
service/
  CatalogoPrioridadService.java  # regla formato / sustantivo → RDS
domain/
  AuditoriaPrioridad.java        # tabla auditoria_prioridad
repo/
  AuditoriaPrioridadRepository.java
config/
  KafkaConfig.java
web/
  CatalogoController.java        # publica tras cambio de nombre
```

#### Tabla `auditoria_prioridad` (RDS Catálogo)

| Columna | Tipo | Notas |
| --- | --- | --- |
| `id` | bigint PK | identity |
| `prioridad_id` | bigint | FK lógica a `prioridades` |
| `nombre_anterior` | varchar | |
| `nombre_nuevo` | varchar | |
| `tipo_cambio` | varchar | `CORRECCION_FORMATO` \| `REVISION_SUSTANTIVA` |
| `event_id` | varchar | idempotencia / trazabilidad |
| `fecha_registro` | timestamptz | default now |

Migración: `ddl-auto=update` documentado en README Nico (o script SQL en `infra/` si el equipo prefiere explícito).

| # | Tarea | Detalle |
| --- | --- | --- |
| 3.1 | DTO alineado a `prioridad-valido.json` / `prioridad-invalido.json` | `tipoEvento = catalogo.prioridad.renombrada` |
| 3.2 | Refactor `CatalogoController.actualizarPrioridad` | Capturar `nombreAnterior` **antes** de `save`; publicar **después** de persistir prioridad |
| 3.3 | Publisher | key `prioridad-{id}`, log `eventId`, key, ack |
| 3.4 | Listener | `@Valid`, `MethodValidationException` / `ConstraintViolation` → log, **no** INSERT |
| 3.5 | Servicio auditoría | Regla contrato §2.3 |
| 3.6 | Ack manual | `acknowledge()` solo tras `save` exitoso |
| 3.7 | Tests unitarios | Tests de `VentaService` (formato vs sustantivo); validación `VentaDTO` |

**Criterio de hecho:** PUT prioridad en local → fila en `auditoria_prioridad`; Rabbit categoría sin cambios en tests existentes.

**Commits sugeridos (pueden ser uno solo):**

- `[ FEAT ]: Publicar evento prioridad renombrada en Kafka`
- `[ FEAT ]: Consumer auditoria_prioridad con validación de contrato`

---

### Fase 4 — Pruebas pauta y regresión EP2 (N5, parte N7)

| # | Tarea | Evidencia |
| --- | --- | --- |
| 4.1 | Publicar manual `prioridad-invalido.json` al tópico (kafka-console o test `@SpringBootTest`) | NIC3-K03 |
| 4.2 | Mismo key `prioridad-2`, publicar `prioridad-valido.json` | NIC3-K04 |
| 4.3 | POST categoría → Rabbit → sin error en logs catálogo | NIC3-K05 |
| 4.4 | Dos PUT con misma prioridad → misma partición, offsets++ | NIC3-K06 |

**Criterio de hecho:** checklist NIC3-K01 … K06 con capturas en `Evidencias/`.

**Commit sugerido:** `[ TEST ]: Pruebas Kafka prioridad y regresión Rabbit EP2` (si hay tests automatizados).

---

### Fase 5 — Deploy cloud EC2 A (N6, N7)

| # | Tarea | Notas |
| --- | --- | --- |
| 5.1 | Recibir `Inventario_AWS_EP3.md` (EIP A, privada Kafka D, Rabbit C, RDS) | SSH por EIP |
| 5.2 | Extender `deploy-ms-catalogo-ep2.ps1` → EP3 o script hermano | `KAFKA_BOOTSTRAP_SERVERS=<ip-privada-D>:9092` |
| 5.3 | `mvn package`, scp JAR + `ms-catalogo.env`, systemd o `nohup` como EP2 | Puerto 8082 SG |
| 5.4 | Crear tópicos en D (Ari o script remoto) si no existen | 3 particiones |
| 5.5 | Prueba PUT en cloud + consulta RDS + capturas | Cierre NIC3 |

**Criterio de hecho:** mismo flujo que Fase 3 en AWS, sin redeploy por IP efímera (EIP).

**Commit sugerido:** `[ CHORE ]: Script deploy ms-catalogo EP3 y variables Kafka`

---

## Estrategia de PR

| Opción | Cuándo |
| --- | --- |
| **Un PR** (`feature/nico-ep3-kafka`) | Equipo prefiere revisión única; commits por fase dentro de la rama |
| **Dos PR** | PR1 Fase 1–2 (infra + config), PR2 Fase 3–4 (código + tests) — menos diff por revisión |

Descripción PR: enlazar **NIC3-***, contrato Giannina, no romper EP2 Rabbit.

---

## Cronograma orientativo (ajustar con fechas del curso)

| Fase | Esfuerzo orientativo | ¿Bloqueo externo? |
| --- | --- | --- |
| 1 | ½ día | No |
| 2 | ½ día | No |
| 3 | 1–1½ días | No |
| 4 | ½ día | No |
| 5 | ½ día | Sí (Ari inventario + Kafka en D) |

**Siguiente acción inmediata:** Fase 1.1 + 1.2 (rama + compose Kafka local).

---

## Registro de avance

Marcar al cerrar cada fase (fecha + commit hash):

| Fase | Hecho | Commit / nota |
| --- | --- | --- |
| 1 | ☑ | rama `feature/nico-ep3-kafka`, `infra/kafka/*` |
| 2 | ☑ | spring-kafka, validation, KafkaConfig, env |
| 3 | ☑ | PrioridadRenombradaDTO, CatalogoConsumer, auditoria_prioridad |
| 4 | ☐ | |
| 5 | ☐ | |