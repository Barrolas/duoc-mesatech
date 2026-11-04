package cl.duoc.mesatech.solicitudes.messaging;

import cl.duoc.mesatech.solicitudes.domain.CategoriaLocal;
import cl.duoc.mesatech.solicitudes.repo.CategoriaLocalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaCopiaServiceTest {

    @Mock
    private CategoriaLocalRepository repository;

    @InjectMocks
    private CategoriaCopiaService service;

    @Test
    void conservaCategoriaIdAlGuardarLaCopia() {
        when(repository.findById(7L)).thenReturn(Optional.empty());
        when(repository.save(any(CategoriaLocal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaLocal copia = service.guardar(evento(7L, "Hardware"));

        assertEquals(7L, copia.getId());
        assertEquals("Hardware", copia.getNombre());
        assertEquals("evt-1", copia.getEventId());
    }

    @Test
    void reentregaActualizaLaMismaCategoria() {
        CategoriaLocal existente = new CategoriaLocal();
        existente.setId(7L);
        existente.setNombre("Viejo");
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(CategoriaLocal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaLocal copia = service.guardar(new CategoriaCreadaEvent(
                "evt-2",
                CategoriaCreadaEvent.TIPO,
                Instant.parse("2026-10-29T15:00:00Z"),
                7L,
                "Hardware nuevo"
        ));

        assertEquals(7L, copia.getId());
        assertEquals("Hardware nuevo", copia.getNombre());
        assertEquals("evt-2", copia.getEventId());
        verify(repository).save(existente);
    }

    @Test
    void rechazaEventoIncompletoSinPersistir() {
        assertThrows(EventoInvalidoException.class, () -> service.guardar(new CategoriaCreadaEvent(
                "evt-1",
                CategoriaCreadaEvent.TIPO,
                Instant.parse("2026-10-29T15:00:00Z"),
                null,
                "Hardware"
        )));
        verify(repository, never()).save(any());
    }

    private CategoriaCreadaEvent evento(Long categoriaId, String nombre) {
        return new CategoriaCreadaEvent(
                "evt-1",
                CategoriaCreadaEvent.TIPO,
                Instant.parse("2026-10-29T15:00:00Z"),
                categoriaId,
                nombre
        );
    }
}
