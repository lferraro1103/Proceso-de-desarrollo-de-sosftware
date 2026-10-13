package api;

/*
 * Cuenta con la que se inicio sesion.
 */
public record CuentaDto(int id, String nombreUsuario, String tipo, boolean activo) {
}
