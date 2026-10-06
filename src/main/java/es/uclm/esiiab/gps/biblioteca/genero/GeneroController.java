package es.uclm.esiiab.gps.biblioteca.genero;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de géneros.
 *   GET  /api/generos        lista los géneros ordenados por nombre
 *   POST /api/generos        crea un género. Cuerpo: {"nombre": "Western"}
 */
@RestController
@RequestMapping("/api/generos")
public class GeneroController {

    private final GeneroService servicio;

    public GeneroController(GeneroService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Genero> listar() {
        return servicio.listar();
    }

    @PostMapping
    public ResponseEntity<Genero> crear(@RequestBody NuevoGenero peticion) {
        Genero creado = servicio.crear(peticion.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosNoValidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /** Datos que envía el cliente para crear un género. */
    public record NuevoGenero(String nombre) {
    }
}
