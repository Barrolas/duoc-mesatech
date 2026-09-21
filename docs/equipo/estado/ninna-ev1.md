# Estado EV1 — Ninna (API Gateway, v1/v2, marca)

**Contingencia:** Nico asume bloques **B + F + H** (antes Ninna). Este documento es la **lista maestra** de pasos, capturas y evidencias.

Referencias: [`../guias/ninna-gateway-marca-versiones.md`](../guias/ninna-gateway-marca-versiones.md), [`../rutas-api-gateway-bff.md`](../rutas-api-gateway-bff.md), [`nico-ev1.md`](nico-ev1.md).

---

## 1. Datos ya listos (Nico / repo)

| Dato | Valor |
| --- | --- |
| Región AWS | **us-east-1** (Learner Lab) |
| BFF integración HTTP | **`http://54.90.110.67:8080`** (EC2 `mesatech-ev1-bff`) |
| Rutas que debe proxy el Gateway | Tabla en [`rutas-api-gateway-bff.md`](../rutas-api-gateway-bff.md) |
| React local | `REACT_APP_API_BASE_URL` = **Invoke URL** (sin `/` final) |
| v1 / v2 en backend | `GET /v1/solicitudes/mias` vs `GET /v2/solicitudes/mias` (BFF → MS) |

**Pendiente Ari (bloquea 200 con token real):** `ENTRA_ISSUER_URI`, audience = Client ID de la app API (`ENTRA_API_CLIENT_ID` en `.env` local).

**Red:** Si el Gateway no alcanza el BFF, revisar **BFF-SG** (8080): además de tu IP, puede hacer falta permitir tráfico desde la integración (en HTTP API la petición sale desde AWS hacia la IP pública del BFF).

---

## 2. Orden de trabajo recomendado (checklist operativo)

| # | Tarea | Captura | Estado |
| --- | --- | --- | --- |
| 1 | Crear **HTTP API** `mesatech-ev1` | **NIN-01** | [ ] |
| 2 | Integración HTTP al BFF (`54.90.110.67:8080`) — ver §3 | **NIN-03** | [ ] |
| 3 | Rutas `/api`, `/v1`, `/v2` (proxy) | **NIN-02** | [ ] |
| 4 | **JWT Authorizer** issuer + audience (Ari) | **NIN-04** | [ ] |
| 5 | Adjuntar authorizer a rutas de negocio | **NIN-04** (attached) | [ ] |
| 6 | **CORS** Gateway (localhost:3000 + headers/métodos) | **NIN-05** | [ ] |
| 7 | Postman: GET `/v1/solicitudes/mias` **sin** token → **401** | **NIN-06** | [ ] |
| 8 | Postman: Bearer `invalid` → **401** | **NIN-07** | [ ] |
| 9 | Postman/React: Bearer válido → **200** | **NIN-08** | [ ] |
| 10 | React + F12: sin error CORS | **NIN-09** | [ ] |
| 11 | Mismo token: v1 lista vs v2 objeto | **NIN-10**, **NIN-11** | [ ] |
| 12 | Paleta + logo + guía marca | **NIN-H1** | [ ] |
| 13 | Actualizar `frontend/.env` Invoke URL; probar E2E | SKA-E06 | [ ] |
| 14 | Actualizar diagrama NIC-10 con Invoke URL | NIC-10 | [ ] |
| 15 | Generar Word evidencia (carpeta capturas) | Ver §4 | [ ] |

---

## 3. Configuración Gateway (resumen técnico)

### 3.1 Integración (principiante — una sola integración)

En **HTTP API** → **Integrations** → Create → **HTTP URI**:

```text
http://54.90.110.67:8080/{proxy}
```

En **Routes** → Create:

| Método | Path | Integración |
| --- | --- | --- |
| ANY | `/{proxy+}` | La integración anterior |

Así cualquier path que use React (`/v1/...`, `/api/usuario`) llega al BFF con el mismo path.

**Alternativa:** rutas explícitas por path (más capturas NIN-02, más trabajo).

**Excepción opcional:** ruta `GET /public/hola` **sin** authorizer (demo pública); el React MVP no la usa.

### 3.2 JWT Authorizer

| Campo | Valor (desde `.env` / Ari) |
| --- | --- |
| Issuer | `https://login.microsoftonline.com/<TENANT_ID>/v2.0` |
| Audience | Client ID app API (ej. mismo que `ENTRA_AUDIENCE`) |
| Identity source | `$request.header.Authorization` |

Adjuntar a rutas `/{proxy+}` o a cada ruta de negocio. No adjuntar a `/public/hola` si queda pública.

