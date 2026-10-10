package es.uclm.esiiab.gps.biblioteca.biblioteca;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uclm.esiiab.gps.biblioteca.titulo.TipoTitulo;
import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloNoEncontradoException;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloRepository;

@Service
public class BibliotecaService {

    private final BibliotecaItemRepository repositorio;
    private final TituloRepository tituloRepositorio;

    public BibliotecaService(BibliotecaItemRepository repositorio, TituloRepository tituloRepositorio) {
        this.repositorio = repositorio;
        this.tituloRepositorio = tituloRepositorio;
    }

    @Transactional(readOnly = true)
    public List<ElementoBiblioteca> listar(Long usuarioId) {
        return repositorio.findByUsuarioIdOrderByFechaModificacionDesc(usuarioId)
                .stream().map(this::convertir).toList();
    }

    @Transactional
    public ElementoBiblioteca anadir(Long usuarioId, NuevoBibliotecaItem peticion) {
        if (peticion == null || peticion.tituloId() == null || peticion.tituloId() <= 0
                || peticion.estado() == null) {
            throw new IllegalArgumentException("Indica un título del catálogo y un estado válido.");
        }
        Titulo titulo = tituloRepositorio.findById(peticion.tituloId())
                .orElseThrow(() -> new TituloNoEncontradoException(peticion.tituloId()));
        if (repositorio.existsByUsuarioIdAndTitulo_Id(usuarioId, peticion.tituloId())) {
            throw new IllegalArgumentException("Ese título ya está en tu biblioteca.");
        }
        return convertir(repositorio.save(new BibliotecaItem(usuarioId, titulo, peticion.estado())));
    }

    @Transactional
    public ElementoBiblioteca actualizarEstado(Long usuarioId, Long id, EstadoBiblioteca estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        BibliotecaItem item = buscarPropio(usuarioId, id);
        item.setEstado(estado);
        return convertir(repositorio.saveAndFlush(item));
    }

    @Transactional
    public void eliminar(Long usuarioId, Long id) {
        repositorio.delete(buscarPropio(usuarioId, id));
    }

    private BibliotecaItem buscarPropio(Long usuarioId, Long id) {
        return repositorio.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new BibliotecaItemNoEncontradoException(
                        "No existe ese elemento en tu biblioteca."));
    }

        private ElementoBiblioteca convertir(BibliotecaItem item) {
        Titulo titulo = item.getTitulo();
        return new ElementoBiblioteca(item.getId(), titulo.getId(), titulo.getTitulo(),
            titulo.getTipo(), item.getEstado(), item.getFechaModificacion());
        }

        public record NuevoBibliotecaItem(Long tituloId, EstadoBiblioteca estado) {
    }

    public record ActualizarEstado(EstadoBiblioteca estado) {
    }

        public record ElementoBiblioteca(Long id, Long tituloId, String titulo,
            TipoTitulo tipoTitulo, EstadoBiblioteca estado, LocalDateTime fechaModificacion) {
        }
}
