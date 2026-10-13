package api;

import java.util.List;

/*
 * Todo lo que la cuenta que pide puede ver, ya filtrado por su rol.
 */
public record EstadoDto(CuentaDto cuenta, List<UsuarioDto> usuarios, List<ArtistaDto> artistas,
                        List<EventoDto> eventos, List<RegistroDto> registros) {
}
