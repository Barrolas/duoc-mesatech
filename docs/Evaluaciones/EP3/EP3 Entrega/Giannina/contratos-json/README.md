# Contratos JSON EP3 (entrega pauta)

**Responsable redacción:** Giannina · **Implementación:** Nico + Skarlett

Colocar aquí **6 archivos**:

| Tópico | Válido | Inválido |
| --- | --- | --- |
| `mesatech.solicitud.creada` | `solicitud-creada-valido.json` | `solicitud-creada-invalido.json` |
| `mesatech.solicitud.estado` | `solicitud-estado-valido.json` | `solicitud-estado-invalido.json` |
| `mesatech.catalogo.prioridad` | `prioridad-valido.json` | `prioridad-invalido.json` |

Especificación: [`../Contrato-eventos-EP3.md`](../Contrato-eventos-EP3.md) (definido 2026-11-24).

| Inválido — motivo de rechazo esperado |
| --- |
| `solicitud-creada-invalido.json` → `prioridadNivel` fuera de rango (1–5) |
| `solicitud-estado-invalido.json` → `estadoNuevo` no pertenece a `EstadoSolicitud` |
| `prioridad-invalido.json` → `nombreNuevo` vacío (`@NotBlank`) |