"""Texto narrativo del informe EP1 — MesaTech Cloud (negocio + encargo EP1)."""

from __future__ import annotations

GATEWAY_URL = "https://7gqw4633sg.execute-api.us-east-1.amazonaws.com"
SCOPE_API = "api://b2ee2da8-bc3f-4538-a9af-6b8b1b7780b9/access_as_user"


def cap1_contexto() -> dict[str, list[str]]:
    return {
        "contexto": [
            "MesaTech es una empresa de servicios de soporte tecnológico orientada a PYMEs. "
            "Sus clientes reportan incidentes (caída de sistemas, errores de aplicación), "
            "requieren consultas y, en algunos casos, registran sugerencias de mejora. "
            "Históricamente esos pedidos llegaban por correo, teléfono o mensajería instantánea.",
            "Ese modelo generaba problemas operativos concretos: un operador no sabía cuántos "
            "casos tenía abiertos ni en qué etapa estaba cada uno; un cliente debía llamar de "
            "nuevo para preguntar «¿en qué quedó mi ticket?»; la gerencia no contaba con una "
            "vista única de volumen, prioridades ni tiempos de resolución. No existía una "
            "fuente única de verdad del servicio de soporte.",
            "MesaTech Cloud es la respuesta de negocio: un canal web donde cada solicitud "
            "queda registrada con categoría (por ejemplo INCIDENTE, CONSULTA, SUGERENCIA), "
            "prioridad (BAJA, MEDIA, URGENTE), responsable implícito en el flujo de estados "
            "y trazabilidad desde su creación hasta el cierre. La plataforma exige identidad "
            "corporativa (Microsoft Entra ID) y que ningún sistema de terceros consuma las "
            "APIs de negocio sin pasar por el API Gateway de AWS, alineado al encargo EP1.",
        ],
        "desafio": [
            "El encargo EP1 no pide solo «pantallas bonitas»: exige un MVP full stack donde "
            "la arquitectura demuestre autenticación moderna, doble validación de JWT, "
            "autorización por rol, reglas de dominio en backend, versionamiento de API y "
            "persistencia real, desplegado en Amazon EC2.",
            "Para MesaTech, eso se traduce en proteger la confidencialidad de los tickets "
            "(un cliente no puede ver solicitudes ajenas), en permitir que operadores avancen "
            "el ciclo de vida del caso sin saltarse etapas (por ejemplo, no marcar RESUELTA "
            "sin haber atendido en EN_PROCESO) y en que administradores mantengan el catálogo "
            "que clasifica el trabajo del equipo de soporte.",
        ],
        "actores": [
            "Cliente (usuario de la PYME atendida): abre tickets cuando tiene un problema "
            "tecnológico, describe el síntoma y hace seguimiento de sus propios casos. "
            "No debe ver la cola global ni cambiar estados: solo crear y consultar «mis solicitudes».",
            "Operador de soporte MesaTech: atiende la cola. Consulta todas las solicitudes, "
            "filtra por estado, asigna/atención (ASIGNADA → EN_PROCESO) y cierra el ciclo "
            "(RESUELTA → CERRADA) cuando la solución fue entregada al cliente.",
            "Administrador MesaTech: configura categorías y prioridades que el formulario "
            "de alta utiliza, y comparte con operadores la visión global. Es el perfil que "
            "habilita estandarizar cómo se clasifica el trabajo (urgencias vs sugerencias).",
        ],
    }


def cap1_escenarios_uso() -> list[tuple[str, str, str]]:
    """Actor, situación, resultado en MesaTech Cloud."""
    return [
        (
            "Cliente",
            "«No puedo ingresar al sistema de ventas»",
            "Crea solicitud INCIDENTE / URGENTE; queda CREADA y visible solo en su bandeja. "
            "Recibe trazabilidad sin depender del teléfono.",
        ),
        (
            "Operador",
            "Toma el caso de la cola general",
            "Ve la solicitud en GET /v1/solicitudes, avanza estados hasta RESUELTA tras "
            "trabajar en EN_PROCESO; el cliente ve el estado actualizado en «mis solicitudes».",
        ),
        (
            "Administrador",
            "Incorpora categoría «MANTENIMIENTO PROGRAMADO»",
            "Usa CRUD de catálogo vía Gateway; nuevas solicitudes pueden seleccionar la "
            "categoría en el formulario React.",
        ),
    ]


