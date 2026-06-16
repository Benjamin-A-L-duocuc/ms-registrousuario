package libreria.RegistroUsuario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.RegistroUsuario.model.Usuario;
import libreria.RegistroUsuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Transactional
	public Usuario guardar(Usuario usuario) {
		return usuarioRepository.save(usuario);
	}

	public Optional<Usuario> obtenerPorId(Long id) {
		return usuarioRepository.findById(id);
	}

	public List<Usuario> obtenerTodos() {
		return usuarioRepository.findAll();
	}

	@Transactional
	public Usuario actualizar(Long id, Usuario usuario) {
		Usuario existente = usuarioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		existente.setNombreCompleto(usuario.getNombreCompleto());
		existente.setEmail(usuario.getEmail());
		existente.setPassword(usuario.getPassword());
		existente.setTipo(usuario.getTipo());
		existente.setFechaRegistro(usuario.getFechaRegistro());
		existente.setActivo(usuario.getActivo());

		return usuarioRepository.save(existente);
	}

	@Transactional
	public void eliminar(Long id) {
		usuarioRepository.deleteById(id);
	}
}
