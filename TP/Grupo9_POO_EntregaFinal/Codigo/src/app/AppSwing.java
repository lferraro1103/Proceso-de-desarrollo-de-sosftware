package app;

import excepciones.AccesoDenegadoException;
import gestor.GestorEventosEnVivo;
import modelo.Cuenta;
import modelo.TipoUsuario;
import persistencia.PersistenciaArchivos;
import ui.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

/*
 * Lanzador opcional de la interfaz grafica Swing.
 *
 * La ventana ofrece todas las operaciones de gestion, por eso solo se abre
 * para una cuenta con rol Administrador.
 */
public class AppSwing {

    public static void main(String[] args) {
        // Creo el gestor igual que en la consola.
        GestorEventosEnVivo gestor = new GestorEventosEnVivo(new PersistenciaArchivos());

        // Cargo los datos iniciales para que la ventana no arranque vacia.
        DatosIniciales.cargar(gestor);

        // Pido las credenciales antes de abrir la ventana.
        String nombreUsuario = JOptionPane.showInputDialog(
                null, "Nombre de usuario:", "Iniciar sesion",
                JOptionPane.QUESTION_MESSAGE);

        if (nombreUsuario == null) {
            return;
        }

        JPasswordField campoContrasena = new JPasswordField();
        int respuesta = JOptionPane.showConfirmDialog(
                null, campoContrasena, "Contrasena",
                JOptionPane.OK_CANCEL_OPTION);

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Cuenta cuenta = gestor.iniciarSesion(
                    nombreUsuario, new String(campoContrasena.getPassword()));

            if (cuenta.getTipoUsuario() != TipoUsuario.ADMINISTRADOR) {
                JOptionPane.showMessageDialog(null,
                        "La interfaz grafica es solo para administradores.");
                return;
            }
        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return;
        }

        // invokeLater es la forma recomendada de abrir ventanas Swing.
        // Ejecuta la creacion de la ventana en el hilo grafico de Java.
        SwingUtilities.invokeLater(() ->
                new VentanaPrincipal(gestor).setVisible(true)
        );
    }
}