def matriz_funcional_ep1() -> list[tuple[str, str, str, str]]:
    return [
        ("Crear solicitud", "Sí", "Opcional", "Opcional"),
        ("Ver propias solicitudes", "Sí", "Sí", "Sí"),
        ("Ver todas las solicitudes", "No", "Sí", "Sí"),
        ("Cambiar estado (PATCH)", "No", "Sí", "Sí"),
        ("Gestionar catálogo", "No", "No", "Sí"),
    ]


def cap1_propuesta() -> list[str]:
    return [
        "La solución propuesta materializa MesaTech Cloud como pipeline digital del servicio "
        "de soporte: el frontend React es la experiencia del usuario; Microsoft Entra ID "
        "garantiza que quien actúa es un colaborador o cliente conocido; API Gateway concentra "
        "el tráfico público; el BFF aplica la política de acceso del negocio (roles); "
        "ms-solicitudes custodia el ciclo de vida del ticket; ms-catalogo custodia las listas "
        "maestras de clasificación.",
        "Separar catálogo y solicitudes en dos microservicios refleja límites de dominio: "
        "cambiar una prioridad en el catálogo no debe acoplarse a la lógica de transición de "
        "estados. El BFF orquesta las llamadas que la UI necesita (crear ticket + cargar "
        "selectores de categoría/prioridad) sin persistir datos, cumpliendo la restricción EP1.",
        "Despliegue en tres EC2 (datos, microservicios, BFF) en us-east-1: PostgreSQL 16 en "
        "Docker (bases mesatech_solicitudes y mesatech_catalogo), JARs Spring Boot en 8081/8082/8080. "
        "Los Security Groups evidencian que Internet solo llega al canal acordado con Ninna "
        "(Gateway → BFF), no a los MS ni a la base de datos.",
        f"En producción de laboratorio, la variable REACT_APP_API_BASE_URL del frontend apunta "
        f"a {GATEWAY_URL}, de modo que ningún build productivo invoca IPs de EC2 en los puertos "
        "de microservicios.",
    ]


def cap1_dominio() -> list[str]:
    return [
        "Entidad Solicitud: identificador, título, descripción, categoría, prioridad, "
        "usuario solicitante (correo Entra), estado y fecha de creación. Al crear, el MS "
        "fija estado CREADA y asocia preferred_username del JWT — así cada cliente solo "
        "recupera lo suyo en /v1/solicitudes/mias.",
        "Estados y significado operativo: CREADA (ingresada, pendiente de toma); ASIGNADA "
        "(operador responsable); EN_PROCESO (trabajo activo); RESUELTA (solución entregada); "
        "CERRADA (cierre administrativo); CANCELADA (anulación cuando aplica).",
        "Regla EP1: RESUELTA solo desde EN_PROCESO — evita «cerrar en papel» un ticket que "
        "nunca se atendió. CERRADA solo desde RESUELTA — evita archivar un caso no resuelto. "
        "Violaciones responden 409 Conflict en la API; la UI limita las opciones del selector "
        "para guiar al operador en el día a día.",
    ]


def cap2_entra_narrativa() -> list[str]:
    return [
        "En MesaTech, Entra ID cumple dos funciones: autenticar personas (clientes simulados, "
        "operadores y administradores del tenant académico) y emitir access tokens con el scope "
        f"{SCOPE_API} y los app roles que el BFF traduce en permisos de negocio.",
        "Registro SPA (React): Client ID, Tenant, Redirect URI http://localhost:3000 en "
        "desarrollo; login redirect y logout redirect. Registro API (resource): expone el scope "
        "access_as_user; el audience del JWT Authorizer en Gateway y del Resource Server del BFF "
        "coinciden con esta API.",
        "App roles Cliente / Operador / Administrador se asignan a usuarios de demostración. "
        "Así un mismo formulario de login conduce a experiencias distintas: el cliente no ve "
        "la bandeja global; el administrador accede al panel de catálogo. Las capturas del "
        "capítulo documentan registros, URIs, scopes y roles sin incluir secretos.",
    ]


