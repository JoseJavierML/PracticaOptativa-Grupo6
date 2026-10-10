package es.uclm.esiiab.gps.biblioteca.titulo;

public class TituloNoEncontradoException extends RuntimeException {

    public TituloNoEncontradoException(Long id) {
        super("No existe un título con identificador " + id + ".");
    }
}
