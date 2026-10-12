package app;

import gestor.GestorEventosEnVivo;
import notificacion.NotificadorInactivo;
import persistencia.PersistenciaArchivos;
import ui.VentanaLogin;

import javax.swing.SwingUtilities;

/*
 * Lanzador de la interfaz grafica Swing.
 *
 * Abre la ventana de login; al iniciar sesion se muestra la ventana con las
 * pestanias y los botones que corresponden al rol de la cuenta.
 */
public class AppSwing {

    public static void main(String[] args) {
        abrir(crearGestor());
    }

    // Crea el gestor igual que la consola y carga los datos iniciales.
    static GestorEventosEnVivo crearGestor() {
        GestorEventosEnVivo gestor = new GestorEventosEnVivo(
                new PersistenciaArchivos(), new NotificadorInactivo());

        DatosIniciales.cargar(gestor);
        return gestor;
    }

    // invokeLater es la forma recomendada de abrir ventanas Swing.
    // Ejecuta la creacion de la ventana en el hilo grafico de Java.
    static void abrir(GestorEventosEnVivo gestor) {
        SwingUtilities.invokeLater(() ->
                new VentanaLogin(gestor).setVisible(true)
        );
    }
}
