package api;

/*
 * Pedido para registrar un usuario.
 */
public record NuevoUsuarioDto(String nombreUsuario, String nombre, String apellido, String email,
                             String contrasena, String plan) {
}
