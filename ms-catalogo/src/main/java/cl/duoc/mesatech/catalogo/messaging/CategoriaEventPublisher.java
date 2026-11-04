package cl.duoc.mesatech.catalogo.messaging;

import cl.duoc.mesatech.catalogo.domain.Categoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class CategoriaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CategoriaEventPublisher.class);
    private static final long CONFIRM_TIMEOUT_MS = 8_000;

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public CategoriaEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${mesatech.rabbitmq.exchange}") String exchange,
            @Value("${mesatech.rabbitmq.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publicarCategoriaCreada(Categoria categoria) {
        CategoriaCreadaEvent evento = new CategoriaCreadaEvent(
                UUID.randomUUID().toString(),
                CategoriaCreadaEvent.TIPO,
                Instant.now(),
                categoria.getId(),
                categoria.getNombre()
        );

        CorrelationData correlation = new CorrelationData(evento.eventId());
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, evento, message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            }, correlation);

            var confirm = correlation.getFuture().get(CONFIRM_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (confirm == null || !confirm.isAck()) {
                throw new PublicacionEventoException(
                        "El broker no confirmó la publicación (nack o sin ack). "
                                + "La categoría puede estar guardada en RDS sin evento."
                );
            }
            log.info(
                    "Evento {} publicado (categoriaId={}, exchange={}, routingKey={})",
                    evento.eventId(),
                    evento.categoriaId(),
                    exchange,
                    routingKey
            );
        } catch (PublicacionEventoException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new PublicacionEventoException(
                    "Timeout esperando publisher confirm del broker. "
                            + "La categoría puede estar guardada en RDS sin evento.",
                    ex
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PublicacionEventoException("Publicación interrumpida", ex);
        } catch (Exception ex) {
            throw new PublicacionEventoException(
                    "No se pudo publicar el evento categoria.creada: " + ex.getMessage(),
                    ex
            );
        }
    }
}
