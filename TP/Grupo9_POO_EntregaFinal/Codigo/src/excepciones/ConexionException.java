package excepciones;

/*
 * No se pudo hablar con el servidor: esta apagado, la direccion es
 * incorrecta o la red fallo.
 */
public class ConexionException extends ErrorDeServicioException {

    public ConexionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
