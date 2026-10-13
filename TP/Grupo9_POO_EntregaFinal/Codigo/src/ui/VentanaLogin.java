package ui;

import excepciones.AccesoDenegadoException;
import excepciones.ErrorDeServicioException;
import gestor.ServicioEventos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/*
 * Ventana de login de la interfaz grafica.
 *
 * Pide nombre de cuenta y contrasena, deja la validacion al gestor y, si
 * son correctas, abre la ventana principal del rol de la cuenta.
 */
public class VentanaLogin extends JFrame {

    private final ServicioEventos gestor;

    private final JTextField campoUsuario = new JTextField(16);
    private final JPasswordField campoContrasena = new JPasswordField(16);

    // Muestra el motivo cuando el login falla.
    private final JLabel mensaje = new JLabel(" ");

    public VentanaLogin(ServicioEventos gestor) {
        this.gestor = gestor;

        setTitle("UADE Beats: iniciar sesion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Logo.aplicar(this);

        construirInterfaz();

        pack();
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        JLabel titulo = new JLabel("Eventos en vivo - UADE Beats",
                Logo.icono(56), JLabel.LEFT);
        titulo.setIconTextGap(12);
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 15f));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        panel.add(titulo, c);

        c.gridwidth = 1;
        c.gridy = 1;
        panel.add(new JLabel("Usuario:"), c);
        c.gridx = 1;
        panel.add(campoUsuario, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("Contrasena:"), c);
        c.gridx = 1;
        panel.add(campoContrasena, c);

        mensaje.setForeground(new Color(176, 58, 46));
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        panel.add(mensaje, c);

        JButton ingresar = new JButton("Iniciar sesion");
        JButton salir = new JButton("Salir");
        ingresar.addActionListener(e -> iniciarSesion());
        salir.addActionListener(e -> System.exit(0));

        JPanel botones = new JPanel();
        botones.add(ingresar);
        botones.add(salir);
        c.gridy = 4;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(botones, c);

        // Enter en cualquier campo equivale a "Iniciar sesion".
        getRootPane().setDefaultButton(ingresar);

        add(panel);
    }

    private void iniciarSesion() {
        try {
            gestor.iniciarSesion(
                    campoUsuario.getText().trim(),
                    new String(campoContrasena.getPassword()));

            // Con la sesion iniciada se abre la ventana del rol.
            new VentanaPrincipal(gestor, true).setVisible(true);
            dispose();

        } catch (AccesoDenegadoException | ErrorDeServicioException e) {
            mensaje.setText(e.getMessage());
            campoContrasena.setText("");
        }
    }
}
