package seguridad;

import excepciones.PermisoDenegadoException;
import modelo.Cuenta;

import java.util.ArrayList;
import java.util.List;

/*
 * Mantiene la cuenta con sesion iniciada y hace cumplir sus permisos.
 *
 * Se separo del gestor para que este no sume la responsabilidad de
 * autorizar: el gestor le pregunta a esta clase si la accion esta permitida
 * antes de ejecutarla.
 */
public class ControlSesion {

    // Tabla de permisos por rol.
    private final Permisos permisos = new Permisos();

    // Cuenta con sesion iniciada, o null si nadie inicio sesion.
    private Cuenta cuentaActual;

    // Inicia la sesion de la cuenta y la deja como cuenta actual.
    public void iniciar(Cuenta cuenta) {
        cerrar();
        cuenta.iniciarSesion();
        cuentaActual = cuenta;
    }

    // Cierra la sesion de la cuenta actual, si hay una.
    public void cerrar() {
        if (cuentaActual != null) {
            cuentaActual.cerrarSesion();
            cuentaActual = null;
        }
    }

    public Cuenta getCuentaActual() {
        return cuentaActual;
    }

    // Indica si la cuenta actual puede ejecutar la accion.
    public boolean permite(Accion accion) {
        return cuentaActual != null
                && permisos.permite(cuentaActual.getTipoUsuario(), accion);
    }

    // Acciones que puede ejecutar la cuenta actual.
    public List<Accion> accionesPermitidas() {
        if (cuentaActual == null) {
            return new ArrayList<>();
        }

        return permisos.accionesPermitidas(cuentaActual.getTipoUsuario());
    }

    // Falla con PermisoDenegadoException si no hay sesion o el rol no permite la accion.
    public void exigir(Accion accion) {
        if (cuentaActual == null) {
            throw new PermisoDenegadoException(
                    "Debe iniciar sesion para realizar esta accion.");
        }

        if (!permite(accion)) {
            throw new PermisoDenegadoException(
                    "Su rol no permite: " + accion.getEtiqueta() + ".");
        }
    }
}
