package es.uclm.esiiab.gps.biblioteca.genero;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneroRepository extends JpaRepository<Genero, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Genero> findAllByOrderByNombreAsc();
}
