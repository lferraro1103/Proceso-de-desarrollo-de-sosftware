package app;

import gestor.ServicioEventos;
import ui.VentanaLogin;

import javax.swing.SwingUtilities;

/*
 * Lanzador de la interfaz grafica Swing.
 *
 * Abre la ventana de login; al iniciar sesion se muestra la ventana con las
 * pestanias y los botones que corresponden al rol de la cuenta.
 */
public class AppSwing {

    // invokeLater es la forma recomendada de abrir ventanas Swing.
    // Ejecuta la creacion de la ventana en el hilo grafico de Java.
    static void abrir(ServicioEventos gestor) {
        SwingUtilities.invokeLater(() ->
                new VentanaLogin(gestor).setVisible(true)
        );
    }
}
