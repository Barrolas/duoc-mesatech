# RabbitMQ en la EC2 C (EP2)

Copia versionada del despliegue que corre en `~/rabbitmq/` de la EC2 C (`mesatech-ep2-rabbitmq`, Ubuntu 24.04). Responsable: Ari.

| Archivo | Uso |
| --- | --- |
| `compose.yaml` | Servicio RabbitMQ, igual al de la EC2 C |
| `.env.example` | Variables del usuario administrador, con valores ficticios. El `.env` real queda solo en la EC2 |

## Configuración

| Clave | Valor | Motivo |
| --- | --- | --- |
| `image` | `rabbitmq:4.2-management` | Imagen oficial con tag explícito y la interfaz de administración |
| `hostname` | `mesatech-rabbitmq` | RabbitMQ asocia sus datos al nombre del nodo. Con el hostname fijo, recrear el contenedor conserva definiciones y mensajes |
| `restart` | `unless-stopped` | Vuelve a arrancar tras un fallo o un reinicio de la EC2, salvo detención manual |
| `ports` | `5672`, `15672` | AMQP para Catálogo y Solicitudes; interfaz web. El security group limita quién llega a cada puerto |
| `env_file` | `.env` | Credenciales del administrador fuera del repositorio |
| `volumes` | `rabbitmq_data:/var/lib/rabbitmq` | Volumen con nombre para definiciones y mensajes persistentes |

## Operación

```text
cd ~/rabbitmq
cp .env.example .env        # y completar la contraseña real
docker compose config --quiet
docker compose up -d
docker compose ps
```

Para recrear el contenedor sin perder datos (prueba P5): `docker compose down` y `docker compose up -d`, **sin** `-v`, porque `-v` borra el volumen.

Detener y volver a levantar el broker (prueba P7): `docker compose stop` y `docker compose start`.

Los recursos del vhost `mesatech` (usuarios de aplicación, `catalogo.exchange`, `solicitudes.catalogo.queue` y el binding `categoria.creada`) se crearon desde la interfaz de administración y no se declaran en este archivo. Los nombres están en `docs/Evaluaciones/EP2/EP2 Entrega/Ari/Inventario_AWS_EP2.md`.
