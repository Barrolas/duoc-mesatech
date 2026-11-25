package cl.duoc.mesatech.solicitudes.messaging.kafka;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SolicitudEstadoKafkaListener {

    private final SeguimientoSolicitudService seguimientoService;
    private final OperacionLogService operacionLog;
    private final String groupId;

    public SolicitudEstadoKafkaListener(
            SeguimientoSolicitudService seguimientoService,
            OperacionLogService operacionLog,
            @Value("${spring.kafka.consumer.group-id}") String groupId
    ) {
        this.seguimientoService = seguimientoService;
        this.operacionLog = operacionLog;
        this.groupId = groupId;
    }

    @KafkaListener(
            id = "solicitudEstadoListener",
            topics = "${mesatech.kafka.topics.solicitud-estado}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${mesatech.kafka.listener.concurrency:3}"
    )
    public void onSolicitudEstado(
            @Payload @Valid SolicitudEstadoEvent evento,
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
    }
}
