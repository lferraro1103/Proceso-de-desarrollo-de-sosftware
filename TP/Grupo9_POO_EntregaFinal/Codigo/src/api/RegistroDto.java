package api;

/*
 * Registro de acceso.
 */
public record RegistroDto(int idUsuario, int idEvento, String fechaHoraIngreso,
                          boolean exitoso, String motivoRechazo) {
}
