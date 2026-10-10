package es.uclm.esiiab.gps.biblioteca.biblioteca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloRepository;

@ExtendWith(MockitoExtension.class)
class BibliotecaServiceTest {

    @Mock
    private BibliotecaItemRepository repositorio;

        @Mock
        private TituloRepository tituloRepositorio;

        @Mock
        private Titulo titulo;

    @InjectMocks
    private BibliotecaService servicio;

    @Test
    void noAceptaUnTituloSinNombre() {
        BibliotecaService.NuevoBibliotecaItem peticion = new BibliotecaService.NuevoBibliotecaItem(
                null, EstadoBiblioteca.PENDIENTE);

        assertThrows(IllegalArgumentException.class, () -> servicio.anadir(9L, peticion));
    }

    @Test
    void noPermiteAnadirDosVecesElMismoTituloAlMismoUsuario() {
        when(tituloRepositorio.findById(3L)).thenReturn(Optional.of(titulo));
        when(repositorio.existsByUsuarioIdAndTitulo_Id(9L, 3L)).thenReturn(true);
        BibliotecaService.NuevoBibliotecaItem peticion = new BibliotecaService.NuevoBibliotecaItem(
                3L, EstadoBiblioteca.PENDIENTE);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.anadir(9L, peticion));

        assertEquals("Ese título ya está en tu biblioteca.", error.getMessage());
        verify(repositorio).existsByUsuarioIdAndTitulo_Id(9L, 3L);
    }
}
