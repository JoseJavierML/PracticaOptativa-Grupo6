package es.uclm.esiiab.gps.biblioteca.usuario;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    public static final String ATRIBUTO_USUARIO_ID = "usuarioId";

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Credenciales credenciales,
            HttpServletRequest solicitud) {
        Usuario usuario = servicio.registrar(credenciales.nombreUsuario(), credenciales.contrasena());
        iniciarSesion(solicitud, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumen(usuario));
    }

    @PostMapping("/sesion")
    public ResponseEntity<?> iniciarSesion(@RequestBody Credenciales credenciales,
            HttpServletRequest solicitud) {
        Usuario usuario = servicio.autenticar(credenciales.nombreUsuario(), credenciales.contrasena());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nombre de usuario o contraseña incorrectos."));
        }
        iniciarSesion(solicitud, usuario);
        return ResponseEntity.ok(resumen(usuario));
    }

    @GetMapping("/sesion")
    public ResponseEntity<?> sesion(HttpSession sesion) {
        if (sesion == null || !(sesion.getAttribute(ATRIBUTO_USUARIO_ID) instanceof Number id)) {
            return noAutorizado();
        }
        Usuario usuario = servicio.buscarPorId(id.longValue());
        return usuario == null ? noAutorizado() : ResponseEntity.ok(resumen(usuario));
    }

    @DeleteMapping("/sesion")
    public ResponseEntity<Void> cerrarSesion(HttpServletRequest solicitud) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosNoValidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    private void iniciarSesion(HttpServletRequest solicitud, Usuario usuario) {
        HttpSession anterior = solicitud.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        solicitud.getSession(true).setAttribute(ATRIBUTO_USUARIO_ID, usuario.getId());
    }

    private UsuarioResumen resumen(Usuario usuario) {
        return new UsuarioResumen(usuario.getId(), usuario.getNombreUsuario());
    }

    private ResponseEntity<Map<String, String>> noAutorizado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Inicia sesión para continuar."));
    }

    public record Credenciales(String nombreUsuario, String contrasena) {
    }

    public record UsuarioResumen(Long id, String nombreUsuario) {
    }
}
