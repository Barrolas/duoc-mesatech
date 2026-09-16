package cl.duoc.mesatech.solicitudes.domain;

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
@Table(name = "solicitudes")
@Getter
@Setter
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descripcion;
    private String categoria;
    private String prioridad;
    private String usuarioSolicitante;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado = EstadoSolicitud.CREADA;

    private Instant fechaCreacion = Instant.now();
}
