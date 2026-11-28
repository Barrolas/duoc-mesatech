package cl.duoc.mesatech.solicitudes.service;

import cl.duoc.mesatech.solicitudes.domain.EstadoSolicitud;
import cl.duoc.mesatech.solicitudes.domain.Solicitud;
import cl.duoc.mesatech.solicitudes.domain.TransicionesEstado;
import cl.duoc.mesatech.solicitudes.producer.SolicitudKafkaProducer;
import cl.duoc.mesatech.solicitudes.repo.CategoriaLocalRepository;
import cl.duoc.mesatech.solicitudes.repo.SolicitudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class SolicitudService {

    private final SolicitudRepository repository;
    private final CategoriaLocalRepository categoriasLocales;
    private final SolicitudKafkaProducer kafkaProducer;

    public SolicitudService(
            SolicitudRepository repository,
            CategoriaLocalRepository categoriasLocales,
            SolicitudKafkaProducer kafkaProducer
    ) {
        this.repository = repository;
        this.categoriasLocales = categoriasLocales;
        this.kafkaProducer = kafkaProducer;
    }

    public Solicitud crear(String usuarioSolicitante, Solicitud body) {
        if (body == null || body.getTitulo() == null || body.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título es obligatorio");
        }
        if (body.getDescripcion() == null || body.getDescripcion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción es obligatoria");
        }
        String categoria = body.getCategoria() == null ? null : body.getCategoria().trim();
        if (categoria != null && !categoria.isEmpty() && !categoriasLocales.existsByNombreIgnoreCase(categoria)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La categoría aún no está disponible en solicitudes. Espere a que se consuma el evento y actualice el listado."
            );
        }

        String prioridadNombre = body.getPrioridad() == null || body.getPrioridad().isBlank()
                ? "Media"
                : body.getPrioridad().trim();
        int prioridadNivel = resolverNivelPrioridad(prioridadNombre, body.getPrioridadNivel());

        body.setId(null);
        body.setTitulo(body.getTitulo().trim());
        body.setDescripcion(body.getDescripcion().trim());
        body.setCategoria(categoria == null || categoria.isEmpty() ? null : categoria);
        body.setPrioridad(prioridadNombre);
        body.setPrioridadNivel(prioridadNivel);
        body.setUsuarioSolicitante(usuarioSolicitante);
        body.setEstado(EstadoSolicitud.CREADA);

        Solicitud guardada = repository.save(body);
        kafkaProducer.publicarSolicitudCreada(guardada, prioridadNombre, prioridadNivel);
        return guardada;
    }

    public Solicitud cambiarEstado(Long id, String rawEstado) {
        Solicitud solicitud = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (rawEstado == null || rawEstado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el campo estado");
        }

        EstadoSolicitud nuevo;
        try {
            nuevo = EstadoSolicitud.valueOf(rawEstado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido: " + rawEstado);
        }

        EstadoSolicitud anterior = solicitud.getEstado();
        if (!TransicionesEstado.esPermitida(anterior, nuevo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    TransicionesEstado.mensajeRechazo(anterior, nuevo)
            );
        }

        solicitud.setEstado(nuevo);
        Solicitud actualizada = repository.save(solicitud);
        kafkaProducer.publicarCambioEstado(actualizada.getId(), anterior, nuevo);
        return actualizada;
    }

    static int resolverNivelPrioridad(String prioridadNombre, Integer nivelExplicito) {
        if (nivelExplicito != null) {
            if (nivelExplicito < 1 || nivelExplicito > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prioridadNivel debe estar entre 1 y 5");
            }
            return nivelExplicito;
        }
        String normalizada = Normalizer.normalize(prioridadNombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
        return switch (normalizada) {
            case "URGENTE", "CRITICA" -> 5;
            case "ALTA" -> 4;
            case "BAJA" -> 2;
            case "MUY BAJA", "MINIMA" -> 1;
            default -> 3;
        };
    }
}
