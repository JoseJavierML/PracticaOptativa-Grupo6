package es.uclm.esiiab.gps.biblioteca.serie;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esiiab.gps.biblioteca.titulo.TituloNoEncontradoException;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/biblioteca/series")
public class ProgresoSerieController {

    private final ProgresoSerieService servicio;

    public ProgresoSerieController(ProgresoSerieService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}/capitulos")
    public ResponseEntity<?> consultar(@PathVariable Long id, HttpSession sesion) {
        Long usuarioId = usuarioId(sesion);
        if (usuarioId == null) {
            return noAutorizado();
        }
        return ResponseEntity.ok(servicio.consultar(usuarioId, id));
    }

    @PostMapping("/{id}/capitulos")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
            @RequestBody ProgresoSerieService.ActualizarCapitulo peticion, HttpSession sesion) {
        Long usuarioId = usuarioId(sesion);
        if (usuarioId == null) {
            return noAutorizado();
        }
        return ResponseEntity.ok(servicio.actualizar(usuarioId, id, peticion));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosNoValidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(TituloNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(TituloNoEncontradoException e) {
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
        Object valor = sesion.getAttribute("usuarioId");
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