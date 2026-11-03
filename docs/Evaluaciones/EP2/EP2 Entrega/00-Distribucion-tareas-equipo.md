# EP2 — Distribución de tareas del equipo

**Asignatura:** DSY1107 · Desarrollo Cloud Native I  
**Evaluación:** Evaluación parcial número 2 — MesaTech Cloud con mensajería y persistencia independiente  
**Equipo:** Ari · Nicolás (Nico) · Giannina (Ninna) · Skarlett  
**Repo:** `duoc-mesatech` (continuidad de la EP1)  
**Fecha del acuerdo:** 2026-10-29  

Este documento fija el reparto propuesto para la EP2. Respeta la experiencia de cada uno en la EP1 y la propuesta de Ari de encargarse de las configuraciones en **AWS Academy**.

---

## 1. Qué pide la EP2 (resumen)

| Componente | Ubicación | Responsabilidad |
| --- | --- | --- |
| Catálogo + publicador | EC2 A | Guardar categorías en su RDS y publicar el evento |
| Solicitudes + consumidor | EC2 B (distinta de A) | Consumir el evento y guardar copia local en su RDS |
| RabbitMQ | EC2 C (distinta de A y B) | Broker con `compose.yaml`, instalado desde cero |
| RDS Catálogo | Instancia exclusiva | Categorías y prioridades originales |
| RDS Solicitudes | Otra instancia exclusiva | Solicitudes + copia de categorías recibidas |
| BFF | Puede compartir EC2 A o B | Seguridad y coordinación de la EP1 (sin RDS propio) |

**Flujo:** React → API Gateway → BFF → Catálogo → RDS Catálogo → exchange RabbitMQ → cola → consumidor Solicitudes → RDS Solicitudes.

**Evento mínimo (creación de categoría):** `eventId`, `tipoEvento`, `fechaEvento`, `categoriaId`, `nombre`.

**Nombres sugeridos de mensajería:**

| Recurso | Nombre sugerido |
| --- | --- |
| Virtual host | `mesatech` |
| Exchange (direct, durable) | `catalogo.exchange` |
| Routing key | `categoria.creada` |
| Cola (durable) | `solicitudes.catalogo.queue` |

**Presentación:** lunes 2 o miércoles 4 de noviembre de 2026 desde las 20:00 (15 minutos). Inscripción **una sola vez** en la planilla del curso.

**Nota:** la pauta habla de 2–3 integrantes; el equipo es el mismo de la EP1 (4 personas). Conviene confirmar con el docente que el grupo completo aplica.

---

## 2. Estado de partida (EP1)

| Tema | Estado actual |
| --- | --- |
| RabbitMQ en el código | **No existe** (sin `spring-amqp` en `ms-catalogo` ni `ms-solicitudes`) |
| Bases de datos | PostgreSQL 16 en Docker (dos bases en el mismo contenedor), **no RDS** |
| EC2 EP1 | 3 instancias: db + ms + bff (`infra/ec2/TOPOLOGIA.md`) |
| Labs de referencia | `EA2/back-rabbitmq-publicador-1` y `EA2/back-rabbitmq-consumidor-1` (caso ventas) |

### Roles que cada uno tuvo en la EP1

| Integrante | Foco EP1 |
| --- | --- |
| Ari | Microsoft Entra ID |
| Nicolás | EC2, Postgres Docker, security groups, presentación |
| Giannina | API Gateway, v1/v2, identidad corporativa |
| Skarlett | Negocio, UI, pruebas, informe |

---

## 3. Reparto EP2

### 3.1 Ari — AWS Academy e infraestructura

**Criterios de pauta que respalda:** C1, C2 (infra), C3, C4, parte de C5 (recursos del broker).

