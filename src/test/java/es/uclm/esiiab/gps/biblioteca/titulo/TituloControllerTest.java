package es.uclm.esiiab.gps.biblioteca.titulo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TituloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void creaYListaUnaPelicula() throws Exception {
        mockMvc.perform(post("/api/titulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Dune","anio":2021,"generoId":5,
                                 "tipo":"PELICULA","duracion":155}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Dune"))
                .andExpect(jsonPath("$.genero.nombre").value("Drama"))
                .andExpect(jsonPath("$.duracion").value(155));

        mockMvc.perform(get("/api/titulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Dune"));
    }

    @Test
    void creaUnaSerieConTemporadas() throws Exception {
        mockMvc.perform(post("/api/titulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Dark","anio":2017,"generoId":2,
                                 "tipo":"SERIE","temporadas":3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("SERIE"))
                .andExpect(jsonPath("$.temporadas").value(3));
    }

    @Test
    void actualizaUnTituloExistente() throws Exception {
        String respuesta = mockMvc.perform(post("/api/titulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Dune","anio":2021,"generoId":5,
                                 "tipo":"PELICULA","duracion":155}
                                """))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(put("/api/titulos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Dune: Parte Uno","anio":2022,"generoId":6,
                                 "tipo":"PELICULA","duracion":156}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Dune: Parte Uno"))
                .andExpect(jsonPath("$.anio").value(2022))
                .andExpect(jsonPath("$.genero.nombre").value("Fantasía"));
    }

    @Test
    void borraUnTituloExistente() throws Exception {
        String respuesta = mockMvc.perform(post("/api/titulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Alien","anio":1979,"generoId":7,
                                 "tipo":"PELICULA","duracion":117}
                                """))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(delete("/api/titulos/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/titulos"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rechazaTituloSinCamposObligatorios() throws Exception {
        mockMvc.perform(post("/api/titulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":" ","anio":2020,"tipo":"PELICULA","duracion":90}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El título es obligatorio."));
    }

    @Test
    void rechazaPeliculaSinDuracion() throws Exception {
        mockMvc.perform(post("/api/titulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Sin duración","anio":2020,"generoId":1,
                                 "tipo":"PELICULA"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La duración de la película debe ser positiva."));
    }

    @Test
    void devuelve404AlModificarTituloInexistente() throws Exception {
        mockMvc.perform(put("/api/titulos/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Dune","anio":2021,"generoId":1,
                                 "tipo":"PELICULA","duracion":155}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void filtraTitulosPorGeneroYAnio() throws Exception {
        crearPelicula("Dune", 2021, 5);
        crearPelicula("Alien", 1979, 7);

        mockMvc.perform(get("/api/titulos").param("generoId", "5").param("anio", "2021"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Dune"));
    }

    @Test
    void filtraTitulosPorEstadoDeLaBibliotecaDelUsuario() throws Exception {
        MockHttpSession sesion = sesionUsuario(42L);
        String respuesta = crearPelicula("Dune", 2021, 5);
        long tituloId = Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));
        crearPelicula("Alien", 1979, 7);

        mockMvc.perform(post("/api/biblioteca").session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloId\":" + tituloId + ",\"estado\":\"VISTO\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/titulos").session(sesion)
                        .param("generoId", "5")
                        .param("anio", "2021")
                        .param("estado", "VISTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Dune"));
    }

    @Test
    void devuelveListaVaciaCuandoLosFiltrosNoEncuentranResultados() throws Exception {
        crearPelicula("Dune", 2021, 5);

        mockMvc.perform(get("/api/titulos").param("generoId", "5").param("anio", "1999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String crearPelicula(String titulo, int anio, int generoId) throws Exception {
        return mockMvc.perform(post("/api/titulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"%s","anio":%d,"generoId":%d,
                                 "tipo":"PELICULA","duracion":120}
                                """.formatted(titulo, anio, generoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private MockHttpSession sesionUsuario(long usuarioId) {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioId", usuarioId);
        return sesion;
    }
}
