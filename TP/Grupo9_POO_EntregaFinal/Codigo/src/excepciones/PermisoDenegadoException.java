package excepciones;

/*
 * Se lanza cuando la cuenta con sesion iniciada intenta una accion que su
 * rol no permite, o cuando no hay ninguna sesion iniciada.
 */
public class PermisoDenegadoException extends RuntimeException {

    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
