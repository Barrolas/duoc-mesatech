package cl.duoc.mesatech.solicitudes.messaging.kafka;

import cl.duoc.mesatech.solicitudes.messaging.EventoInvalidoException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Collectors;

@EnableKafka
@Configuration
public class KafkaConfig implements KafkaListenerConfigurer {

    private final LocalValidatorFactoryBean validator;

    public KafkaConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(this.validator);
    }

    @Bean
    RecordMessageConverter recordMessageConverter(JsonMapper jsonMapper) {
        return new StringJacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            OperacionLogService operacionLog,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${mesatech.kafka.topics.dlq:mesatech.dlq.eventos}") String dlqTopic
    ) {
        DeadLetterPublishingRecoverer dlpr = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(dlqTopic, record.partition() >= 0 ? record.partition() % 3 : 0)
        );

        ConsumerRecordRecoverer loggingRecoverer = (ConsumerRecord<?, ?> record, Exception ex) -> {
            String eventId = operacionLog.extraerEventId(record.value());
            String key = record.key() != null ? record.key().toString() : null;
            String motivo = resumirMotivo(ex);
            String resultado = esErrorDeContrato(ex)
                    ? "RECHAZADO_VALIDACION_DLT:" + dlqTopic
                    : "ERROR_TRANSITORIO_AGOTADO_DLT:" + dlqTopic;

            operacionLog.registrar(
                    "ERROR",
                    groupId,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    key,
                    eventId,
                    resultado,
                    motivo
            );
            dlpr.accept(record, ex);
        };

        // Reintento acotado (2 reintentos cada 1s) para fallos transitorios de red/RDS.
        DefaultErrorHandler handler = new DefaultErrorHandler(loggingRecoverer, new FixedBackOff(1000L, 2L));
        handler.addNotRetryableExceptions(
                MethodArgumentNotValidException.class,
                MessageConversionException.class,
                JacksonException.class,
                EventoInvalidoException.class
        );
        return handler;
    }

    static boolean esErrorDeContrato(Throwable ex) {
        return buscarCausa(ex, MethodArgumentNotValidException.class) != null
                || buscarCausa(ex, MessageConversionException.class) != null
                || buscarCausa(ex, JacksonException.class) != null
                || buscarCausa(ex, EventoInvalidoException.class) != null;
    }

    static String resumirMotivo(Throwable ex) {
        MethodArgumentNotValidException validationEx = buscarCausa(ex, MethodArgumentNotValidException.class);
        if (validationEx != null && validationEx.getBindingResult() != null) {
            String errores = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(KafkaConfig::formatearFieldError)
                    .collect(Collectors.joining("; "));
            if (!errores.isBlank()) {
                return errores;
            }
            if (!validationEx.getBindingResult().getGlobalErrors().isEmpty()) {
                return validationEx.getBindingResult().getGlobalErrors().get(0).getDefaultMessage();
            }
        }
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }

    private static String formatearFieldError(FieldError err) {
        return err.getField() + ": " + err.getDefaultMessage();
    }

    private static <T extends Throwable> T buscarCausa(Throwable ex, Class<T> target) {
        Throwable cur = ex;
        while (cur != null) {
            if (target.isInstance(cur)) {
                return target.cast(cur);
            }
            cur = cur.getCause();
        }
        return null;
    }
}
