import { useState } from "react";

export default function FormCrearSolicitud({ catalogo, onCrear, ocupado }) {
  const [titulo, setTitulo] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [categoria, setCategoria] = useState("");
  const [prioridad, setPrioridad] = useState("");

  const categorias = catalogo?.categorias || [];
  const prioridades = catalogo?.prioridades || [];

  const enviar = async (evento) => {
    evento.preventDefault();
    await onCrear({
      titulo: titulo.trim(),
      descripcion: descripcion.trim(),
      categoria: categoria || null,
      prioridad: prioridad || null,
    });
    setTitulo("");
    setDescripcion("");
    setCategoria("");
    setPrioridad("");
  };

  return (
    <form className="mt-card" onSubmit={enviar}>
      <div className="mt-card-body">
        <p className="mt-kicker">Mesa de ayuda</p>
        <h2>Registrar solicitud de soporte</h2>
        <div className="mb-3">
          <label className="form-label" htmlFor="titulo">Título</label>
          <input
            id="titulo"
            className="form-control"
            required
            maxLength={120}
            placeholder="Resuma el incidente"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label className="form-label" htmlFor="descripcion">Descripción</label>
          <textarea
            id="descripcion"
            className="form-control"
            rows={3}
            required
            maxLength={1000}
            placeholder="Indique el contexto, el equipo afectado y lo que ya se intentó."
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
          />
        </div>
        <div className="row">
          <div className="col-md-6 mb-3">
            <label className="form-label" htmlFor="categoria">Categoría</label>
            <select
              id="categoria"
              className="form-select"
              value={categoria}
              onChange={(e) => setCategoria(e.target.value)}
            >
              <option value="">Seleccione una categoría</option>
              {categorias.map((item) => (
                <option key={item.id} value={item.nombre}>
                  {item.nombre}
                </option>
              ))}
            </select>
          </div>
          <div className="col-md-6 mb-3">
            <label className="form-label" htmlFor="prioridad">Prioridad</label>
            <select
              id="prioridad"
              className="form-select"
              value={prioridad}
              onChange={(e) => setPrioridad(e.target.value)}
            >
              <option value="">Seleccione una prioridad</option>
              {prioridades.map((item) => (
                <option key={item.id} value={item.nombre}>
                  {item.nombre}
                </option>
              ))}
            </select>
          </div>
        </div>
        <button className="btn btn-primary" type="submit" disabled={ocupado}>
          Enviar solicitud
        </button>
      </div>
    </form>
  );
}
