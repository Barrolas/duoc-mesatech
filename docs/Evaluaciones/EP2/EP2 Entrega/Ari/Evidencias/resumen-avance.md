# Avance EP2 — Ari

**Fuente:** `Evidencia EP2 - Ari.docx` (capturas del 29 y 30 de octubre, y del 4 de noviembre de 2026)  
**Inventario vigente:** [../Inventario_AWS_EP2.md](../Inventario_AWS_EP2.md) (IDs, IPs, endpoints y security groups)  
**Rol:** AWS Academy e infraestructura (tareas A1–A8 de `00-Distribucion-tareas-equipo.md`, sección 3.1)  
**Región:** `us-east-1`

Están creadas las tres EC2 (Catálogo, Solicitudes y RabbitMQ), las dos RDS de PostgreSQL sin acceso público, Docker/Compose en la EC2 C y los recursos de mensajería del acuerdo. En Catálogo y Solicitudes quedó instalado OpenJDK 17.

Los números (instance ID, IP, endpoint, security group) están en el inventario. Este archivo solo registra el estado de las tareas.

---

## Estado por tarea

| Tarea | Estado | Evidencia |
| --- | --- | --- |
| A1 — 3 EC2 | Hecho | `mesatech-ep2-catalogo`, `mesatech-ep2-solicitudes` y `mesatech-ep2-rabbitmq`, las tres `t3.medium` en `us-east-1b`. Ubuntu 24.04. OpenJDK 17 en A y en B. |
| A2 — 2 RDS | Hecho | `mesatech-ep2-catalogo` y `mesatech-ep2-solicitudes`, PostgreSQL, `db.t3.micro`, acceso público **No**, bases `mesatech_catalogo` y `mesatech_solicitudes`. Usuario maestro `mesatech_admin` (password fuera del repo). |
| A3 — Docker desde cero | Hecho | Docker Engine 29.9.2 y Compose v5.6.1 en la EC2 C (Ubuntu 24.04). |
| A4 — `compose.yaml` | Hecho | Imagen `rabbitmq:4.2-management`, hostname `mesatech-rabbitmq`, puertos 5672/15672, volumen `/var/lib/rabbitmq`, `restart: unless-stopped`. Versionado en `infra/rabbitmq/compose.yaml`, con `.env.example` sin contraseñas. |
| A5 — Security groups | Hecho | 22 y 15672 de RabbitMQ solo desde la IP admin `/32`. 5672 solo desde los SG de Catálogo y Solicitudes. 5432 de cada RDS solo desde el SG de su EC2. |
| A6 — Recursos RabbitMQ | Hecho | Vhost `mesatech`, exchange, cola y binding. El 04-11-2026, Admin → Users: `catalogo_publicador` y `solicitudes_consumidor` entran solo a `mesatech` y no tienen tag administrator. |
| A7 — Inventario | Hecho | `Inventario_AWS_EP2.md`. |
| A8 — Entra ID (P8) | Hecho | Roles Cliente, Operador y Administrador en `api-cloud-native`. Login, 401 y 403 comprobados en P8 (bitácora de Skarlett). |

IPv4 públicas del 04-11-2026, en el inventario: RabbitMQ `100.53.8.151`, Catálogo `3.85.18.107`, Solicitudes `98.91.199.82`. La privada de RabbitMQ sigue siendo `172.31.18.133`.

---

## Evidencias RabbitMQ (04-11-2026)

- A6: usuarios `catalogo_publicador` y `solicitudes_consumidor` con acceso solo al vhost `mesatech`, sin tag administrador (captura Admin → Users). `mesatech_admin` sí tiene el tag administrator.
- `docker compose ps` y `logs`: contenedor `rabbitmq:4.2-management` activo; conexión de `solicitudes_consumidor` desde la EC2 B (`172.31.23.125`) al vhost `mesatech`.
- `rabbitmq-diagnostics status`: RabbitMQ 4.2.9, sin alarmas, listeners 5672 (AMQP) y 15672 (management).
- A8: roles Cliente, Operador y Administrador asignados en `api-cloud-native`; login, 401 y 403 comprobados en P8.
