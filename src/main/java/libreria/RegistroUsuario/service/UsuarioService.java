package libreria.RegistroUsuario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.RegistroUsuario.dto.UsuarioDTO;
import libreria.RegistroUsuario.dto.UsuarioRequest;
import libreria.RegistroUsuario.model.Usuario;
import libreria.RegistroUsuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNombreCompleto(),
            usuario.getEmail(),
            usuario.getTipo(),
            usuario.getFechaRegistro(),
            usuario.getActivo()
        );
    }

    @Transactional
    public UsuarioDTO registrar(UsuarioRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya esta registrado");
        }
        if (request.getTipo() == null) {
            request.setTipo(libreria.RegistroUsuario.model.enums.TipoUsuario.Cliente);
        }
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setTipo(request.getTipo());
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setActivo(true);
        return toDTO(usuarioRepository.save(usuario));
    }

    public Optional<UsuarioDTO> obtenerPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    public Optional<UsuarioDTO> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(this::toDTO);
    }

    public List<UsuarioDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioRequest request) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!existente.getEmail().equals(request.getEmail()) &&
                usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya esta registrado por otro usuario");
        }
        existente.setNombreCompleto(request.getNombreCompleto());
        existente.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existente.setPassword(request.getPassword());
        }
        if (request.getTipo() != null) {
            existente.setTipo(request.getTipo());
        }
        return toDTO(usuarioRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public UsuarioDTO cambiarEstado(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(activo);
        return toDTO(usuarioRepository.save(usuario));
    }
}
