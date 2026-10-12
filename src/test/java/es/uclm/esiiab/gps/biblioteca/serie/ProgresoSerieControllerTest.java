package es.uclm.esiiab.gps.biblioteca.serie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import es.uclm.esiiab.gps.biblioteca.biblioteca.BibliotecaService;
import es.uclm.esiiab.gps.biblioteca.genero.GeneroRepository;
import es.uclm.esiiab.gps.biblioteca.titulo.TipoTitulo;
import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloRepository;
import es.uclm.esiiab.gps.biblioteca.usuario.Usuario;
import es.uclm.esiiab.gps.biblioteca.usuario.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProgresoSerieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeneroRepository generoRepository;

    @Autowired
    private TituloRepository tituloRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void actualizaProgresoYMarcaLaTemporadaCompleta() throws Exception {
        Usuario usuario = crearUsuario("progreso");
        Titulo serie = crearSerie();
        MockHttpSession sesion = sesion(usuario.getId());
        anadirBiblioteca(sesion, serie.getId());

        for (int capitulo = 1; capitulo <= 3; capitulo++) {
            marcar(sesion, serie.getId(), capitulo, true)
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/biblioteca/series/{id}/capitulos", serie.getId()).session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalleTemporadas[0].capitulosVistos").value(3))
                .andExpect(jsonPath("$.detalleTemporadas[0].totalCapitulos").value(10))
                .andExpect(jsonPath("$.detalleTemporadas[0].vista").value(false));

        for (int capitulo = 4; capitulo <= 10; capitulo++) {
            marcar(sesion, serie.getId(), capitulo, true)
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/biblioteca/series/{id}/capitulos", serie.getId()).session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalleTemporadas[0].capitulosVistos").value(10))
                .andExpect(jsonPath("$.detalleTemporadas[0].vista").value(true));

        marcar(sesion, serie.getId(), 10, false)
                .andExpect(jsonPath("$.detalleTemporadas[0].capitulosVistos").value(9))
                .andExpect(jsonPath("$.detalleTemporadas[0].vista").value(false));
    }

    @Test
    void exigeSesionYQueLaSerieEsteEnLaBibliotecaDelUsuario() throws Exception {
        Usuario propietario = crearUsuario("propietario");
        Usuario otroUsuario = crearUsuario("otro-usuario");
        Titulo serie = crearSerie();
        MockHttpSession sesionPropietario = sesion(propietario.getId());
        anadirBiblioteca(sesionPropietario, serie.getId());

        mockMvc.perform(get("/api/biblioteca/series/{id}/capitulos", serie.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/biblioteca/series/{id}/capitulos", serie.getId())
                        .session(sesion(otroUsuario.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"temporada\":1,\"capitulo\":1,\"visto\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaCapitulosFueraDelDesglose() throws Exception {
        Usuario usuario = crearUsuario("validacion");
        Titulo serie = crearSerie();
        MockHttpSession sesion = sesion(usuario.getId());
        anadirBiblioteca(sesion, serie.getId());

        marcar(sesion, serie.getId(), 11, true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    private Usuario crearUsuario(String nombre) {
        return usuarioRepository.saveAndFlush(new Usuario(nombre, "hash"));
    }

    private Titulo crearSerie() {
        Titulo serie = new Titulo("Serie", 2025, generoRepository.findAll().get(0),
                TipoTitulo.SERIE, null, 1, java.util.List.of(10));
        return tituloRepository.saveAndFlush(serie);
    }

    private void anadirBiblioteca(MockHttpSession sesion, Long tituloId) throws Exception {
        mockMvc.perform(post("/api/biblioteca")
                        .session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloId\":" + tituloId + ",\"estado\":\"PENDIENTE\"}"))
                .andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.ResultActions marcar(
            MockHttpSession sesion, Long serieId, int capitulo, boolean visto) throws Exception {
        return mockMvc.perform(post("/api/biblioteca/series/{id}/capitulos", serieId)
                .session(sesion)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"temporada\":1,\"capitulo\":" + capitulo + ",\"visto\":" + visto + "}"));
    }

    private MockHttpSession sesion(Long usuarioId) {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioId", usuarioId);
        return sesion;
    }
}