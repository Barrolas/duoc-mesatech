package cl.duoc.mesatech.solicitudes.messaging;

import cl.duoc.mesatech.solicitudes.domain.CategoriaLocal;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaCreadaListenerTest {

    @Mock
    private CategoriaCopiaService copias;

    @Mock
    private Channel channel;

    private CategoriaCreadaListener listener;

    @BeforeEach
    void setUp() {
        listener = new CategoriaCreadaListener(copias);
    }

    @Test
    void confirmaElMensajeDespuesDePersistir() throws IOException {
        CategoriaLocal copia = new CategoriaLocal();
        copia.setId(7L);
        copia.setNombre("Hardware");
        copia.setEventId("evt-1");
        when(copias.guardar(any())).thenReturn(copia);

        listener.onMensaje(evento(), channel, 11L);

        InOrder orden = inOrder(copias, channel);
        orden.verify(copias).guardar(any());
        orden.verify(channel).basicAck(11L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void descartaEventoInvalidoSinReencolar() throws IOException {
        doThrow(new EventoInvalidoException("Falta categoriaId")).when(copias).guardar(any());

        listener.onMensaje(evento(), channel, 11L);

        verify(channel).basicNack(11L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void reencolaSiFallaLaPersistencia() throws IOException {
        doThrow(new RuntimeException("rds")).when(copias).guardar(any());

        listener.onMensaje(evento(), channel, 11L);

        verify(channel).basicNack(11L, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    private CategoriaCreadaEvent evento() {
        return new CategoriaCreadaEvent(
                "evt-1",
                CategoriaCreadaEvent.TIPO,
                Instant.parse("2026-10-29T15:00:00Z"),
                7L,
                "Hardware"
        );
    }
}
