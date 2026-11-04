package cl.duoc.mesatech.catalogo.messaging;

import java.time.Instant;

/** Mismo contrato JSON que {@code ms-solicitudes} · {@code CategoriaCreadaEvent}. */
public record CategoriaCreadaEvent(
        String eventId,
        String tipoEvento,
        Instant fechaEvento,
        Long categoriaId,
        String nombre
) {
    public static final String TIPO = "categoria.creada";
}
