# Cómo hacer capturas para el informe EP1

Referencia común para **Ari, Ninna, Nico y Skarlet**. La rúbrica exige un informe **pormenorizado** con prints de configuración Cloud (Azure + AWS) y de React ([`../../caso/ep1-guia-oficial.md`](../../caso/ep1-guia-oficial.md) §12, §1 entregables).

---

## Reglas de oro

| Regla | Detalle |
| --- | --- |
| **Un paso = una o más capturas** | Cada subpaso importante de tu guía debe tener al menos 1 print en el informe. |
| **Sin secretos** | No client secrets, `.pem`, passwords, tokens Bearer completos, ni `.env` con valores reales. |
| **Redactar si hace falta** | Tacha con barra negra: token JWT, contraseñas, correos personales si el equipo prefiere. |
| **Client ID visible OK** | Los *Application (client) ID* de Entra **no** son secretos; sí demuestran que es el tenant del equipo. |
| **Fecha y contexto** | Pie de figura: “Figura X — …” + 1 frase de qué demuestra para la rúbrica. |
| **Resolución legible** | Texto de consola legible; zoom 100–125 % en portal si hace falta. |

---

## Herramientas (Windows)

| Acción | Atajo / herramienta |
| --- | --- |
| Captura región | `Win + Shift + S` → recorte → pegar en Word o guardar PNG |
| Ventana completa | `Win + PrtScn` (carpeta Imágenes/Capturas de pantalla) |
| Navegador | F12 solo cuando la guía lo pida; captura **pestaña Network** o **Console**, no todo el token |
| Postman | Captura status **401/403/200** + URL; ocultar valor del Bearer |
| Terminal SSH | Captura salida de `docker ps`, `curl -w "%{http_code}"`, etc. |

---

## Organización de archivos (recomendado)

Carpeta compartida del equipo (OneDrive/Drive, **no** Git con secretos):

```text
informe-ev1/
├── capturas/
│   ├── ari/     ARI-01.png, ARI-02.png, ...
│   ├── ninna/   NIN-01.png, ...
│   ├── nico/    NIC-01.png, ...
│   └── skarlet/ SKA-01.png, ...
└── informe.docx   Skarlet integra
```

**Skarlet** arma el Word final; cada integrante entrega su subcarpeta + pies de figura sugeridos.

---

## Mapa rúbrica → responsable

| Requisito EP1 §12 / §14 | Owner capturas | Guía detallada |
| --- | --- | --- |
| Microsoft Entra ID (apps, redirect, scopes) | Ari | [`ari-identidad-azure.md`](ari-identidad-azure.md) § Capturas |
| API Gateway (HTTP API, rutas, CORS, JWT) | Ninna | [`ninna-gateway-marca-versiones.md`](ninna-gateway-marca-versiones.md) § Capturas |
| EC2 + persistencia + despliegue BFF/MS | Nico | [`nico-ec2-seguridad-presentacion.md`](nico-ec2-seguridad-presentacion.md) § Capturas |
| React, login, token, API, autorización, pruebas §11 | Skarlet (+ reparto) | [`skarlet-negocio-pruebas-informe.md`](skarlet-negocio-pruebas-informe.md) § Capturas |
| Diagrama solución | Skarlet + Nico | NIC/SKA |
| Identidad corporativa | Ninna | NIN-H |

---

## Pie de figura (plantilla)

```text
Figura ARI-03 — Permisos de API de la SPA MesaTech con consentimiento de administrador
concedido para el scope access_as_user (Microsoft Entra ID, EP1 §12).
```
