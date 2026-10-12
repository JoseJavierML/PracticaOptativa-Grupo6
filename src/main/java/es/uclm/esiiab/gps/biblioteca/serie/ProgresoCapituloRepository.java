package es.uclm.esiiab.gps.biblioteca.serie;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgresoCapituloRepository extends JpaRepository<ProgresoCapitulo, Long> {

    List<ProgresoCapitulo> findByUsuario_IdAndSerie_IdOrderByTemporadaAscCapituloAsc(Long usuarioId, Long serieId);

    Optional<ProgresoCapitulo> findByUsuario_IdAndSerie_IdAndTemporadaAndCapitulo(
            Long usuarioId, Long serieId, Integer temporada, Integer capitulo);
}