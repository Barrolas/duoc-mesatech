package cl.duoc.mesatech.solicitudes.consumer;

import cl.duoc.mesatech.solicitudes.dto.SolicitudCreadaDTO;
import cl.duoc.mesatech.solicitudes.dto.SolicitudEstadoDTO;
import cl.duoc.mesatech.solicitudes.service.OperacionLogService;
import cl.duoc.mesatech.solicitudes.service.SeguimientoSolicitudService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Listeners Kafka de solicitudes (metodología lab: package {@code consumer}, dos {@code @KafkaListener}).
 * Mismo group {@code mesatech-solicitudes-kafka}, ack manual después de guardar e idempotencia en el servicio.
 */
@Service
public class SolicitudesConsumer {

    private static final Logger log = LoggerFactory.getLogger(SolicitudesConsumer.class);

    private final SeguimientoSolicitudService seguimientoService;
    private final OperacionLogService operacionLog;
    private final String groupId;

    public SolicitudesConsumer(
            SeguimientoSolicitudService seguimientoService,
            OperacionLogService operacionLog,
            @Value("${spring.kafka.consumer.group-id}") String groupId
    ) {
        this.seguimientoService = seguimientoService;
        this.operacionLog = operacionLog;
        this.groupId = groupId;
    }

    @KafkaListener(
            id = "solicitudCreadaListener",
            topics = "${app.kafka.mistopicos.solicitud-creada}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${mesatech.kafka.listener.concurrency:3}"
    )
    public void onSolicitudCreada(
            @Payload @Valid SolicitudCreadaDTO evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topico,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int particion,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Acknowledgment acknowledgment
    ) {
        var guardado = seguimientoService.registrarCreacion(evento, topico, particion, offset, key);
        acknowledgment.acknowledge();

        String resultado = guardado
                .map(s -> "CONSUMIDO_GUARDADO_" + s.getTipoAtencion())
                .orElse("IGNORADO_DUPLICADO_IDEMPOTENTE");
        operacionLog.registrar(
                "INFO",
                groupId,
                topico,
                particion,
                offset,
                key,
                evento.eventId(),
                resultado,
                "solicitudId=" + evento.solicitudId()
        );
        log.info(
                "Solicitud creada eventId={} resultado={} | key={} partición={} offset={}",
                evento.eventId(),
                resultado,
                key,
                particion,
                offset
        );
    }

    @KafkaListener(
            id = "solicitudEstadoListener",
            topics = "${app.kafka.mistopicos.solicitud-estado}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${mesatech.kafka.listener.concurrency:3}"
    )
    public void onSolicitudEstado(
            @Payload @Valid SolicitudEstadoDTO evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topico,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int particion,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Acknowledgment acknowledgment
    ) {
        var guardado = seguimientoService.registrarCambioEstado(evento, topico, particion, offset, key);
        acknowledgment.acknowledge();

        String resultado = guardado
                .map(h -> "CONSUMIDO_GUARDADO_" + h.getEstadoSeguimiento())
                .orElse("IGNORADO_DUPLICADO_IDEMPOTENTE");
        operacionLog.registrar(
                "INFO",
                groupId,
                topico,
                particion,
                offset,
                key,
                evento.eventId(),
                resultado,
                evento.estadoAnterior() + "->" + evento.estadoNuevo()
        );
        log.info(
                "Solicitud estado eventId={} resultado={} | key={} partición={} offset={}",
                evento.eventId(),
                resultado,
                key,
                particion,
                offset
        );
    }
}
