package es.uclm.esiiab.gps.biblioteca.biblioteca;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.titulo.TipoTitulo;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_biblioteca_usuario_titulo",
    columnNames = {"usuario_id", "titulo_id"}))
public class BibliotecaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "titulo_id", nullable = false)
    private Titulo titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_titulo", nullable = false, length = 20)
    private TipoTitulo tipoTitulo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String nombreTitulo;

    @Column(name = "nombre_titulo", nullable = false, length = 200)
    private String nombreTituloMigracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBiblioteca estado;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    protected BibliotecaItem() {
    }

    public BibliotecaItem(Long usuarioId, Titulo titulo, EstadoBiblioteca estado) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.tipoTitulo = titulo.getTipo();
        this.nombreTitulo = titulo.getTitulo();
        this.nombreTituloMigracion = titulo.getTitulo();
        this.estado = estado;
    }

    @PrePersist
    @PreUpdate
    void actualizarFechaModificacion() {
        fechaModificacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Titulo getTitulo() {
        return titulo;
    }

    public EstadoBiblioteca getEstado() {
        return estado;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setEstado(EstadoBiblioteca estado) {
        this.estado = estado;
    }
}
