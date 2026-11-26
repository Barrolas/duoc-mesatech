package cl.duoc.mesatech.solicitudes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * DTO Kafka — contrato Giannina §2.1 · tópico {@code mesatech.solicitud.creada}.
 */
public record SolicitudCreadaDTO(
        @NotBlank(message = "es obligatorio")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "debe ser un UUID válido"
        )
        String eventId,

        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = "^solicitud\\.creada$", message = "debe ser solicitud.creada")
        String tipoEvento,

        @NotNull(message = "es obligatorio en formato ISO-8601 UTC")
        Instant fechaEvento,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Long solicitudId,

        @NotBlank(message = "es obligatorio")
        @Size(max = 120, message = "máximo 120 caracteres")
        String titulo,

        @NotBlank(message = "es obligatorio")
        @Size(max = 50, message = "máximo 50 caracteres")
        String prioridadNombre,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe estar entre 1 y 5")
        @Max(value = 5, message = "debe estar entre 1 y 5")
        Integer prioridadNivel,

        @Size(max = 80, message = "máximo 80 caracteres")
        String categoriaNombre
) {
    public static final String TIPO = "solicitud.creada";
}
