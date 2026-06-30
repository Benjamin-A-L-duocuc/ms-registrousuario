package libreria.RegistroUsuario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import libreria.RegistroUsuario.dto.UsuarioDTO;
import libreria.RegistroUsuario.dto.UsuarioRequest;
import libreria.RegistroUsuario.model.enums.TipoUsuario;
import libreria.RegistroUsuario.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void crearReturns200() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Juan", "juan@test.com", "Test1234!", TipoUsuario.Cliente);
        UsuarioDTO dto = new UsuarioDTO(1L, "Juan", "juan@test.com", TipoUsuario.Cliente, LocalDateTime.now(), true);
        when(usuarioService.registrar(any(UsuarioRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void crearWithBadPasswordReturns400() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Juan", "juan@test.com", "12345678", TipoUsuario.Cliente);

        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La contrasena debe tener al menos 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial"));
    }

    @Test
    void obtenerTodosReturns200() throws Exception {
        List<UsuarioDTO> usuarios = List.of(
            new UsuarioDTO(1L, "Juan", "juan@test.com", TipoUsuario.Cliente, LocalDateTime.now(), true),
            new UsuarioDTO(2L, "Ana", "ana@test.com", TipoUsuario.AdministradorGeneral, LocalDateTime.now(), true)
        );
        when(usuarioService.obtenerTodos()).thenReturn(usuarios);

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void obtenerPorIdReturns200() throws Exception {
        Long id = 1L;
        UsuarioDTO dto = new UsuarioDTO(id, "Juan", "juan@test.com", TipoUsuario.Cliente, LocalDateTime.now(), true);
        when(usuarioService.obtenerPorId(id)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void obtenerPorIdReturns404() throws Exception {
        Long id = 999L;
        when(usuarioService.obtenerPorId(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/usuarios/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorEmailReturns200() throws Exception {
        String email = "juan@test.com";
        UsuarioDTO dto = new UsuarioDTO(1L, "Juan", email, TipoUsuario.Cliente, LocalDateTime.now(), true);
        when(usuarioService.obtenerPorEmail(email)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/usuarios/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void obtenerPorEmailReturns404() throws Exception {
        String email = "noexiste@test.com";
        when(usuarioService.obtenerPorEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/usuarios/email/{email}", email))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarReturns200() throws Exception {
        Long id = 1L;
        UsuarioRequest request = new UsuarioRequest("Juan Actualizado", "juan@test.com", "Test1234!", TipoUsuario.AdministradorGeneral);
        UsuarioDTO dto = new UsuarioDTO(id, "Juan Actualizado", "juan@test.com", TipoUsuario.AdministradorGeneral, LocalDateTime.now(), true);
        when(usuarioService.actualizar(any(Long.class), any(UsuarioRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/usuarios/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Actualizado"));
    }

    @Test
    void cambiarEstadoReturns200() throws Exception {
        Long id = 1L;
        UsuarioDTO dto = new UsuarioDTO(id, "Juan", "juan@test.com", TipoUsuario.Cliente, LocalDateTime.now(), false);
        when(usuarioService.cambiarEstado(id, false)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/usuarios/{id}/estado", id)
                .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void eliminarReturns204() throws Exception {
        Long id = 1L;
        doNothing().when(usuarioService).eliminar(id);

        mockMvc.perform(delete("/api/v1/usuarios/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarReturns404() throws Exception {
        Long id = 999L;
        doThrow(new RuntimeException("Usuario no encontrado")).when(usuarioService).eliminar(id);

        mockMvc.perform(delete("/api/v1/usuarios/{id}", id))
                .andExpect(status().isNotFound());
    }
}
