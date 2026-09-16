const PERMITIDAS = {
  CREADA: ["ASIGNADA", "CANCELADA"],
  ASIGNADA: ["EN_PROCESO", "CANCELADA"],
  EN_PROCESO: ["RESUELTA", "CANCELADA"],
  RESUELTA: ["CERRADA"],
  CERRADA: [],
  CANCELADA: [],
};

export function validarTransicion(actual, nuevo) {
  if (!(PERMITIDAS[actual] || []).includes(nuevo)) {
    if (nuevo === "RESUELTA") {
      return "Una solicitud no puede pasar a RESUELTA si no está EN_PROCESO";
    }
    return `No se puede pasar de ${actual} a ${nuevo}`;
  }
  return null;
}
