package cl.duoc.mesatech.solicitudes.domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Flujo EP1 + correcciones operador (retroceso / reabrir). La regla de negocio
 * vive en el microservicio, no en el token.
 */
public final class TransicionesEstado {

    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> AVANZAR = Map.of(
            EstadoSolicitud.CREADA, Set.of(EstadoSolicitud.ASIGNADA, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.ASIGNADA, Set.of(EstadoSolicitud.EN_PROCESO, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.EN_PROCESO, Set.of(EstadoSolicitud.RESUELTA, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.RESUELTA, Set.of(EstadoSolicitud.CERRADA),
            EstadoSolicitud.CERRADA, Set.of(),
            EstadoSolicitud.CANCELADA, Set.of()
    );

    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> CORREGIR = Map.of(
            EstadoSolicitud.ASIGNADA, Set.of(EstadoSolicitud.CREADA),
            EstadoSolicitud.EN_PROCESO, Set.of(EstadoSolicitud.ASIGNADA),
            EstadoSolicitud.RESUELTA, Set.of(EstadoSolicitud.EN_PROCESO),
            EstadoSolicitud.CERRADA, Set.of(EstadoSolicitud.RESUELTA, EstadoSolicitud.EN_PROCESO),
            EstadoSolicitud.CANCELADA, Set.of(EstadoSolicitud.CREADA)
    );

    private TransicionesEstado() {
    }

    public static boolean esPermitida(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (actual == null || nuevo == null) {
            return false;
        }
        Set<EstadoSolicitud> destinos = new HashSet<>(AVANZAR.getOrDefault(actual, Set.of()));
        destinos.addAll(CORREGIR.getOrDefault(actual, Set.of()));
        return destinos.contains(nuevo);
    }

    public static String mensajeRechazo(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (nuevo == EstadoSolicitud.RESUELTA && actual != EstadoSolicitud.EN_PROCESO) {
            return "Una solicitud no puede pasar a RESUELTA si no está EN_PROCESO";
        }
        if (nuevo == EstadoSolicitud.CERRADA && actual != EstadoSolicitud.RESUELTA) {
            return "Solo se puede cerrar una solicitud que esté RESUELTA";
        }
        return "No se puede pasar de " + actual + " a " + nuevo;
    }
}
