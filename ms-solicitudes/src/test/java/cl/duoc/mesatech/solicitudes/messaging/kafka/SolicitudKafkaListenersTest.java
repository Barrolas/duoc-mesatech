package cl.duoc.mesatech.solicitudes.messaging.kafka;

import cl.duoc.mesatech.solicitudes.consumer.SolicitudesConsumer;
import cl.duoc.mesatech.solicitudes.domain.HistorialEstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.SeguimientoSolicitud;
import cl.duoc.mesatech.solicitudes.dto.SolicitudCreadaDTO;
import cl.duoc.mesatech.solicitudes.dto.SolicitudEstadoDTO;
import cl.duoc.mesatech.solicitudes.service.OperacionLogService;
import cl.duoc.mesatech.solicitudes.service.SeguimientoSolicitudService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudKafkaListenersTest {

    @Mock
    private SeguimientoSolicitudService seguimientoService;

    @Mock
    private OperacionLogService operacionLog;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    void confirmaOffsetDespuesDePersistirCreacion() {
        SeguimientoSolicitud guardado = new SeguimientoSolicitud();
        guardado.setTipoAtencion("PREFERENTE");
        when(seguimientoService.registrarCreacion(any(), anyString(), anyInt(), anyLong(), anyString()))
                .thenReturn(Optional.of(guardado));

        SolicitudesConsumer listener = new SolicitudesConsumer(
                seguimientoService, operacionLog, "mesatech-solicitudes-kafka"
        );
        SolicitudCreadaDTO evento = new SolicitudCreadaDTO(
                "a3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f",
                SolicitudCreadaDTO.TIPO,
                Instant.parse("2026-11-24T21:10:00Z"),
                1001L,
                "Incidente",
                "URGENTE",
                5,
                "Infraestructura"
        );

        listener.onSolicitudCreada(evento, "mesatech.solicitud.creada", 0, 1L, "solicitud-1001", acknowledgment);

        InOrder orden = inOrder(seguimientoService, acknowledgment);
        orden.verify(seguimientoService).registrarCreacion(evento, "mesatech.solicitud.creada", 0, 1L, "solicitud-1001");
        orden.verify(acknowledgment).acknowledge();
    }

    @Test
    void confirmaOffsetDespuesDePersistirEstado() {
        HistorialEstadoSolicitud guardado = new HistorialEstadoSolicitud();
        guardado.setEstadoSeguimiento("SALIO_DE_ATENCION");
        when(seguimientoService.registrarCambioEstado(any(), anyString(), anyInt(), anyLong(), anyString()))
                .thenReturn(Optional.of(guardado));

        SolicitudesConsumer listener = new SolicitudesConsumer(
                seguimientoService, operacionLog, "mesatech-solicitudes-kafka"
        );
        SolicitudEstadoDTO evento = new SolicitudEstadoDTO(
                "c4d5e6f7-8901-4abc-def0-123456789abc",
                SolicitudEstadoDTO.TIPO,
                Instant.parse("2026-11-24T21:12:30Z"),
                1001L,
                "EN_PROCESO",
                "RESUELTA"
        );

        listener.onSolicitudEstado(evento, "mesatech.solicitud.estado", 1, 2L, "solicitud-1001", acknowledgment);

        InOrder orden = inOrder(seguimientoService, acknowledgment);
        orden.verify(seguimientoService).registrarCambioEstado(evento, "mesatech.solicitud.estado", 1, 2L, "solicitud-1001");
        orden.verify(acknowledgment).acknowledge();
    }
}
