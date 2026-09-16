import { useEffect, useState } from "react";
import {
  AuthenticatedTemplate,
  UnauthenticatedTemplate,
  useMsal,
} from "@azure/msal-react";
import { loginRequest } from "./authConfig";
import { apiGet } from "./api/http";

function App() {
  const { instance, accounts } = useMsal();
  const [sesionApi, setSesionApi] = useState(null);
  const [solicitudes, setSolicitudes] = useState(null);
  const [error, setError] = useState(null);

  const iniciarSesion = () => {
    instance.loginRedirect(loginRequest).catch((err) => console.error(err));
  };

  const cerrarSesion = () => {
    instance.logoutRedirect();
  };

  useEffect(() => {
    if (accounts.length === 0) {
      return;
    }

    const cargar = async () => {
      try {
        const usuario = await apiGet(instance, accounts[0], "/api/usuario");
        setSesionApi(usuario);
        const mias = await apiGet(instance, accounts[0], "/v1/solicitudes/mias");
        setSolicitudes(mias);
      } catch (err) {
        setError("No se pudo consumir la API. Revise Gateway/BFF y el Access Token.");
      }
    };

    cargar();
  }, [accounts, instance]);

  const cuenta = accounts[0];
  const claims = cuenta?.idTokenClaims || {};

  return (
    <div className="container py-4">
      <h1>MesaTech Cloud</h1>
      <p className="text-muted">Gestión de solicitudes de soporte</p>

      <UnauthenticatedTemplate>
        <div className="alert alert-warning">El usuario no está autenticado.</div>
        <button className="btn btn-primary" onClick={iniciarSesion}>
          Iniciar sesión con Microsoft Entra ID
        </button>
      </UnauthenticatedTemplate>

      <AuthenticatedTemplate>
        <div className="card mb-3">
          <div className="card-body">
            <h2 className="h5">Sesión</h2>
            <p><strong>Nombre:</strong> {cuenta?.name}</p>
            <p><strong>Usuario / correo:</strong> {cuenta?.username}</p>
            <p><strong>Claim oid:</strong> {claims.oid}</p>
            <button className="btn btn-outline-danger" onClick={cerrarSesion}>
              Cerrar sesión
            </button>
          </div>
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        {sesionApi && (
          <pre className="bg-light p-3">{JSON.stringify(sesionApi, null, 2)}</pre>
        )}

        {solicitudes && (
          <div className="card">
            <div className="card-body">
              <h2 className="h5">Mis solicitudes</h2>
              <pre className="mb-0">{JSON.stringify(solicitudes, null, 2)}</pre>
            </div>
          </div>
        )}
      </AuthenticatedTemplate>
    </div>
  );
}

export default App;
