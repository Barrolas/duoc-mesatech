package cl.duoc.mesatech.solicitudes.repo;

import cl.duoc.mesatech.solicitudes.domain.HistorialEstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoSolicitudRepository extends JpaRepository<HistorialEstadoSolicitud, Long> {

    boolean existsByEventId(String eventId);

    List<HistorialEstadoSolicitud> findBySolicitudIdOrderByProcesadoEnDesc(Long solicitudId);
}
