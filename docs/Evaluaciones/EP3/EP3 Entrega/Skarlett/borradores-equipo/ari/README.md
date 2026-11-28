# Borrador para Ari — Kafka KRaft en EC2 D (EP3 MesaTech)

Base: `ARQUITECTURA-ACUERDOS.md` (Alternativa 1) + `docs/Evaluaciones/EP3/EP3 Entrega/Ari/BACKLOG.md`.  
Todos los valores de red van como placeholders (`<IP_PRIVADA_EC2_D>`, `<TU_IP_ADMIN>/32`). **No hay contraseñas ni IPs reales.**

---

## 1. Security Group de la EC2 D (reglas Inbound en AWS)

| Puerto | Protocolo | Origen permitido | Uso |
| --- | --- | --- | --- |
| `22` | TCP | `<TU_IP_ADMIN>/32` | SSH administración |
| `9092` | TCP | **Solo SG de EC2 A (`ms-catalogo`) y SG de EC2 B (`ms-solicitudes`)** (o sus `/32` privadas) | Tráfico Kafka (`KAFKA_BOOTSTRAP_SERVERS=<IP_PRIVADA_EC2_D>:9092`) |
| `8088` | TCP | `<TU_IP_ADMIN>/32` *(opcional)* | Kafka UI (`http://<EIP_EC2_D>:8088`); **no** abrir a `0.0.0.0/0` (no tiene login) |

- **EC2 A (`ms-catalogo`) y EC2 B (`ms-solicitudes`)** no necesitan abrir `9092` de entrada: hacen conexión **saliente** hacia EC2 D:9092.
- Puerto interno `29092` y controlador KRaft `9093` **no se exponen** fuera de Docker.

---

## 2. Instalar Docker en EC2 D (Ubuntu 24.04)

```bash
chmod +x instalar-docker-ubuntu.sh
./instalar-docker-ubuntu.sh
# Si es la primera vez, cierra SSH y vuelve a entrar para usar docker sin sudo
```

---

## 3. Levantar Kafka KRaft (`apache/kafka:3.9.0`) y crear los 4 tópicos

> **Nota sobre el fix aplicado:** En `apache/kafka:3.9.0`, usar `0.0.0.0` en `KAFKA_LISTENERS` provoca fallo de arranque (`advertised.listeners cannot use the nonroutable meta-address 0.0.0.0`). El compose de esta carpeta usa `EXTERNAL://:9092,INTERNAL://:29092,CONTROLLER://:9093` y lee `KAFKA_ADVERTISED_HOST` desde `.env`.

```bash
mkdir -p ~/kafka
cp docker-compose.kafka-ec2d.yml crear-topicos.sh verificar-kafka.sh ~/kafka/
cp env.ec2d.example ~/kafka/.env
cd ~/kafka

# Editar .env y reemplazar <IP_PRIVADA_EC2_D> por la IPv4 privada real de la EC2 D
# Ejemplo para obtenerla dentro de la misma EC2: hostname -I | awk '{print $1}'
nano .env

# Validar sintaxis y levantar (crea los 4 tópicos automáticamente via mesatech-kafka-init)
docker compose -f docker-compose.kafka-ec2d.yml config --quiet
docker compose -f docker-compose.kafka-ec2d.yml up -d
docker logs mesatech-kafka-init
```

Los 4 tópicos creados (todos con **3 particiones** y `replication-factor = 1`):
1. `mesatech.solicitud.creada`
2. `mesatech.solicitud.estado`
3. `mesatech.catalogo.prioridad`
4. `mesatech.dlq.eventos`

*(Si necesitas re-ejecutar la creación manualmente:)*

```bash
docker exec -i mesatech-kafka /bin/bash < crear-topicos.sh
```

---

## 4. Comandos de verificación (capturas ARI3 / Evidencia 1 y 5)

```bash
./verificar-kafka.sh
```

O manualmente:

```bash
# 1) Estado de tópicos y particiones (deben verse 4 tópicos × 3 particiones cada uno)
docker exec mesatech-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --describe

# 2) Consumer group de Solicitudes (6 filas: 3 particiones de creada + 3 de estado)
docker exec mesatech-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --group mesatech-solicitudes-kafka --describe

# 3) Consumer group de Catálogo (3 filas: mesatech.catalogo.prioridad)
docker exec mesatech-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --group mesatech-catalogo-kafka --describe
```

**Dato para entregar a Nicolás (EC2 A) y Skarlett (EC2 B):**

```text
KAFKA_BOOTSTRAP_SERVERS=<IP_PRIVADA_EC2_D>:9092
```

---

## 5. Procedimiento de Consumer Lag: Concurrencia 1 vs 3 (Ari en D + Skarlett en B)

Objetivo: mostrar **antes / durante / después** del backlog en `mesatech.solicitud.creada` mientras `mesatech.solicitud.estado` sigue en lag `0` (`PROCEDIMIENTO-CONSUMER-LAG.md`).

### Corrida A — `KAFKA_LISTENER_CONCURRENCY=1` (en EC2 B)

1. **En EC2 B (Skarlett):** en su archivo env poner `KAFKA_LISTENER_CONCURRENCY=1` y reiniciar `ms-solicitudes`.
2. **Antes (Ari en EC2 D):** ejecutar `--describe --group mesatech-solicitudes-kafka`.
   - Debes ver **1 único `CONSUMER-ID`** atendiendo las 3 particiones de `mesatech.solicitud.creada` y 1 único `CONSUMER-ID` atendiendo las 3 particiones de `mesatech.solicitud.estado`, ambos con `LAG = 0`. *(Captura `conc1-antes`)*
3. **Generar incidente:** detener brevemente `ms-solicitudes` en EC2 B (o publicar un lote grande equivalente con keys variadas `solicitud-2001`..`solicitud-2050` en `mesatech.solicitud.creada` mientras el consumidor está detenido o saturado).
4. **Durante (Ari en EC2 D):** ejecutar `--describe` y capturar el `LAG` > 0 por partición (`LAG = LOG-END-OFFSET − CURRENT-OFFSET`). *(Captura `conc1-durante`)*
5. **Recuperación:** arrancar `ms-solicitudes` con `KAFKA_LISTENER_CONCURRENCY=1` y medir con cronómetro/timestamps de `operacion.jsonl` cuánto tarda en volver a `LAG = 0`. *(Captura `conc1-despues`)*

### Corrida B — `KAFKA_LISTENER_CONCURRENCY=3` (en EC2 B)

1. **En EC2 B (Skarlett):** cambiar a `KAFKA_LISTENER_CONCURRENCY=3` y reiniciar `ms-solicitudes`.
2. **Antes (Ari en EC2 D):** ejecutar `--describe --group mesatech-solicitudes-kafka`.
   - Debes ver **3 `CONSUMER-ID` distintos** en `mesatech.solicitud.creada` (1 por partición) y **3 `CONSUMER-ID` distintos** en `mesatech.solicitud.estado`, con `LAG = 0`. *(Captura `conc3-antes`)*
3. **Generar lote equivalente** (mismo volumen y misma distribución de keys) con el consumidor detenido.
4. **Durante (Ari en EC2 D):** capturar `--describe` con `LAG` > 0. *(Captura `conc3-durante`)*
5. **Recuperación:** arrancar `ms-solicitudes` con `KAFKA_LISTENER_CONCURRENCY=3`, medir el tiempo hasta `LAG = 0` y comparar contra la Corrida A. *(Captura `conc3-despues`)*