### 3.3 CORS (consola HTTP API)

| Campo | Valor |
| --- | --- |
| Allow origins | `http://localhost:3000` |
| Allow headers | `Authorization`, `Content-Type` |
| Allow methods | GET, POST, PATCH, PUT, DELETE, OPTIONS |

### 3.4 BFF CORS (Nico)

En EC2 / `mesatech.env`, añadir origen del front si hace falta; para prueba vía Gateway el navegador ve origen `localhost:3000` contra el **dominio execute-api** — el CORS principal es el del **Gateway**, no sustituye el del BFF para llamadas directas al BFF.

### 3.5 Pruebas rápidas (PowerShell)

Sustituir `<INVOKE>` por la URL sin barra final:

```powershell
# NIN-06 — 401 sin token
curl.exe -i "https://<INVOKE>/v1/solicitudes/mias"

# NIN-07 — 401 token inválido
curl.exe -i -H "Authorization: Bearer invalid" "https://<INVOKE>/v1/solicitudes/mias"

# NIN-08 / NIN-10 / NIN-11 — con token (pegar Bearer en variable, NO commitear)
# curl.exe -i -H "Authorization: Bearer <TOKEN>" "https://<INVOKE>/v1/solicitudes/mias"
# curl.exe -i -H "Authorization: Bearer <TOKEN>" "https://<INVOKE>/v2/solicitudes/mias"
```

---

## 4. Carpeta de capturas y Word (mismo formato que Nico)

**Carpeta sugerida en tu PC:**

```text
E:\DOWNLOADS\MesaTech-EV1-Ninna\
```

**Nombres de archivo** (guardar PNG tal cual para el generador):

| Archivo | ID |
| --- | --- |
| `NIN-01 HTTP API Invoke URL.png` | NIN-01 |
| `NIN-02 Rutas v1 v2 api.png` | NIN-02 |
| `NIN-03 Integracion BFF 8080.png` | NIN-03 |
| `NIN-04 JWT Authorizer.png` | NIN-04 |
| `NIN-05 CORS.png` | NIN-05 |
| `NIN-06 401 sin token.png` | NIN-06 |
| `NIN-07 401 JWT invalido.png` | NIN-07 |
| `NIN-08 200 JWT valido.png` | NIN-08 |
| `NIN-09 CORS React F12.png` | NIN-09 |
| `NIN-10 v1 solicitudes mias.png` | NIN-10 |
| `NIN-11 v2 solicitudes mias.png` | NIN-11 |
| `NIN-CRUD.png` | NIN-CRUD (catálogo admin POST/PUT/DELETE; Nico) |
| `NIN-H1 Marca paleta logo.png` | NIN-H1 |

Generar informe Word:

```powershell
python "E:\DOWNLOADS\MesaTech-EV1-Ninna\_generar_evidencia_ninna.py"
```

Salida: `MesaTech_EV1_Evidencia_Ninna_Gateway_Marca.docx` en la misma carpeta.

Copia del script en repo: [`../../../infra/scripts/generar-evidencia-ninna-word.py`](../../../infra/scripts/generar-evidencia-ninna-word.py).

---

## 5. Bloque F — Texto informe (copiar/adaptar)

```text
MesaTech expone GET /v1/solicitudes/mias y GET /v2/solicitudes/mias a través del
HTTP API Gateway (Invoke URL), con integración HTTP al BFF en EC2 (:8080).

v1: respuesta JSON — array de solicitudes.
v2: objeto JSON { "version": "2", "usuario": "<preferred_username>", "solicitudes": [ ... ] }.

Se mantiene v1 para contrato estable; v2 agrega metadatos sin romper clientes v1 (EP1 §14).
```

---

## 6. Bloque H — Marca (mínimo entregable)

| Entregable | Ubicación repo |
| --- | --- |
| `guia-marca.md` | [`docs/assets/marca/guia-marca.md`](../../assets/marca/guia-marca.md) |
| Logo SVG/PNG | `docs/assets/marca/` + opcional `frontend/public/logo.svg` (ya hay logo base) |

---

## 7. Git / rama sugerida

```text
feature/ninna-gateway
```

Commits: solo docs/marca/config de ejemplo; **no** Invoke URL con secretos si el equipo prefiere; sí actualizar `diagrama.md` y `env.example` cuando el Gateway esté estable.

---

## 8. Coordinación

| Persona | Necesita de ti |
| --- | --- |
| **Skarlet** | Invoke URL, Postman collection exportada, NIN-08/09 OK |
| **Nico** | Invoke URL para NIC-10 diagrama + PPT |
| **Ari** | Issuer/audience verificados en NIN-04 |
