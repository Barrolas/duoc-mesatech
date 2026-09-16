import { useState } from "react";

export default function PanelCatalogo({ catalogo, onCrearCategoria, onCrearPrioridad, ocupado }) {
  const [nombreCategoria, setNombreCategoria] = useState("");
  const [nombrePrioridad, setNombrePrioridad] = useState("");
  const [nivel, setNivel] = useState(1);

  const categorias = catalogo?.categorias || [];
  const prioridades = catalogo?.prioridades || [];

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
                    {item.nombre}
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
              <label className="form-label" htmlFor="nombre-categoria">Nueva categoría</label>
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
                  <li className="list-group-item px-0 d-flex justify-content-between" key={item.id}>
                    <span>{item.nombre}</span>
                    <span className="text-muted">Nivel {item.nivel}</span>
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
              <label className="form-label" htmlFor="nombre-prioridad">Nueva prioridad</label>
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
