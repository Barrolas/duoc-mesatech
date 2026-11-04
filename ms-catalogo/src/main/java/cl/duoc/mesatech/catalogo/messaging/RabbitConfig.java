package cl.duoc.mesatech.catalogo.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
                "cl.duoc.mesatech.catalogo.messaging"
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
    RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                  MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        template.setReturnsCallback(returned ->
                org.slf4j.LoggerFactory.getLogger(RabbitConfig.class).warn(
                        "Mensaje returned: exchange={} routingKey={} reply={}",
                        returned.getExchange(),
                        returned.getRoutingKey(),
                        returned.getReplyText()
                ));
        return template;
    }
}
