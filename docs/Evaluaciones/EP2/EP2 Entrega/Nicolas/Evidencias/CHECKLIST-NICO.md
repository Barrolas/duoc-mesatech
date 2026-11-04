# Checklist evidencias Nico (EP2 publicador)

Orden: completar prueba → guardar PNG aquí. **Cada captura que envíes en el chat se copia a esta carpeta** (nombre `NIC-…`).

| ID | Qué capturar | Criterio | Archivo | Estado |
| --- | --- | --- | --- | --- |
| NIC-01 | SSH: JAR en `~/apps`, puerto 8082 | N5 | `NIC-01-proceso-jar-puerto-8082.png` | Listo |
| NIC-01b | SSH `OK_SSH` (opcional) | N5 | `NIC-01b-ssh-OK_SSH.png` | Listo |
| NIC-02 | Log: RDS + `Started CatalogoApplication` | N5 / N4 | `NIC-02-log-rds-started.png` | Listo |
| NIC-03 | `curl` POST categoría → HTTP 2xx (Bearer redactado) | N2 | `NIC-03-post-categoria-HTTP-200-y-log.png` | Listo (tachar token antes de Git) |
| NIC-04 | Log: `Evento … publicado` | N2 | `NIC-04-log-evento-publicado.png` | Listo |
| NIC-05 | Rabbit UI: exchange/cola (sin passwords) | N2 | `NIC-05-rabbit-cola-…png` + `NIC-05-rabbit-bindings-…png` | Listo (tachar usuario arriba-derecha si va a Git) |
| NIC-06 | RDS Catálogo: fila en `categorias` | N4 | `NIC-06-rds-categorias.png` | Listo |

**Auxiliar** (no van al informe): `_auxiliar-no-informe/` — timeouts SSH, prueba fallida del script, ensayo local React.

Sin secretos en Git. Tokens y contraseñas tachados en capturas.
