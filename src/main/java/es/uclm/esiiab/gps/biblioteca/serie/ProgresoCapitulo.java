package es.uclm.esiiab.gps.biblioteca.serie;

import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "progreso_capitulo", uniqueConstraints = @UniqueConstraint(
        name = "uk_progreso_usuario_serie_capitulo",
        columnNames = {"usuario_id", "serie_id", "temporada", "capitulo"}))
public class ProgresoCapitulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serie_id", nullable = false)
    private Titulo serie;

    @Column(nullable = false)
    private Integer temporada;

    @Column(nullable = false)
    private Integer capitulo;

    @Column(nullable = false)
    private boolean visto;

    protected ProgresoCapitulo() {
    }

    public ProgresoCapitulo(Usuario usuario, Titulo serie, Integer temporada, Integer capitulo, boolean visto) {
        this.usuario = usuario;
        this.serie = serie;
        this.temporada = temporada;
        this.capitulo = capitulo;
        this.visto = visto;
    }

    public boolean isVisto() {
        return visto;
    }

    public Integer getTemporada() {
        return temporada;
    }

    public Integer getCapitulo() {
        return capitulo;
    }

    public void setVisto(boolean visto) {
        this.visto = visto;
    }
}