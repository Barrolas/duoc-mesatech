package cl.duoc.mesatech.bff.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @GetMapping("/usuario")
    public Map<String, Object> usuario(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Token JWT validado en el BFF");
        respuesta.put("nombre", jwt.getClaimAsString("name"));
        respuesta.put("usuario", jwt.getClaimAsString("preferred_username"));
        respuesta.put("oid", jwt.getClaimAsString("oid"));
        respuesta.put("scopes", jwt.getClaimAsString("scp"));
        respuesta.put("audience", jwt.getAudience());
        Object roles = jwt.getClaims().get("roles");
        respuesta.put("roles", roles != null ? roles : List.of());
        return respuesta;
    }
}
