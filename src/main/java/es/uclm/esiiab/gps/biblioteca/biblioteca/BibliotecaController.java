package es.uclm.esiiab.gps.biblioteca.biblioteca;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    private static final String ATRIBUTO_USUARIO_ID = "usuarioId";

    private final BibliotecaService servicio;

    public BibliotecaController(BibliotecaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpSession sesion) {
        Long usuarioId = usuarioId(sesion);
        if (usuarioId == null) {
            return noAutorizado();
        }
        List<BibliotecaItem> items = servicio.listar(usuarioId);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<?> anadir(@RequestBody BibliotecaService.NuevoBibliotecaItem peticion,
            HttpSession sesion) {
        Long usuarioId = usuarioId(sesion);
        if (usuarioId == null) {
            return noAutorizado();
        }
        BibliotecaItem creado = servicio.anadir(usuarioId, peticion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
            @RequestBody BibliotecaService.ActualizarEstado peticion, HttpSession sesion) {
        Long usuarioId = usuarioId(sesion);
        if (usuarioId == null) {
            return noAutorizado();
        }
        return ResponseEntity.ok(servicio.actualizarEstado(usuarioId, id, peticion.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession sesion) {
        Long usuarioId = usuarioId(sesion);
        if (usuarioId == null) {
            return noAutorizado();
        }
        servicio.eliminar(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosNoValidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BibliotecaItemNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(BibliotecaItemNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> solicitudNoLegible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "La solicitud contiene datos no válidos."));
    }

    private Long usuarioId(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        Object valor = sesion.getAttribute(ATRIBUTO_USUARIO_ID);
        if (valor instanceof Number numero && numero.longValue() > 0) {
            return numero.longValue();
        }
        if (valor instanceof String texto) {
            try {
                long id = Long.parseLong(texto);
                return id > 0 ? id : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private ResponseEntity<Map<String, String>> noAutorizado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Inicia sesión para consultar tu biblioteca."));
    }
}
