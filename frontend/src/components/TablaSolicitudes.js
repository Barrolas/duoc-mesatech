import { useState } from "react";
import { ESTADOS } from "../roles";
import { opcionesTransicion, validarTransicion } from "../transiciones";
import BadgeEstado from "./BadgeEstado";

function etiquetaEstado(estado) {
  return estado.replaceAll("_", " ");
}

export default function TablaSolicitudes({
  titulo,
  solicitudes,
  vacio,
  puedeCambiarEstado,
  onCambiarEstado,
  onTransicionInvalida,
}) {
  const [filtro, setFiltro] = useState("");
  const lista = Array.isArray(solicitudes) ? solicitudes : [];
  const filtradas = filtro ? lista.filter((item) => item.estado === filtro) : lista;

  const intentarCambio = (item, nuevo, tipo) => {
    const actual = item.estado || "CREADA";
    const rechazo = validarTransicion(actual, nuevo);
    if (rechazo) {
      onTransicionInvalida?.(rechazo);
      return;
    }
    const prefijo = tipo === "corregir" ? "Corrección: " : "";
    onCambiarEstado(item.id, nuevo, `${prefijo}estado actualizado a ${etiquetaEstado(nuevo)}.`);
  };

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
                {etiquetaEstado(estado)}
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
                {filtradas.map((item) => {
                  const actual = item.estado || "CREADA";
                  const { avanzar, corregir, hayAlguna } = opcionesTransicion(actual);

                  return (
                    <tr key={item.id}>
                      <td>{String(item.id).padStart(4, "0")}</td>
                      <td>
                        <div className="fw-semibold">{item.titulo}</div>
                        {item.descripcion && (
                          <small className="text-muted">{item.descripcion}</small>
                        )}
                      </td>
                      <td>
                        <BadgeEstado estado={actual} />
                      </td>
                      <td>{item.categoria || "—"}</td>
                      <td>{item.prioridad || "—"}</td>
                      <td>{item.usuarioSolicitante || "—"}</td>
                      {puedeCambiarEstado && (
                        <td>
                          {!hayAlguna ? (
                            <span className="text-muted small">Sin cambios disponibles</span>
                          ) : (
                            <select
                              key={`${item.id}-${actual}`}
                              className="form-select form-select-sm"
                              defaultValue=""
                              onChange={(e) => {
                                const raw = e.target.value;
                                if (!raw) {
                                  return;
                                }
                                const [tipo, estado] = raw.split(":");
                                intentarCambio(item, estado, tipo);
                              }}
                              aria-label={`Transición para solicitud ${item.id}`}
                            >
                              <option value="" disabled>
                                Elegir acción…
                              </option>
                              {avanzar.length > 0 && (
                                <optgroup label="Avanzar flujo">
                                  {avanzar.map(({ estado }) => (
                                    <option key={estado} value={`avanzar:${estado}`}>
                                      → {etiquetaEstado(estado)}
                                    </option>
                                  ))}
                                </optgroup>
                              )}
                              {corregir.length > 0 && (
                                <optgroup label="Corregir / reabrir">
                                  {corregir.map(({ estado }) => (
                                    <option key={estado} value={`corregir:${estado}`}>
                                      ↩ {etiquetaEstado(estado)}
                                    </option>
                                  ))}
                                </optgroup>
                              )}
                            </select>
                          )}
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
