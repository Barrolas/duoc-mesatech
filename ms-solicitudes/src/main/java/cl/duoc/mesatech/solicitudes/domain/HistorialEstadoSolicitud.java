package cl.duoc.mesatech.solicitudes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "historial_estado_solicitud")
@Getter
@Setter
public class HistorialEstadoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false, length = 30)
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private EstadoSolicitud estadoNuevo;

    @Column(name = "en_atencion", nullable = false)
    private boolean enAtencion;

    @Column(name = "estado_seguimiento", nullable = false, length = 40)
    private String estadoSeguimiento;

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
