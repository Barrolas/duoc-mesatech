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

**Obtenido.** 2026-11-03 01:18 UTC. En la EC2 B (`i-0b99595991c09d866`, `172.31.23.125`) `mesatech-ms-solicitudes` queda `active (running)`. El log muestra la conexión a la RDS de solicitudes (HikariPool), la creación de `categorias_locales` y la conexión a RabbitMQ en `172.31.18.133:5672` como `solicitudes_consumidor`. En la RDS `mesatech_solicitudes` existen `categorias_locales` y `solicitudes`. `categorias_locales` está vacía: todavía no hay publicador. Desde las 02:12 UTC corre con el perfil por defecto y valida JWT contra Entra: sin token responde 401, y un token mal firmado responde 401 `invalid_token`.

**Evidencia.** `P1-ec2-b-proceso.png`, `P1-rds-solicitudes.png`. Se obtienen con `bash capturas-p1.sh <IP_PUBLICA_EC2_B>`.

---

## Red — acceso al puerto 8081 de la EC2 B

No es una de las pruebas P1–P8. Respalda C4: el puerto de `ms-solicitudes` solo queda abierto para la EC2 donde debe correr el BFF.

**Preparación.** Ari agregó al security group de la EC2 B (`sg-0571f2e504fd01ab3`) una regla de entrada TCP 8081 con origen `sg-054c453030ed5f8e8` (EC2 A · Catálogo).

**Esperado.** Desde la EC2 A conecta y la API pide token. Desde la EC2 C y desde internet, el puerto no responde.

**Obtenido.** 2026-11-03 02:49 UTC.

| Origen | Resultado |
| --- | --- |
| EC2 A · Catálogo `172.31.17.69` | Conecta. `GET /v1/solicitudes/mias` sin token responde 401 |
| EC2 C · RabbitMQ `172.31.18.133` | Timeout: el SG no lo autoriza |
| Internet, IP pública de la EC2 B | Sin respuesta |

La EC2 A todavía no tiene el BFF ni `ms-catalogo` desplegados. Cuando estén, el BFF debe usar `MS_SOLICITUDES_URL=http://172.31.23.125:8081`.

**Evidencia.** `RED-8081-acceso.png`.

---

## P2 — Flujo completo

**Preparación.** Consumidor arriba en la EC2 B. Nicolás con el publicador arriba en la EC2 A. Administrador autenticado en React.

**Datos.** Una categoría nueva, por ejemplo `Redes-EP2`. Anotar `categoriaId` y `eventId`.

**Esperado.** La fila existe en RDS Catálogo, el mensaje se publicó, el log del consumidor cita ese `eventId` y `categorias_locales.categoria_id` conserva el id original. Luego se crea una solicitud eligiendo esa categoría en el formulario.

**Obtenido.** 2026-11-04 03:34 UTC. El administrador crea `Redes-EP2-AWS` desde React. React y el BFF corren en el PC y llegan a `ms-catalogo` (EC2 A) y a `ms-solicitudes` (EC2 B) por túnel SSH, porque el BFF todavía no está desplegado en la nube. React confirma "ya está disponible para crear solicitudes" y la categoría aparece en el catálogo, que se lee desde la RDS de Catálogo. En la EC2 A, `ms-catalogo` publica a las 03:34:20.103 el evento `be46e6ed-e156-4a91-9b88-44ab7606283a` con `categoriaId=1`. En la EC2 B, el consumidor la persiste a las 03:34:20.285 con `categoria_id = 1` y el mismo `event_id`. La cola queda con 0 Ready, 0 Unacked y 1 consumidor.

Pendiente: crear una solicitud con esa categoría. Faltan prioridades en la RDS de Catálogo.

**Evidencia.** `P2-react-categoria.jpg`, `P2-aws-flujo.png`.

---

## P3 — Consumidor detenido

**Preparación.** `sudo systemctl stop mesatech-ms-solicitudes` en la EC2 B. Broker encendido.

**Datos.** Otra categoría nueva, distinta de P2.

**Esperado.** La categoría queda en RDS Catálogo. En la cola `solicitudes.catalogo.queue` el mensaje figura Ready. `categorias_locales` todavía no tiene esa fila. No usar Get messages con confirmación que retire el mensaje.

**Obtenido.** 2026-11-04 03:36 UTC. Con `mesatech-ms-solicitudes` detenido en la EC2 B, el administrador crea `Soporte-EP2-AWS`. `ms-catalogo` publica a las 03:36:05.675 el evento `ded82e91-a7ee-4ccb-b8cf-4265622aaa49` con `categoriaId=2`. La cola queda con 1 Ready, 0 Unacked y 0 consumidores, y `categorias_locales` no tiene la fila. React muestra "Internal Server Error": no viene del POST, que sí guarda, sino de la recarga posterior de `/v1/solicitudes/categorias` y `/v1/solicitudes/mias` con Solicitudes caído.

