package cl.duoc.mesatech.catalogo.service;

import cl.duoc.mesatech.catalogo.domain.AuditoriaPrioridad;
import cl.duoc.mesatech.catalogo.dto.PrioridadRenombradaDTO;
import cl.duoc.mesatech.catalogo.repo.AuditoriaPrioridadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Regla de negocio contrato §2.3 (formato vs sustantivo).
 * Estructura análoga a {@code VentaService.registrarVenta} del laboratorio.
 */
@Service
public class CatalogoPrioridadService {

    private static final Pattern ESPACIOS_MULTIPLES = Pattern.compile("\\s+");

    private final AuditoriaPrioridadRepository repository;

    public CatalogoPrioridadService(AuditoriaPrioridadRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AuditoriaPrioridad registrarCambioPrioridad(
            PrioridadRenombradaDTO dto,
            String key,
            int partition,
            long offset
    ) {
        if (!PrioridadRenombradaDTO.TIPO.equals(dto.tipoEvento())) {
            throw new IllegalArgumentException(
                    "tipoEvento debe ser " + PrioridadRenombradaDTO.TIPO + ", recibido: " + dto.tipoEvento()
            );
        }

        AuditoriaPrioridad fila = new AuditoriaPrioridad();
        fila.setPrioridadId(dto.prioridadId());
        fila.setNombreAnterior(dto.nombreAnterior());
        fila.setNombreNuevo(dto.nombreNuevo());
        fila.setTipoCambio(clasificarCambio(dto.nombreAnterior(), dto.nombreNuevo()));
        fila.setEventId(dto.eventId());
        return repository.save(fila);
    }

    static String clasificarCambio(String anterior, String nuevo) {
        String normAnterior = normalizar(anterior);
        String normNuevo = normalizar(nuevo);
        if (normAnterior.equals(normNuevo) && !anterior.equals(nuevo)) {
            return "CORRECCION_FORMATO";
        }
        return "REVISION_SUSTANTIVA";
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String colapsado = ESPACIOS_MULTIPLES.matcher(texto.trim()).replaceAll(" ");
        return colapsado.toLowerCase(Locale.ROOT);
    }
}