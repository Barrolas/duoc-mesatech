package cl.duoc.mesatech.catalogo.consumer;

import cl.duoc.mesatech.catalogo.dto.PrioridadRenombradaDTO;
import cl.duoc.mesatech.catalogo.service.CatalogoPrioridadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Collectors;

/**
 * Listener Kafka del catálogo (metodología lab: package {@code consumer}, estilo {@code VentaConsumer}).
 */
@Service
public class CatalogoConsumer {

    private static final Logger log = LoggerFactory.getLogger(CatalogoConsumer.class);

    private final CatalogoPrioridadService catalogoPrioridadService;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    public CatalogoConsumer(
            CatalogoPrioridadService catalogoPrioridadService,
            JsonMapper jsonMapper,
            Validator validator
    ) {
        this.catalogoPrioridadService = catalogoPrioridadService;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
    }

    @KafkaListener(
            topics = "${app.kafka.mistopicos.prioridad}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Acknowledgment acknowledgment
    ) {
        PrioridadRenombradaDTO dto;
        try {
            dto = jsonMapper.readValue(message, PrioridadRenombradaDTO.class);
        } catch (Exception ex) {
            log.warn("Payload no es PrioridadRenombradaDTO: {}", ex.getMessage());
            acknowledgment.acknowledge();
            return;
        }

        var violaciones = validator.validate(dto);
        if (!violaciones.isEmpty()) {
            String motivo = violaciones.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            log.warn(
                    "PrioridadRenombradaDTO rechazado (key={}, partición={}, offset={}): {}",
                    key,
                    partition,
                    offset,
                    motivo
            );
            acknowledgment.acknowledge();
            return;
        }

        try {
            var guardada = catalogoPrioridadService.registrarCambioPrioridad(dto, key, partition, offset);
            acknowledgment.acknowledge();
            log.info(
                    "Auditoría prioridad id={} tipoCambio={} | key={} partición={} offset={}",
                    guardada.getId(),
                    guardada.getTipoCambio(),
                    key,
                    partition,
                    offset
            );
        } catch (IllegalArgumentException ex) {
            log.warn("Regla de negocio rechazada: {}", ex.getMessage());
            acknowledgment.acknowledge();
        }
    }
}