| # | Tarea | Evidencia / entrega al equipo |
| --- | --- | --- |
| A1 | Crear **3 EC2**: Catálogo (A), Solicitudes (B), RabbitMQ (C con Ubuntu 24.04 LTS) | Instance ID, nombre, IP privada de cada una |
| A2 | Crear **2 RDS exclusivas** (Catálogo y Solicitudes) + usuarios de aplicación | Endpoints distintos (sin contraseñas en git) |
| A3 | En EC2 C: instalar **Docker Engine + plugin Compose desde cero** | Capturas de instalación; `docker compose` funcionando |
| A4 | Escribir y ejecutar `compose.yaml` de RabbitMQ (imagen management con tag explícito, hostname, puertos 5672/15672, volumen `/var/lib/rabbitmq`, restart) | Archivo en repo **sin secretos** + `docker compose ps` |
| A5 | Configurar **security groups**: 5672 solo desde SG de EC2 A/B; 15672 y 22 solo desde IP admin `/32`; RDS sin acceso público | Capturas de reglas + justificación |
| A6 | Recursos de mensajería: vhost `mesatech`, usuarios publicador/consumidor (no `guest`), permisos, exchange, cola, binding | Capturas del management UI (sin passwords) |
| A7 | Inventario para el informe (IDs, IPs, endpoints, versiones Docker/Compose) | Archivo o sección en `EP2 Entrega/Ari/` |
| A8 | Mantener Entra ID de la EP1 operativo si hace falta para P8 | Confirmación al equipo |

**Carpeta de trabajo:** `docs/Evaluaciones/EP2/EP2 Entrega/Ari/`

**Orden sugerido para Ari:** A3–A4 (RabbitMQ) → A6 (vhost/usuarios) → A1–A2 (EC2 app + RDS) → A5 (SG) → A7.

---

### 3.2 Nicolás — Publicador + despliegue de Catálogo

**Criterios:** C5 (código publicador), parte de C2/C6.

| # | Tarea | Evidencia / entrega |
| --- | --- | --- |
| N1 | Adaptar el lab publicador a `ms-catalogo`: dependencia `spring-boot-starter-amqp` | Commit / PR |
| N2 | Al crear categoría válida: persistir en RDS Catálogo y publicar JSON del evento con confirms / returned messages | Logs + captura de mensaje en RabbitMQ |
| N3 | Externalizar `spring.rabbitmq.*` y datasource a variables (no hardcodear como en el lab) | `env.example` actualizado |
| N4 | Conectar `ms-catalogo` a **su RDS** (dejar de usar solo Postgres Docker compartido) | Consulta a tablas en RDS Catálogo |
| N5 | Desplegar JAR de Catálogo en **EC2 A** (BFF puede ir en A o B; documentar decisión) | Proceso escuchando + health |
| N6 | Coordinar con Ari nombres finales de exchange, routing key y credenciales de app | Checklist compartido |

**Carpeta de trabajo:** `docs/Evaluaciones/EP2/EP2 Entrega/Nicolas/`

**Referencia de código:** `EA2/back-rabbitmq-publicador-1` (`VentaPublisher`, confirms correlacionados).

---

### 3.3 Giannina — Continuidad EP1 + diagrama + documentación

**Criterios:** C7, C8.

| # | Tarea | Evidencia / entrega |
| --- | --- | --- |
| G1 | Verificar que **API Gateway + Entra + BFF** siguen el flujo seguro con la topología nueva (base de P8) | Capturas 401 / 200 vía Gateway |
| G2 | Diagrama real de despliegue: React, Entra ID, API Gateway, BFF, EC2 A/B/C, ambos RDS, flechas de mensajes y de datos | Archivo en `EP2 Contexto/Entregables/` o `docs/arquitectura/` |
| G3 | Documentar el **contrato del evento** (tipos, formato de fecha, ejemplo real) | MD o sección del informe |
| G4 | Actualizar README: versiones, arranque, detención, orden de recuperación; `.env.example` sin secretos | PR |
| G5 | Kit visual / slides si hay presentación oral (como en EP1) | Assets en entregables |

**Carpeta de trabajo:** `docs/Evaluaciones/EP2/EP2 Entrega/Giannina/`

---

### 3.4 Skarlett — Consumidor + pruebas P1–P8 + informe

**Criterios:** C5 (consumo), C6, C8.

| # | Tarea | Evidencia / entrega |
| --- | --- | --- |
| S1 | Adaptar el lab consumidor a `ms-solicitudes`: `@RabbitListener`, copia local de categoría conservando `categoriaId` | Commit / PR |
| S2 | Conectar `ms-solicitudes` a **su RDS** y desplegar JAR en **EC2 B** | Consulta a tablas en RDS Solicitudes |
| S3 | UI / flujo: reconocer cuándo la categoría ya está disponible para crear solicitud (retraso asíncrono aceptable) | Captura de uso |
| S4 | Ejecutar y documentar **P1 a P8** (incluye P7: detener RabbitMQ y documentar resultado) | Bitácora + capturas en `Evidencias/` |
| S5 | Redactar el **informe técnico** integrando capturas de Ari, Nicolás y Giannina | Word/PDF en `EP2 Contexto/Entregables/` |

