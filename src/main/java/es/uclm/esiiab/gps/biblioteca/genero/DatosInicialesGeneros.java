package es.uclm.esiiab.gps.biblioteca.genero;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga unos géneros de ejemplo SOLO si la tabla está vacía.
 * Así, lo que añadáis se conserva entre arranques.
 */
@Component
public class DatosInicialesGeneros implements CommandLineRunner {

    private static final List<String> GENEROS = List.of(
            "Animación", "Ciencia ficción", "Comedia", "Documental",
            "Drama", "Fantasía", "Terror", "Thriller");

    private final GeneroRepository repositorio;

    public DatosInicialesGeneros(GeneroRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() == 0) {
            GENEROS.forEach(nombre -> repositorio.save(new Genero(nombre)));
        }
    }
}
