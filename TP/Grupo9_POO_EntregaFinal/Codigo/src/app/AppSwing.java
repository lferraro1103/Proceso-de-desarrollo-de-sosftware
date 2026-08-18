package app;

import gestor.GestorEventosEnVivo;
import ui.VentanaPrincipal;

import javax.swing.SwingUtilities;

/*
 * Lanzador opcional de la interfaz grafica Swing.
 */
public class AppSwing {

    public static void main(String[] args) {
        // Creo el gestor igual que en la consola.
        GestorEventosEnVivo gestor = new GestorEventosEnVivo();

        // Cargo los datos iniciales para que la ventana no arranque vacia.
        App.cargarDatosIniciales(gestor);

        // invokeLater es la forma recomendada de abrir ventanas Swing.
        // Ejecuta la creacion de la ventana en el hilo grafico de Java.
        SwingUtilities.invokeLater(() ->
                new VentanaPrincipal(gestor).setVisible(true)
        );
    }
}
