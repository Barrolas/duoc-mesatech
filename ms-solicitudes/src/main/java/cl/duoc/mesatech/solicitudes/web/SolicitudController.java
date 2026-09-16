package cl.duoc.mesatech.solicitudes.web;

import cl.duoc.mesatech.solicitudes.domain.EstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.Solicitud;
import cl.duoc.mesatech.solicitudes.domain.TransicionesEstado;
import cl.duoc.mesatech.solicitudes.repo.SolicitudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SolicitudController {

    private final SolicitudRepository repository;

    public SolicitudController(SolicitudRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/v1/solicitudes")
    public Solicitud crear(@AuthenticationPrincipal Jwt jwt, @RequestBody Solicitud body) {
        if (body.getTitulo() == null || body.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título es obligatorio");
        }
        if (body.getDescripcion() == null || body.getDescripcion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción es obligatoria");
        }
        body.setId(null);
        body.setUsuarioSolicitante(jwt.getClaimAsString("preferred_username"));
        body.setEstado(EstadoSolicitud.CREADA);
        return repository.save(body);
    }

    @GetMapping("/v1/solicitudes/mias")
    public List<Solicitud> mias(@AuthenticationPrincipal Jwt jwt) {
        return repository.findByUsuarioSolicitante(jwt.getClaimAsString("preferred_username"));
    }

    @GetMapping("/v1/solicitudes")
    public List<Solicitud> todas() {
        return repository.findAll();
    }

    @PatchMapping("/v1/solicitudes/{id}/estado")
    public Solicitud cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Solicitud solicitud = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String raw = body == null ? null : body.get("estado");
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el campo estado");
        }

        EstadoSolicitud nuevo;
        try {
            nuevo = EstadoSolicitud.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido: " + raw);
        }

        if (!TransicionesEstado.esPermitida(solicitud.getEstado(), nuevo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    TransicionesEstado.mensajeRechazo(solicitud.getEstado(), nuevo)
            );
        }

        solicitud.setEstado(nuevo);
        return repository.save(solicitud);
    }

    @GetMapping("/v2/solicitudes/mias")
    public Map<String, Object> miasV2(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("version", "v2");
        respuesta.put("usuario", jwt.getClaimAsString("preferred_username"));
        respuesta.put("solicitudes", mias(jwt));
        return respuesta;
    }
}
