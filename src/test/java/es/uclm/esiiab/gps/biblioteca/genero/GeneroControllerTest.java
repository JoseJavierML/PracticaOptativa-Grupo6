package es.uclm.esiiab.gps.biblioteca.genero;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GeneroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaLosGenerosIniciales() throws Exception {
        mockMvc.perform(get("/api/generos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].nombre").value("Animación"));
    }

    @Test
    void creaUnGeneroNuevo() throws Exception {
        mockMvc.perform(post("/api/generos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Western\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Western"));
    }

    @Test
    void rechazaUnGeneroSinNombre() throws Exception {
        mockMvc.perform(post("/api/generos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void rechazaUnGeneroDuplicado() throws Exception {
        mockMvc.perform(post("/api/generos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"drama\"}"))
                .andExpect(status().isBadRequest());
    }
}