def cap3_gateway_narrativa() -> list[str]:
    return [
        "Desde negocio, Gateway es la «puerta de entrada» al servicio de soporte MesaTech: "
        "todas las operaciones (alta de ticket, listados, cambio de estado, catálogo) "
        "publican bajo el mismo host execute-api, facilitando CORS y políticas de seguridad.",
        "Rutas /v1/solicitudes*, /v1/catalogo*, /v2/solicitudes/mias y /api/usuario integran "
        "por HTTP al BFF. JWT Authorizer valida issuer/Audience de Entra antes de reenviar — "
        "un atacante sin token no alcanza siquiera a preguntar por tickets ajenos.",
        "CORS habilita Authorization y métodos POST/GET/PATCH/PUT/DELETE que usa React. "
        "Las pruebas 401 (sin token, token inválido) demuestran que el perímetro AWS cumple "
        "su rol antes de cargar al equipo de soporte en el BFF.",
    ]


def cap4_infra_narrativa() -> list[str]:
    return [
        "La persistencia elegida es PostgreSQL en EC2 (Docker), justificada para Learner Lab: "
        "costo acotado, control total del motor y cumplimiento EP1 «BD en EC2 o RDS». "
        "Los datos de tickets y catálogo sobreviven reinicios de contenedor y respaldan "
        "la promesa de MesaTech Cloud frente al cliente («su solicitud no se pierde»).",
        "Topología operativa del equipo: EC2 DB (Postgres :5432), EC2 MS (8081/8082), EC2 BFF "
        "(8080). Comunicación MS→DB por IP privada VPC. Evidencias de aislamiento (timeout "
        "a :8081 desde Internet) refuerzan que un externo no puede leer la cola de soporte "
        "saltándose Gateway y roles.",
    ]


def cap5_backend_narrativa() -> list[str]:
    return [
        "ms-solicitudes implementa el corazón operativo: POST crea tickets, GET /mias filtra "
        "por solicitante, GET /v1/solicitudes alimenta la bandeja del operador, PATCH /estado "
        "aplica TransicionesEstado. ms-catalogo sirve GET /v1/catalogo al formulario de alta "
        "y CRUD restringido para mantener categorías/prioridades alineadas al negocio.",
        "El BFF traduce la política MesaTech en código: EntraRoles.esOperadorOAdmin antes de "
        "proxy a listado global o cambio de estado; esAdministrador en mutaciones de catálogo. "
        "Un cliente autenticado pero no autorizado recibe 403 — distinto de 401 (no identificado) "
        "— lo cual refleja «sé quién eres, pero esta acción no es para tu rol en soporte».",
        "Doble validación JWT (Gateway + BFF Resource Server) protege incluso si alguien "
        "configurara mal una ruta en AWS: el BFF sigue exigiendo token válido para ms-solicitudes "
        "y ms-catalogo, que a su vez validan JWT en capa MS para coherencia de identidad.",
    ]


def cap6_react_narrativa() -> list[str]:
    return [
        "La SPA materializa los procesos de MesaTech: pantalla pública con marca corporativa; "
        "tras login, el cliente ve formulario de nueva solicitud (categoría/prioridad desde "
        "catálogo), listado «Mis solicitudes» y detalle de estado sin acceso a la cola global.",
        "Operador/administrador acceden a bandeja general, selector de transiciones (avanzar "
        "flujo / corregir según reglas) y feedback ante errores 403/409. Administrador accede "
        "al panel de catálogo (altas, ediciones, bajas) invisible para clientes.",
        "MSAL gestiona sesión Entra; Axios inyecta Bearer hacia Gateway. La UI muestra nombre "
        "y correo y permite inspeccionar claims — requisito EP1 de comprender identidad más "
        "allá del botón «Iniciar sesión». Logout cierra sesión corporativa al terminar el turno "
        "de soporte en un puesto compartido.",
    ]


def cap7_autorizacion_narrativa() -> list[str]:
    return [
        "La matriz del caso (§4.1 guía EP1) se hace efectiva en BFF + navegación React: "
        "cliente sin enlace a «Todas las solicitudes»; operador sin panel catálogo admin.",
        "Evidencia SKA-T05/T05b: mismo usuario cliente con JWT válido recibe 403 en "
        "GET /v1/solicitudes vía Postman — demuestra que la regla no depende de ocultar botones.",
        "Pruebas SKA-T03/T07/T09 en Postman confirman rechazo de identidad inválida, "
        "rechazo de transición de negocio inválida y lectura de datos persistidos fuera del "
        "frontend, respectivamente.",
    ]


