package es.uclm.esiiab.gps.biblioteca.biblioteca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BibliotecaServiceTest {

    @Mock
    private BibliotecaItemRepository repositorio;

    @InjectMocks
    private BibliotecaService servicio;

    @Test
    void noAceptaUnTituloSinNombre() {
        BibliotecaService.NuevoBibliotecaItem peticion = new BibliotecaService.NuevoBibliotecaItem(
                TipoTitulo.PELICULA, 3L, "  ", EstadoBiblioteca.PENDIENTE);

        assertThrows(IllegalArgumentException.class, () -> servicio.anadir(9L, peticion));
    }

    @Test
    void noPermiteAnadirDosVecesElMismoTituloAlMismoUsuario() {
        when(repositorio.existsByUsuarioIdAndTipoTituloAndTituloId(
                9L, TipoTitulo.SERIE, 3L)).thenReturn(true);
        BibliotecaService.NuevoBibliotecaItem peticion = new BibliotecaService.NuevoBibliotecaItem(
                TipoTitulo.SERIE, 3L, "Una serie", EstadoBiblioteca.PENDIENTE);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.anadir(9L, peticion));

        assertEquals("Ese título ya está en tu biblioteca.", error.getMessage());
        verify(repositorio).existsByUsuarioIdAndTipoTituloAndTituloId(
                9L, TipoTitulo.SERIE, 3L);
    }
}
