# Borrador para Frontend — Envío de `prioridadNivel` (EP3 MesaTech)

## Por qué hace falta este cambio

En `origin/dev` (`fb26b69`), `frontend/src/components/FormCrearSolicitud.js` solo enviaba `{ titulo, descripcion, categoria, prioridad }` al llamar `POST /v1/solicitudes` (`App.js`).  
En el backend (`borrador_skarlett.diff`), `Solicitud.java` ya acepta el campo `prioridadNivel` (con fallback por nombre en `SolicitudService` si viene `null`). Con este parche mínimo, React toma el `nivel` real del catálogo (`catalogo.prioridades`) y lo envía explícitamente.

> Nota de datos en RDS Catálogo: verificar que las prioridades en `mesatech_catalogo.prioridades` usen la escala del contrato (`1..5`, donde `>= 4` equivale a `PREFERENTE`, ej. Alta=4, Urgente=5, Media=3, Baja=2).

## Qué cambia `frontend-prioridad-nivel.diff`

- `frontend/src/components/FormCrearSolicitud.js` (+2 líneas): busca la prioridad seleccionada en `catalogo.prioridades` y agrega `prioridadNivel: prioridadSeleccionada?.nivel ?? null` al payload de `onCrear(...)`.

## Cómo aplicarlo y compilar

```bash
git apply /ruta/a/frontend-prioridad-nivel.diff

cd frontend
npm ci
npm run build
```

*(Verificado en copia limpia: `CI=true npm run build` compila sin errores ni warnings.)*
