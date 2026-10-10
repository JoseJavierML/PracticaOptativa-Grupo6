package es.uclm.esiiab.gps.biblioteca.usuario;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import es.uclm.esiiab.gps.biblioteca.genero.Genero;
import es.uclm.esiiab.gps.biblioteca.genero.GeneroRepository;
import es.uclm.esiiab.gps.biblioteca.titulo.TipoTitulo;
import es.uclm.esiiab.gps.biblioteca.titulo.Titulo;
import es.uclm.esiiab.gps.biblioteca.titulo.TituloRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

        @Autowired
        private GeneroRepository generoRepository;

        @Autowired
        private TituloRepository tituloRepository;

    @Test
    void registraCuentaEIniciaSesionSinGuardarLaContrasenaEnClaro() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"CineFan\",\"contrasena\":\"contrasena-segura\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreUsuario").value("cinefan"))
                .andReturn();

        Usuario usuario = usuarioRepository.findByNombreUsuarioIgnoreCase("cinefan").orElseThrow();
        assertNotEquals("contrasena-segura", usuario.getHashContrasena());
        MockHttpSession sesion = (MockHttpSession) resultado.getRequest().getSession(false);
        mockMvc.perform(get("/api/usuarios/sesion").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId()));
    }

    @Test
    void permiteIniciarYCerrarSesionConCredencialesValidas() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"usuario1\",\"contrasena\":\"contrasena-segura\"}"))
                .andExpect(status().isCreated());

        MvcResult resultado = mockMvc.perform(post("/api/usuarios/sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"USUARIO1\",\"contrasena\":\"contrasena-segura\"}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession sesion = (MockHttpSession) resultado.getRequest().getSession(false);

        mockMvc.perform(delete("/api/usuarios/sesion").session(sesion))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/usuarios/sesion").session(sesion))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void laSesionDeRegistroPermiteAnadirUnTituloRealALaBiblioteca() throws Exception {
        MvcResult registro = mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"cinefan\",\"contrasena\":\"contrasena-segura\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpSession sesion = (MockHttpSession) registro.getRequest().getSession(false);
        Genero genero = generoRepository.findAll().get(0);
        Titulo titulo = tituloRepository.save(new Titulo(
                "Película favorita", 2025, genero, TipoTitulo.PELICULA, 120, null));

        mockMvc.perform(post("/api/biblioteca")
                        .session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloId\":" + titulo.getId()
                                + ",\"estado\":\"PENDIENTE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Película favorita"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void rechazaContrasenaIncorrectaYRegistroDuplicado() throws Exception {
        String registro = "{\"nombreUsuario\":\"usuario1\",\"contrasena\":\"contrasena-segura\"}";
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/usuarios/sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"usuario1\",\"contrasena\":\"incorrecta-segura\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/usuarios/sesion"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validaNombreYLongitudDeContrasena() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ab\",\"contrasena\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
