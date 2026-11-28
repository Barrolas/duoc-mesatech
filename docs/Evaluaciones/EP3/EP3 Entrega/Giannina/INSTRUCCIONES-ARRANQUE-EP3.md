# Instrucciones de arranque — EP3 MesaTech

**Asignatura:** DSY1107 · Cloud Native I  
**Evaluación:** EP3 · Seguimiento operativo con Apache Kafka  
**Autora:** Giannina  
**Arquitectura:** Alternativa 1, cerrada en [`../ARQUITECTURA-ACUERDOS.md`](../ARQUITECTURA-ACUERDOS.md)  
**Contrato:** [`Contrato-eventos-EP3.md`](Contrato-eventos-EP3.md)  
**Diagrama:** [`Diagrama-despliegue-EP3-source.html`](Diagrama-despliegue-EP3-source.html)

Estas instrucciones dicen en qué orden se levanta el sistema y qué variable usa cada borde. No incluyen contraseñas, tokens ni IDs de Entra. Esos valores viven en `.env` local o en el canal privado del equipo.

Las Elastic IP, la IP privada de Kafka y la invoke URL todavía no están en el inventario de Ari. Donde el texto dice `<...>`, se copia el valor de [`../Ari/Inventario_AWS_EP3.md`](../Ari/Inventario_AWS_EP3.md) cuando Ari lo complete. No se inventa una IP.

---

## 1. Qué queda igual y qué agrega la EP3

| Borde | Qué hace | Dónde vive |
| --- | --- | --- |
| React → Entra → API Gateway → BFF | La petición del usuario sigue en línea | React en el PC. Gateway en AWS. BFF en **EC2 A**, puerto **8080** |
| RabbitMQ `categoria.creada` | Copia de la categoría, como en la EP2 | **EC2 C**, puerto **5672** |
| Kafka, 3 tópicos | Seguimiento operativo nuevo | **EC2 D**, puerto **9092** |

El Gateway es pass-through: no valida el JWT. El BFF sí lo valida. Una llamada a la invoke URL sin token debe responder **401**. Con token de Entra y rol correcto, **200**.

---

## 2. Variables

Copiar `env.example` de la raíz a `.env`. No commitear `.env`.

| Variable | Local | Cloud |
| --- | --- | --- |
| `REACT_APP_API_BASE_URL` | `http://localhost:8080` | Invoke URL del HTTP API, sin `/` final |
| `MS_CATALOGO_URL` | `http://localhost:8082` | `http://127.0.0.1:8082` en la EC2 A (el BFF está en la misma máquina) |
| `MS_SOLICITUDES_URL` | `http://localhost:8081` | `http://<privada B>:8081` desde la EC2 A |
| `SOLICITUDES_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/mesatech_solicitudes` | Endpoint RDS Solicitudes, puerto 5432 |
| `CATALOGO_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/mesatech_catalogo` | Endpoint RDS Catálogo, puerto 5432 |
| `RABBITMQ_HOST` | `localhost` | **IP privada** de la EC2 C |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | **IP privada** de la EC2 D, puerto 9092 |

En cloud, Rabbit y Kafka se anuncian con la IP privada. La Elastic IP sirve para SSH y para la integración pública del Gateway hacia el BFF. No se usa como bootstrap de Kafka.

Plantilla de la EC2: `infra/ec2/env.ec2.example`. Nicolás copia `KAFKA_BOOTSTRAP_SERVERS` a `ms-catalogo.env`. Skarlett hace lo mismo en el env de Solicitudes.

---

## 3. Orden local

Postgres local publica el puerto **5433** (`infra/docker-compose.yml`, mapeo `5433:5432`).

1. Postgres: `docker compose -f infra/docker-compose.yml up -d`
2. Rabbit local, si se va a probar la copia de categoría: `infra/rabbitmq/` con su `.env` (no versionado) y `docker compose up -d`
3. Kafka local: el compose del curso está descrito en `docs/Evaluaciones/EP3/EP3 Contexto/Trabajando-con-Kafka-extract.txt`. En local, `KAFKA_ADVERTISED_LISTENERS` externo queda en `localhost:9092`. El archivo `infra/docker-compose.kafka.yml` lo agrega Nicolás; hasta que exista, se usa el yml del extracto en una carpeta `kafka/` fuera del commit de secretos.
4. Crear los tres tópicos de negocio con **3 particiones** y replication factor **1**:
   - `mesatech.solicitud.creada`
   - `mesatech.solicitud.estado`
   - `mesatech.catalogo.prioridad`
