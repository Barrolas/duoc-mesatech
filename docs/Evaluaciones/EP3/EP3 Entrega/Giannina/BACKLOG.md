# Backlog Giannina — EP3

**Criterios:** Continuidad EP1/EP2, entregables (JSON, instrucciones), diagrama, apoyo defensa.

---

## G0 — Coordinación inicial

1. Convocar reunión 30 min; cerrar `ARQUITECTURA-ACUERDOS.md`.
2. Mantener contacto con Ari hasta tener invoke URL.

---

## G1 — API Gateway (cuenta nueva)

| Subpaso | Acción |
| --- | --- |
| G1.1 | Recibir API ID + invoke URL de Ari. |
| G1.2 | Rutas hacia BFF: `/public/*`, `/api/*`, `/v1/*`, `/v2/*` (o `/{proxy+}`). |
| G1.3 | CORS: `localhost:3000` + origen front si aplica. |
| G1.4 | Documentar: JWT validado en **BFF** (Gateway pass-through). |
| G1.5 | `curl` sin token → 401; con token Entra → 200. |
| G1.6 | Actualizar `frontend/.env.example` y guía (nueva URL, no EP2). |
| G1.7 | Evidencias `Evidencias/NIN-EP3-*.png`. |

---

## G2 — Contratos Kafka + 6 JSON

| Subpaso | Acción |
| --- | --- |
| G2.1 | `Contrato-eventos-EP3.md`: campos comunes (`eventId`, `tipoEvento`, `fechaEvento` ISO-8601 UTC). |
| G2.2 | Por tópico: campos, Bean Validation sugerida. |
| G2.3 | En `contratos-json/`: `*-valido.json` y `*-invalido.json` (3 pares). |
| G2.4 | Inválido: JSON bien formado, regla rota. |
| G2.5 | Review con Nico y Skarlett antes de merge a `dev`. |

---

## G3 — Diagrama despliegue EP3

1. Partir de diagrama EP2.
2. Añadir EC2 D Kafka, **EIP**, flechas Kafka A/B→D, mantener Rabbit.
3. Export PNG/PDF aquí.

---

## G4 — Instrucciones reproducibles

Crear en carpeta Giannina o `docs/Evaluaciones/EP3/`: `INSTRUCCIONES-ARRANQUE-EP3.md`

1. Orden arranque local/cloud.
2. Variables Kafka en `env.example`.
3. Enlaces a scripts deploy Nico/Skarlett.
4. Cómo generar `operacion.jsonl` (ref. Skarlett).

---

## G5 — Presentación / marca

Actualizar `entregables/presentacion-ep2/` → EP3 o slide deck informe: bloque Kafka + continuidad Rabbit.

---

## Defensa (preparación)

Explicar flujo React→Gateway→BFF y diferencia Gateway vs validación JWT en BFF.