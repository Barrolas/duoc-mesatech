package cl.duoc.mesatech.solicitudes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OperacionLogService {

    private static final Logger opLog = LoggerFactory.getLogger("OPERACION_JSONL");
    private static final Logger fallbackLog = LoggerFactory.getLogger(OperacionLogService.class);
    private static final String SERVICIO = "ms-solicitudes";

    private final JsonMapper jsonMapper;

    public OperacionLogService(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void registrar(
            String nivel,
            String grupo,
            String topico,
            Integer particion,
            Long offset,
            String key,
            String eventId,
            String resultado,
            String detalle
    ) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("timestampUtc", Instant.now().toString());
        entry.put("nivel", nivel);
        entry.put("servicio", SERVICIO);
        entry.put("grupo", grupo);
        entry.put("topico", topico);
        entry.put("particion", particion);
        entry.put("offset", offset);
        entry.put("key", key);
        entry.put("eventId", eventId);
        entry.put("resultado", resultado);
        if (detalle != null && !detalle.isBlank()) {
            entry.put("detalle", detalle);
        }
        try {
            String line = jsonMapper.writeValueAsString(entry);
            if ("ERROR".equalsIgnoreCase(nivel)) {
                opLog.error(line);
            } else if ("WARN".equalsIgnoreCase(nivel)) {
                opLog.warn(line);
            } else {
                opLog.info(line);
            }
        } catch (Exception ex) {
            fallbackLog.warn("No se pudo serializar registro operativo: {}", ex.getMessage());
        }
    }

    public String extraerEventId(Object rawPayload) {
        if (rawPayload == null) {
            return null;
        }
        try {
            String text = rawPayload.toString();
            JsonNode node = jsonMapper.readTree(text);
            JsonNode idNode = node.get("eventId");
            return idNode != null && idNode.isTextual() ? idNode.asText() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
