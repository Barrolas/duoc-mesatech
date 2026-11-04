import FormCrearSolicitud from "./FormCrearSolicitud";
import TablaSolicitudes from "./TablaSolicitudes";
import PanelCatalogo from "./PanelCatalogo";

export default function EspacioTrabajo({
  aviso,
  mensaje,
  vista,
  permisos,
  catalogo,
  categoriasDisponibles,
  onActualizarCategorias,
  mias,
  todas,
  ocupado,
  onCrear,
  onCambiarEstado,
  onTransicionInvalida,
  onCrearCategoria,
  onActualizarCategoria,
  onEliminarCategoria,
  onCrearPrioridad,
  onActualizarPrioridad,
  onEliminarPrioridad,
}) {
  return (
    <>
      {aviso && <div className="alert alert-info">{aviso}</div>}
      {mensaje && (
        <div
          className={`mt-feedback alert alert-${mensaje.tipo} fade show`}
          role="status"
          aria-live="polite"
        >
          {mensaje.texto}
        </div>
      )}

      {vista === "mias" && (
        <>
          <FormCrearSolicitud
            catalogo={catalogo}
            categoriasDisponibles={categoriasDisponibles}
            onActualizarCategorias={onActualizarCategorias}
            onCrear={onCrear}
            ocupado={ocupado}
          />
          <TablaSolicitudes
            titulo="Mis solicitudes"
            solicitudes={mias}
            vacio="No hay solicitudes asociadas a su cuenta."
            puedeCambiarEstado={permisos.cambiarEstado}
            onCambiarEstado={onCambiarEstado}
            onTransicionInvalida={onTransicionInvalida}
          />
        </>
      )}

      {vista === "todas" && permisos.verTodas && (
        <TablaSolicitudes
          titulo="Bandeja general de atención"
          solicitudes={todas}
          vacio="No existen solicitudes registradas en la bandeja."
          puedeCambiarEstado={permisos.cambiarEstado}
          onCambiarEstado={onCambiarEstado}
          onTransicionInvalida={onTransicionInvalida}
        />
      )}

      {vista === "catalogo" && permisos.catalogo && (
        <PanelCatalogo
          catalogo={catalogo}
          onCrearCategoria={onCrearCategoria}
          onActualizarCategoria={onActualizarCategoria}
          onEliminarCategoria={onEliminarCategoria}
          onCrearPrioridad={onCrearPrioridad}
          onActualizarPrioridad={onActualizarPrioridad}
          onEliminarPrioridad={onEliminarPrioridad}
          ocupado={ocupado}
        />
      )}
    </>
  );
}
