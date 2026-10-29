package cl.duoc.mesatech.solicitudes.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CategoriaCreadaListener {

    private static final Logger log = LoggerFactory.getLogger(CategoriaCreadaListener.class);

    private final CategoriaCopiaService copias;

    public CategoriaCreadaListener(CategoriaCopiaService copias) {
        this.copias = copias;
    }

    @RabbitListener(queues = "${mesatech.rabbitmq.queue}")
    public void onMensaje(
            CategoriaCreadaEvent evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        try {
            var copia = copias.guardar(evento);
            channel.basicAck(deliveryTag, false);
            log.info(
                    "Categoría {} ({}) persistida desde el evento {}",
                    copia.getId(),
                    copia.getNombre(),
                    copia.getEventId()
            );
        } catch (EventoInvalidoException ex) {
            log.warn("Evento de categoría rechazado sin reencolar: {}", ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (RuntimeException ex) {
            log.error("No se pudo persistir la categoría. El mensaje vuelve a la cola.", ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
