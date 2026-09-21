# Diccionario de dominio — MesaTech Cloud

Vocabulario de **negocio** y **contratos HTTP** del MVP. La guía oficial está en [`../caso/ep1-guia-oficial.md`](../caso/ep1-guia-oficial.md).

---

## Actores

| Actor | Objetivo en el sistema |
| --- | --- |
| **Cliente** | Crear solicitudes, ver **solo las propias**, consultar estado. |
| **Operador** | Ver solicitudes (asignadas o globales según diseño), **cambiar estados**, atender. |
| **Administrador** | **CRUD catálogo** (categorías/prioridades), visión global de solicitudes. |

Roles técnicos: App roles o grupos en Entra (Ari) → claims en JWT → autorización en BFF (Skarlet).

---

## Entidades

| Entidad | Descripción |
| --- | --- |
| **Solicitud** | Ticket de soporte con título, descripción, estado, usuario creador, categoría/prioridad. |
| **Categoría** | Clasificación del tipo de problema (catálogo). |
| **Prioridad** | Nivel de urgencia (catálogo). |
| **Catálogo** | Conjunto administrable de categorías y prioridades (`ms-catalogo`). |

---

## Estados de solicitud

Flujo objetivo (transiciones válidas a implementar/validar):

```text
CREADA → ASIGNADA → EN_PROCESO → RESUELTA → CERRADA
   └────────────────── CANCELADA (reglas según equipo)
```

| Estado | Significado |
| --- | --- |
| **CREADA** | Ingresada por el cliente; aún no asignada. |
| **ASIGNADA** | Operador responsable asignado. |
| **EN_PROCESO** | Atención activa. |
| **RESUELTA** | Problema resuelto (**solo** desde `EN_PROCESO` — regla obligatoria EP1). |
| **CERRADA** | Caso cerrado administrativamente. |
| **CANCELADA** | Anulada según reglas de negocio. |

---

## Matriz de autorización (objetivo EV1)

| Acción | Cliente | Operador | Admin |
| --- | --- | --- | --- |
| Crear solicitud | Sí | Opcional | Opcional |
| Ver propias (`/mias`) | Sí | Sí | Sí |
| Ver todas (`GET /v1/solicitudes`) | No | Sí | Sí |
| Cambiar estado (`PATCH …/estado`) | No | Sí | Sí |
| Catálogo CRUD | No | No | Sí |

Respuesta esperada si el rol no alcanza: **403 Forbidden** (con JWT válido).

---

## API — rutas expuestas vía BFF

Todas las rutas de negocio pasan por **API Gateway** en AWS. El BFF reenvía a microservicios.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/public/hola` | Demo pública (solo BFF; no sustituye auth de negocio). |
| `GET` | `/api/usuario` | Claims del JWT (demo identidad). |
| `POST` | `/v1/solicitudes` | Crear solicitud. |
| `GET` | `/v1/solicitudes/mias` | Listado del usuario autenticado. |
| `GET` | `/v1/solicitudes` | Listado global (operador/admin). |
| `PATCH` | `/v1/solicitudes/{id}/estado` | Cambio de estado con reglas. |
| `GET` | `/v2/solicitudes/mias` | Misma data que v1 en envoltorio `{ version, usuario, solicitudes }`. |
| `GET` | `/v1/catalogo` | Consulta catálogo. |
| `POST` | `/v1/catalogo/categorias` | Alta categoría (admin). |
| `PUT` | `/v1/catalogo/categorias/{id}` | Editar categoría (admin). |
| `DELETE` | `/v1/catalogo/categorias/{id}` | Eliminar categoría (admin). |
| `POST` | `/v1/catalogo/prioridades` | Alta prioridad (admin). |
| `PUT` | `/v1/catalogo/prioridades/{id}` | Editar prioridad (admin). |
| `DELETE` | `/v1/catalogo/prioridades/{id}` | Eliminar prioridad (admin). |

Implementación: `bff/.../controller/*`, dominio en `ms-solicitudes/` y `ms-catalogo/`.

---

## Versionamiento v1 / v2

| Versión | Contrato | Motivo de coexistencia |
| --- | --- | --- |
| **v1** | Respuesta “plana” (lista de solicitudes). | Clientes existentes / contrato estable. |
| **v2** | Metadatos + lista (misma información de negocio). | Evolución sin romper v1 (Ninna documenta en informe). |

---

## Términos del informe

| Término | Qué evidenciar |
| --- | --- |
| **MVP** | Login, solicitudes, estados, catálogo mínimo, tres perfiles distintos. |
| **Trazabilidad** | Estados y persistencia en PostgreSQL tras recargar. |
| **Diferencias reales entre usuarios** | UI distinta + 403 en acciones prohibidas. |
