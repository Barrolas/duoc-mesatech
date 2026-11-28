package cl.duoc.mesatech.solicitudes.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * DTO Kafka — contrato Giannina §2.2 · tópico {@code mesatech.solicitud.estado}.
 */
public record SolicitudEstadoDTO(
        @NotBlank(message = "es obligatorio")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "debe ser un UUID válido"
        )
        String eventId,

        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = "^solicitud\\.estado$", message = "debe ser solicitud.estado")
        String tipoEvento,

        @NotNull(message = "es obligatorio en formato ISO-8601 UTC")
        Instant fechaEvento,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Long solicitudId,

        @NotBlank(message = "es obligatorio")
        @Pattern(
                regexp = "^(CREADA|ASIGNADA|EN_PROCESO|RESUELTA|CERRADA|CANCELADA)$",
                message = "debe pertenecer a EstadoSolicitud"
        )
        String estadoAnterior,

        @NotBlank(message = "es obligatorio")
        @Pattern(
                regexp = "^(CREADA|ASIGNADA|EN_PROCESO|RESUELTA|CERRADA|CANCELADA)$",
                message = "debe pertenecer a EstadoSolicitud"
        )
        String estadoNuevo
) {
    public static final String TIPO = "solicitud.estado";

    @AssertTrue(message = "estadoNuevo debe ser distinto de estadoAnterior")
    public boolean isCambioValido() {
        if (estadoAnterior == null || estadoNuevo == null) {
            return true;
        }
        return !estadoAnterior.equals(estadoNuevo);
    }
}
