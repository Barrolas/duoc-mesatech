package cl.duoc.mesatech.bff.controller;

import cl.duoc.mesatech.bff.client.MicroservicioClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SolicitudesGatewayController {

    private final MicroservicioClient client;

    public SolicitudesGatewayController(MicroservicioClient client) {
        this.client = client;
    }

    @PostMapping("/v1/solicitudes")
    public ResponseEntity<Object> crear(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Object body
    ) {
        return client.postSolicitudes("/v1/solicitudes", authorization, body);
    }

    @GetMapping("/v1/solicitudes/mias")
    public ResponseEntity<Object> mias(@RequestHeader("Authorization") String authorization) {
        return client.getSolicitudes("/v1/solicitudes/mias", authorization);
    }

    @GetMapping("/v1/solicitudes")
    public ResponseEntity<Object> todas(@RequestHeader("Authorization") String authorization) {
        return client.getSolicitudes("/v1/solicitudes", authorization);
    }

    @PatchMapping("/v1/solicitudes/{id}/estado")
    public ResponseEntity<Object> estado(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization,
            @RequestBody Object body
    ) {
        return client.patchSolicitudes("/v1/solicitudes/" + id + "/estado", authorization, body);
    }

    @GetMapping("/v2/solicitudes/mias")
    public ResponseEntity<Object> miasV2(@RequestHeader("Authorization") String authorization) {
        return client.getSolicitudes("/v2/solicitudes/mias", authorization);
    }
}
