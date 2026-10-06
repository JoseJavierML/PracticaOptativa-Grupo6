package es.uclm.esiiab.gps.biblioteca.genero;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Género de películas y series (por ejemplo: Drama, Comedia, Ciencia ficción).
 * Es la entidad de ejemplo de la plantilla: sirve de patrón para crear las demás.
 */
@Entity
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    protected Genero() {
        // Constructor vacío requerido por JPA
    }

    public Genero(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
