# Avance EP2 — Ari

**Fuente:** `Evidencia EP2 - Ari.docx` (capturas del 29 de octubre de 2026)  
**Rol:** AWS Academy e infraestructura (tareas A1–A8 de `00-Distribucion-tareas-equipo.md`, sección 3.1)  
**Región:** `us-east-1` (Norte de Virginia)

Lo que ya está operativo es la **EC2 C (RabbitMQ)**: Docker y Compose instalados desde cero, el contenedor en ejecución y los recursos de mensajería del acuerdo del equipo. Faltan las EC2 de Catálogo y Solicitudes, las dos RDS y el cierre de red del puerto 5672.

---

## Estado por tarea

| Tarea | Estado | Qué muestran las capturas |
| --- | --- | --- |
| A1 — 3 EC2 | Parcial | Solo existe la EC2 C (`mesatech-ep2-rabbitmq`). No hay EC2 A ni EC2 B. |
| A2 — 2 RDS | Pendiente | No hay capturas de RDS Catálogo ni RDS Solicitudes. |
| A3 — Docker desde cero | Hecho | Instalación con `apt` en Ubuntu 24.04. `docker version` 29.9.2 y `docker compose version` v5.1.1. |
| A4 — `compose.yaml` | Hecho en la EC2 | El contenedor quedó `Up` con imagen, hostname, puertos, volumen y `restart`. El archivo todavía no está en el repo. |
| A5 — Security groups | Parcial | SSH (22) y management (15672) solo desde la IP de administración `/32`. No aparece la regla del 5672 desde los SG de A y B (esas EC2 aún no existen). |
| A6 — Recursos RabbitMQ | Hecho en lo visible | Vhost `mesatech`, usuarios de app (sin `guest`), exchange, cola durable y binding. No hay captura de los permisos configure/write/read de cada usuario. |
| A7 — Inventario | En este archivo | IDs, IP, versiones y nombres de mensajería de lo ya creado. |
| A8 — Entra ID (P8) | Sin evidencia | El documento no muestra comprobación de Entra. |

---

## Inventario de lo creado

### EC2 C — RabbitMQ

| Dato | Valor |
| --- | --- |
| Nombre | `mesatech-ep2-rabbitmq` |
| Instance ID | `i-05fc13c5d3273e566` |
| Estado al capturar | En ejecución |
| AMI | Ubuntu 24.04 LTS (`ami-02a9cdd4e98bc3c78`); el login muestra Ubuntu 24.04.5 LTS |
| Tipo | `t3.medium` |
| Zona | `us-east-1b` |
| VPC | `vpc-003265ec261b137e2` |
| Par de claves | `mesatech-ep2` (queda fuera del repo) |
| Disco | 8 GiB gp3, raíz, sin cifrar |
| Security group editado | `sg-0194df0045c5f001c` |
| IPv4 pública anotada en el Word | `54.196.111.36` (el DNS de la consola empieza por `ec2-54-196-111-36`) |
| IPv4 privada (banner SSH) | `172.31.18.133` |
| Usuario SSH | `ubuntu` |

La UI de management se abrió en `http://54.242.28.186:15672`. Esa IP no coincide con la `54.196.111.36` anotada en el Word. Conviene confirmar en la consola cuál es la IPv4 pública actual antes de pasársela a Nicolás y Skarlett (una IP pública de EC2 cambia si la instancia se detiene y se vuelve a iniciar).

### Docker y Compose (en `~/rabbitmq`)

Versiones comprobadas en la EC2:

- Docker Engine `29.9.2`
- Docker Compose `v5.1.1`

El `compose.yaml` de la captura queda así (el `.env` vive solo en el servidor; no va al repo):

```yaml
services:
  rabbitmq:
    image: rabbitmq:4.2-management
    hostname: mesatech-rabbitmq
    restart: unless-stopped
    ports:
      - "5672:5672"
      - "15672:15672"
    env_file:
      - .env
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  rabbitmq_data:
```

`docker compose up -d` dejó el contenedor `rabbitmq-rabbitmq-1` en estado **Up**, con la imagen `rabbitmq:4.2-management`. La UI informa **RabbitMQ 4.2.9** y **Erlang 27.3.4.19**. Nodo: `rabbit@mesatech-rabbitmq`.

### Red (security group de la EC2 C)

| Puerto | Origen en la captura | Comentario |
| --- | --- | --- |
| 22 (SSH) | `190.114.35.100/32` | Solo la IP de administración |
| 15672 (management) | `190.114.35.100/32` | Solo la IP de administración |
| 5672 (AMQP) | No aparece en la captura | Falta abrirlo solo desde los security groups de EC2 A y EC2 B |

### Mensajería

| Recurso | Valor visto |
| --- | --- |
| Virtual host | `mesatech` (running), además del `/` por defecto |
| Usuario admin de la UI | `mesatech_admin` (tag administrator; acceso a `/` y `mesatech`) |
| Publicador | `catalogo_publicador` (acceso al vhost `mesatech`, con contraseña) |
| Consumidor | `solicitudes_consumidor` (acceso al vhost `mesatech`, con contraseña) |
| `guest` | No aparece en la lista de usuarios |
| Exchange | `catalogo.exchange`, tipo **direct**, durable, vhost `mesatech` |
| Cola | `solicitudes.catalogo.queue`, classic, **durable**, running, 0 mensajes, 0 consumidores |
| Binding | `catalogo.exchange` → cola, routing key `categoria.creada` |

Esos nombres coinciden con los acordados en el reparto del equipo.

---

## Qué falta para cerrar la parte de Ari

1. **EC2 A** (Catálogo) y **EC2 B** (Solicitudes), distintas entre sí y de la C.
2. **Dos RDS exclusivas** (Catálogo y Solicitudes), sin acceso público, con endpoints para Nicolás y Skarlett. Sin contraseñas en git.
3. **Puerto 5672** permitido solo desde los security groups de A y B. Hoy el contenedor escucha 5672, pero el security group de la captura no lo abre.
4. **Permisos** de `catalogo_publicador` y `solicitudes_consumidor` (configure / write / read sobre `mesatech`). La captura muestra que entran al vhost, no el detalle de permisos.
5. **Subir `compose.yaml` al repo**, sin el `.env` ni contraseñas.
6. Confirmar la **IPv4 pública vigente** de la EC2 C y anotarla en el inventario.
7. **Entra ID** de la EP1, si el equipo lo necesita para la prueba P8.

Hasta que existan los endpoints de RDS, el host AMQP confirmado y los usuarios de aplicación, Nicolás y Skarlett no pueden cerrar el despliegue de los JAR contra esta infraestructura.
