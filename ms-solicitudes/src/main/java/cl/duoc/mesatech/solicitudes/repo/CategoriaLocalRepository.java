package cl.duoc.mesatech.solicitudes.repo;

import cl.duoc.mesatech.solicitudes.domain.CategoriaLocal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaLocalRepository extends JpaRepository<CategoriaLocal, Long> {

    List<CategoriaLocal> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
