# Avance EP2 — Ari

**Fuente:** `Evidencia EP2 - Ari.docx` (capturas del 29 y 30 de octubre de 2026)  
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
| A4 — `compose.yaml` | Hecho en la EC2 | Imagen `rabbitmq:4.2-management`, hostname `mesatech-rabbitmq`, puertos 5672/15672, volumen `/var/lib/rabbitmq`, `restart: unless-stopped`. El archivo sigue en `~/rabbitmq/` y todavía no está en el repo. |
| A5 — Security groups | Hecho | 22 y 15672 de RabbitMQ solo desde la IP admin `/32`. 5672 solo desde los SG de Catálogo y Solicitudes. 5432 de cada RDS solo desde el SG de su EC2. |
| A6 — Recursos RabbitMQ | Hecho en lo visible | Vhost `mesatech`, `catalogo_publicador` y `solicitudes_consumidor` (sin `guest`), exchange `catalogo.exchange` (direct, durable), cola `solicitudes.catalogo.queue` y binding `categoria.creada`. Falta captura de los permisos configure/write/read. |
| A7 — Inventario | Hecho | `Inventario_AWS_EP2.md`. |
| A8 — Entra ID (P8) | Sin evidencia | El documento no muestra comprobación de Entra. |

IPv4 pública vigente de RabbitMQ: `54.90.144.4`. La nota suelta del Word que dice `54.196.111.36` es de la primera captura y ya no aplica.

---

## Qué falta para cerrar la carpeta

1. Subir `compose.yaml` al repo, sin el `.env` ni contraseñas.
2. Captura de permisos de `catalogo_publicador` y `solicitudes_consumidor` sobre el vhost `mesatech`.
3. Confirmar Entra ID de la EP1 si el equipo lo usa en P8.

Los security groups de Catálogo y Solicitudes, en esta evidencia, solo abren el SSH desde la IP de administración. Los puertos de los JAR (y el del BFF) quedan para cuando Nicolás y Skarlett desplieguen.
