package cl.duoc.mesatech.solicitudes.service;

import cl.duoc.mesatech.solicitudes.domain.EstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.HistorialEstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.SeguimientoSolicitud;
import cl.duoc.mesatech.solicitudes.dto.SolicitudCreadaDTO;
import cl.duoc.mesatech.solicitudes.dto.SolicitudEstadoDTO;
import cl.duoc.mesatech.solicitudes.messaging.EventoInvalidoException;
import cl.duoc.mesatech.solicitudes.repo.HistorialEstadoSolicitudRepository;
import cl.duoc.mesatech.solicitudes.repo.SeguimientoSolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class SeguimientoSolicitudService {

    public static final String ATENCION_PREFERENTE = "PREFERENTE";
    public static final String ATENCION_NORMAL = "NORMAL";
    public static final String SEGUIMIENTO_SALIO = "SALIO_DE_ATENCION";
    public static final String SEGUIMIENTO_CONTINUA = "CONTINUA_EN_SEGUIMIENTO";

    private final SeguimientoSolicitudRepository seguimientoRepo;
    private final HistorialEstadoSolicitudRepository historialRepo;

    public SeguimientoSolicitudService(
            SeguimientoSolicitudRepository seguimientoRepo,
            HistorialEstadoSolicitudRepository historialRepo
    ) {
        this.seguimientoRepo = seguimientoRepo;
        this.historialRepo = historialRepo;
    }

    @Transactional
    public Optional<SeguimientoSolicitud> registrarCreacion(
            SolicitudCreadaDTO evento,
            String topico,
            int particion,
            long offset,
            String key
    ) {
        if (evento == null) {
            throw new EventoInvalidoException("El evento de solicitud creada viene nulo");
        }
        if (seguimientoRepo.existsByEventId(evento.eventId())) {
            return Optional.empty();
        }

        String tipoAtencion = evento.prioridadNivel() >= 4 ? ATENCION_PREFERENTE : ATENCION_NORMAL;

        SeguimientoSolicitud registro = new SeguimientoSolicitud();
        registro.setEventId(evento.eventId());
        registro.setSolicitudId(evento.solicitudId());
        registro.setTitulo(evento.titulo().trim());
        registro.setPrioridadNombre(evento.prioridadNombre().trim());
        registro.setPrioridadNivel(evento.prioridadNivel());
        registro.setCategoriaNombre(
                evento.categoriaNombre() != null && !evento.categoriaNombre().isBlank()
                        ? evento.categoriaNombre().trim()
                        : null
        );
        registro.setTipoAtencion(tipoAtencion);
        registro.setTopico(topico);
        registro.setParticion(particion);
        registro.setOffsetKafka(offset);
        registro.setEventoKey(key);
        registro.setFechaEvento(evento.fechaEvento());
        registro.setProcesadoEn(Instant.now());

        return Optional.of(seguimientoRepo.save(registro));
    }

    @Transactional
    public Optional<HistorialEstadoSolicitud> registrarCambioEstado(
            SolicitudEstadoDTO evento,
            String topico,
            int particion,
            long offset,
            String key
    ) {
        if (evento == null) {
            throw new EventoInvalidoException("El evento de cambio de estado viene nulo");
        }
        if (historialRepo.existsByEventId(evento.eventId())) {
            return Optional.empty();
        }

        EstadoSolicitud anterior = EstadoSolicitud.valueOf(evento.estadoAnterior());
        EstadoSolicitud nuevo = EstadoSolicitud.valueOf(evento.estadoNuevo());
        if (anterior == nuevo) {
            throw new EventoInvalidoException("estadoNuevo debe ser distinto de estadoAnterior");
        }

        boolean enAtencion = nuevo != EstadoSolicitud.RESUELTA && nuevo != EstadoSolicitud.CERRADA;
        String estadoSeguimiento = enAtencion ? SEGUIMIENTO_CONTINUA : SEGUIMIENTO_SALIO;

        HistorialEstadoSolicitud historial = new HistorialEstadoSolicitud();
        historial.setEventId(evento.eventId());
        historial.setSolicitudId(evento.solicitudId());
        historial.setEstadoAnterior(anterior);
        historial.setEstadoNuevo(nuevo);
        historial.setEnAtencion(enAtencion);
        historial.setEstadoSeguimiento(estadoSeguimiento);
        historial.setTopico(topico);
        historial.setParticion(particion);
        historial.setOffsetKafka(offset);
        historial.setEventoKey(key);
        historial.setFechaEvento(evento.fechaEvento());
        historial.setProcesadoEn(Instant.now());

        return Optional.of(historialRepo.save(historial));
    }
}
