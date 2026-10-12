package es.uclm.esiiab.gps.biblioteca.titulo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TituloRepository extends JpaRepository<Titulo, Long> {

    @Query("""
            select distinct t from Titulo t
            left join es.uclm.esiiab.gps.biblioteca.biblioteca.BibliotecaItem bi
                on bi.titulo = t and bi.usuarioId = :usuarioId
            where (:generoId is null or t.genero.id = :generoId)
              and (:anio is null or t.anio = :anio)
              and (:estado is null or bi.estado = :estado)
            order by t.titulo
            """)
    List<Titulo> buscar(@Param("generoId") Long generoId, @Param("anio") Integer anio,
            @Param("estado") es.uclm.esiiab.gps.biblioteca.biblioteca.EstadoBiblioteca estado,
            @Param("usuarioId") Long usuarioId);
}
