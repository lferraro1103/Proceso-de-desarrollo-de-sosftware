package excepciones;

/*
 * Excepcion propia del modulo.
 *
 * Se usa cuando una regla de acceso no se cumple.
 * Por ejemplo:
 * - plan insuficiente;
 * - evento pausado o finalizado;
 * - capacidad agotada;
 * - usuario ya conectado.
 */
public class AccesoDenegadoException extends Exception {

    // Recibe el motivo concreto del rechazo y lo guarda como mensaje.
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
