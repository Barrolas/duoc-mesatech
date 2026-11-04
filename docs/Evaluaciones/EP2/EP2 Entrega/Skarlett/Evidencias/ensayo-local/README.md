# Ensayo local — no es evidencia del informe

Capturas del stack corriendo en el PC: RabbitMQ y Postgres en Docker, `ms-solicitudes`, `ms-catalogo` y el BFF como JAR. Sirven para preparar la defensa. Las pruebas P1–P8 del informe se hacen en AWS.

El evento `categoria.creada` se publicó directo al exchange `catalogo.exchange`, con el mismo JSON que arma `ms-catalogo`. Así se probó el consumidor sin pasar por el login de administrador.

| Captura | Qué muestra |
| --- | --- |
| `ENSAYO-P2-consumo.png` | Consumidor activo: la categoría 101 se guarda en `categorias_locales` con su `categoria_id` y la cola queda en 0 |
| `ENSAYO-P3-cola-ready.png` | Consumidor detenido: el evento de la categoría 102 queda Ready en la cola (0 consumidores) y no hay fila local |
| `ENSAYO-P4-consumo-diferido.png` | Consumidor arrancado de nuevo: consume el mensaje pendiente, guarda la 102 y la cola vuelve a 0 |
| `ENSAYO-P2-react-admin.jpg` | Flujo real desde React: el administrador crea `Redes-EP2-admin` y las prioridades Baja, Media y Alta |
| `ENSAYO-P7-react-503.jpg` | Con RabbitMQ detenido, crear `Soporte-P7-sin-broker` desde React responde 503 "Service Unavailable" |
| `ENSAYO-P7-inconsistencia.png` | Broker detenido: la categoría 2 quedó guardada en `mesatech_catalogo` y no existe en `categorias_locales` |
| `ENSAYO-P7-broker-de-vuelta.png` | Con RabbitMQ arriba otra vez, el consumidor se reconecta, pero la categoría 2 sigue sin copia: el evento nunca se publicó y no hay recuperación automática |
| `ENSAYO-P2-flujo-completo.png` | La misma categoría en `mesatech_catalogo` (id 1), la cola en 0 y la copia en `categorias_locales` con id 1 y el `event_id` que publicó `ms-catalogo` |
