package cl.duoc.mesatech.catalogo.web;

import cl.duoc.mesatech.catalogo.domain.Categoria;
import cl.duoc.mesatech.catalogo.domain.Prioridad;
import cl.duoc.mesatech.catalogo.repo.CategoriaRepository;
import cl.duoc.mesatech.catalogo.repo.PrioridadRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class CatalogoController {

    private final CategoriaRepository categorias;
    private final PrioridadRepository prioridades;

    public CatalogoController(CategoriaRepository categorias, PrioridadRepository prioridades) {
        this.categorias = categorias;
        this.prioridades = prioridades;
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
        body.setId(null);
        return categorias.save(body);
    }

    @PostMapping("/v1/catalogo/prioridades")
    public Prioridad crearPrioridad(@RequestBody Prioridad body) {
        body.setId(null);
        return prioridades.save(body);
    }
}
