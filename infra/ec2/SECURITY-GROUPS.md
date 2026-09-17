# Security groups — MesaTech EV1 (1 EC2)

Documento para informe y configuración en consola AWS.

## Reglas inbound objetivo

| Puerto | Protocolo | Origen | Servicio | Notas |
| --- | --- | --- | --- | --- |
| 22 | TCP | IP del equipo / VPN | SSH | Admin; no `0.0.0.0/0` en entrega |
| 8080 | TCP | Solo tráfico Gateway* | BFF | *En EV1 simple: restringir lo máximo posible; idealmente IP de integración Gateway o prueba temporal acotada |
| 8081 | — | **No** Internet | ms-solicitudes | Solo localhost / SG interno |
| 8082 | — | **No** Internet | ms-catalogo | Solo localhost / SG interno |
| 5432 | — | **No** Internet | PostgreSQL | Solo 127.0.0.1 (`docker-compose.ec2.yml`) |

## Regla EP1

- Acceso directo a MS desde Internet: **bloqueado** (evidencia NIC-08).
- BFF sin JWT: **401** (evidencia NIC-07).
- React en producción: **API Gateway**, no `:8081`/`:8082`.

## Outbound

Por defecto: permitir salida (Entra JWKS, apt, docker pull).

## Diagrama

```text
Internet → API Gateway → (8080) BFF → 127.0.0.1:8081 / :8082 → MS → 127.0.0.1:5432 Postgres
Internet -X→ 8081, 8082, 5432
```
