package api;

/*
 * Respuesta al iniciar sesion: el token que se envia en cada pedido.
 */
public record SesionDto(String token, CuentaDto cuenta) {
}
