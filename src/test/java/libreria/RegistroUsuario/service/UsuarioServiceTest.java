package libreria.RegistroUsuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import libreria.RegistroUsuario.dto.UsuarioDTO;
import libreria.RegistroUsuario.dto.UsuarioRequest;
import libreria.RegistroUsuario.model.Usuario;
import libreria.RegistroUsuario.model.enums.TipoUsuario;
import libreria.RegistroUsuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarSuccessWithDefaultTipoCliente() {
        UsuarioRequest request = new UsuarioRequest("Juan", "juan@test.com", "Password1!", null);
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioDTO dto = usuarioService.registrar(request);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombreCompleto()).isEqualTo("Juan");
        assertThat(dto.getEmail()).isEqualTo("juan@test.com");
        assertThat(dto.getTipo()).isEqualTo(TipoUsuario.Cliente);
        assertThat(dto.getActivo()).isTrue();
        assertThat(dto.getFechaRegistro()).isNotNull();
        verify(usuarioRepository).findByEmail(request.getEmail());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrarDuplicateEmailThrows() {
        UsuarioRequest request = new UsuarioRequest("Juan", "juan@test.com", "Password1!", TipoUsuario.Cliente);
        Usuario existente = new Usuario(1L, "Otro", "juan@test.com", "pass", TipoUsuario.Cliente, LocalDateTime.now(), true);
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El email ya esta registrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void obtenerPorId() {
        Long id = 1L;
        Usuario usuario = new Usuario(id, "Juan", "juan@test.com", "pass", TipoUsuario.Cliente, LocalDateTime.now(), true);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        Optional<UsuarioDTO> result = usuarioService.obtenerPorId(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void obtenerPorEmail() {
        String email = "juan@test.com";
        Usuario usuario = new Usuario(1L, "Juan", email, "pass", TipoUsuario.Cliente, LocalDateTime.now(), true);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        Optional<UsuarioDTO> result = usuarioService.obtenerPorEmail(email);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    void obtenerTodos() {
        List<Usuario> usuarios = List.of(
            new Usuario(1L, "Juan", "juan@test.com", "pass", TipoUsuario.Cliente, LocalDateTime.now(), true),
            new Usuario(2L, "Ana", "ana@test.com", "pass", TipoUsuario.AdministradorGeneral, LocalDateTime.now(), true)
        );
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<UsuarioDTO> result = usuarioService.obtenerTodos();

        assertThat(result).hasSize(2);
    }

    @Test
    void actualizarSuccess() {
        Long id = 1L;
        Usuario existente = new Usuario(id, "Juan", "juan@test.com", "pass", TipoUsuario.Cliente, LocalDateTime.now(), true);
        UsuarioRequest request = new UsuarioRequest("Juan Actualizado", "juan@test.com", "NewPass1!", TipoUsuario.AdministradorGeneral);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO dto = usuarioService.actualizar(id, request);

        assertThat(dto.getNombreCompleto()).isEqualTo("Juan Actualizado");
        assertThat(dto.getEmail()).isEqualTo("juan@test.com");
        assertThat(dto.getTipo()).isEqualTo(TipoUsuario.AdministradorGeneral);
    }

    @Test
    void actualizarDuplicateEmailThrows() {
        Long id = 1L;
        Usuario existente = new Usuario(id, "Juan", "juan@test.com", "pass", TipoUsuario.Cliente, LocalDateTime.now(), true);
        UsuarioRequest request = new UsuarioRequest("Juan", "otro@test.com", "NewPass1!", TipoUsuario.Cliente);
        Usuario otro = new Usuario(2L, "Otro", "otro@test.com", "pass", TipoUsuario.Cliente, LocalDateTime.now(), true);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> usuarioService.actualizar(id, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El email ya esta registrado por otro usuario");
    }

    @Test
    void eliminarNotFoundThrows() {
        Long id = 999L;
        when(usuarioRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.eliminar(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");

        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    void eliminarSuccess() {
        Long id = 1L;
        when(usuarioRepository.existsById(id)).thenReturn(true);

        usuarioService.eliminar(id);

        verify(usuarioRepository).deleteById(id);
    }

    @Test
    void cambiarEstado() {
        Long id = 1L;
        Usuario usuario = new Usuario(id, "Juan", "juan@test.com", "pass", TipoUsuario.Cliente, LocalDateTime.now(), true);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO dto = usuarioService.cambiarEstado(id, false);

        assertThat(dto.getActivo()).isFalse();
    }
}
