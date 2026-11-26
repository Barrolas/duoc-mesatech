# EP3 — Distribución de tareas del equipo

**Asignatura:** DSY1107  
**Evaluación:** EP3 · Seguimiento operativo con Apache Kafka  
**Equipo:** Ari · Nicolás · Giannina · Skarlett  
**Repo:** `duoc-mesatech`  
**Fecha:** 2026-11-24  
**Cuenta AWS:** Learner Lab de Ari (recrear infra + API Gateway).

El detalle paso a paso está **separado por integrante**. Este archivo es el **índice**.

---

## Documentos compartidos

| Documento | Contenido |
| --- | --- |
| [`README.md`](README.md) | Mapa de carpetas |
| [`equipo/PAUTA-Y-CONTEXTO.md`](equipo/PAUTA-Y-CONTEXTO.md) | Qué pide la pauta, criterios 100 pts, defensa, lecciones EP2 |
| [`ARQUITECTURA-ACUERDOS.md`](ARQUITECTURA-ACUERDOS.md) | **Alternativa 1 cerrada** — BFF en A, tópicos, groups, tablas |
| [`equipo/INSTRUCCIONES-REFACTOR-KAFKA.md`](equipo/INSTRUCCIONES-REFACTOR-KAFKA.md) | **Contrato Giannina + paquetes lab** — qué debe refactorizar cada uno |

---

## Backlog por integrante

| Integrante | README | BACKLOG |
| --- | --- | --- |
| **Ari** | [Ari/README.md](Ari/README.md) | [Ari/BACKLOG.md](Ari/BACKLOG.md) |
| **Giannina** | [Giannina/README.md](Giannina/README.md) | [Giannina/BACKLOG.md](Giannina/BACKLOG.md) |
| **Nicolás** | [Nicolas/README.md](Nicolas/README.md) | [Nicolas/BACKLOG.md](Nicolas/BACKLOG.md) |
| **Skarlett** | [Skarlett/README.md](Skarlett/README.md) | [Skarlett/BACKLOG.md](Skarlett/BACKLOG.md) |

### Otros archivos clave

| Archivo | Dueño |
| --- | --- |
| [Ari/Inventario_AWS_EP3.md](Ari/Inventario_AWS_EP3.md) | Ari |
| [Giannina/contratos-json/](Giannina/contratos-json/) | Giannina (6 JSON) |
| [Nicolas/REFERENCIAS-KAFKA.md](Nicolas/REFERENCIAS-KAFKA.md) | Todos (labs) |
| [Skarlett/PRUEBAS-EP3.md](Skarlett/PRUEBAS-EP3.md) | Skarlett |
| [Skarlett/PROCEDIMIENTO-CONSUMER-LAG.md](Skarlett/PROCEDIMIENTO-CONSUMER-LAG.md) | Skarlett + Ari |

---

## Dependencias (quién espera a quién)

| Espera | De | Qué |
| --- | --- | --- |
| Giannina | Ari | API ID, invoke URL, EIP |
| Nico / Skarlett | Ari | Inventario, bootstrap Kafka/Rabbit, RDS |
| Nico / Skarlett | Giannina | Contrato + JSON por tópico |
| Skarlett | Nico | Prioridad Kafka operativa para demo integrada |
| Informe | Todos | Evidencias en cada `Evidencias/` |

---

## Próximos pasos (semana 1)

1. Reunión 30 min → cerrar `ARQUITECTURA-ACUERDOS.md`.
2. Ari: EIP + EC2 D Kafka + inventario borrador.
3. Giannina: contratos + Gateway en cuanto exista invoke URL.
4. Nico / Skarlett: clonar repos ejemplo, ramas `feature/*-ep3-kafka`.

---

## Enlaces contexto

- PDF: `docs/Evaluaciones/EP3/EP3 Contexto/EP3_MesaTech.pdf`
- EP2 reparto (referencia): `docs/Evaluaciones/EP2/EP2 Entrega/00-Distribucion-tareas-equipo.md`