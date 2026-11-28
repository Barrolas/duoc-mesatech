# Ari — EP3 Infraestructura AWS

**Cuenta:** AWS Academy Learner Lab, la **misma cuenta de la EP2 reutilizada** (decisión del 2026-11-27 por créditos restantes). ID `4329-7071-7799`, región `us-east-1`.

**Estado (2026-11-28, America/Santiago):** infraestructura lista (4 EC2 con EIP, 2 RDS, Rabbit, Kafka y API Gateway base). Inventario enviado al equipo. El BFF en A todavía no está desplegado: el Gateway responde 503 hasta el deploy del sábado con Nico.

## Documentos

| Documento | Para qué |
| --- | --- |
| [`BACKLOG.md`](BACKLOG.md) | Tareas A0–A8 y pendientes |
| [`Inventario_AWS_EP3.md`](Inventario_AWS_EP3.md) | Única fuente de verdad de IP, EIP, RDS, brokers y Gateway |
| [`Evidencias/CHECKLIST-ARI.md`](Evidencias/CHECKLIST-ARI.md) | Checklist de capturas |
| [`Evidencias/Evidencia EP3 - Ari.docx`](Evidencias/Evidencia%20EP3%20-%20Ari.docx) | Capturas |

**Dependen de este inventario:** Nico (EC2 A, RDS catálogo, privadas Rabbit/Kafka), Skarlett (EC2 B, RDS solicitudes) y Giannina (EIP + invoke URL del Gateway).

## Pendientes de Ari

- Deploy del sábado con Nico: BFF y JAR de catálogo con Kafka en A.
- Soporte a la prueba de consumer lag con Skarlett (ARI3-L01).
- Aporte al informe y preparación de la defensa.
- Opcional: revisar el PR #22 de Skarlett.
