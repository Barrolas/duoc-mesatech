package cl.duoc.mesatech.catalogo.web;

import cl.duoc.mesatech.catalogo.domain.Categoria;
import cl.duoc.mesatech.catalogo.domain.Prioridad;
import cl.duoc.mesatech.catalogo.dto.PrioridadRenombradaDTO;
import cl.duoc.mesatech.catalogo.messaging.CategoriaEventPublisher;
import cl.duoc.mesatech.catalogo.messaging.PublicacionEventoException;
import cl.duoc.mesatech.catalogo.repo.CategoriaRepository;
import cl.duoc.mesatech.catalogo.repo.PrioridadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class CatalogoController {

    private final CategoriaRepository categorias;
    private final PrioridadRepository prioridades;
    private final CategoriaEventPublisher eventos;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final String topicoPrioridad;

    public CatalogoController(
            CategoriaRepository categorias,
            PrioridadRepository prioridades,
            CategoriaEventPublisher eventos,
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            @Value("${app.kafka.mistopicos.prioridad}") String topicoPrioridad
    ) {
        this.categorias = categorias;
        this.prioridades = prioridades;
        this.eventos = eventos;
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.topicoPrioridad = topicoPrioridad;
    }

    @GetMapping("/v1/catalogo")
    public Map<String, Object> listar() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("categorias", categorias.findAll());
        respuesta.put("prioridades", prioridades.findAll());
        return respuesta;
    }

    @PostMapping("/v1/catalogo/categorias")
    public Categoria crearCategoria(@RequestBody Categoria body) {
        if (body.getNombre() == null || body.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar nombre");
        }
        body.setId(null);
        body.setNombre(body.getNombre().trim());
        Categoria guardada = categorias.save(body);
        try {
            eventos.publicarCategoriaCreada(guardada);
        } catch (PublicacionEventoException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ex.getMessage()
            );
        }
        return guardada;
    }

    @PostMapping("/v1/catalogo/prioridades")
    public Prioridad crearPrioridad(@RequestBody Prioridad body) {
        body.setId(null);
        return prioridades.save(body);
    }

    @PutMapping("/v1/catalogo/categorias/{id}")
    public Categoria actualizarCategoria(@PathVariable Long id, @RequestBody Categoria body) {
        Categoria existente = categorias.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (body.getNombre() == null || body.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar nombre");
        }
        existente.setNombre(body.getNombre().trim());
        return categorias.save(existente);
    }

    @DeleteMapping("/v1/catalogo/categorias/{id}")
    public void eliminarCategoria(@PathVariable Long id) {
        if (!categorias.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        categorias.deleteById(id);
    }

    @PutMapping("/v1/catalogo/prioridades/{id}")
    public Prioridad actualizarPrioridad(@PathVariable Long id, @RequestBody Prioridad body) {
        Prioridad existente = prioridades.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (body.getNombre() == null || body.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar nombre");
        }
        if (body.getNivel() == null || body.getNivel() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar nivel válido");
        }
        String nombreAnterior = existente.getNombre();
        existente.setNombre(body.getNombre().trim());
        existente.setNivel(body.getNivel());
        Prioridad guardada = prioridades.save(existente);
        if (!nombreAnterior.equals(guardada.getNombre())) {
            publicarPrioridadRenombrada(nombreAnterior, guardada);
        }
        return guardada;
    }

    private void publicarPrioridadRenombrada(String nombreAnterior, Prioridad prioridad) {
        String key = "prioridad-" + prioridad.getId();
        PrioridadRenombradaDTO dto = new PrioridadRenombradaDTO(
                UUID.randomUUID().toString(),
                PrioridadRenombradaDTO.TIPO,
                Instant.now(),
                prioridad.getId(),
                nombreAnterior,
                prioridad.getNombre(),
                prioridad.getNivel()
        );
        try {
            String json = jsonMapper.writeValueAsString(dto);
            kafkaTemplate.send(topicoPrioridad, key, json);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo publicar el evento Kafka: " + ex.getMessage()
            );
        }
    }

    @DeleteMapping("/v1/catalogo/prioridades/{id}")
    public void eliminarPrioridad(@PathVariable Long id) {
        if (!prioridades.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        prioridades.deleteById(id);
    }
}