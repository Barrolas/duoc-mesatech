package cl.duoc.mesatech.solicitudes.messaging.kafka;

import cl.duoc.mesatech.solicitudes.domain.EstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.HistorialEstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.SeguimientoSolicitud;
import cl.duoc.mesatech.solicitudes.messaging.EventoInvalidoException;
import cl.duoc.mesatech.solicitudes.repo.HistorialEstadoSolicitudRepository;
import cl.duoc.mesatech.solicitudes.repo.SeguimientoSolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeguimientoSolicitudServiceTest {

    @Mock
    private SeguimientoSolicitudRepository seguimientoRepo;

    @Mock
    private HistorialEstadoSolicitudRepository historialRepo;

    @InjectMocks
    private SeguimientoSolicitudService service;

    @Test
    void marcaPreferenteCuandoPrioridadNivelEsCuatroOMas() {
        when(seguimientoRepo.existsByEventId("a3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f")).thenReturn(false);
        when(seguimientoRepo.save(any(SeguimientoSolicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudCreadaEvent evento = new SolicitudCreadaEvent(
                "a3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f",
                SolicitudCreadaEvent.TIPO,
                Instant.parse("2026-11-24T21:10:00Z"),
                1001L,
                "Incidente red sucursal Oriente",
                "URGENTE",
                5,
                "Infraestructura"
        );

        SeguimientoSolicitud guardado = service.registrarCreacion(
                evento, "mesatech.solicitud.creada", 1, 12L, "solicitud-1001"
        ).orElseThrow();

        assertEquals(SeguimientoSolicitudService.ATENCION_PREFERENTE, guardado.getTipoAtencion());
        assertEquals(1, guardado.getParticion());
        assertEquals(12L, guardado.getOffsetKafka());
    }

    @Test
    void marcaNormalCuandoPrioridadNivelEsTresOMenos() {
        when(seguimientoRepo.existsByEventId("b3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f")).thenReturn(false);
        when(seguimientoRepo.save(any(SeguimientoSolicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudCreadaEvent evento = new SolicitudCreadaEvent(
                "b3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f",
                SolicitudCreadaEvent.TIPO,
                Instant.parse("2026-11-24T21:11:00Z"),
                1002L,
                "Consulta menor",
                "Media",
                3,
                "Soporte"
        );

        SeguimientoSolicitud guardado = service.registrarCreacion(
                evento, "mesatech.solicitud.creada", 0, 5L, "solicitud-1002"
        ).orElseThrow();

        assertEquals(SeguimientoSolicitudService.ATENCION_NORMAL, guardado.getTipoAtencion());
    }

    @Test
    void ignoraCreacionDuplicadaPorEventId() {
        when(seguimientoRepo.existsByEventId("a3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f")).thenReturn(true);

        SolicitudCreadaEvent evento = new SolicitudCreadaEvent(
                "a3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f",
                SolicitudCreadaEvent.TIPO,
                Instant.parse("2026-11-24T21:10:00Z"),
                1001L,
                "Incidente",
                "URGENTE",
                5,
                null
        );

        Optional<SeguimientoSolicitud> res = service.registrarCreacion(
                evento, "mesatech.solicitud.creada", 1, 12L, "solicitud-1001"
        );

        assertTrue(res.isEmpty());
        verify(seguimientoRepo, never()).save(any());
    }

    @Test
    void marcaSalioDeAtencionCuandoPasaAResueltaOCerrada() {
        when(historialRepo.existsByEventId("c4d5e6f7-8901-4abc-def0-123456789abc")).thenReturn(false);
        when(historialRepo.save(any(HistorialEstadoSolicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudEstadoEvent evento = new SolicitudEstadoEvent(
                "c4d5e6f7-8901-4abc-def0-123456789abc",
                SolicitudEstadoEvent.TIPO,
                Instant.parse("2026-11-24T21:12:30Z"),
                1001L,
                "EN_PROCESO",
                "RESUELTA"
        );

        HistorialEstadoSolicitud guardado = service.registrarCambioEstado(
                evento, "mesatech.solicitud.estado", 2, 7L, "solicitud-1001"
        ).orElseThrow();

        assertFalse(guardado.isEnAtencion());
        assertEquals(SeguimientoSolicitudService.SEGUIMIENTO_SALIO, guardado.getEstadoSeguimiento());
        assertEquals(EstadoSolicitud.EN_PROCESO, guardado.getEstadoAnterior());
        assertEquals(EstadoSolicitud.RESUELTA, guardado.getEstadoNuevo());
    }

    @Test
    void marcaContinuaEnSeguimientoParaOtrosEstadosValidos() {
        when(historialRepo.existsByEventId("d4d5e6f7-8901-4abc-def0-123456789abc")).thenReturn(false);
        when(historialRepo.save(any(HistorialEstadoSolicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudEstadoEvent evento = new SolicitudEstadoEvent(
                "d4d5e6f7-8901-4abc-def0-123456789abc",
                SolicitudEstadoEvent.TIPO,
                Instant.parse("2026-11-24T21:12:00Z"),
                1001L,
                "CREADA",
                "ASIGNADA"
        );

        HistorialEstadoSolicitud guardado = service.registrarCambioEstado(
                evento, "mesatech.solicitud.estado", 2, 6L, "solicitud-1001"
        ).orElseThrow();

        assertTrue(guardado.isEnAtencion());
        assertEquals(SeguimientoSolicitudService.SEGUIMIENTO_CONTINUA, guardado.getEstadoSeguimiento());
    }

    @Test
    void rechazaCambioConEstadoIgualSinGuardar() {
        when(historialRepo.existsByEventId("e4d5e6f7-8901-4abc-def0-123456789abc")).thenReturn(false);

        SolicitudEstadoEvent evento = new SolicitudEstadoEvent(
                "e4d5e6f7-8901-4abc-def0-123456789abc",
                SolicitudEstadoEvent.TIPO,
                Instant.parse("2026-11-24T21:12:00Z"),
                1001L,
                "ASIGNADA",
                "ASIGNADA"
        );

        assertThrows(EventoInvalidoException.class, () -> service.registrarCambioEstado(
                evento, "mesatech.solicitud.estado", 2, 6L, "solicitud-1001"
        ));
        verify(historialRepo, never()).save(any());
    }
}
