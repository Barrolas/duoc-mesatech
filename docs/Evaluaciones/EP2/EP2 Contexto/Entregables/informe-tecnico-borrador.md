# Informe técnico EP2 — MesaTech Cloud (borrador)

**Asignatura:** DSY1107 · Desarrollo Cloud Native I  
**Equipo:** Ari · Nicolás · Giannina · Skarlett  
**Estado:** borrador de redacción. El publicador, el consumidor, el diagrama, el `compose.yaml` y las pruebas P1–P8 ya están en el repositorio. Falta alinear este texto con el Word o PDF final.

Este borrador cubre la parte que ya se puede redactar. Las capturas se pegan en `EP2 Entrega/<Nombre>/Evidencias/` y se referencian aquí antes de pasar el informe a Word o PDF.

---

## 1. Cambio respecto de la EP1

En la EP1, Solicitudes leía el catálogo por la API. En la EP2, Solicitudes no consulta la RDS de Catálogo. Cuando un administrador crea una categoría, Catálogo la guarda en su RDS y publica un evento. Solicitudes consume ese evento y guarda una copia en su propia RDS. Crear la solicitud usa esa copia. El retraso es aceptable: la pantalla avisa y vuelve a consultar `GET /v1/solicitudes/categorias`.

Se mantienen React con MSAL, Entra ID, API Gateway, el BFF, las transiciones de estado y las rutas v1 y v2.

## 2. Dónde corre cada cosa

Fuente de IDs: `EP2 Entrega/Ari/Inventario_AWS_EP2.md` (2026-10-30).

| Componente | Dónde | Dueño del dato |
| --- | --- | --- |
| Catálogo y publicador | EC2 A `i-0a7c485a5fd3c064c` | RDS Catálogo |
| Solicitudes y consumidor | EC2 B `i-0b99595991c09d866` | RDS Solicitudes |
| RabbitMQ | EC2 C `i-05fc13c5d3273e566` | Broker, no base de negocio |
| BFF | EC2 A o B | No tiene RDS |

React llega al negocio solo por API Gateway y el BFF. El BFF no lee RDS ni publica ni consume.

Diagrama con flechas de mensajes y de datos: `EP2 Entrega/Giannina/Diagrama-despliegue-EP2.pdf`.

## 3. Red

| Destino | Puerto | Origen |
| --- | --- | --- |
| RabbitMQ | 5672 | Security groups de las EC2 A y B |
| RabbitMQ | 15672 y 22 | IP de administración `/32` |
| Cada RDS | 5432 | Security group de su EC2 |
| `ms-solicitudes` | 8081 | El BFF, por la IP privada de la EC2 B |

La RDS de solicitudes no tiene acceso público. La consulta de `categorias_locales` se hace desde la EC2 B.

## 4. Mensaje

Al crear una categoría válida, Catálogo publica un JSON persistente:

```json
{
  "eventId": "evt-1",
  "tipoEvento": "categoria.creada",
  "fechaEvento": "2026-10-29T15:00:00Z",
  "categoriaId": 7,
  "nombre": "Hardware"
}
```

| Recurso | Nombre |
| --- | --- |
| Virtual host | `mesatech` |
| Exchange direct durable | `catalogo.exchange` |
| Routing key | `categoria.creada` |
| Cola durable | `solicitudes.catalogo.queue` |
| Usuario publicador | `catalogo_publicador` |
| Usuario consumidor | `solicitudes_consumidor` |

El 04-11-2026, en la EC2 C, `rabbitmq-diagnostics status` mostró RabbitMQ 4.2.9 sin alarmas, con listeners en 5672 y 15672. Los usuarios de aplicación acceden solo al vhost `mesatech` y no tienen tag administrator. El detalle está en `EP2 Entrega/Ari/Evidencias/resumen-avance.md`.

`categoriaId` se guarda tal cual en `categorias_locales.categoria_id`. No se reemplaza por un id nuevo. `fechaEvento` va en ISO-8601.

La confirmación del publicador (el broker aceptó el mensaje, o lo devolvió porque no había ruta) no prueba que Solicitudes ya escribió en su RDS. El consumidor hace `basicAck` solo después de `save`. Si la base falla, el mensaje vuelve a la cola. Si el JSON no trae los campos del contrato, se descarta sin reencolar. Volver a entregar el mismo `categoriaId` actualiza la misma fila. Eso no es una garantía de exactamente una vez: la pauta no la pide.

Si Catálogo guarda la fila y después no puede publicar, el registro puede quedar solo en RDS Catálogo. No hay outbox ni compensación automática.

## 5. Consumidor

Implementado en `ms-solicitudes`:

- Cola `solicitudes.catalogo.queue`, ack manual.
- Tabla `categorias_locales`, creada al arrancar con `ddl-auto=update`.
- `GET /v1/solicitudes/categorias`, con el mismo JWT de la EP1, a través del BFF.
- Al crear una solicitud, la categoría tiene que existir en esa copia. Si no llegó, la API responde 409.
- El formulario solo lista las categorías ya recibidas y ofrece actualizar el listado.

En la EC2 B, `RABBITMQ_DECLARE=false`: los recursos del broker los creó Ari. El paso de despliegue está en `EP2 Entrega/Skarlett/despliegue-ec2-b.md`.

## 6. Pruebas

La bitácora está en `EP2 Entrega/Skarlett/Evidencias/bitacora-p1-p8.md`. P1 a P8 tienen resultado de ejecución en AWS (3 y 4 de noviembre de 2026), con capturas en esa carpeta. En P2 y P8, React y el BFF corrieron en el PC y llegaron a las EC2 por túnel SSH.

## 7. Qué falta para cerrar el informe

1. Desplegar el BFF en la EC2 A o B y dejar de depender del túnel SSH para el flujo de la presentación.
2. Pasar este borrador al Word o PDF de esta misma carpeta, con las figuras de P1–P8.