La hora del encabezado de `P3-aws-cola-ready.png` es la del PC, adelantado unos 54 s respecto de AWS. Las horas de los logs son las de AWS.

**Evidencia.** `P3-aws-cola-ready.png`, `P3-react-solicitudes-detenido.jpg`.

---

## P4 — Consumo diferido

**Preparación.** Con el mensaje de P3 aún Ready, `sudo systemctl start mesatech-ms-solicitudes`.

**Datos.** El mismo `categoriaId` de P3.

**Esperado.** El consumidor persiste la fila, hace ack y la cola queda sin ese mensaje en Ready ni Unacked. La fila en RDS es la prueba; la cola vacía sola no alcanza.

**Obtenido.** 2026-11-04 03:37 UTC. `sudo systemctl start mesatech-ms-solicitudes` en la EC2 B. Conecta al broker a las 03:37:24.509 y persiste la categoría 2 (`Soporte-EP2-AWS`) a las 03:37:24.875, con el `event_id` publicado en P3. La cola vuelve a 0 Ready, 0 Unacked y 1 consumidor. El mensaje se consumió después de guardar la fila en la RDS.

**Evidencia.** `P4-aws-consumo-diferido.png`.

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

**Obtenido.** 2026-11-04 03:40 UTC. Antes del reinicio, `ms-catalogo` corre en la EC2 A con PID 1152 y `ms-solicitudes` en la EC2 B con PID 1576, y `categorias_locales` tiene las categorías 1 y 2. Se reinicia `ms-catalogo` con `start-ms-catalogo.sh` (PID 1993, arriba 03:40:43) y `ms-solicitudes` con `systemctl restart` (PID 2173, arriba 03:40:58). Después, `categorias_locales` muestra las mismas dos filas, con los mismos `event_id` y `recibida_en`. React, a través del `ms-catalogo` reiniciado, sigue listando `Redes-EP2-AWS` y `Soporte-EP2-AWS` desde la RDS de Catálogo. No se perdieron datos.

Antes de reiniciar se respaldó el log de `ms-catalogo` en la EC2 A como `~/apps/ms-catalogo.log.respaldo-p2-p4`, porque el script de arranque lo vacía y ahí están las líneas de publicación de P2 y P3.

**Evidencia.** `P6-aws-antes.png`, `P6-aws-despues.png`, `P6-react-catalogo.jpg`.

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

**Obtenido.** 2026-11-04 03:42–03:44 UTC. React y el BFF corren en el PC, con túnel SSH a `ms-catalogo` y `ms-solicitudes` en AWS (ver P2).

- Login y operación autorizada: el administrador entra con Entra y crea las prioridades Baja, Media y Alta en la RDS de Catálogo. El formulario solo ofrece las categorías recibidas por el evento (`Redes-EP2-AWS`, `Soporte-EP2-AWS`). Se registra la solicitud 0001, "Sin conexión en el laboratorio 3", con categoría `Redes-EP2-AWS` y prioridad Alta, y queda CREADA en la RDS de Solicitudes.
- Transiciones: desde CREADA solo se ofrecen ASIGNADA y CANCELADA. Desde ASIGNADA, EN PROCESO, CANCELADA o volver a CREADA: RESUELTA no aparece. Desde EN_PROCESO aparece RESUELTA, y se pasa a RESUELTA. El backend también lo exige: `TransicionesEstado` rechaza con 409 "Una solicitud no puede pasar a RESUELTA si no está EN_PROCESO".
- Rechazo sin token: `GET /v1/solicitudes/mias` y `GET /v2/solicitudes/mias` sin `Authorization` responden 401, tanto en el BFF como en el API Gateway.

- Permisos insuficientes: con `cliente@estqbanquito2.onmicrosoft.com` (rol `cliente`) React solo muestra "Mis solicitudes", sin Bandeja general ni Catálogo, y no ve la solicitud del administrador. A las 03:49 UTC se llamó al BFF desde la consola del navegador con la sesión del cliente, sin pasar por la interfaz. `GET /v1/solicitudes` (bandeja general) responde 403, y `POST /v1/catalogo/categorias` también responde 403, así que la categoría no se crea.
- v1 y v2: con el mismo token, `GET /v1/solicitudes/mias` responde 200 con una lista, y `GET /v2/solicitudes/mias` responde 200 con el formato v2 `{"version":"v2","usuario":…,"solicitudes":[]}`.

**Evidencia.** `P8-react-form-categoria-recibida.jpg`, `P8-react-solicitud-creada.jpg`, `P8-react-resuelta.jpg`, `P8-aws-401-y-rds.png`, `P8-cliente-menu.jpg`, `P8-cliente-403-y-v2.jpg`.
