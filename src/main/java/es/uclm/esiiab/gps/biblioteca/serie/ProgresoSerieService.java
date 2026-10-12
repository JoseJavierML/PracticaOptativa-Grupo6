package es.uclm.esiiab.gps.biblioteca.serie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uclm.esiiab.gps.biblioteca.biblioteca.BibliotecaItemRepository;
import es.uclm.esiiab.gps.biblioteca.titulo.TipoTitulo;
import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloNoEncontradoException;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloRepository;
import es.uclm.esiiab.gps.biblioteca.usuario.Usuario;
import es.uclm.esiiab.gps.biblioteca.usuario.UsuarioRepository;

@Service
public class ProgresoSerieService {

    private final ProgresoCapituloRepository progresoRepository;
    private final BibliotecaItemRepository bibliotecaRepository;
    private final TituloRepository tituloRepository;
    private final UsuarioRepository usuarioRepository;

    public ProgresoSerieService(ProgresoCapituloRepository progresoRepository,
            BibliotecaItemRepository bibliotecaRepository, TituloRepository tituloRepository,
            UsuarioRepository usuarioRepository) {
        this.progresoRepository = progresoRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.tituloRepository = tituloRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public ProgresoSerie consultar(Long usuarioId, Long serieId) {
        Titulo serie = buscarSerieDeUsuario(usuarioId, serieId);
        return convertir(usuarioId, serie);
    }

    @Transactional
    public ProgresoSerie actualizar(Long usuarioId, Long serieId, ActualizarCapitulo peticion) {
        Titulo serie = buscarSerieDeUsuario(usuarioId, serieId);
        if (peticion == null || peticion.temporada() == null || peticion.capitulo() == null
                || peticion.visto() == null) {
            throw new IllegalArgumentException("Indica temporada, capítulo y estado visto.");
        }
        if (peticion.temporada() < 1 || peticion.temporada() > serie.getTemporadas()
                || peticion.capitulo() < 1
                || peticion.capitulo() > serie.getCapitulosPorTemporada().get(peticion.temporada() - 1)) {
            throw new IllegalArgumentException("El capítulo indicado no existe en esa temporada.");
        }

        ProgresoCapitulo progreso = progresoRepository
                .findByUsuario_IdAndSerie_IdAndTemporadaAndCapitulo(
                        usuarioId, serieId, peticion.temporada(), peticion.capitulo())
                .orElseGet(() -> new ProgresoCapitulo(
                        usuarioRepository.getReferenceById(usuarioId), serie, peticion.temporada(),
                        peticion.capitulo(), peticion.visto()));
        progreso.setVisto(peticion.visto());
        progresoRepository.save(progreso);
        return convertir(usuarioId, serie);
    }

    private Titulo buscarSerieDeUsuario(Long usuarioId, Long serieId) {
        if (!bibliotecaRepository.findByUsuarioIdAndTitulo_Id(usuarioId, serieId).isPresent()) {
            throw new TituloNoEncontradoException(serieId);
        }
        Titulo serie = tituloRepository.findById(serieId)
                .orElseThrow(() -> new TituloNoEncontradoException(serieId));
        if (serie.getTipo() != TipoTitulo.SERIE) {
            throw new IllegalArgumentException("El título indicado no es una serie.");
        }
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("La sesión de usuario no es válida."));
        return serie;
    }

    private ProgresoSerie convertir(Long usuarioId, Titulo serie) {
        Map<String, Boolean> vistos = new HashMap<>();
        progresoRepository.findByUsuario_IdAndSerie_IdOrderByTemporadaAscCapituloAsc(usuarioId, serie.getId())
            .forEach(progreso -> vistos.put(clave(progreso.getTemporada(), progreso.getCapitulo()),
                        progreso.isVisto()));
        List<TemporadaProgreso> temporadas = new ArrayList<>();
        for (int indice = 0; indice < serie.getCapitulosPorTemporada().size(); indice++) {
            int numeroTemporada = indice + 1;
            int total = serie.getCapitulosPorTemporada().get(indice);
            List<CapituloProgreso> capitulos = new ArrayList<>();
            for (int numeroCapitulo = 1; numeroCapitulo <= total; numeroCapitulo++) {
                capitulos.add(new CapituloProgreso(numeroCapitulo,
                        vistos.getOrDefault(clave(numeroTemporada, numeroCapitulo), false)));
            }
            int vistosEnTemporada = (int) capitulos.stream().filter(CapituloProgreso::visto).count();
            temporadas.add(new TemporadaProgreso(numeroTemporada, vistosEnTemporada, total,
                    vistosEnTemporada == total, capitulos));
        }
        return new ProgresoSerie(serie.getId(), serie.getTitulo(), temporadas.size(), serie.getCapitulos(), temporadas);
    }

    private String clave(int temporada, int capitulo) {
        return temporada + ":" + capitulo;
    }

    public record ActualizarCapitulo(Integer temporada, Integer capitulo, Boolean visto) {
    }

    public record ProgresoSerie(Long serieId, String titulo, Integer temporadas, Integer capitulos,
                                List<TemporadaProgreso> detalleTemporadas) {
    }

    public record TemporadaProgreso(Integer numero, Integer capitulosVistos, Integer totalCapitulos,
                                    boolean vista, List<CapituloProgreso> capitulos) {
    }

    public record CapituloProgreso(Integer numero, boolean visto) {
    }
}