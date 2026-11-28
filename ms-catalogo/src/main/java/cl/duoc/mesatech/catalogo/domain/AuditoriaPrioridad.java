package cl.duoc.mesatech.catalogo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "auditoria_prioridad")
@Getter
@Setter
public class AuditoriaPrioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long prioridadId;
    private String nombreAnterior;
    private String nombreNuevo;
    private String tipoCambio;
    private String eventId;
    private Instant fechaRegistro = Instant.now();
}