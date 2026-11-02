# MesaTech Cloud — EP2 (DSY1107)

MesaTech es el caso de mesa de ayuda del curso: solicitudes con flujo de estados, catálogo de categorías/prioridades e identidad corporativa. En la EP2 el catálogo publica el evento de creación de categoría y Solicitudes guarda una copia local vía RabbitMQ, cada uno en su propia RDS.

Equipo: **Ari**, **Giannina (Ninna)**, **Nicolás (Nico)**, **Skarlett**.

---

## Arquitectura EP2

```text
React + MSAL → API Gateway → BFF :8080
                               ├→ ms-catalogo :8082 (EC2 A) → RDS Catálogo
                               │         └→ publica AMQP → RabbitMQ (EC2 C)
                               └→ ms-solicitudes :8081 (EC2 B) → RDS Solicitudes
                                         └← consume cola solicitudes.catalogo.queue
```

| Componente | Ubicación (AWS Academy, us-east-1) |
| --- | --- |
| Catálogo + publicador | EC2 A `mesatech-ep2-catalogo` · RDS `mesatech-ep2-catalogo` |
| Solicitudes + consumidor | EC2 B `mesatech-ep2-solicitudes` · RDS `mesatech-ep2-solicitudes` |
| RabbitMQ 4.2-management | EC2 C `mesatech-ep2-rabbitmq` · vhost `mesatech` |
| BFF | Comparte EC2 A o B (decisión de despliegue del equipo) |
| Frontend | React + MSAL; en cloud apunta al HTTP API (Gateway) |

Números (IDs, IPs, endpoints, SG): [`docs/Evaluaciones/EP2/EP2 Entrega/Ari/Inventario_AWS_EP2.md`](docs/Evaluaciones/EP2/EP2%20Entrega/Ari/Inventario_AWS_EP2.md).

![Diagrama de despliegue EP2](docs/Evaluaciones/EP2/EP2%20Entrega/Giannina/Diagrama-despliegue-EP2.png)

Contrato del evento `categoria.creada` (campos, canal, ack, pendientes del publicador):  
[`docs/Evaluaciones/EP2/EP2 Entrega/Giannina/Contrato-evento-categoria-creada.md`](docs/Evaluaciones/EP2/EP2%20Entrega/Giannina/Contrato-evento-categoria-creada.md).

### Continuidad EP1 (sigue vigente)

- **Microsoft Entra ID** + MSAL en el frontend; BFF y MS como OAuth2 Resource Server (`ENTRA_ISSUER_URI`, `ENTRA_AUDIENCE`).
- **AWS API Gateway** como único ingreso HTTP público; `REACT_APP_API_BASE_URL` = Invoke URL del HTTP API.
- Rutas **v1 / v2** y reglas de estado de solicitudes (p. ej. no resolver sin `EN_PROCESO`).
- Topología EP1 (referencia histórica): [`infra/ec2/TOPOLOGIA.md`](infra/ec2/TOPOLOGIA.md).

---

## Versiones

| Pieza | Versión |
| --- | --- |
| Java | 17 |
| Spring Boot | 4.1.1 (`bff`, `ms-catalogo`, `ms-solicitudes`) |
| React | 19.2.8 |
| `@azure/msal-browser` / `@azure/msal-react` | 5.20.0 / 5.7.0 |
| PostgreSQL (local Docker) | 16 (`infra/docker-compose.yml` → `postgres:16`) |
| PostgreSQL (RDS) | Motor PostgreSQL (versión mayor no fijada en el inventario Ari) |
| RabbitMQ | `rabbitmq:4.2-management` |
| Docker Engine / Compose (EC2 C) | 29.9.2 / v5.6.1 |
| SO broker | Ubuntu 24.04 LTS |

---

## Variables de entorno

Plantilla sin secretos: [`env.example`](env.example). Frontend CRA: [`frontend/.env.example`](frontend/.env.example).  
Despliegue EC2 B (consumidor): plantilla en rama Skarlett `docs/Evaluaciones/EP2/EP2 Entrega/Skarlett/mesatech.env.example`.

| Grupo | Variables (nombres del código) |
| --- | --- |
| Entra / JWT | `ENTRA_TENANT_ID`, `ENTRA_SPA_CLIENT_ID`, `ENTRA_API_CLIENT_ID`, `ENTRA_ISSUER_URI`, `ENTRA_AUDIENCE` |
| Frontend | `REACT_APP_ENTRA_*`, `REACT_APP_API_BASE_URL` |
| BFF | `MS_SOLICITUDES_URL`, `MS_CATALOGO_URL`, `CORS_ALLOWED_ORIGINS` |
| Postgres app | `POSTGRES_USER`, `POSTGRES_PASSWORD` |
| Solicitudes (EP2, rama consumidor) | `SOLICITUDES_DATASOURCE_URL`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `RABBITMQ_VHOST`, `RABBITMQ_DECLARE` |
| Nombres de mensajería | Fijos en props del consumidor: exchange `catalogo.exchange`, cola `solicitudes.catalogo.queue`, key `categoria.creada` |
| Catálogo / publicador | Datasource URL aún hardcodeada en `dev`; vars Rabbit del publicador **pendientes de implementación** |

En `origin/dev` la URL de `ms-solicitudes` sigue fija a localhost. En `origin/feature/skarlett-consumidor` usa `${SOLICITUDES_DATASOURCE_URL:...}`. Tras el merge, preferir la plantilla EP2.

---

## Arranque

### Local (desarrollo)

