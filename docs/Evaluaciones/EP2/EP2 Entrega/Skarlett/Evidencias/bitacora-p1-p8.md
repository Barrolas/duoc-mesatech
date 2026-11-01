# Bitácora P1–P8 — Skarlett

Cada prueba pide preparación, datos, resultado esperado, resultado obtenido y evidencia. Los resultados de ejecución quedan pendientes hasta el despliegue en la EC2 B y el publicador de Nicolás. No se inventan capturas.

Consulta de la copia local, desde la EC2 B:

```text
SELECT categoria_id, nombre, event_id, fecha_evento, recibida_en
FROM categorias_locales
ORDER BY recibida_en;
```

Log esperado del consumidor, después de persistir:

```text
Categoría <categoriaId> (<nombre>) persistida desde el evento <eventId>
```

---

## P1 — Separación real

**Preparación.** Inventario de Ari, 2026-10-30.

**Datos.**

| Rol | Identificador | Endpoint o IP |
| --- | --- | --- |
| EC2 A · Catálogo | `i-0a7c485a5fd3c064c` | privada `172.31.17.69` |
| EC2 B · Solicitudes | `i-0b99595991c09d866` | privada `172.31.23.125` |
| RDS Catálogo | `mesatech-ep2-catalogo` | `mesatech-ep2-catalogo.cg33a3yt7ns0.us-east-1.rds.amazonaws.com` |
| RDS Solicitudes | `mesatech-ep2-solicitudes` | `mesatech-ep2-solicitudes.cg33a3yt7ns0.us-east-1.rds.amazonaws.com` |

**Esperado.** IDs de EC2 distintos, endpoints RDS distintos, y el proceso de `ms-solicitudes` en la EC2 B usando solo la RDS de solicitudes.

**Obtenido.** Pendiente. Falta la captura de `systemctl status mesatech-ms-solicitudes` en la EC2 B y un `SELECT` a `categorias_locales` en esa RDS.

**Evidencia.** Pendiente: `P1-ec2-b-proceso.png`, `P1-rds-solicitudes.png`.

---

## P2 — Flujo completo

**Preparación.** Consumidor arriba en la EC2 B. Nicolás con el publicador arriba en la EC2 A. Administrador autenticado en React.

**Datos.** Una categoría nueva, por ejemplo `Redes-EP2`. Anotar `categoriaId` y `eventId`.

**Esperado.** La fila existe en RDS Catálogo, el mensaje se publicó, el log del consumidor cita ese `eventId` y `categorias_locales.categoria_id` conserva el id original. Luego se crea una solicitud eligiendo esa categoría en el formulario.

**Obtenido.** Pendiente. El publicador todavía no está en el repositorio.

**Evidencia.** Pendiente: `P2-react-categoria.png`, `P2-rds-catalogo.png`, `P2-cola-o-log.png`, `P2-rds-solicitudes.png`, `P2-solicitud.png`.

---

## P3 — Consumidor detenido

**Preparación.** `sudo systemctl stop mesatech-ms-solicitudes` en la EC2 B. Broker encendido.

**Datos.** Otra categoría nueva, distinta de P2.

**Esperado.** La categoría queda en RDS Catálogo. En la cola `solicitudes.catalogo.queue` el mensaje figura Ready. `categorias_locales` todavía no tiene esa fila. No usar Get messages con confirmación que retire el mensaje.

**Obtenido.** Pendiente.

**Evidencia.** Pendiente: `P3-cola-ready.png`, `P3-sin-fila-local.png`.

---

## P4 — Consumo diferido

**Preparación.** Con el mensaje de P3 aún Ready, `sudo systemctl start mesatech-ms-solicitudes`.

**Datos.** El mismo `categoriaId` de P3.

**Esperado.** El consumidor persiste la fila, hace ack y la cola queda sin ese mensaje en Ready ni Unacked. La fila en RDS es la prueba; la cola vacía sola no alcanza.

**Obtenido.** Pendiente.

**Evidencia.** Pendiente: `P4-log-ack.png`, `P4-fila-local.png`, `P4-cola-vacia.png`.

---

## P5 — Reinicio del broker

**Preparación.** Consumidor detenido. Publicar otra categoría y ver el mensaje pendiente. Recrear el contenedor de RabbitMQ conservando el volumen y el hostname `mesatech-rabbitmq`, sin borrar volúmenes. Lo ejecuta quien administra la EC2 C (Ari), con Skarlett al volver a iniciar el consumidor.

**Datos.** Categoría nueva de esta prueba.

**Esperado.** Siguen el vhost `mesatech`, el exchange `catalogo.exchange`, la cola y el mensaje. Al iniciar el consumidor, la fila aparece en RDS Solicitudes.

**Obtenido.** Pendiente.

**Evidencia.** Pendiente: `P5-mensaje-antes.png`, `P5-compose-ps.png`, `P5-despues-consumo.png`.

---

## P6 — Reinicio de aplicaciones

**Preparación.** Con filas ya guardadas en ambas RDS, reiniciar `ms-catalogo` y `ms-solicitudes`.

**Datos.** Los `categoriaId` de P2 y P4.

**Esperado.** Las consultas posteriores muestran los mismos registros. No se pierden datos de ninguna RDS.

**Obtenido.** Pendiente.

**Evidencia.** Pendiente: `P6-consulta-catalogo.png`, `P6-consulta-solicitudes.png`.

---

## P7 — Fallo de publicación

**Preparación.** Detener RabbitMQ en la EC2 C. Intentar crear otra categoría desde React. Responsable principal del publicador: Nicolás. Skarlett documenta el efecto en solicitudes: no aparece fila local de esa categoría.

**Datos.** Categoría que no debe copiarse mientras el broker está detenido.

**Esperado.** Respuesta controlada del publicador. La consulta a RDS Catálogo muestra si el registro quedó guardado igual. Al levantar el broker se explica la inconsistencia, sin recuperación automática.

**Obtenido.** Pendiente.

**Evidencia.** Pendiente: `P7-error-publicador.png`, `P7-rds-catalogo.png`, `P7-sin-copia.png`.

---

## P8 — Continuidad de la EP1

**Preparación.** Flujo React → API Gateway → BFF, con Entra ID.

**Datos.** Usuario con rol y una llamada sin token.

**Esperado.** Login y operación autorizada. Rechazo sin token y por permisos insuficientes. No se puede pasar a `RESUELTA` si el estado no es `EN_PROCESO`. Responden `GET /v1/solicitudes/mias` y `GET /v2/solicitudes/mias`. La categoría recibida se puede elegir al crear la solicitud.

**Obtenido.** Pendiente de repetir sobre la topología nueva. El código de estados y de v1/v2 se mantiene desde la EP1.

**Evidencia.** Pendiente: `P8-401.png`, `P8-403.png`, `P8-transicion.png`, `P8-v1-v2.png`.
