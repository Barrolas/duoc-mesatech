import { useState } from "react";
import { ESTADOS } from "../roles";
import BadgeEstado from "./BadgeEstado";

export default function TablaSolicitudes({
  titulo,
  solicitudes,
  vacio,
  puedeCambiarEstado,
  onCambiarEstado,
}) {
  const [filtro, setFiltro] = useState("");
  const lista = Array.isArray(solicitudes) ? solicitudes : [];
  const filtradas = filtro ? lista.filter((item) => item.estado === filtro) : lista;

  return (
    <div className="mt-card">
      <div className="mt-card-body">
        <div className="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
          <h2 className="mb-0">{titulo}</h2>
          <select
            className="form-select w-auto"
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
            aria-label="Filtrar por estado"
          >
            <option value="">Todos los estados</option>
            {ESTADOS.map((estado) => (
              <option key={estado} value={estado}>
                {estado.replaceAll("_", " ")}
              </option>
            ))}
          </select>
        </div>

        {filtradas.length === 0 ? (
          <div className="mt-empty">{vacio}</div>
        ) : (
          <div className="table-responsive">
            <table className="table mt-table align-middle">
              <thead>
                <tr>
                  <th>Folio</th>
                  <th>Solicitud</th>
                  <th>Estado</th>
                  <th>Categoría</th>
                  <th>Prioridad</th>
                  <th>Solicitante</th>
                  {puedeCambiarEstado && <th>Actualizar estado</th>}
                </tr>
              </thead>
              <tbody>
                {filtradas.map((item) => (
                  <tr key={item.id}>
                    <td>{String(item.id).padStart(4, "0")}</td>
                    <td>
                      <div className="fw-semibold">{item.titulo}</div>
                      {item.descripcion && (
                        <small className="text-muted">{item.descripcion}</small>
                      )}
                    </td>
                    <td>
                      <BadgeEstado estado={item.estado} />
                    </td>
                    <td>{item.categoria || "—"}</td>
                    <td>{item.prioridad || "—"}</td>
                    <td>{item.usuarioSolicitante || "—"}</td>
                    {puedeCambiarEstado && (
                      <td>
                        <select
                          className="form-select form-select-sm"
                          value=""
                          onChange={(e) => {
                            const estado = e.target.value;
                            e.target.value = "";
                            if (estado) {
                              onCambiarEstado(item.id, estado);
                            }
                          }}
                          aria-label={`Actualizar estado de la solicitud ${item.id}`}
                        >
                          <option value="">Seleccione…</option>
                          {ESTADOS.map((estado) => (
                            <option key={estado} value={estado}>
                              {estado.replaceAll("_", " ")}
                            </option>
                          ))}
                        </select>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
