package cl.duoc.mesatech.solicitudes.repo;

import cl.duoc.mesatech.solicitudes.domain.SeguimientoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoSolicitudRepository extends JpaRepository<SeguimientoSolicitud, Long> {

    boolean existsByEventId(String eventId);

    List<SeguimientoSolicitud> findBySolicitudIdOrderByProcesadoEnDesc(Long solicitudId);
}
