# EP3 — Pauta, criterios y contexto (todo el equipo)

**Fuente:** `docs/Evaluaciones/EP3/EP3 Contexto/EP3_MesaTech.pdf`

---

## Continuidad EP2 (10 pts)

| Requisito | Cómo se demuestra |
| --- | --- |
| React → Entra → API Gateway → BFF → MS | Flujo seguro + 401 sin token |
| v1 / v2 | Capturas o curl vía Gateway |
| EC2 distintas + RDS propios | Inventario + consultas |
| Rabbit `categoria.creada` | POST categoría → copia en RDS Solicitudes |

## Hechos Kafka (3 tópicos, 3 particiones c/u)

| Hecho | Servicio dueño | Regla consumer |
| --- | --- | --- |
| Solicitud creada | `ms-solicitudes` | Alta → atención preferente; resto → normal |
| Cambio estado solicitud | `ms-solicitudes` | RESUELTA/CERRADA → fuera atención; otros → seguimiento |
| Cambio nombre prioridad | `ms-catalogo` | Sustantivo → revisión; solo formato → corrección |

## Entregables obligatorios

- Commit evaluado, **6 JSON** (válido + inválido × 3 tópicos)
- Config tópicos y consumer groups
- Instrucciones reproducibles
- Informe + evidencias
- `operacion.log` o `.jsonl`
- Presentación en vivo

## Repos ejemplo (patrones)

Ver [`Nicolas/REFERENCIAS-KAFKA.md`](../Nicolas/REFERENCIAS-KAFKA.md).

## Lecciones EP2 → EP3

| EP2 | EP3 |
| --- | --- |
| IP efímera | **Elastic IP** por EC2 |
| API Gateway viejo | Recrear en cuenta **Ari** |
| Rabbit/Kafka bootstrap | IP **privada** en `.env` cloud |
| Redeploy por IP | EIP + restart JAR |

## Orden de fases

```text
0 Ari (EIP, EC2, RDS, Rabbit, Kafka, SG, inventario)
1 Ninna Gateway + contratos JSON
2 Nico + Skarlett código Kafka local
3 Deploy cloud
4 Skarlett pruebas aceptación + lag
5 Informe, logs, defensa
```

## Defensa individual (todos)

1. Seguir `eventId` hasta log + RDS  
2. Key → partición → offsets  
3. Rechazo DTO vs lag vs fallo transitorio  
4. EIP vs IP privada Kafka/Rabbit  

## Pauta 100 pts (resumen)

| Criterio | Pts |
| --- | --- |
| Continuidad EP2 | 10 |
| Producción Kafka | 15 |
| Validación DTO | 15 |
| Services y RDS | 15 |
| Particiones y grupos | 10 |
| Consumer lag | 15 |
| Operación y logs | 10 |
| Defensa individual | 10 |

Detalle completo en PDF. Subcriterios de 5 pts: 5 / 2,5 / 0 según evidencia.