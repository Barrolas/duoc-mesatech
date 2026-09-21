package cl.duoc.mesatech.bff.controller;

import cl.duoc.mesatech.bff.client.MicroservicioClient;
import cl.duoc.mesatech.bff.security.AccesoDenegadoException;
import cl.duoc.mesatech.bff.security.EntraRoles;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogoGatewayController {

    private final MicroservicioClient client;

    public CatalogoGatewayController(MicroservicioClient client) {
        this.client = client;
    }

    @GetMapping("/v1/catalogo")
    public ResponseEntity<Object> catalogo(@RequestHeader("Authorization") String authorization) {
        return client.getCatalogo("/v1/catalogo", authorization);
    }

    @PostMapping("/v1/catalogo/categorias")
    public ResponseEntity<Object> crearCategoria(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authorization,
            @RequestBody Object body
    ) {
        exigirAdmin(jwt);
        return client.postCatalogo("/v1/catalogo/categorias", authorization, body);
    }

    @PutMapping("/v1/catalogo/categorias/{id}")
    public ResponseEntity<Object> actualizarCategoria(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody Object body
    ) {
        exigirAdmin(jwt);
        return client.putCatalogo("/v1/catalogo/categorias/" + id, authorization, body);
    }

    @DeleteMapping("/v1/catalogo/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id
    ) {
        exigirAdmin(jwt);
        return client.deleteCatalogo("/v1/catalogo/categorias/" + id, authorization);
    }

    @PostMapping("/v1/catalogo/prioridades")
    public ResponseEntity<Object> crearPrioridad(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authorization,
            @RequestBody Object body
    ) {
        exigirAdmin(jwt);
        return client.postCatalogo("/v1/catalogo/prioridades", authorization, body);
    }

    @PutMapping("/v1/catalogo/prioridades/{id}")
    public ResponseEntity<Object> actualizarPrioridad(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody Object body
    ) {
        exigirAdmin(jwt);
        return client.putCatalogo("/v1/catalogo/prioridades/" + id, authorization, body);
    }

    @DeleteMapping("/v1/catalogo/prioridades/{id}")
    public ResponseEntity<Void> eliminarPrioridad(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id
    ) {
        exigirAdmin(jwt);
        return client.deleteCatalogo("/v1/catalogo/prioridades/" + id, authorization);
    }

    private static void exigirAdmin(Jwt jwt) {
        if (!EntraRoles.esAdmin(jwt)) {
            throw new AccesoDenegadoException();
        }
    }
}
