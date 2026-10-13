package excepciones;

/*
 * Falla del servicio que no es un rechazo por las reglas del negocio: no se
 * pudo guardar en la base o no se pudo hablar con el servidor.
 *
 * Es no verificada: las vistas la atrapan en un unico lugar para mostrar el
 * motivo, sin que cada operacion tenga que declararla.
 */
public class ErrorDeServicioException extends RuntimeException {

    public ErrorDeServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ErrorDeServicioException(String mensaje) {
        super(mensaje);
    }
}
