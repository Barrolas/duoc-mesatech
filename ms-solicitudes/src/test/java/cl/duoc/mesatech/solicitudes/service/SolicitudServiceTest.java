package cl.duoc.mesatech.solicitudes.service;

import cl.duoc.mesatech.solicitudes.domain.EstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.Solicitud;
import cl.duoc.mesatech.solicitudes.messaging.kafka.SolicitudKafkaProducer;
import cl.duoc.mesatech.solicitudes.repo.CategoriaLocalRepository;
import cl.duoc.mesatech.solicitudes.repo.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository repository;

    @Mock
    private CategoriaLocalRepository categoriasLocales;

    @Mock
    private SolicitudKafkaProducer kafkaProducer;

    @InjectMocks
    private SolicitudService service;

    @Test
    void creaSolicitudResuelveNivelYPublicaEvento() {
        when(categoriasLocales.existsByNombreIgnoreCase("Redes")).thenReturn(true);
        when(repository.save(any(Solicitud.class))).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            s.setId(55L);
            return s;
        });

        Solicitud input = new Solicitud();
        input.setTitulo("Sin acceso VPN");
        input.setDescripcion("Error al autenticar");
        input.setCategoria("Redes");
        input.setPrioridad("Alta");

        Solicitud creada = service.crear("sk.tropan@duocuc.cl", input);

        assertEquals(55L, creada.getId());
        assertEquals(4, creada.getPrioridadNivel());
        verify(kafkaProducer).publicarSolicitudCreada(creada, "Alta", 4);
    }

    @Test
    void cambiaEstadoYPublicaAnteriorYNuevo() {
        Solicitud existente = new Solicitud();
        existente.setId(55L);
        existente.setEstado(EstadoSolicitud.CREADA);
        when(repository.findById(55L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud actualizada = service.cambiarEstado(55L, "asignada");

        assertEquals(EstadoSolicitud.ASIGNADA, actualizada.getEstado());
        verify(kafkaProducer).publicarCambioEstado(55L, EstadoSolicitud.CREADA, EstadoSolicitud.ASIGNADA);
    }
}
