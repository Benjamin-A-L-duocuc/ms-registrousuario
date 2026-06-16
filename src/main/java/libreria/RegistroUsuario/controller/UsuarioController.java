package libreria.RegistroUsuario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import libreria.RegistroUsuario.model.Usuario;
import libreria.RegistroUsuario.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;

	@PostMapping
	public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
		Usuario guardado = usuarioService.guardar(usuario);
		return ResponseEntity.ok(guardado);
	}

	@GetMapping
	public ResponseEntity<List<Usuario>> obtenerTodos() {
		return ResponseEntity.ok(usuarioService.obtenerTodos());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
		return usuarioService.obtenerPorId(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
		try {
			Usuario actualizado = usuarioService.actualizar(id, usuario);
			return ResponseEntity.ok(actualizado);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		usuarioService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
