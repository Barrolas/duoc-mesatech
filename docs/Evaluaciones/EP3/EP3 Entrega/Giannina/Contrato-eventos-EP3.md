# Contrato de eventos Kafka — EP3 MesaTech

**Estado:** definido (Alternativa 1) · **Autoría contrato:** Giannina  
**Implementación DTO/listeners:** Nico (`mesatech.catalogo.prioridad`), Skarlett (`mesatech.solicitud.*`)  
**Tópicos y keys:** [`../ARQUITECTURA-ACUERDOS.md`](../ARQUITECTURA-ACUERDOS.md)

Cada mensaje es **un JSON** (valor del record). La **key** de Kafka va aparte (`solicitud-{id}` o `prioridad-{id}`).

---

## 1. Envelope común (todos los eventos)

| Campo | Tipo JSON | Obligatorio | Validación sugerida (Bean Validation) |
| --- | --- | --- | --- |
| `eventId` | string | Sí | `@NotBlank` + `@Pattern` UUID v4 |
| `tipoEvento` | string | Sí | `@NotBlank` — literal fijo por tópico (tabla §2) |
| `fechaEvento` | string | Sí | `@NotBlank` + ISO-8601 UTC con `Z` (instant) |

**UUID v4 (regex):** `^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$` (case insensitive).

**`fechaEvento`:** ejemplo `2026-11-24T21:15:30Z`. No admitir offset local sin normalizar.

---

## 2. Eventos por tópico

### 2.1 `mesatech.solicitud.creada`

**Productor:** `ms-solicitudes` (tras POST exitoso). **Key:** `solicitud-{solicitudId}`.

| Campo | Tipo | Obligatorio | Validación |
| --- | --- | --- | --- |
| `tipoEvento` | string | Sí | Constante `solicitud.creada` |
| `solicitudId` | number (entero) | Sí | `@NotNull` `@Positive` |
| `titulo` | string | Sí | `@NotBlank` `@Size(max = 120)` |
| `prioridadNombre` | string | Sí | `@NotBlank` `@Size(max = 50)` |
| `prioridadNivel` | number (entero) | Sí | `@NotNull` `@Min(1)` `@Max(5)` |
| `categoriaNombre` | string | No | `@Size(max = 80)` si viene |

**Regla consumer (`seguimiento_solicitud`):**

| Condición | `tipoAtencion` (columna sugerida) |
| --- | --- |
| `prioridadNivel >= 4` | `PREFERENTE` |
| `prioridadNivel <= 3` | `NORMAL` |

*(Nombres `ALTA` / `URGENTE` en catálogo suelen mapear a nivel 4–5; alinear seed RDS con el equipo.)*

---

### 2.2 `mesatech.solicitud.estado`

**Productor:** `ms-solicitudes` (transición válida según `TransicionesEstado`). **Key:** `solicitud-{solicitudId}`.

| Campo | Tipo | Obligatorio | Validación |
| --- | --- | --- | --- |
| `tipoEvento` | string | Sí | Constante `solicitud.estado` |
| `solicitudId` | number | Sí | `@NotNull` `@Positive` |
| `estadoAnterior` | string | Sí | `@NotNull` — enum `EstadoSolicitud` |
| `estadoNuevo` | string | Sí | `@NotNull` — enum `EstadoSolicitud` |

**Valores `EstadoSolicitud`:** `CREADA`, `ASIGNADA`, `EN_PROCESO`, `RESUELTA`, `CERRADA`, `CANCELADA`.

**Regla consumer (`historial_estado_solicitud`):**

| `estadoNuevo` | `enAtencion` (boolean o equivalente) |
| --- | --- |
| `RESUELTA` o `CERRADA` | `false` — “salió de atención” |
| Cualquier otro válido | `true` — “continúa en seguimiento” |

**Contrato adicional:** `estadoAnterior` debe ser distinto de `estadoNuevo` (validar en producer o consumer; si son iguales → rechazo con motivo explícito).

---

### 2.3 `mesatech.catalogo.prioridad`

**Productor:** `ms-catalogo` (tras PATCH/PUT de `Prioridad.nombre`). **Key:** `prioridad-{prioridadId}`.

| Campo | Tipo | Obligatorio | Validación |
| --- | --- | --- | --- |
| `tipoEvento` | string | Sí | Constante `catalogo.prioridad.renombrada` |
| `prioridadId` | number | Sí | `@NotNull` `@Positive` |
| `nombreAnterior` | string | Sí | `@NotBlank` `@Size(max = 80)` |
| `nombreNuevo` | string | Sí | `@NotBlank` `@Size(max = 80)` |
| `nivel` | number | No | `@Min(1)` `@Max(5)` si viene (copia del catálogo) |

**Regla consumer (`auditoria_prioridad`):** normalizar con `trim` + colapsar espacios + comparar **sin distinguir mayúsculas**:

| Comparación | `tipoCambio` (columna sugerida) |
| --- | --- |
| Normalizados iguales, literales distintos | `CORRECCION_FORMATO` |
| Normalizados distintos | `REVISION_SUSTANTIVA` |

---

## 3. Validación en listener

1. Deserializar a **record/class DTO** por tópico.
2. `@Valid` + opcional `@Validated` en método listener.
3. Si falla: log **campo + mensaje** (p. ej. `prioridadNivel: debe estar entre 1 y 5`), **sin** INSERT en tablas EP3; estrategia DLQ/retry según backlog (no ack hasta decisión documentada).
4. Prueba pauta: publicar payload **inválido** del repo → sin fila; luego **válido** con **misma key** → fila persistida.

---

## 4. Archivos JSON de autoría

Ubicación: [`contratos-json/`](contratos-json/).

| Archivo | Uso en defensa |
| --- | --- |
| `*-valido.json` | Caso que pasa validación y dispara regla de negocio |
| `*-invalido.json` | JSON sintácticamente válido; rompe una regla del §2 |

**Keys sugeridas al publicar en prueba:**

| Archivo | Kafka key |
| --- | --- |
| `solicitud-creada-*.json` | `solicitud-1001` |
| `solicitud-estado-*.json` | `solicitud-1001` |
| `prioridad-*.json` | `prioridad-2` |

---

## 5. Referencia rápida `tipoEvento`

| Tópico | `tipoEvento` |
| --- | --- |
| `mesatech.solicitud.creada` | `solicitud.creada` |
| `mesatech.solicitud.estado` | `solicitud.estado` |
| `mesatech.catalogo.prioridad` | `catalogo.prioridad.renombrada` |

---

## 6. Aprobación

| Integrante | Rol | Fecha |
| --- | --- | --- |
| Giannina | Redacción contrato + JSON | 2026-11-24 |
| Nicolás | Productor/consumer prioridad | pendiente review |
| Skarlett | Productor/listeners solicitud | pendiente review |