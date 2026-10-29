package cl.duoc.mesatech.solicitudes.messaging;

import java.time.Instant;

public record CategoriaCreadaEvent(
        String eventId,
        String tipoEvento,
        Instant fechaEvento,
        Long categoriaId,
        String nombre
) {
    public static final String TIPO = "categoria.creada";
}