1. Configurar Entra del equipo ([`docs/guia/entra-spring-react.md`](docs/guia/entra-spring-react.md)).
2. Copiar `env.example` → `.env` y `frontend/.env.example` → `frontend/.env` (IDs por canal privado).
3. `cd infra && docker compose up -d` (PostgreSQL 16; bases `mesatech_catalogo` / `mesatech_solicitudes`).
4. Si se prueba mensajería en local: broker RabbitMQ propio + vars `RABBITMQ_*` (consumidor en rama Skarlett).
5. Exportar `ENTRA_ISSUER_URI` y `ENTRA_AUDIENCE`; en terminales: `mvn spring-boot:run` en `ms-catalogo`, `ms-solicitudes`, `bff`.
6. `cd frontend && npm install && npm start`.

### AWS Academy (orden)

1. **RDS** Catálogo y Solicitudes disponibles (SG solo desde EC2 A/B).
2. **EC2 C** RabbitMQ: en `~/rabbitmq/`, `docker compose up -d` (imagen management, volumen persistente).
3. **EC2 A** Catálogo (JAR :8082) — publicador aún pendiente en código.
4. **EC2 B** Solicitudes (JAR :8081) con `RABBITMQ_DECLARE=false` y usuario `solicitudes_consumidor` ([`despliegue-ec2-b.md`](docs/Evaluaciones/EP2/EP2%20Entrega/Skarlett/despliegue-ec2-b.md) en rama Skarlett).
5. **BFF** (:8080) apuntando a IPs privadas de A/B.
6. **Frontend** con `REACT_APP_API_BASE_URL` = Invoke URL del API Gateway.

Detalle de IPs/endpoints: inventario Ari. Contraseñas solo fuera de git.

---

## Detención

Orden inverso al arranque AWS: frontend → BFF → Solicitudes → Catálogo → `docker compose down` en EC2 C (sin `-v` si hay que conservar mensajes) → detener **EC2** y **RDS** cuando no se trabaje para ahorrar créditos del Learner Lab.

Local: detener los tres `mvn` / el front; `cd infra && docker compose down`.

---

## Recuperación ante fallas

| Fallo | Qué ocurre (según código / acuerdo) |
| --- | --- |
| **Broker caído (P7)** | Publicador: respuesta HTTP controlada y estado del registro en RDS Catálogo — **pendiente** (no hay publicador AMQP en `ms-catalogo`). Consumidor: no recibe; al volver el broker reintenta conexiones (`missing-queues-fatal=false`). |
| **RDS Solicitudes caída** | Listener hace `basicNack(..., requeue=true)`; el mensaje vuelve a la cola. |
| **Consumidor detenido** | Mensajes publicados quedan Ready en la cola durable (si el publicador envió persistente — garantía del publicador pendiente). Al reiniciar el consumidor se procesan y se hace `basicAck` tras persistir en `categorias_locales`. |
| **Evento inválido** | `basicNack` sin reencolar. |
| **Reinicio del contenedor RabbitMQ** | Conservar volumen y hostname; vhost/exchange/cola/mensaje deben sobrevivir (prueba P5). |

No afirmar exactly-once ni outbox: el consumidor hace upsert por `categoriaId`.

---

## Verificación rápida

- Procesos escuchando: BFF `:8080`, Solicitudes `:8081`, Catálogo `:8082` (no hay Actuator/`/health` en el código actual).
- Management UI RabbitMQ solo desde IP admin `/32`: `http://<IP_PUBLICA_EC2_C>:15672` (inventario Ari; usuario `mesatech_admin`).
- Cola `solicitudes.catalogo.queue` / exchange `catalogo.exchange` en vhost `mesatech`.
- Desde EC2 B (RDS no pública):  
  `SELECT categoria_id, nombre, event_id, fecha_evento, recibida_en FROM categorias_locales ORDER BY recibida_en;`
- Gateway + token: 401 sin JWT; 200 con rol adecuado en rutas v1/v2 (continuidad EP1).

---

## Estructura del repositorio

| Carpeta | Puerto | Rol |
| --- | --- | --- |
| `frontend/` | 3000 | React, MSAL, Axios → Gateway o BFF local |
| `bff/` | 8080 | Resource Server, sin JPA, proxy a MS |
| `ms-solicitudes/` | 8081 | Solicitudes + consumidor EP2 (rama Skarlett) |
| `ms-catalogo/` | 8082 | Categorías/prioridades; publicador EP2 pendiente |
| `infra/` | 5432 / EC2 | Postgres Docker EP1; scripts y topología EC2 |
| `docs/Evaluaciones/EP2/` | — | Distribución, inventario Ari, entregas por integrante |

Guía general del monorepo: [`docs/README.md`](docs/README.md). Reglas Git: [`docs/equipo/reglas-repositorio.md`](docs/equipo/reglas-repositorio.md).

---

## Documentación útil

| Documento | Uso |
| --- | --- |
| [Distribución EP2](docs/Evaluaciones/EP2/EP2%20Entrega/00-Distribucion-tareas-equipo.md) | Reparto Ari / Nico / Giannina / Skarlett |
| [Inventario AWS](docs/Evaluaciones/EP2/EP2%20Entrega/Ari/Inventario_AWS_EP2.md) | IDs, IPs, RDS, RabbitMQ |
| [Contrato evento](docs/Evaluaciones/EP2/EP2%20Entrega/Giannina/Contrato-evento-categoria-creada.md) | Payload y garantías |
| [Onboarding](docs/onboarding/refs-y-entorno.md) | Clone, secretos, setup |
| [Entra + Spring + React](docs/guia/entra-spring-react.md) | Identidad |
| [Rutas Gateway / BFF](docs/equipo/rutas-api-gateway-bff.md) | Paths que llama el front |