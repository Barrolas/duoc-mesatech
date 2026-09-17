import FormCrearSolicitud from "./FormCrearSolicitud";
import TablaSolicitudes from "./TablaSolicitudes";
import PanelCatalogo from "./PanelCatalogo";

export default function EspacioTrabajo({
  aviso,
  mensaje,
  vista,
  permisos,
  catalogo,
  mias,
  todas,
  ocupado,
  onCrear,
  onCambiarEstado,
  onCrearCategoria,
  onCrearPrioridad,
}) {
  return (
    <>
      {aviso && <div className="alert alert-info">{aviso}</div>}
      {mensaje && <div className={`alert alert-${mensaje.tipo}`}>{mensaje.texto}</div>}

      {vista === "mias" && (
        <>
          <FormCrearSolicitud catalogo={catalogo} onCrear={onCrear} ocupado={ocupado} />
          <TablaSolicitudes
            titulo="Mis solicitudes"
            solicitudes={mias}
            vacio="No hay solicitudes asociadas a su cuenta."
            puedeCambiarEstado={permisos.cambiarEstado}
            onCambiarEstado={onCambiarEstado}
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
        />
      )}

      {vista === "catalogo" && permisos.catalogo && (
        <PanelCatalogo
          catalogo={catalogo}
          onCrearCategoria={onCrearCategoria}
          onCrearPrioridad={onCrearPrioridad}
          ocupado={ocupado}
        />
      )}
    </>
  );
}
