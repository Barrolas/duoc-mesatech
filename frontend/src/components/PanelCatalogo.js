import { useState } from "react";

export default function PanelCatalogo({
  catalogo,
  onCrearCategoria,
  onActualizarCategoria,
  onEliminarCategoria,
  onCrearPrioridad,
  onActualizarPrioridad,
  onEliminarPrioridad,
  ocupado,
}) {
  const [nombreCategoria, setNombreCategoria] = useState("");
  const [nombrePrioridad, setNombrePrioridad] = useState("");
  const [nivel, setNivel] = useState(1);
  const [editCategoriaId, setEditCategoriaId] = useState(null);
  const [editCategoriaNombre, setEditCategoriaNombre] = useState("");
  const [editPrioridadId, setEditPrioridadId] = useState(null);
  const [editPrioridadNombre, setEditPrioridadNombre] = useState("");
  const [editPrioridadNivel, setEditPrioridadNivel] = useState(1);

  const categorias = catalogo?.categorias || [];
  const prioridades = catalogo?.prioridades || [];

  const confirmarEliminar = (etiqueta) => {
    return window.confirm(`¿Eliminar ${etiqueta}? Las solicitudes existentes conservan el texto guardado.`);
  };

  return (
    <div className="row">
      <div className="col-md-6">
        <div className="mt-card">
          <div className="mt-card-body">
            <p className="mt-kicker">Administración</p>
            <h2>Categorías</h2>
            {categorias.length === 0 ? (
              <div className="mt-empty">No hay categorías definidas.</div>
            ) : (
              <ul className="list-group list-group-flush mb-3">
                {categorias.map((item) => (
                  <li className="list-group-item px-0" key={item.id}>
                    {editCategoriaId === item.id ? (
                      <form
                        className="d-flex gap-2 flex-wrap"
                        onSubmit={async (evento) => {
                          evento.preventDefault();
                          await onActualizarCategoria(item.id, {
                            nombre: editCategoriaNombre.trim(),
                          });
                          setEditCategoriaId(null);
                        }}
                      >
                        <input
                          className="form-control form-control-sm"
                          required
                          value={editCategoriaNombre}
                          onChange={(e) => setEditCategoriaNombre(e.target.value)}
                          aria-label="Editar nombre de categoría"
                        />
                        <button className="btn btn-sm btn-primary" type="submit" disabled={ocupado}>
                          Guardar
                        </button>
                        <button
                          className="btn btn-sm btn-outline-secondary"
                          type="button"
                          disabled={ocupado}
                          onClick={() => setEditCategoriaId(null)}
                        >
                          Cancelar
                        </button>
                      </form>
                    ) : (
                      <div className="d-flex justify-content-between align-items-center gap-2">
                        <span>{item.nombre}</span>
                        <span className="btn-group btn-group-sm">
                          <button
                            type="button"
                            className="btn btn-outline-primary"
                            disabled={ocupado}
                            onClick={() => {
                              setEditCategoriaId(item.id);
                              setEditCategoriaNombre(item.nombre);
                            }}
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            className="btn btn-outline-danger"
                            disabled={ocupado}
                            onClick={async () => {
                              if (!confirmarEliminar(`la categoría «${item.nombre}»`)) {
                                return;
                              }
                              await onEliminarCategoria(item.id);
                            }}
                          >
                            Eliminar
                          </button>
                        </span>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            )}
            <form
              onSubmit={async (evento) => {
                evento.preventDefault();
                await onCrearCategoria({ nombre: nombreCategoria.trim() });
                setNombreCategoria("");
              }}
            >
              <label className="form-label" htmlFor="nombre-categoria">
                Nueva categoría
              </label>
              <div className="input-group">
                <input
                  id="nombre-categoria"
                  className="form-control"
                  required
                  value={nombreCategoria}
                  onChange={(e) => setNombreCategoria(e.target.value)}
                />
                <button className="btn btn-primary" type="submit" disabled={ocupado}>
                  Registrar
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <div className="col-md-6">
        <div className="mt-card">
          <div className="mt-card-body">
            <p className="mt-kicker">Administración</p>
            <h2>Prioridades</h2>
            {prioridades.length === 0 ? (
              <div className="mt-empty">No hay prioridades definidas.</div>
            ) : (
              <ul className="list-group list-group-flush mb-3">
                {prioridades.map((item) => (
                  <li className="list-group-item px-0" key={item.id}>
                    {editPrioridadId === item.id ? (
                      <form
                        className="row g-2 align-items-center"
                        onSubmit={async (evento) => {
                          evento.preventDefault();
                          await onActualizarPrioridad(item.id, {
                            nombre: editPrioridadNombre.trim(),
                            nivel: Number(editPrioridadNivel),
                          });
                          setEditPrioridadId(null);
                        }}
                      >
                        <div className="col-6">
                          <input
                            className="form-control form-control-sm"
                            required
                            value={editPrioridadNombre}
                            onChange={(e) => setEditPrioridadNombre(e.target.value)}
                            aria-label="Editar nombre de prioridad"
                          />
                        </div>
                        <div className="col-3">
                          <input
                            type="number"
                            className="form-control form-control-sm"
                            min={1}
                            max={5}
                            required
                            value={editPrioridadNivel}
                            onChange={(e) => setEditPrioridadNivel(e.target.value)}
                            aria-label="Editar nivel"
                          />
                        </div>
                        <div className="col-3 d-flex gap-1">
                          <button className="btn btn-sm btn-primary flex-grow-1" type="submit" disabled={ocupado}>
                            OK
                          </button>
                          <button
                            className="btn btn-sm btn-outline-secondary"
                            type="button"
                            disabled={ocupado}
                            onClick={() => setEditPrioridadId(null)}
                          >
                            ×
                          </button>
                        </div>
                      </form>
                    ) : (
                      <div className="d-flex justify-content-between align-items-center gap-2">
                        <span>
                          {item.nombre}{" "}
                          <span className="text-muted">Nivel {item.nivel}</span>
                        </span>
                        <span className="btn-group btn-group-sm">
                          <button
                            type="button"
                            className="btn btn-outline-primary"
                            disabled={ocupado}
                            onClick={() => {
                              setEditPrioridadId(item.id);
                              setEditPrioridadNombre(item.nombre);
                              setEditPrioridadNivel(item.nivel);
                            }}
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            className="btn btn-outline-danger"
                            disabled={ocupado}
                            onClick={async () => {
                              if (!confirmarEliminar(`la prioridad «${item.nombre}»`)) {
                                return;
                              }
                              await onEliminarPrioridad(item.id);
                            }}
                          >
                            Eliminar
                          </button>
                        </span>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            )}
            <form
              onSubmit={async (evento) => {
                evento.preventDefault();
                await onCrearPrioridad({
                  nombre: nombrePrioridad.trim(),
                  nivel: Number(nivel),
                });
                setNombrePrioridad("");
                setNivel(1);
              }}
            >
              <label className="form-label" htmlFor="nombre-prioridad">
                Nueva prioridad
              </label>
              <div className="row g-2">
                <div className="col-7">
                  <input
                    id="nombre-prioridad"
                    className="form-control"
                    required
                    value={nombrePrioridad}
                    onChange={(e) => setNombrePrioridad(e.target.value)}
                  />
                </div>
                <div className="col-3">
                  <input
                    type="number"
                    className="form-control"
                    min={1}
                    max={5}
                    value={nivel}
                    onChange={(e) => setNivel(e.target.value)}
                    aria-label="Nivel de prioridad"
                  />
                </div>
                <div className="col-2">
                  <button className="btn btn-primary w-100" type="submit" disabled={ocupado}>
                    +
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
