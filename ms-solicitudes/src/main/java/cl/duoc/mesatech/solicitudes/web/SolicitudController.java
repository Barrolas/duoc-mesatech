package cl.duoc.mesatech.solicitudes.web;

import cl.duoc.mesatech.solicitudes.domain.CategoriaLocal;
import cl.duoc.mesatech.solicitudes.domain.Solicitud;
import cl.duoc.mesatech.solicitudes.repo.CategoriaLocalRepository;
import cl.duoc.mesatech.solicitudes.repo.SolicitudRepository;
import cl.duoc.mesatech.solicitudes.service.SolicitudService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SolicitudController {

    private final SolicitudRepository repository;
    private final CategoriaLocalRepository categoriasLocales;
    private final SolicitudService solicitudService;

    public SolicitudController(
            SolicitudRepository repository,
            CategoriaLocalRepository categoriasLocales,
            SolicitudService solicitudService
    ) {
        this.repository = repository;
        this.categoriasLocales = categoriasLocales;
        this.solicitudService = solicitudService;
    }

    @GetMapping("/v1/solicitudes/categorias")
    public List<Map<String, Object>> categoriasDisponibles() {
        return categoriasLocales.findAllByOrderByNombreAsc().stream()
                .map(this::categoriaDisponible)
                .toList();
    }

    @PostMapping("/v1/solicitudes")
    public Solicitud crear(@AuthenticationPrincipal Jwt jwt, @RequestBody Solicitud body) {
        String usuario = jwt != null ? jwt.getClaimAsString("preferred_username") : null;
        return solicitudService.crear(usuario, body);
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
        String raw = body == null ? null : body.get("estado");
        return solicitudService.cambiarEstado(id, raw);
    }

    @GetMapping("/v2/solicitudes/mias")
    public Map<String, Object> miasV2(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("version", "v2");
        respuesta.put("usuario", jwt.getClaimAsString("preferred_username"));
        respuesta.put("solicitudes", mias(jwt));
        return respuesta;
    }

    private Map<String, Object> categoriaDisponible(CategoriaLocal copia) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", copia.getId());
        item.put("nombre", copia.getNombre());
        return item;
    }
}
