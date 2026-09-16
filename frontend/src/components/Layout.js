function iniciales(nombre) {
  return String(nombre || "MT")
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0].toUpperCase())
    .join("");
}

export default function Layout({
  usuario,
  onCerrarSesion,
  nav,
  vista,
  onVista,
  children,
}) {
  return (
    <div className="mt-app">
      <header className="mt-header">
        <div className="mt-header-inner">
          <a className="mt-brand" href="/">
            <img src={`${process.env.PUBLIC_URL}/logo.svg`} alt="" />
            <span>
              <span className="mt-brand-name">MesaTech Cloud</span>
              <span className="mt-brand-tag">Soporte tecnológico centralizado</span>
            </span>
          </a>
          {usuario && (
            <div className="mt-user">
              <div className="mt-user-meta d-none d-sm-block">
                <strong>{usuario.nombre}</strong>
                <span>{usuario.rol}</span>
              </div>
              <div className="mt-avatar" aria-hidden="true">
                {iniciales(usuario.nombre)}
              </div>
              {onCerrarSesion && (
                <button type="button" className="btn btn-sm btn-outline-light" onClick={onCerrarSesion}>
                  Cerrar sesión
                </button>
              )}
            </div>
          )}
        </div>
      </header>

      {nav?.length > 0 && (
        <nav className="mt-subnav" aria-label="Secciones">
          <div className="mt-subnav-inner">
            {nav.map((item) => (
              <button
                key={item.id}
                type="button"
                className={vista === item.id ? "active" : ""}
                onClick={() => onVista(item.id)}
              >
                {item.label}
              </button>
            ))}
          </div>
        </nav>
      )}

      <main className="mt-main">{children}</main>

      <footer className="mt-footer">
        <div className="mt-footer-inner">
          <span>MesaTech Cloud · Plataforma institucional de soporte TI</span>
          <span>DSY1107 · Desarrollo Cloud Native I</span>
        </div>
      </footer>
    </div>
  );
}
