# EP3 — Acuerdos de arquitectura

**Redacta:** Giannina · **Aprueba:** equipo  
**Estado:** **cerrado** — **Alternativa 1** adoptada (2026-11-24)

## Propuesta inicial (reunión 30 min)

| Tema | Propuesta | Decisión final |
| --- | --- | --- |
| EC2 Kafka | **D** dedicada, Docker compose curso | **D** |
| Rabbit EP2 | **C** (recrear si lab rotó) | **C** |
| Elastic IP | Una por A, B, C, D | **Sí** |
| BFF | EC2 A o B, puerto 8080 | **EC2 A** (Catálogo) |
| Kafka UI | Puerto **8088** en D (no 8080) | **8088** |

## Tópicos (3 particiones, RF=1)

| Hecho | Tópico | Key |
| --- | --- | --- |
| Solicitud creada | `mesatech.solicitud.creada` | `solicitud-{id}` |
| Estado solicitud | `mesatech.solicitud.estado` | `solicitud-{id}` |
| Prioridad renombrada | `mesatech.catalogo.prioridad` | `prioridad-{id}` |

DLQ opcional: `mesatech.dlq.eventos` (3 particiones).

## Consumer groups

| Grupo | Servicio | Listeners |
| --- | --- | --- |
| `mesatech-catalogo-kafka` | ms-catalogo (EC2 A) | `mesatech.catalogo.prioridad` |
| `mesatech-solicitudes-kafka` | ms-solicitudes (EC2 B) | `mesatech.solicitud.creada` + `mesatech.solicitud.estado` |

## Tablas RDS (seguimiento)

| Tabla | RDS | Owner |
| --- | --- | --- |
| `auditoria_prioridad` | Catálogo | Nico |
| `seguimiento_solicitud` | Solicitudes | Skarlett |
| `historial_estado_solicitud` | Solicitudes | Skarlett |

## Cinco alternativas (elegir **una** en reunión)

Cada alternativa es un paquete coherente: **BFF + tópicos + consumer groups + tablas nuevas EP3**.

### Alternativa 1 — Estándar equipo ✅ **ADOPTADA**

| Decisión | Valor |
| --- | --- |
| **BFF** | **EC2 A** (junto a Catálogo; Gatway → misma EIP “front MS”) |
| **Tópico solicitud creada** | `mesatech.solicitud.creada` |
| **Tópico cambio estado** | `mesatech.solicitud.estado` |
| **Tópico prioridad** | `mesatech.catalogo.prioridad` |
| **Keys** | `solicitud-{id}`, `solicitud-{id}`, `prioridad-{id}` |
| **Group Catálogo** | `mesatech-catalogo-kafka` |
| **Group Solicitudes** | `mesatech-solicitudes-kafka` (2 listeners, mismo group) |
| **DLQ (opcional)** | `mesatech.dlq.eventos` |
| **Tablas RDS Catálogo** | `auditoria_prioridad` |
| **Tablas RDS Solicitudes** | `seguimiento_solicitud`, `historial_estado_solicitud` |

**Por qué:** Nombres ya usados en backlog; un group por MS (como `mi-grupo-educativo` del lab); BFF en A replica EP2 habitual (Nico despliega A).

---

### Alternativa 2 — BFF en Solicitudes

| Decisión | Valor |
| --- | --- |
| **BFF** | **EC2 B** (cerca del tráfico operador/solicitudes) |
| **Tópicos** | Iguales que alt. 1 (`mesatech.solicitud.*`, `mesatech.catalogo.prioridad`) |
| **Group Catálogo** | `catalogo-prioridad-consumer` |
| **Group Solicitudes** | `solicitudes-seguimiento-consumer` |
| **Tablas RDS Catálogo** | `ep3_auditoria_prioridad` |
| **Tablas RDS Solicitudes** | `ep3_seguimiento_solicitud`, `ep3_historial_estado` |

**Por qué:** Si la demo en vivo es más “bandeja/solicitudes”, el BFF vive con Skarlett en B; tópicos sin cambio.

---

### Alternativa 3 — Un consumer group por tópico (lag más claro)

| Decisión | Valor |
| --- | --- |
| **BFF** | **EC2 A** |
| **Tópicos** | `mesatech.solicitud.creada`, `mesatech.solicitud.estado`, `mesatech.catalogo.prioridad` |
| **Group por tópico** | `mesatech-g-solicitud-creada`, `mesatech-g-solicitud-estado`, `mesatech-g-prioridad` |
| **Tablas RDS Catálogo** | `kafka_auditoria_prioridad` |
| **Tablas RDS Solicitudes** | `kafka_seguimiento_creada`, `kafka_historial_estado` |

**Por qué:** En informe/lag cada tópico tiene group distinto → `kafka-consumer-groups --describe` más legible; 3 grupos en vez de 2 JARs con grupos distintos.

---

### Alternativa 4 — Nombres explícitos (pauta / informe)

| Decisión | Valor |
| --- | --- |
| **BFF** | **EC2 A** |
| **Tópico solicitud creada** | `mesatech.solicitudes.creada` |
| **Tópico cambio estado** | `mesatech.solicitudes.estado-cambiado` |
| **Tópico prioridad** | `mesatech.catalogo.prioridad-renombrada` |
| **Group Catálogo** | `mesatech-ms-catalogo` |
| **Group Solicitudes** | `mesatech-ms-solicitudes` |
| **Tablas RDS Catálogo** | `auditoria_cambio_prioridad` |
| **Tablas RDS Solicitudes** | `seguimiento_operativo`, `historial_transicion_estado` |

**Por qué:** Tópicos autodescriptivos para el docente; alineado con tablas `solicitudes` / `categorias` existentes.

---

### Alternativa 5 — Nombres cortos + DLQ formal

| Decisión | Valor |
| --- | --- |
| **BFF** | **EC2 B** |
| **Tópico solicitud creada** | `solicitudes.creada` |
| **Tópico cambio estado** | `solicitudes.estado` |
| **Tópico prioridad** | `catalogo.prioridad` |
| **Group Catálogo** | `mesatech-catalogo-auditoria` |
| **Group Solicitudes** | `mesatech-solicitudes-seguimiento` |
| **DLQ** | `mesatech.eventos.invalidos` (3 particiones) |
| **Tablas RDS Catálogo** | `auditoria_prioridad` |
| **Tablas RDS Solicitudes** | `seguimiento_operativo` (tipo: CREADA \| ESTADO), `detalle_seguimiento` (JSON o columnas) |

**Por qué:** Tópicos tipo “dominio.evento”; opción de **una** tabla de seguimiento con discriminador si quieren menos DDL (validar con docente que las dos ramas de regla queden trazables).

---

## Decisión final del equipo

| Campo | Valor (Alternativa 1) |
| --- | --- |
| BFF en EC2 | **A** (:8080), integración Gateway hacia EIP A |
| Tópicos (3) | `mesatech.solicitud.creada`, `mesatech.solicitud.estado`, `mesatech.catalogo.prioridad` |
| Consumer groups | `mesatech-catalogo-kafka`, `mesatech-solicitudes-kafka` |
| Tablas RDS nuevas | `auditoria_prioridad`, `seguimiento_solicitud`, `historial_estado_solicitud` |
| DLQ | `mesatech.dlq.eventos` — **opcional** (3 particiones); definir al implementar rechazos |

## Acta reunión

- Fecha: **2026-11-24**
- Asistentes: (completar nombres en entrega)
- Alternativa adoptada: **1**
- Cambios respecto al borrador: ninguno en nombres; tablas `seguimiento_solicitud` / `historial_estado_solicitud` (no `seguimiento_operativo`)