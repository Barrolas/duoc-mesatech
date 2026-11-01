package cl.duoc.mesatech.solicitudes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;

@Configuration
@Profile("local")
public class LocalJwtConfig {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> {
            throw new BadJwtException("Perfil local: el consumidor no valida Entra en esta copia");
        };
    }
}