def cap8_versionamiento_narrativa() -> list[str]:
    return [
        "MesaTech prevé integraciones futuras (por ejemplo, un dashboard externo). Mantener "
        "GET /v1/solicitudes/mias como array JSON estable protege al frontend actual.",
        "GET /v2/solicitudes/mias envuelve la misma información con metadatos version, usuario "
        "y colección solicitudes — contrato explícito para consumidores nuevos sin romper v1. "
        "Ambas rutas coexisten en Gateway y BFF, cumpliendo §7.1 y §15 EP1.",
    ]


def cap9_marca_narrativa() -> list[str]:
    return [
        "La identidad visual refuerza confianza del cliente PYME: MesaTech Cloud se presenta "
        "como producto profesional (paleta azul #1e3a5f / acento #0ea5e9, logotipo en header). "
        "Coherencia entre informe, SPA y presentación oral del equipo.",
    ]


def cap10_e2e_pasos() -> list[tuple[str, str]]:
    return [
        (
            "1",
            "Un cliente MesaTech abre la URL de MesaTech Cloud (React).",
        ),
        (
            "2",
            "Sin sesión, solo ve landing e «Iniciar sesión» — no hay datos de tickets.",
        ),
        (
            "3–4",
            "MSAL redirige a Entra ID; tras credenciales corporativas, vuelve con tokens.",
        ),
        (
            "5",
            "React obtiene access token con scope access_as_user para invocar soporte.",
        ),
        (
            "6",
            f"Al crear o listar solicitudes, Axios envía Bearer a {GATEWAY_URL}.",
        ),
        (
            "7",
            "Gateway valida JWT y CORS; rechaza llamadas anónimas (401).",
        ),
        (
            "8–9",
            "BFF revalida token y roles; reenvía a ms-solicitudes o ms-catalogo.",
        ),
        (
            "10",
            "MS persiste o consulta PostgreSQL (ticket con estado CREADA, etc.).",
        ),
        (
            "11–12",
            "JSON regresa a React; el usuario ve su ticket o la cola según rol.",
        ),
    ]


def mapeo_componente_negocio() -> list[tuple[str, str, str]]:
    return [
        ("React + MSAL", "Canal digital MesaTech", "Alta y seguimiento de tickets"),
        ("Entra ID", "Identidad corporativa", "Roles cliente/operador/admin"),
        ("API Gateway", "Puerta pública única", "CORS + JWT en el borde"),
        ("BFF", "Política de acceso", "403 por rol; sin BD"),
        ("ms-solicitudes", "Operación de soporte", "Estados y persistencia tickets"),
        ("ms-catalogo", "Clasificación", "Categorías y prioridades"),
        ("PostgreSQL", "Memoria del servicio", "Datos sobreviven reinicios"),
    ]


def cap11_conclusiones() -> list[str]:
    return [
        "MesaTech Cloud entrega valor de negocio medible para el caso EP1: centraliza "
        "solicitudes de soporte, expone estado trazable a clientes y operadores, y restringe "
        "mantenimiento de catálogo a administradores, sobre una arquitectura cloud-native "
        "con identidad Microsoft y entrada AWS estándar para el curso.",
        "Técnicamente se cumplieron React+MSAL, Entra (SPA+API+roles), HTTP API con JWT "
        "Authorizer, BFF Resource Server sin JPA, dos microservicios persistentes en EC2, "
        "regla RESUELTA←EN_PROCESO verificada con 409, coexistencia v1/v2 y las nueve pruebas §11.",
        "El MVP no reemplaza aún un ITSM completo (SLA, asignación nominativa, notificaciones "
        "por correo), pero sí demuestra el flujo mínimo exigido y deja base de monorepo y "
        "despliegue para evoluciones (CI/CD, RDS, observabilidad) fuera del alcance EP1.",
        "Las evidencias de este informe vinculan configuración cloud y funcionamiento "
        "extremo a extremo con el relato del servicio de soporte MesaTech, conforme a la "
        "guía oficial DSY1107 EP1 y al checklist §15.",
    ]


def anexo_local() -> list[str]:
    return [
        "Desarrollo: infra/docker-compose.yml + run-local-stack.ps1 simulan el flujo MesaTech "
        "en localhost (BFF :8080) antes de publicar JARs en EC2 y apuntar React al Gateway.",
        "Secretos (Entra, Postgres) en .env / ~/mesatech.env — nunca en Git, conforme §12 EP1.",
    ]
