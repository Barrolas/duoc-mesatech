# Inventario AWS — EP3 MesaTech

**Responsable:** Ari  
**Región:** us-east-1  
**Última actualización EIP:** _pendiente_

> Completar sin contraseñas. Tras cada reinicio de lab, confirmar que **EIP** sigue asociada.

## 1. EC2

| Rol | Nombre | Instance ID | Tipo | AZ | IPv4 privada | **Elastic IP** | SG |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A · Catálogo | | | | | | | |
| B · Solicitudes | | | | | | | |
| C · Rabbit | | | | | | | |
| D · Kafka | | | | | | | |

**SSH:** `ubuntu@<EIP>` con `.pem` del equipo.

## 2. RDS

| Rol | Identifier | Endpoint | DB | Public | SG |
| --- | --- | --- | --- | --- | --- |
| Catálogo | | | mesatech_catalogo | No | |
| Solicitudes | | | mesatech_solicitudes | No | |

## 3. Rabbit (C)

| Recurso | Valor |
| --- | --- |
| AMQP (desde A/B) | `<privada C>:5672` |
| Management UI | `http://<EIP C>:15672` |
| vhost | `mesatech` |

## 4. Kafka (D)

| Recurso | Valor |
| --- | --- |
| Bootstrap (desde A/B) | `<privada D>:9092` |
| UI (opcional) | `http://<EIP D>:8088` |
| Tópicos | 3 negocio × 3 particiones |

## 5. API Gateway

| Campo | Valor |
| --- | --- |
| API ID | |
| Invoke URL | |
| Integración BFF | |

## 6. IP admin SSH/SG

`___/32` (actualizar si cambia)

## 7. Handoff rápido

| Integrante | Variables clave |
| --- | --- |
| Nico | `CATALOGO_DATASOURCE_URL`, `RABBITMQ_HOST`, `KAFKA_BOOTSTRAP_SERVERS` |
| Skarlett | `SOLICITUDES_DATASOURCE_URL`, `RABBITMQ_HOST`, `KAFKA_BOOTSTRAP_SERVERS` |
| Giannina | EIP todas, invoke URL |