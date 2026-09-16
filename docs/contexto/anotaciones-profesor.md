# Anotaciones del profesor — EP1 MesaTech Cloud

Documento vivo para registrar instrucciones verbales o aclaraciones del docente que **complementan o flexibilizan** la guía oficial ([`docs/caso/ep1-guia-oficial.md`](../caso/ep1-guia-oficial.md)).

> Cómo usarlo: cada vez que el profesor dé una instrucción nueva, se agrega una entrada con fecha. Si contradice la guía, se deja explícito qué queda permitido.

---

## Resumen rápido de lo autorizado

| Tema | Guía oficial | Lo que dijo el profesor |
| --- | --- | --- |
| Identidad | Microsoft Entra ID (Azure) | El profesor permitió Cognito, pero el equipo usará **Microsoft Entra ID (Azure) como IDaaS** |
| Frontend | React (CRA) + MSAL, no Angular | Se puede usar **React** (en vez de Angular del diagrama) |
| Base de datos | RDS o motor de BD en EC2 | Se puede **dockerizar** el motor y **subirlo a una EC2**, o usar **RDS Aurora** |
| API Gateway | AWS API Gateway (HTTP API) | **Único punto de entrada.** Si se intenta ir directo al BFF o a un microservicio → **401** |
| Backend | BFF + 2 microservicios Spring Boot en EC2 | Sin cambio por ahora. No se consumen en forma directa |

---

## 2026-09-14 — Flexibilidad de stack

### Identidad: Cognito en lugar de Azure

El profesor autorizó usar **Amazon Cognito** como proveedor de identidad, en vez de Microsoft Entra ID.

Implicaciones para la solución:

- El login/logout del frontend se haría contra Cognito (User Pool + App Client), no contra Entra ID / MSAL.
- Los JWT los emite Cognito. API Gateway y el BFF deben validar issuer, audience, firma y expiración de esos tokens.
- En la guía oficial Cognito aparece como “fuera de alcance / no se evalúa”. Con esta instrucción verbal **sí queda permitido** como alternativa de identidad.
- Si se elige Cognito, hay que documentarlo y evidenciar la configuración (User Pool, App Client, roles/grupos, scopes, issuer y audience).

### Frontend: React en vez de Angular

El diagrama de referencia muestra Angular + MSAL. El profesor confirmó que la solución puede hacerse en **React**.

Esto coincide con la guía escrita (sección 3: React + MSAL). Queda claro que:

- No hay que implementar Angular.
- Si la identidad es Entra ID, se usa MSAL (`@azure/msal-browser` / `@azure/msal-react`).
- Si la identidad es Cognito, se usa el SDK/flujo correspondiente de Cognito (no MSAL de Azure).

### Persistencia: Docker en EC2 o RDS Aurora

El profesor amplió las opciones de base de datos. Además de lo escrito en la guía (RDS o motor instalado en EC2), se puede:

- **Dockerizar** el motor y **subirlo a una instancia EC2**.
- Usar **Amazon RDS Aurora**.

**Decisión del equipo:** PostgreSQL 16 en Docker (`infra/`), a desplegar en EC2. Solo los microservicios acceden a los datos.

La elección debe justificarse en el informe. Evidenciar el contenedor en EC2 y que los datos sobrevivan a nuevas consultas.

### Punto de entrada único: todo pasa por API Gateway (401 si no)

Regla de seguridad de la solución:

- React **nunca** llama al BFF ni a los microservicios por IP/puerto de EC2.
- Toda petición de negocio sale hacia **AWS API Gateway** (HTTP API) con `Authorization: Bearer <access_token>`.
- API Gateway valida el JWT (JWT Authorizer). Sin token, token inválido o expirado: **401** y la request **no llega** a la lógica de negocio.
- Si alguien intenta entrar **en forma directa** al BFF o a un microservicio (bypass del Gateway), la respuesta debe ser **401 Unauthorized**.
- El BFF vuelve a validar el JWT (segunda capa). Los microservicios no quedan expuestos como API pública: solo los invoca el BFF.

Evidencias mínimas a dejar en el informe:

- Consumo correcto: React → API Gateway → 200 (con JWT válido y permisos).
- Sin token o JWT inválido contra API Gateway → **401**.
- Llamada directa al backend en EC2 → **401**.

---

## Decisiones tentativas del equipo

A completar cuando el grupo cierre el stack.

| Componente | Opción elegida | Notas |
| --- | --- | --- |
| Identidad | **Microsoft Entra ID (Azure)** | Decisión del equipo (2026-09-14). No Cognito |
| Frontend | React | Autorizado; no usar Angular |
| Base de datos | **PostgreSQL 16 Docker en EC2** | Alternativa permitida: Aurora |
| BFF y microservicios | Spring Boot en EC2 | Según guía |
| API Gateway | HTTP API · único entrypoint | Directo a EC2 = 401 |

---

## Pendientes / por confirmar con el profesor

- ¿Aurora Serverless cuenta como “RDS” para las evidencias?
- Si la BD va en Docker sobre EC2, ¿puede compartir la misma instancia que BFF/microservicios o conviene una EC2 aparte?

---

## 2026-09-14 — Decisión: Entra ID como IDaaS

El equipo **no usará Cognito**. La identidad queda en **Microsoft Entra ID (Azure)**, alineada a la guía oficial y a la PPT *Configuración Cloud + Backend*:

- Dos App Registrations: SPA React + API `api-cloud-native`.
- Scope `access_as_user`, access token v2, consentimiento de administrador.
- React con MSAL + Axios; BFF como OAuth2 Resource Server (`iss` + `aud`).
- Paso a paso detallado: [`docs/guia/entra-spring-react.md`](../guia/entra-spring-react.md).
- Esqueleto del repo: `frontend/` (MSAL + Axios), `bff/` (Resource Server sin BD), `ms-solicitudes/`, `ms-catalogo/`, `infra/` (PostgreSQL Docker). Partimos de los repos de ejemplo del profesor, sin copiar sus Client ID.

---

## Registro de nuevas anotaciones

_Las próximas instrucciones del profesor se agregan acá, con fecha._