**Carpeta de trabajo:** `docs/Evaluaciones/EP2/EP2 Entrega/Skarlett/`

**Referencia de código:** `EA2/back-rabbitmq-consumidor-1` (`VentaListener`).

**Pruebas a cubrir (P1–P8):** flujo feliz con datos en ambos RDS; mensaje pendiente y consumido; espera/reinicio sin perder datos; fallo de publicación con broker detenido (P7); continuidad login/autorización/estados/v1–v2 (P8). Detalle completo en el PDF oficial.

---

## 4. Orden de trabajo del equipo

```text
1. Ari: EC2 C + Docker/Compose + RabbitMQ + vhost/usuarios/exchange/cola
2. Ari: EC2 A/B + 2 RDS + security groups
3. En paralelo: Nicolás (publicador) y Skarlett (consumidor) adaptan código con los labs
4. Despliegue de JARs contra RDS y RabbitMQ (Nico en A, Skarlett en B)
5. Skarlett corre P1–P8; Giannina cierra diagrama/README; todos aportan capturas
6. Ensayo de defensa individual (todos)
```

### Dependencias

| Quién espera | De quién | Qué |
| --- | --- | --- |
| Nicolás / Skarlett | Ari | Host RabbitMQ, vhost, usuarios app, nombres exchange/cola, endpoints RDS |
| Giannina | Ari + Nico + Skarlett | IPs/IDs reales para el diagrama |
| Skarlett (P1–P8) | Nico + Ari | Publicador desplegado y broker estable |
| Informe final | Todos | Capturas etiquetadas en cada carpeta `Evidencias/` |

---

## 5. Defensa individual (C9 — 10 puntos)

Cada integrante debe poder explicar:

1. El **recorrido del dato** (desde crear categoría en React hasta la copia en RDS Solicitudes).
2. La **configuración de infraestructura** (por qué 3 EC2 y 2 RDS; puertos 5672 y 15672).
3. La diferencia entre **confirmación del publicador** (broker ack / returned) y **confirmación del consumidor** (ack tras persistir).
4. Interpretar un **fallo** con evidencia del propio proyecto (ej. P7).

---

## 6. Pauta (recordatorio de topes)

| Criterio | Máx. | Tope si incumplen |
| --- | --- | --- |
| C1 Despliegue EC2 | 10 | Si ambos MS comparten EC2 → máx. 5 |
| C2 Persistencia RDS | 20 | Si comparten una RDS (aunque sean esquemas) → máx. 10; sin RDS → 0 |
| C3 Docker Compose | 15 | Sin compose → 0; funciona pero sin evidencia de instalación desde cero → máx. 7,5 |
| C4 Red y acceso | 10 | — |
| C5 Mensajería | 10 | — |
| C6 Integración y pruebas | 15 | — |
| C7 Continuidad EP1 | 5 | — |
| C8 Entregables | 5 | — |
| C9 Defensa individual | 10 | Por persona |

---

## 7. Reglas del equipo

1. **No subir secretos** al repo (`.env` reales, passwords RDS/RabbitMQ, tokens). Solo `env.example` con valores ficticios.
2. Las capturas **no deben mostrar contraseñas**.
3. Detener EC2/RDS cuando no se trabaje (créditos AWS Academy).
4. Cada integrante deja evidencias en su carpeta bajo `EP2 Entrega/<Nombre>/Evidencias/`.
5. Cambios de código por PR a `dev` / `main` según `docs/equipo/reglas-repositorio.md`.

---

## 8. Enlaces útiles

| Recurso | Ubicación |
| --- | --- |
| PDF oficial EP2 | `docs/Evaluaciones/EP2/EP2 Contexto/Informacion de la evaluacion/EP2_MesaTech_Cloud.pdf` |
| Plan EP1 | `docs/equipo/plan-trabajo.md` |
| Labs publicador/consumidor | `Cloud Native/EA2/back-rabbitmq-publicador-*` |
| Topología EC2 EP1 | `infra/ec2/TOPOLOGIA.md` |