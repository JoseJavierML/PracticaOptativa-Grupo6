package es.uclm.esiiab.gps.biblioteca.titulo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uclm.esiiab.gps.biblioteca.genero.Genero;
import es.uclm.esiiab.gps.biblioteca.genero.GeneroRepository;
import es.uclm.esiiab.gps.biblioteca.biblioteca.EstadoBiblioteca;

@Service
public class TituloService {

    private final TituloRepository repositorio;
    private final GeneroRepository generoRepository;

    public TituloService(TituloRepository repositorio, GeneroRepository generoRepository) {
        this.repositorio = repositorio;
        this.generoRepository = generoRepository;
    }

    @Transactional(readOnly = true)
    public List<Titulo> listar() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public List<Titulo> buscar(Long generoId, Integer anio, EstadoBiblioteca estado, Long usuarioId) {
        validarFiltros(generoId, anio);
        return repositorio.buscar(generoId, anio, estado, usuarioId);
    }

    @Transactional
    public Titulo crear(DatosTitulo datos) {
        DatosValidados validados = validar(datos);
        return repositorio.save(new Titulo(validados.titulo(), validados.anio(), validados.genero(),
                validados.tipo(), validados.duracion(), validados.temporadas()));
    }

    @Transactional
    public Titulo actualizar(Long id, DatosTitulo datos) {
        Titulo titulo = repositorio.findById(id)
                .orElseThrow(() -> new TituloNoEncontradoException(id));
        DatosValidados validados = validar(datos);
        titulo.actualizar(validados.titulo(), validados.anio(), validados.genero(), validados.tipo(),
                validados.duracion(), validados.temporadas());
        return titulo;
    }

    @Transactional
    public void borrar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new TituloNoEncontradoException(id);
        }
        repositorio.deleteById(id);
    }

    private void validarFiltros(Long generoId, Integer anio) {
        if (generoId != null && generoId <= 0) {
            throw new IllegalArgumentException("El género indicado no es válido.");
        }
        if (anio != null && anio < 1) {
            throw new IllegalArgumentException("El año debe ser un número positivo.");
        }
    }

    private DatosValidados validar(DatosTitulo datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del título son obligatorios.");
        }
        if (datos.titulo() == null || datos.titulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (datos.anio() == null || datos.anio() < 1) {
            throw new IllegalArgumentException("El año debe ser un número positivo.");
        }
        if (datos.generoId() == null) {
            throw new IllegalArgumentException("El género es obligatorio.");
        }
        if (datos.tipo() == null) {
            throw new IllegalArgumentException("El tipo debe ser PELICULA o SERIE.");
        }
        Genero genero = generoRepository.findById(datos.generoId())
                .orElseThrow(() -> new IllegalArgumentException("El género indicado no existe."));
        if (datos.tipo() == TipoTitulo.PELICULA
                && (datos.duracion() == null || datos.duracion() < 1)) {
            throw new IllegalArgumentException("La duración de la película debe ser positiva.");
        }
        if (datos.tipo() == TipoTitulo.SERIE
                && (datos.temporadas() == null || datos.temporadas() < 1)) {
            throw new IllegalArgumentException("El número de temporadas debe ser positivo.");
        }
        return new DatosValidados(datos.titulo().strip(), datos.anio(), genero, datos.tipo(),
                datos.duracion(), datos.temporadas());
    }

    public record DatosTitulo(String titulo, Integer anio, Long generoId, TipoTitulo tipo,
                              Integer duracion, Integer temporadas) {
    }

    private record DatosValidados(String titulo, Integer anio, Genero genero, TipoTitulo tipo,
                                  Integer duracion, Integer temporadas) {
    }
}
