package libreria.RegistroUsuario.dto;

import java.time.LocalDateTime;

import libreria.RegistroUsuario.model.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private TipoUsuario tipo;
    private LocalDateTime fechaRegistro;
    private Boolean activo;
}
