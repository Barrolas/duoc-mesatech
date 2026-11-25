package cl.duoc.mesatech.solicitudes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "seguimiento_solicitud")
@Getter
@Setter
public class SeguimientoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(name = "prioridad_nombre", nullable = false, length = 50)
    private String prioridadNombre;

    @Column(name = "prioridad_nivel", nullable = false)
    private Integer prioridadNivel;

    @Column(name = "categoria_nombre", length = 80)
    private String categoriaNombre;

    @Column(name = "tipo_atencion", nullable = false, length = 30)
    private String tipoAtencion;

    @Column(nullable = false, length = 80)
    private String topico;

    @Column(nullable = false)
    private Integer particion;

    @Column(name = "offset_kafka", nullable = false)
    private Long offsetKafka;

    @Column(name = "evento_key", length = 64)
    private String eventoKey;

    @Column(name = "fecha_evento", nullable = false)
    private Instant fechaEvento;

    @Column(name = "procesado_en", nullable = false)
    private Instant procesadoEn = Instant.now();
}
