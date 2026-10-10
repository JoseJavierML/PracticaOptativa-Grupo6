package es.uclm.esiiab.gps.biblioteca.biblioteca;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BibliotecaService {

    private final BibliotecaItemRepository repositorio;

    public BibliotecaService(BibliotecaItemRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<BibliotecaItem> listar(Long usuarioId) {
        return repositorio.findByUsuarioIdOrderByFechaModificacionDesc(usuarioId);
    }

    @Transactional
    public BibliotecaItem anadir(Long usuarioId, NuevoBibliotecaItem peticion) {
        if (peticion == null || peticion.tipoTitulo() == null || peticion.tituloId() == null
                || peticion.tituloId() <= 0 || peticion.titulo() == null
                || peticion.titulo().isBlank() || peticion.titulo().strip().length() > 200
                || peticion.estado() == null) {
            throw new IllegalArgumentException("Indica un título, su tipo y un estado válido.");
        }
        if (repositorio.existsByUsuarioIdAndTipoTituloAndTituloId(
                usuarioId, peticion.tipoTitulo(), peticion.tituloId())) {
            throw new IllegalArgumentException("Ese título ya está en tu biblioteca.");
        }
        return repositorio.save(new BibliotecaItem(usuarioId, peticion.tipoTitulo(),
                peticion.tituloId(), peticion.titulo().strip(), peticion.estado()));
    }

    @Transactional
    public BibliotecaItem actualizarEstado(Long usuarioId, Long id, EstadoBiblioteca estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        BibliotecaItem item = buscarPropio(usuarioId, id);
        item.setEstado(estado);
        return repositorio.save(item);
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

    public record NuevoBibliotecaItem(
            TipoTitulo tipoTitulo, Long tituloId, String titulo, EstadoBiblioteca estado) {
    }

    public record ActualizarEstado(EstadoBiblioteca estado) {
    }
}
