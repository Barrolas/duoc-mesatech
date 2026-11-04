package cl.duoc.mesatech.solicitudes.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitConfig {

    @Bean
    MessageConverter jacksonJsonMessageConverter(JsonMapper jsonMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(
                jsonMapper,
                "cl.duoc.mesatech.solicitudes.messaging"
        );
        converter.setAlwaysConvertToInferredType(true);
        converter.setAssumeSupportedContentType(true);
        return converter;
    }

    @Bean
    @ConditionalOnProperty(name = "mesatech.rabbitmq.declare", havingValue = "true", matchIfMissing = true)
    DirectExchange catalogoExchange(@Value("${mesatech.rabbitmq.exchange}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    @ConditionalOnProperty(name = "mesatech.rabbitmq.declare", havingValue = "true", matchIfMissing = true)
    Queue solicitudesCatalogoQueue(@Value("${mesatech.rabbitmq.queue}") String name) {
        return new Queue(name, true);
    }

    @Bean
    @ConditionalOnProperty(name = "mesatech.rabbitmq.declare", havingValue = "true", matchIfMissing = true)
    Binding categoriaCreadaBinding(
            Queue solicitudesCatalogoQueue,
            DirectExchange catalogoExchange,
            @Value("${mesatech.rabbitmq.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(solicitudesCatalogoQueue).to(catalogoExchange).with(routingKey);
    }
}
