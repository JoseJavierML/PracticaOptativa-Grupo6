package es.uclm.esiiab.gps.biblioteca.biblioteca;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_biblioteca_usuario_titulo",
        columnNames = {"usuario_id", "tipo_titulo", "titulo_id"}))
public class BibliotecaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_titulo", nullable = false, length = 20)
    private TipoTitulo tipoTitulo;

    @Column(name = "titulo_id", nullable = false)
    private Long tituloId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBiblioteca estado;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    protected BibliotecaItem() {
    }

    public BibliotecaItem(Long usuarioId, TipoTitulo tipoTitulo, Long tituloId,
            String titulo, EstadoBiblioteca estado) {
        this.usuarioId = usuarioId;
        this.tipoTitulo = tipoTitulo;
        this.tituloId = tituloId;
        this.titulo = titulo;
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

    public TipoTitulo getTipoTitulo() {
        return tipoTitulo;
    }

    public Long getTituloId() {
        return tituloId;
    }

    public String getTitulo() {
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
