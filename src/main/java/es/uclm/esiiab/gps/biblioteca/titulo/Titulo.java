package es.uclm.esiiab.gps.biblioteca.titulo;

import es.uclm.esiiab.gps.biblioteca.genero.Genero;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Titulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private Integer anio;
    private Integer duracion;
    private Integer temporadas;

    @Enumerated(EnumType.STRING)
    private TipoTitulo tipo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "genero_id", nullable = false)
    private Genero genero;

    protected Titulo() {
    }

    public Titulo(String titulo, Integer anio, Genero genero, TipoTitulo tipo,
                  Integer duracion, Integer temporadas) {
        this.titulo = titulo;
        this.anio = anio;
        this.genero = genero;
        this.tipo = tipo;
        this.duracion = duracion;
        this.temporadas = temporadas;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getAnio() {
        return anio;
    }

    public Genero getGenero() {
        return genero;
    }

    public TipoTitulo getTipo() {
        return tipo;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public Integer getTemporadas() {
        return temporadas;
    }

    public void actualizar(String titulo, Integer anio, Genero genero, TipoTitulo tipo,
                           Integer duracion, Integer temporadas) {
        this.titulo = titulo;
        this.anio = anio;
        this.genero = genero;
        this.tipo = tipo;
        this.duracion = duracion;
        this.temporadas = temporadas;
    }
}
