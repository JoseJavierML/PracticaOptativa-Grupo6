package es.uclm.esiiab.gps.biblioteca.biblioteca;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BibliotecaItemRepository extends JpaRepository<BibliotecaItem, Long> {

    List<BibliotecaItem> findByUsuarioIdOrderByFechaModificacionDesc(Long usuarioId);

    Optional<BibliotecaItem> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndTitulo_Id(Long usuarioId, Long tituloId);
}
