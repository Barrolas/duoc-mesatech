package cl.duoc.mesatech.solicitudes.repo;

import cl.duoc.mesatech.solicitudes.domain.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByUsuarioSolicitante(String usuarioSolicitante);
}
