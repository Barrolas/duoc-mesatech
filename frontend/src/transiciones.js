/** Flujo EP1 (avance) + correcciones operador (retroceso / reabrir). */
const AVANZAR = {
  CREADA: ["ASIGNADA", "CANCELADA"],
  ASIGNADA: ["EN_PROCESO", "CANCELADA"],
  EN_PROCESO: ["RESUELTA", "CANCELADA"],
  RESUELTA: ["CERRADA"],
  CERRADA: [],
  CANCELADA: [],
};

/** Volver atrás o reabrir ticket (operador / admin). */
const CORREGIR = {
  ASIGNADA: ["CREADA"],
  EN_PROCESO: ["ASIGNADA"],
  RESUELTA: ["EN_PROCESO"],
  CERRADA: ["RESUELTA", "EN_PROCESO"],
  CANCELADA: ["CREADA"],
};

function destinosUnicos(actual, mapa) {
  return mapa[actual] || [];
}

export function opcionesTransicion(estadoActual) {
  const actual = estadoActual || "CREADA";
  const avanzar = destinosUnicos(actual, AVANZAR).map((estado) => ({
    estado,
    tipo: "avanzar",
  }));
  const corregir = destinosUnicos(actual, CORREGIR).map((estado) => ({
    estado,
    tipo: "corregir",
  }));
  return { avanzar, corregir, hayAlguna: avanzar.length + corregir.length > 0 };
}

export function esTransicionValida(actual, nuevo) {
  const a = actual || "CREADA";
  const av = AVANZAR[a] || [];
  const co = CORREGIR[a] || [];
  return av.includes(nuevo) || co.includes(nuevo);
}

export function validarTransicion(actual, nuevo) {
  if (esTransicionValida(actual, nuevo)) {
    return null;
  }
  if (nuevo === "RESUELTA") {
    return "Una solicitud no puede pasar a RESUELTA si no está EN_PROCESO";
  }
  if (nuevo === "CERRADA" && actual !== "RESUELTA") {
    return "Solo se puede cerrar una solicitud que esté RESUELTA";
  }
  return `No se puede pasar de ${actual.replaceAll("_", " ")} a ${nuevo.replaceAll("_", " ")}`;
}

/** @deprecated use opcionesTransicion */
export function estadosDestinoPermitidos(estadoActual) {
  const { avanzar, corregir } = opcionesTransicion(estadoActual);
  return [...avanzar, ...corregir].map((o) => o.estado);
}
