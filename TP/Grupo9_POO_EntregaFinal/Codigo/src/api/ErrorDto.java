package api;

/*
 * Error devuelto por el servidor; tipo indica que excepcion recrear.
 */
public record ErrorDto(String tipo, String mensaje) {
}
