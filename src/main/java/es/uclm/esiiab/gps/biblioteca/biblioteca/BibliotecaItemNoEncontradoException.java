package es.uclm.esiiab.gps.biblioteca.biblioteca;

public class BibliotecaItemNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BibliotecaItemNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
