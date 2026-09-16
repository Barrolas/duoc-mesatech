package cl.duoc.mesatech.catalogo.repo;

import cl.duoc.mesatech.catalogo.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
