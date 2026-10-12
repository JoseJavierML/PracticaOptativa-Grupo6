package es.uclm.esiiab.gps.biblioteca.serie;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import es.uclm.esiiab.gps.biblioteca.biblioteca.BibliotecaItem;
import es.uclm.esiiab.gps.biblioteca.biblioteca.BibliotecaItemRepository;
import es.uclm.esiiab.gps.biblioteca.titulo.TipoTitulo;
import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloRepository;
import es.uclm.esiiab.gps.biblioteca.usuario.Usuario;
import es.uclm.esiiab.gps.biblioteca.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ProgresoSerieServiceTest {

    @Mock
    private ProgresoCapituloRepository progresoRepository;

    @Mock
    private BibliotecaItemRepository bibliotecaRepository;

    @Mock
    private TituloRepository tituloRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProgresoSerieService servicio;

    @Test
    void rechazaUnCapituloQueNoExisteSinGuardarProgreso() {
        long usuarioId = 7L;
        long serieId = 12L;
        Titulo serie = new Titulo("Serie", 2025, null, TipoTitulo.SERIE,
                null, 1, List.of(2));
        ReflectionTestUtils.setField(serie, "id", serieId);
        when(bibliotecaRepository.findByUsuarioIdAndTitulo_Id(usuarioId, serieId))
                .thenReturn(Optional.of(mock(BibliotecaItem.class)));
        when(tituloRepository.findById(serieId)).thenReturn(Optional.of(serie));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(new Usuario("usuario", "hash")));

        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(usuarioId, serieId,
                new ProgresoSerieService.ActualizarCapitulo(1, 3, true)));

        verify(progresoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}