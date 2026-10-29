package cl.duoc.mesatech.solicitudes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "categorias_locales")
@Getter
@Setter
public class CategoriaLocal {

    @Id
    @Column(name = "categoria_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "fecha_evento", nullable = false)
    private Instant fechaEvento;

    @Column(name = "recibida_en", nullable = false)
    private Instant recibidaEn;
}
