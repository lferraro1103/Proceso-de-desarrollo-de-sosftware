package servicio;

import seguridad.Accion;
import excepciones.AccesoDenegadoException;
import modelo.Cuenta;

import java.util.List;

/*
 * Operaciones de sesion que necesitan las vistas.
 *
 * Es una de las interfaces chicas en que se divide ServicioEventos, para que
 * cada cliente dependa solo de lo que usa.
 */
public interface ServicioSesion {

    // Inicia sesion o lanza la excepcion con el motivo del rechazo.
    Cuenta iniciarSesion(String nombreUsuario, String contrasena)
            throws AccesoDenegadoException;

    // Cierra la sesion actual.
    void cerrarSesion();

    // Cuenta con sesion iniciada, o null.
    Cuenta getCuentaActual();

    // Indica si la cuenta actual puede ejecutar la accion.
    boolean puede(Accion accion);

    // Acciones que puede ejecutar la cuenta actual.
    List<Accion> accionesPermitidas();
}
