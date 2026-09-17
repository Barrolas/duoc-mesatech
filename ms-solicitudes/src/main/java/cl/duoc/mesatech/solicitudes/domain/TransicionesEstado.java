package cl.duoc.mesatech.solicitudes.domain;

import java.util.Map;
import java.util.Set;

/**
 * Transiciones válidas del MVP. Independiente de Entra: la regla de negocio
 * vive en el microservicio, no en el token.
 */
public final class TransicionesEstado {

    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> PERMITIDAS = Map.of(
            EstadoSolicitud.CREADA, Set.of(EstadoSolicitud.ASIGNADA, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.ASIGNADA, Set.of(EstadoSolicitud.EN_PROCESO, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.EN_PROCESO, Set.of(EstadoSolicitud.RESUELTA, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.RESUELTA, Set.of(EstadoSolicitud.CERRADA),
            EstadoSolicitud.CERRADA, Set.of(),
            EstadoSolicitud.CANCELADA, Set.of()
    );

    private TransicionesEstado() {
    }

    public static boolean esPermitida(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (actual == null || nuevo == null) {
            return false;
        }
        return PERMITIDAS.getOrDefault(actual, Set.of()).contains(nuevo);
    }

    public static String mensajeRechazo(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (nuevo == EstadoSolicitud.RESUELTA) {
            return "Una solicitud no puede pasar a RESUELTA si no está EN_PROCESO";
        }
        return "No se puede pasar de " + actual + " a " + nuevo;
    }
}
