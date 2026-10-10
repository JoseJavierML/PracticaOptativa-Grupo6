package es.uclm.esiiab.gps.biblioteca.titulo;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esiiab.gps.biblioteca.biblioteca.EstadoBiblioteca;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/titulos")
public class TituloController {

    private final TituloService servicio;

    public TituloController(TituloService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Long generoId,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) EstadoBiblioteca estado,
            HttpSession sesion) {
        Long usuarioId = usuarioId(sesion);
        if (estado != null && usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Inicia sesión para filtrar por estado."));
        }
        return ResponseEntity.ok(servicio.buscar(generoId, anio, estado, usuarioId));
    }

    @PostMapping
    public ResponseEntity<Titulo> crear(@RequestBody TituloService.DatosTitulo datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    public Titulo actualizar(@PathVariable Long id, @RequestBody TituloService.DatosTitulo datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        servicio.borrar(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosNoValidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(TituloNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(TituloNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
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
}
