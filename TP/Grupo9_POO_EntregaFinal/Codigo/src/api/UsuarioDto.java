package api;

/*
 * Usuario o administrador. Nunca lleva la contrasena.
 */
public record UsuarioDto(int id, String nombreUsuario, String nombre, String apellido,
                         String email, String plan, boolean activo, String tipo) {
}
