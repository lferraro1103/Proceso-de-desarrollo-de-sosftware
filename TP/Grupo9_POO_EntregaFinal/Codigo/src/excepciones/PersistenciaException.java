package excepciones;

/*
 * Falla al guardar o leer los datos del programa.
 *
 * Es no verificada para que las operaciones del gestor no tengan que
 * declarar IOException: guardar es parte de cada operacion y las vistas
 * solo necesitan mostrar el motivo.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