5. Compilar: `infra/scripts/build-jars.ps1`
6. Levantar en este orden: `ms-catalogo` **8082**, `ms-solicitudes` **8081**, BFF **8080**, frontend **3000**

Grupos de consumo, ya cerrados:

| Grupo | Servicio | Tópicos |
| --- | --- | --- |
| `mesatech-catalogo-kafka` | ms-catalogo | `mesatech.catalogo.prioridad` |
| `mesatech-solicitudes-kafka` | ms-solicitudes | `mesatech.solicitud.creada` y `mesatech.solicitud.estado` |

---

## 4. Orden cloud

Ari deja la cuenta antes que el resto. El detalle de EC2, RDS, security groups y EIP está en su inventario.

1. Ari: Elastic IP en A, B, C y D. RDS Catálogo y RDS Solicitudes, sin acceso público. Rabbit en C. Kafka en D, con la UI en el puerto **8088** (no 8080). Tópicos con 3 particiones.
2. Ari entrega a Giannina: API ID, invoke URL y las cuatro EIP.
3. Giannina confirma las rutas del HTTP API hacia el BFF (`/public/*`, `/api/*`, `/v1/*`, `/v2/*` o `/{proxy+}`) y el CORS de `http://localhost:3000`.
4. Nicolás despliega Catálogo y el BFF en la EC2 A. Guía base: `infra/ec2/README.md` y `infra/ec2/start-ms-catalogo.sh`.
5. Skarlett despliega Solicitudes en la EC2 B.
6. Frontend: `REACT_APP_API_BASE_URL` = invoke URL. React no apunta a una IP de EC2.

Comprobación del borde, cuando exista la invoke URL:

```text
curl -i <invoke URL>/v1/solicitudes
```

Sin header `Authorization` la respuesta esperada es **401**. Con un access token de Entra del scope `access_as_user` y un rol permitido, **200**. Esas dos capturas son `Evidencias/NIN-EP3-02` y `NIN-EP3-03`.

---

## 5. Cómo se usan los 6 JSON

Están en [`contratos-json/`](contratos-json/). Nicolás publica el par de prioridad. Skarlett publica los dos pares de solicitud.

Por cada tópico, el orden de la pauta es:

1. Publicar el `*-invalido.json` con la key del contrato.
2. Comprobar que no aparece fila de negocio en la tabla EP3.
3. Publicar el `*-valido.json` con la **misma key**.
4. Comprobar la fila en RDS y la línea en el log, con el mismo `eventId`.

| Archivo inválido | Regla que rompe | Key |
| --- | --- | --- |
| `solicitud-creada-invalido.json` | `prioridadNivel` 9, fuera de 1–5 | `solicitud-1001` |
| `solicitud-estado-invalido.json` | `estadoNuevo` `FINALIZADA`, no está en `EstadoSolicitud` | `solicitud-1001` |
| `prioridad-invalido.json` | `nombreNuevo` vacío | `prioridad-2` |

---

## 6. Log de operación

El archivo `operacion.jsonl` lo genera Skarlett desde una ejecución real. Cada línea de consumo lleva fecha UTC, nivel, servicio, grupo, tópico, partición, offset, key, `eventId` y resultado. El procedimiento está en [`../Skarlett/PROCEDIMIENTO-CONSUMER-LAG.md`](../Skarlett/PROCEDIMIENTO-CONSUMER-LAG.md) y el plan de pruebas en [`../Skarlett/PRUEBAS-EP3.md`](../Skarlett/PRUEBAS-EP3.md).

Para seguir un caso en la defensa: tomar el `eventId` de uno de los JSON válidos, buscarlo en `operacion.jsonl` y mostrar la fila correspondiente en RDS.
