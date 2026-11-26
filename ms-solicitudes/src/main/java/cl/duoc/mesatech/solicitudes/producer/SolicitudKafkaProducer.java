package cl.duoc.mesatech.solicitudes.producer;

import cl.duoc.mesatech.solicitudes.domain.EstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.Solicitud;
import cl.duoc.mesatech.solicitudes.dto.SolicitudCreadaDTO;
import cl.duoc.mesatech.solicitudes.dto.SolicitudEstadoDTO;
import cl.duoc.mesatech.solicitudes.service.OperacionLogService;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Component
public class SolicitudKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(SolicitudKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final OperacionLogService operacionLog;
    private final String topicCreada;
    private final String topicEstado;
    private final String groupId;

    public SolicitudKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            OperacionLogService operacionLog,
            @Value("${app.kafka.mistopicos.solicitud-creada}") String topicCreada,
            @Value("${app.kafka.mistopicos.solicitud-estado}") String topicEstado,
            @Value("${spring.kafka.consumer.group-id}") String groupId
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.operacionLog = operacionLog;
        this.topicCreada = topicCreada;
        this.topicEstado = topicEstado;
        this.groupId = groupId;
    }

    public String publicarSolicitudCreada(Solicitud solicitud, String prioridadNombre, int prioridadNivel) {
        String eventId = UUID.randomUUID().toString();
        String key = "solicitud-" + solicitud.getId();
        SolicitudCreadaDTO evento = new SolicitudCreadaDTO(
                eventId,
                SolicitudCreadaDTO.TIPO,
                Instant.now(),
                solicitud.getId(),
                solicitud.getTitulo(),
                prioridadNombre,
                prioridadNivel,
                solicitud.getCategoria()
        );
        enviar(topicCreada, key, eventId, evento);
        return eventId;
    }

    public String publicarCambioEstado(Long solicitudId, EstadoSolicitud estadoAnterior, EstadoSolicitud estadoNuevo) {
        String eventId = UUID.randomUUID().toString();
        String key = "solicitud-" + solicitudId;
        SolicitudEstadoDTO evento = new SolicitudEstadoDTO(
                eventId,
                SolicitudEstadoDTO.TIPO,
                Instant.now(),
                solicitudId,
                estadoAnterior.name(),
                estadoNuevo.name()
        );
        enviar(topicEstado, key, eventId, evento);
        return eventId;
    }

    private void enviar(String topic, String key, String eventId, Object evento) {
        try {
            String json = jsonMapper.writeValueAsString(evento);
            kafkaTemplate.send(topic, key, json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Error enviando evento {} a {} key={}: {}", eventId, topic, key, ex.getMessage());
                    operacionLog.registrar(
                            "ERROR",
                            groupId,
                            topic,
                            null,
                            null,
                            key,
                            eventId,
                            "PRODUCTOR_FALLO_ENVIO",
                            ex.getMessage()
                    );
                    return;
                }
                RecordMetadata meta = result != null ? result.getRecordMetadata() : null;
                Integer partition = meta != null ? meta.partition() : null;
                Long offset = meta != null ? meta.offset() : null;
                operacionLog.registrar(
                        "INFO",
                        groupId,
                        topic,
                        partition,
                        offset,
                        key,
                        eventId,
                        "PRODUCTOR_ENVIADO_ACK",
                        null
                );
            });
        } catch (Exception ex) {
            log.error("No se pudo serializar o encolar evento {} para {}: {}", eventId, topic, ex.getMessage());
            operacionLog.registrar(
                    "ERROR",
                    groupId,
                    topic,
                    null,
                    null,
                    key,
                    eventId,
                    "PRODUCTOR_ERROR_SERIALIZACION",
                    ex.getMessage()
            );
        }
    }
}
