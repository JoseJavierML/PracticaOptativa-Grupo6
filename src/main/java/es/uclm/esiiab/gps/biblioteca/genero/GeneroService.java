package es.uclm.esiiab.gps.biblioteca.genero;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio de los géneros. Las reglas (validaciones, duplicados...)
 * van aquí, no en el controlador.
 */
@Service
public class GeneroService {

    private final GeneroRepository repositorio;

    public GeneroService(GeneroRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Genero> listar() {
        return repositorio.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Genero crear(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del género es obligatorio.");
        }
        String limpio = nombre.strip();
        if (limpio.length() > 50) {
            throw new IllegalArgumentException("El nombre del género no puede superar 50 caracteres.");
        }
        if (repositorio.existsByNombreIgnoreCase(limpio)) {
            throw new IllegalArgumentException("Ya existe un género llamado \"" + limpio + "\".");
        }
        return repositorio.save(new Genero(limpio));
    }
}
