package cl.duoc.mesatech.solicitudes.messaging.kafka;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitudEventosValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rechazaSolicitudCreadaConPrioridadNivelFueraDeRango() {
        SolicitudCreadaEvent invalido = new SolicitudCreadaEvent(
                "b1c2d3e4-f5a6-4789-0123-456789abcdef",
                "solicitud.creada",
                Instant.parse("2026-11-24T21:10:00Z"),
                1001L,
                "Incidente red sucursal Oriente",
                "URGENTE",
                9,
                "Infraestructura"
        );

        Set<ConstraintViolation<SolicitudCreadaEvent>> violaciones = validator.validate(invalido);
        assertEquals(1, violaciones.size());
        ConstraintViolation<SolicitudCreadaEvent> v = violaciones.iterator().next();
        assertEquals("prioridadNivel", v.getPropertyPath().toString());
        assertEquals("debe estar entre 1 y 5", v.getMessage());
    }

    @Test
    void aceptaSolicitudCreadaValida() {
        SolicitudCreadaEvent valido = new SolicitudCreadaEvent(
                "a3f2c8e1-9b4d-4c7a-8f1e-2d6b5a4c3e2f",
                "solicitud.creada",
                Instant.parse("2026-11-24T21:10:00Z"),
                1001L,
                "Incidente red sucursal Oriente",
                "URGENTE",
                5,
                "Infraestructura"
        );

        assertTrue(validator.validate(valido).isEmpty());
    }

    @Test
    void aceptaSolicitudEstadoValida() {
        SolicitudEstadoEvent valido = new SolicitudEstadoEvent(
                "c4d5e6f7-8901-4abc-def0-123456789abc",
                "solicitud.estado",
                Instant.parse("2026-11-24T21:12:30Z"),
                1001L,
                "EN_PROCESO",
                "RESUELTA"
        );

        assertTrue(validator.validate(valido).isEmpty());
    }

    @Test
    void rechazaEstadoNuevoFueraDelEnum() {
        SolicitudEstadoEvent invalido = new SolicitudEstadoEvent(
                "d5e6f7a8-9012-4bcd-ef01-23456789abcd",
                "solicitud.estado",
                Instant.parse("2026-11-24T21:12:30Z"),
                1001L,
                "EN_PROCESO",
                "FINALIZADA"
        );

        Set<ConstraintViolation<SolicitudEstadoEvent>> violaciones = validator.validate(invalido);
        assertEquals(1, violaciones.size());
        assertEquals("estadoNuevo", violaciones.iterator().next().getPropertyPath().toString());
    }
}
