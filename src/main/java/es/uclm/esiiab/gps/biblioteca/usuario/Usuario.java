package es.uclm.esiiab.gps.biblioteca.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nombreUsuario;

    @Column(nullable = false, length = 200)
    private String hashContrasena;

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String hashContrasena) {
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }
}
