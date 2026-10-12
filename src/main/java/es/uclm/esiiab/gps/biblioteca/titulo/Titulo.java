package es.uclm.esiiab.gps.biblioteca.titulo;

import java.util.List;

import es.uclm.esiiab.gps.biblioteca.genero.Genero;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;

@Entity
public class Titulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private Integer anio;
    private Integer duracion;
    private Integer temporadas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "titulo_capitulos_temporada", joinColumns = @JoinColumn(name = "titulo_id"))
    @OrderColumn(name = "temporada")
    @Column(name = "capitulos", nullable = false)
    private List<Integer> capitulosPorTemporada = List.of();

    @Enumerated(EnumType.STRING)
    private TipoTitulo tipo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "genero_id", nullable = false)
    private Genero genero;

    protected Titulo() {
    }

    public Titulo(String titulo, Integer anio, Genero genero, TipoTitulo tipo,
                  Integer duracion, Integer temporadas) {
        this(titulo, anio, genero, tipo, duracion, temporadas, List.of());
    }

    public Titulo(String titulo, Integer anio, Genero genero, TipoTitulo tipo,
                  Integer duracion, Integer temporadas, List<Integer> capitulosPorTemporada) {
        this.titulo = titulo;
        this.anio = anio;
        this.genero = genero;
        this.tipo = tipo;
        this.duracion = duracion;
        this.temporadas = temporadas;
        this.capitulosPorTemporada = capitulosPorTemporada == null
            ? List.of() : List.copyOf(capitulosPorTemporada);
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

    public List<Integer> getCapitulosPorTemporada() {
        return List.copyOf(capitulosPorTemporada);
    }

    public Integer getCapitulos() {
        return capitulosPorTemporada.stream().mapToInt(Integer::intValue).sum();
    }

    public void actualizar(String titulo, Integer anio, Genero genero, TipoTitulo tipo,
                           Integer duracion, Integer temporadas) {
        actualizar(titulo, anio, genero, tipo, duracion, temporadas, List.of());
    }

    public void actualizar(String titulo, Integer anio, Genero genero, TipoTitulo tipo,
                           Integer duracion, Integer temporadas, List<Integer> capitulosPorTemporada) {
        this.titulo = titulo;
        this.anio = anio;
        this.genero = genero;
        this.tipo = tipo;
        this.duracion = duracion;
        this.temporadas = temporadas;
        this.capitulosPorTemporada = capitulosPorTemporada == null
            ? List.of() : List.copyOf(capitulosPorTemporada);
    }
}
