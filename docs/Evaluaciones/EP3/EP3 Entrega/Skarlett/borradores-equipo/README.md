# Borradores EP3 para el equipo — MesaTech Cloud

**Fecha presentación:** miércoles 2-dic-2026 20:15 (Chile)  
**Arquitectura:** Alternativa 1 (`ARQUITECTURA-ACUERDOS.md`)  
**Nota:** ningún archivo aquí contiene contraseñas, tokens ni IPs reales (van como placeholders `<...>`). Tampoco se modificó el clon `repo-mesatech`.

---

## Contenido por integrante / frente

| Carpeta / Archivo | Destinatario | Qué contiene | Verificación realizada |
| --- | --- | --- | --- |
| `ari/` | **Ari** (EC2 D · Kafka) | `instalar-docker-ubuntu.sh`, `docker-compose.kafka-ec2d.yml` (KRaft `apache/kafka:3.9.0` sin `0.0.0.0`), `env.ec2d.example` (`KAFKA_ADVERTISED_HOST=<IP_PRIVADA_EC2_D>`), `crear-topicos.sh`, `verificar-kafka.sh`, `README.md` (SG, `--describe`, procedimiento lag 1 vs 3). | `bash -n` OK; `docker compose config --quiet` OK. |
| `nicolas/` | **Nicolás** (`ms-catalogo` · EC2 A) | `ms-catalogo-kafka.diff` + `README.md` (productor `mesatech.catalogo.prioridad` key `prioridad-{id}`, consumidor `mesatech-catalogo-kafka`, `auditoria_prioridad`, `@Valid`, ack manual post-save, DLQ `mesatech.dlq.eventos`, idempotencia `eventId`, `operacion.jsonl`, continuidad RabbitMQ EP2, 4 clases de tests). | `mvn test`: **12/12 OK**; `git apply --check` OK junto con los diffs de Skarlett sobre `fb26b69` y `561dcd3`. |
| `frontend/` | **Frontend / React** (Giannina o quien integre front) | `frontend-prioridad-nivel.diff` + `README.md` (envía `prioridadNivel` desde `FormCrearSolicitud.js` al crear solicitud). | `npm ci` + `CI=true npm run build` OK; `git apply --check` OK junto con Skarlett + Nicolás sobre `fb26b69` y `561dcd3`. |
| `../borrador_skarlett.diff` y `../borrador_skarlett_sobre_giannina.diff` | **Skarlett** (`ms-solicitudes` · EC2 B) | Parche completo de `ms-solicitudes`: el primero contra `origin/dev` (`fb26b69`), el segundo compatible limpio con `origin/feature/giannina-ep3` (`561dcd3`). | `mvn test`: **20/20 OK**; E2E Docker KRaft + Postgres + RabbitMQ OK; `git apply --check` OK en ambas bases. |
| `../material/guion_disertacion_skarlett.{md,pdf}` | **Skarlett** (Exposición ~5 min) | Guion hablado alineado con los `data-say` de `presentacion-ep3/index.html` (`561dcd3`) + parte EP3 + preguntas posibles del profesor. | Revisado contra `561dcd3` y evidencias locales. |

*(Nota sobre Giannina: en `origin/feature/giannina-ep3` (`561dcd3`) ya corrigió los UUID v4 de los 6 JSON de `contratos-json/`; los 4 de solicitudes y los 2 de prioridad fueron verificados contra Bean Validation y `/workspace/duoc/cloudnative/ep3/material/json/` quedó sincronizado.)*

---

## Orden sugerido para aplicar los parches en Git (sin conflictos)

### Caso A: Si `origin/feature/giannina-ep3` (`561dcd3`) **ya se mergeó a `dev`** (o parten desde `561dcd3`)

```bash
git fetch origin
# Cada uno en su rama (desde dev actualizado con 561dcd3):
# Skarlett:
git checkout -b feature/skarlett-ep3-kafka origin/dev
git apply /ruta/a/borrador_skarlett_sobre_giannina.diff

# Nicolás:
git checkout -b feature/nico-ep3-kafka origin/dev
git apply /ruta/a/ms-catalogo-kafka.diff

# Frontend (en su rama o integrado):
git apply /ruta/a/frontend-prioridad-nivel.diff
```

### Caso B: Si parten directamente desde `origin/dev` (`fb26b69`, antes de mergear a Giannina)

Usar `borrador_skarlett.diff` en lugar de `borrador_skarlett_sobre_giannina.diff` para Skarlett; `ms-catalogo-kafka.diff` y `frontend-prioridad-nivel.diff` aplican igual sin conflictos.

---

## Valores reales que debe completar el equipo antes de la demo AWS

1. **Ari (`Inventario_AWS_EP3.md`):** `<IP_PRIVADA_EC2_D>:9092`, IPs privadas y EIPs de A/B/C/D, endpoints RDS, Invoke URL de API Gateway.
2. **Nicolás (`ms-catalogo.env` en EC2 A):** `KAFKA_BOOTSTRAP_SERVERS=<IP_PRIVADA_EC2_D>:9092`, `KAFKA_LISTENER_CONCURRENCY=3`, credenciales RDS Catálogo y RabbitMQ C.
3. **Skarlett (`ms-solicitudes.env` en EC2 B):** `KAFKA_BOOTSTRAP_SERVERS=<IP_PRIVADA_EC2_D>:9092`, `KAFKA_LISTENER_CONCURRENCY=3`, credenciales RDS Solicitudes y RabbitMQ C.
4. **Frontend (`.env`):** `REACT_APP_API_BASE_URL=<Invoke URL sin / final>`.
