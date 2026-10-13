package excepciones;

/*
 * Falla al guardar o leer los datos en la base.
 */
public class PersistenciaException extends ErrorDeServicioException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
