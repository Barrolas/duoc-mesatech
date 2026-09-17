package cl.duoc.mesatech.bff.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * App roles de Entra ID (claim {@code roles}). Nombres en Azure pueden ser
 * Cliente / Operador / Administrador; se normalizan para las reglas del BFF.
 */
public final class EntraRoles {

    private EntraRoles() {
    }

    public static Set<String> rolesNormalizados(Jwt jwt) {
        Set<String> out = new HashSet<>();
        if (jwt == null) {
            return out;
        }
        Object claim = jwt.getClaims().get("roles");
        if (claim instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item != null) {
                    out.add(normalizar(item.toString()));
                }
            }
        } else if (claim instanceof String s && !s.isBlank()) {
            out.add(normalizar(s));
        }
        return out;
    }

    private static String normalizar(String raw) {
        String v = raw.toLowerCase(Locale.ROOT).trim();
        if (List.of("administrador", "administrator", "admin").contains(v)) {
            return "admin";
        }
        if (List.of("operador", "operator").contains(v)) {
            return "operador";
        }
        if (List.of("cliente", "client").contains(v)) {
            return "cliente";
        }
        return v;
    }

    public static boolean esOperadorOAdmin(Jwt jwt) {
        Set<String> roles = rolesNormalizados(jwt);
        return roles.contains("admin") || roles.contains("operador");
    }

    public static boolean esAdmin(Jwt jwt) {
        return rolesNormalizados(jwt).contains("admin");
    }
}
