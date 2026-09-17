# Estado EV1 — Skarlet (negocio, UI, pruebas, informe)

Última actualización: código integrado en **`dev`** (PR #2 Skarlet + commits Nico en BFF/MS). Rama **`feature/skarlet-negocio`**. **`main`** sin merge aún.

Estado Nico / EC2: [`nico-ev1.md`](nico-ev1.md).

---

## 1. Qué ya está en Git (rama `dev` / `feature/skarlet-negocio`)

| Área | Estado | Commits / archivos clave |
| --- | --- | --- |
| **UI React** | Hecho (MVP) | `App.js`, `Layout`, `FormCrearSolicitud`, `TablaSolicitudes`, `PanelCatalogo`, `App.css`, `logo.svg`, `package-lock.json` |
| **MSAL + Axios** | Hecho | `authConfig.js`, `api/http.js` (GET/POST/PATCH, mensajes 401/403) |
| **Roles en UI** | Hecho | `roles.js`, nav por permisos, aviso sin app roles |
| **Vista sin Entra** | Hecho | `VistaPrevia.js` + `index.js` (mock local) |
| **Transiciones estado (MS)** | Hecho | `TransicionesEstado.java`, `SolicitudController` → **409** |
| **BFF proxy errores** | Hecho | `ProxyExceptionHandler` (409 no → 500) |
| **BFF `/api/usuario` + roles** | Hecho | `UsuarioController` expone claim `roles` |
| **Autorización 403 (BFF)** | Hecho en repo | `EntraRoles`, controllers gateway (integrado por Nico en `f63c1e9`; cumple E.2 del plan) |
| **Marca Ninna (H)** | Parcial | CSS/logo base en repo; kit corporativo completo pendiente Ninna |

---

## 2. Bloque E — Negocio y autorización

| Ítem | Estado | Notas |
| --- | --- | --- |
| E.1 Matriz permisos (doc) | Hecho | Tabla en guía; implementación BFF alineada |
| E.2.1–E.2.4 BFF 403 | **Hecho en Git** | Ver `bff/security/EntraRoles.java` |
| E.2.5 Probar 3 usuarios | **Pendiente** | Requiere app roles Entra (**Ari**) + capturas SKA-T05 |
| E.3.1 Login/logout | Hecho | MSAL redirect |
| E.3.2 Rol y nombre | Hecho | `Layout` + `/api/usuario` |
| E.3.3 Crear solicitud | Hecho | |
| E.3.4 Mis solicitudes | Hecho | |
| E.3.5 Bandeja general | Hecho (UI) | API 403 si cliente (BFF) |
| E.3.6 Cambiar estado | Hecho | |
| E.3.7 Catálogo admin | Hecho (UI) | POST → 403 si no admin |
| E.3.8 Marca Ninna | Parcial | |
| E.3.9 Mensaje 403 | Hecho | `http.js` |
| E.4 Demo tres perfiles | **Pendiente** | Tabla informe + capturas |

---

## 3. Bloque G — Pruebas §11 (capturas)

| ID | Prueba | Código listo | Captura / entorno |
| --- | --- | --- | --- |
| SKA-T01 | Sin auth | Sí (UI login) | Pendiente captura |
| SKA-T02 / T03 | 401 Gateway | — | **Ninna** (NIN-06/07) |
| SKA-T04 | JWT OK + 200 | Sí | Local :8080 OK; **Gateway** pendiente Ninna |
| SKA-T05 | 403 roles | Sí (BFF) | Pendiente con usuarios Ari |
| SKA-T06 | CORS | — | Gateway + React (**Ninna** + Skarlet) |
| SKA-T07 | Regla RESUELTA | Sí (MS 409) | Pendiente captura |
| SKA-T08 | v1 / v2 | v2 en BFF/MS | Evidencia funcional pendiente |
| SKA-T09 | Persistencia | Sí (MS+PG) | **NIC-06** / refresh UI |

### UI informe (SKA-UI)

| ID | Estado |
| --- | --- |
| SKA-UI01 Pantalla principal | Hecho en repo (captura informe pendiente) |
| SKA-UI02 Crear solicitud | Hecho |
| SKA-UI03 Lista mías | Hecho |
| SKA-UI04 Lista todas (operador) | Hecho |
| SKA-UI05 Cambio estado | Hecho |
| SKA-UI06 Catálogo admin | Hecho |
| SKA-UI07 Logout | Hecho |

### E2E §10 (SKA-E01 … E12)

Mayoría **pendiente de captura** con flujo **React → Gateway → BFF** (pasos 6–8 dependen Ninna). Pasos 9–10 pueden reutilizar NIC-07 / NIC-05.

---

## 4. Checklist §15 (avance cruzado equipo)

| Ítem §15 | Estado Skarlet / repo | Evidencia |
| --- | --- | --- |
| Login/logout Entra | Código listo | Captura SKA-E03/E04 |
| Access Token API | Código listo | SKA-T04 |
| Front solo vía Gateway | **Pendiente entrega** | `REACT_APP_API_BASE_URL` = Invoke URL |
| CORS | Pendiente cloud | SKA-T06 |
| Gateway sin JWT | Pendiente | Ninna |
| BFF revalida JWT | Hecho (Nico + Ari config) | NIC-07 |
| BFF → MS, sin BD | Código listo | NIC-11 pendiente |
| Dos MS persisten | Hecho en cloud | NIC-06 / SKA-T09 |
| v1 + v2 coexisten | Código listo | SKA-T08 |
| Despliegue EC2 | Hecho (Nico) | NIC-01… |
| Diferencias por usuario | Código listo | SKA-T05 + demo E.4 |
| Sin secretos en repo | Revisión continua | G.5 |

---

## 5. Pendientes principales (Skarlet)

1. **Ari:** usuarios Entra con app roles → cerrar SKA-T05 y E.4.
2. **Ninna:** Gateway → SKA-T02–04–06, E2E pasos 6–8, front en producción.
3. **Capturas** en `capturas/skarlet/` y **informe** único (G.2–G.3).
4. **Checklist §15** con columna “Figura evidencia”.
5. Opcional: PR follow-up solo front/marca en `feature/skarlet-negocio` → `dev`.

---

## 6. Git

```bash
git checkout feature/skarlet-negocio
git pull origin dev   # o merge dev → tu rama si trabajas en feature
git push origin feature/skarlet-negocio
```

Integración actual: merge PR #2 ya en **`dev`**; Skarlet puede traer `dev` a su rama antes del próximo PR.

---

## Documentos relacionados

- [`guias/skarlet-negocio-pruebas-informe.md`](../guias/skarlet-negocio-pruebas-informe.md)
- [`rutas-api-gateway-bff.md`](../rutas-api-gateway-bff.md) (paths Axios)
- [`plan-trabajo.md`](../plan-trabajo.md) (sección Skarlet)
