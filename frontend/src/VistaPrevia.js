import { useState } from "react";
import { etiquetaRol, navDesdePermisos, permisosDesdeRoles } from "./roles";
import { validarTransicion } from "./transiciones";
import Layout from "./components/Layout";
import EspacioTrabajo from "./components/EspacioTrabajo";

const USUARIA = "skarlet@mesatech.local";

function datosIniciales() {
  return {
    catalogo: {
      categorias: [{ id: 1, nombre: "Hardware" }, { id: 2, nombre: "Software" }],
      prioridades: [{ id: 1, nombre: "Alta", nivel: 1 }, { id: 2, nombre: "Media", nivel: 2 }],
    },
    solicitudes: [
      {
        id: 1,
        titulo: "Estación de trabajo no enciende",
        descripcion: "Equipo asignado al área de operaciones. Sin indicador luminoso al conectar la corriente.",
        categoria: "Hardware",
        prioridad: "Alta",
        usuarioSolicitante: USUARIA,
        estado: "CREADA",
      },
      {
        id: 2,
        titulo: "Configuración de acceso VPN",
        descripcion: "Requiere conectividad remota para jornada híbrida.",
        categoria: "Software",
        prioridad: "Media",
        usuarioSolicitante: "cliente@mesatech.local",
        estado: "ASIGNADA",
      },
    ],
  };
}

export default function VistaPrevia() {
  const inicial = datosIniciales();
  const [catalogo, setCatalogo] = useState(inicial.catalogo);
  const [solicitudes, setSolicitudes] = useState(inicial.solicitudes);
  const [vista, setVista] = useState("mias");
  const [mensaje, setMensaje] = useState(null);
  const [ocupado, setOcupado] = useState(false);
  const [siguienteId, setSiguienteId] = useState(3);

  const permisos = permisosDesdeRoles([]);
  const mias = solicitudes.filter((item) => item.usuarioSolicitante === USUARIA);

  const avisar = (texto, tipo = "danger") => setMensaje({ texto, tipo });

  const ejecutar = async (accion, ok) => {
    setOcupado(true);
    setMensaje(null);
    try {
      await accion();
      if (ok) {
        avisar(ok, "success");
      }
    } catch (err) {
      avisar(err.message);
    } finally {
      setOcupado(false);
    }
  };

  return (
    <Layout
      usuario={{
        nombre: "Skarlet MesaTech",
        rol: etiquetaRol([]),
      }}
      nav={navDesdePermisos(permisos)}
      vista={vista}
      onVista={setVista}
    >
      <div className="alert alert-warning">
        Entorno de demostración local. La autenticación con Microsoft Entra ID
        se habilitará cuando existan las credenciales institucionales.
      </div>
      <EspacioTrabajo
        aviso="El perfil institucional aún no figura en el token. Se muestran todas las funciones hasta que se asignen los roles."
        mensaje={mensaje}
        vista={vista}
        permisos={permisos}
        catalogo={catalogo}
        mias={mias}
        todas={solicitudes}
        ocupado={ocupado}
        onCrear={(body) =>
          ejecutar(async () => {
            setSolicitudes((actual) => [
              ...actual,
              { ...body, id: siguienteId, usuarioSolicitante: USUARIA, estado: "CREADA" },
            ]);
            setSiguienteId((n) => n + 1);
          }, "La solicitud fue registrada.")
        }
        onCambiarEstado={(id, estado) =>
          ejecutar(async () => {
            const actual = solicitudes.find((item) => item.id === id);
            const rechazo = validarTransicion(actual?.estado, estado);
            if (rechazo) {
              throw new Error(rechazo);
            }
            setSolicitudes((lista) =>
              lista.map((item) => (item.id === id ? { ...item, estado } : item))
            );
          }, `El estado se actualizó a ${estado.replaceAll("_", " ")}.`)
        }
        onCrearCategoria={(body) =>
          ejecutar(async () => {
            setCatalogo((actual) => ({
              ...actual,
              categorias: [...actual.categorias, { id: Date.now(), nombre: body.nombre }],
            }));
          }, "La categoría fue registrada.")
        }
        onCrearPrioridad={(body) =>
          ejecutar(async () => {
            setCatalogo((actual) => ({
              ...actual,
              prioridades: [...actual.prioridades, { id: Date.now(), ...body }],
            }));
          }, "La prioridad fue registrada.")
        }
      />
    </Layout>
  );
}
