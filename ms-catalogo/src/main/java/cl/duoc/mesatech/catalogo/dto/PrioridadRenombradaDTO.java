package cl.duoc.mesatech.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * DTO Kafka — contrato Giannina §2.3 · tópico {@code mesatech.catalogo.prioridad}.
 * Record (mismo estilo que {@code CategoriaCreadaEvent} en Rabbit EP2).
 */
public record PrioridadRenombradaDTO(
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
        String eventId,
        @NotBlank String tipoEvento,
        @NotNull Instant fechaEvento,
        @NotNull @Positive Long prioridadId,
        @NotBlank @Size(max = 80) String nombreAnterior,
        @NotBlank @Size(max = 80) String nombreNuevo,
        @Positive Integer nivel
) {
    public static final String TIPO = "catalogo.prioridad.renombrada";
}