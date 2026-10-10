package es.uclm.esiiab.gps.biblioteca.biblioteca;

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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BibliotecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rechazaConsultasSinSesionDeUsuario() throws Exception {
        mockMvc.perform(get("/api/biblioteca"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
        mockMvc.perform(post("/api/biblioteca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/biblioteca/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"VISTO\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/biblioteca/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permiteAnadirCambiarConsultarYEliminarSoloLaRelacionPropia() throws Exception {
        MockHttpSession sesionUsuario1 = sesion(1L);
        mockMvc.perform(post("/api/biblioteca")
                        .session(sesionUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoTitulo":"PELICULA","tituloId":12,
                                 "titulo":"La película","estado":"PENDIENTE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.fechaModificacion").exists());

        mockMvc.perform(get("/api/biblioteca").session(sesionUsuario1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tituloId").value(12));
        mockMvc.perform(get("/api/biblioteca").session(sesion(2L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(put("/api/biblioteca/1")
                        .session(sesionUsuario1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"VISTO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VISTO"));

        mockMvc.perform(delete("/api/biblioteca/1").session(sesionUsuario1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/biblioteca").session(sesionUsuario1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void noPermiteModificarElementosDeOtroUsuario() throws Exception {
        mockMvc.perform(post("/api/biblioteca")
                        .session(sesion(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoTitulo":"SERIE","tituloId":7,
                                 "titulo":"La serie","estado":"ABANDONADO"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/biblioteca/1")
                        .session(sesion(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"VISTO\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/biblioteca/1").session(sesion(2L)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/biblioteca").session(sesion(1L)))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void rechazaEstadoNoValidoYDuplicados() throws Exception {
        MockHttpSession sesion = sesion(4L);
        String contenido = """
                {"tipoTitulo":"PELICULA","tituloId":21,
                 "titulo":"Duplicada","estado":"VISTO"}
                """;
        mockMvc.perform(post("/api/biblioteca").session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contenido))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/biblioteca").session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contenido))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/biblioteca").session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoTitulo":"PELICULA","tituloId":22,
                                 "titulo":"Sin estado"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/biblioteca").session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        mockMvc.perform(post("/api/biblioteca").session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoTitulo":"PELICULA","tituloId":23,
                                 "titulo":"Estado incorrecto","estado":"EN_CURSO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    private MockHttpSession sesion(Long usuarioId) {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioId", usuarioId);
        return sesion;
    }
}
