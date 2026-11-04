package cl.duoc.mesatech.solicitudes.messaging;

import cl.duoc.mesatech.solicitudes.domain.CategoriaLocal;
import cl.duoc.mesatech.solicitudes.repo.CategoriaLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CategoriaCopiaService {

    private final CategoriaLocalRepository repository;

    public CategoriaCopiaService(CategoriaLocalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CategoriaLocal guardar(CategoriaCreadaEvent evento) {
        validar(evento);
        CategoriaLocal copia = repository.findById(evento.categoriaId()).orElseGet(CategoriaLocal::new);
        copia.setId(evento.categoriaId());
        copia.setNombre(evento.nombre().trim());
        copia.setEventId(evento.eventId().trim());
        copia.setFechaEvento(evento.fechaEvento());
        copia.setRecibidaEn(Instant.now());
        return repository.save(copia);
    }

    private void validar(CategoriaCreadaEvent evento) {
        if (evento == null) {
            throw new EventoInvalidoException("El evento no trae cuerpo");
        }
        if (evento.eventId() == null || evento.eventId().isBlank()) {
            throw new EventoInvalidoException("Falta eventId");
        }
        if (!CategoriaCreadaEvent.TIPO.equals(evento.tipoEvento())) {
            throw new EventoInvalidoException("tipoEvento no soportado");
        }
        if (evento.fechaEvento() == null) {
            throw new EventoInvalidoException("Falta fechaEvento");
        }
        if (evento.categoriaId() == null) {
            throw new EventoInvalidoException("Falta categoriaId");
        }
        if (evento.nombre() == null || evento.nombre().isBlank()) {
            throw new EventoInvalidoException("Falta nombre");
        }
    }
}
