package ui;

import modelo.Artista;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.TipoUsuario;
import modelo.Usuario;
import servicio.ServicioEventos;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
 * Formularios de alta de la ventana: usuario, artista y evento.
 *
 * Cada formulario pide los datos en una sola ventana, valida lo que ingreso la
 * persona y le pide el alta al gestor. Devuelve lo creado y deja a la ventana
 * mostrar el resultado y refrescar las tablas.
 */
class Formularios {

    // Ventana sobre la que se muestran los formularios.
    private final JFrame ventana;

    private final ServicioEventos gestor;

    // Como se informa un error a la persona.
    private final Consumer<String> errores;

    Formularios(JFrame ventana, ServicioEventos gestor, Consumer<String> errores) {
        this.ventana = ventana;
        this.gestor = gestor;
        this.errores = errores;
    }

    /*
     * Formulario de una sola ventana: etiquetas a la izquierda y campos a
     * la derecha. Reemplaza la cadena de ventanitas que pedia cada dato.
     */
    private static class Formulario {

        private final JPanel panel = new JPanel(new GridBagLayout());
        private int fila = 0;

        JPanel panel() {
            return panel;
        }

        private <T extends Component> T agregar(String etiqueta, T campo) {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.gridy = fila++;
            c.gridx = 0;
            c.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel(etiqueta), c);
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            panel.add(campo, c);
            return campo;
        }

        JTextField texto(String etiqueta) {
            return agregar(etiqueta, new JTextField(22));
        }

        JPasswordField clave(String etiqueta) {
            return agregar(etiqueta, new JPasswordField(22));
        }

        <T> JComboBox<T> combo(String etiqueta, T[] valores) {
            return agregar(etiqueta, new JComboBox<>(valores));
        }

        JSpinner numero(String etiqueta, int inicial, int minimo, int maximo) {
            return agregar(etiqueta, new JSpinner(
                    new SpinnerNumberModel(inicial, minimo, maximo, 1)));
        }

        JCheckBox marca(String etiqueta) {
            return agregar(etiqueta, new JCheckBox());
        }
    }

    /*
     * Muestra el formulario hasta que el validador no devuelva errores o la
     * persona cancele. Si hay un error, lo informa y vuelve a mostrar el
     * formulario con lo que ya se habia cargado.
     */
    private boolean pedir(String titulo, Formulario formulario,
                          Supplier<String> validador) {

        while (true) {
            int respuesta = JOptionPane.showConfirmDialog(ventana,
                    formulario.panel(), titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (respuesta != JOptionPane.OK_OPTION) {
                return false;
            }

            String error = validador.get();

            if (error == null) {
                return true;
            }

            errores.accept(error);
        }
    }

    private static boolean vacio(JTextField campo) {
        return campo.getText().trim().isEmpty();
    }

    /*
     * Pide los datos de un usuario nuevo y lo registra. Devuelve el usuario
     * creado, o null si se cancelo o no se pudo.
     */
    Usuario registrarUsuario() {
        Formulario f = new Formulario();
        JTextField cuenta = f.texto("Nombre de usuario:");
        JTextField nombre = f.texto("Nombre:");
        JTextField apellido = f.texto("Apellido:");
        JTextField email = f.texto("Email:");
        JPasswordField clave = f.clave("Contrasena:");
        JComboBox<PlanSuscripcion> plan = f.combo("Plan:", PlanSuscripcion.values());

        boolean aceptado = pedir("Registrar usuario", f, () -> {
            if (vacio(cuenta)) {
                return "El nombre de usuario no puede estar vacio.";
            }
            if (gestor.existeNombreUsuario(cuenta.getText())) {
                return "Ese nombre de usuario ya esta registrado.";
            }
            if (vacio(nombre) || vacio(apellido) || vacio(email)) {
                return "Nombre, apellido y email son obligatorios.";
            }
            if (clave.getPassword().length == 0) {
                return "La contrasena no puede estar vacia.";
            }
            return null;
        });

        if (!aceptado) {
            return null;
        }

        // El gestor asigna el ID y construye el usuario.
        Usuario usuario = gestor.registrarUsuario(
                cuenta.getText().trim(),
                nombre.getText().trim(),
                apellido.getText().trim(),
                email.getText().trim(),
                new String(clave.getPassword()),
                (PlanSuscripcion) plan.getSelectedItem());

        if (usuario == null) {
            errores.accept("Ese nombre de usuario ya esta registrado.");
        }

        return usuario;
    }

    // Pide los datos de un artista nuevo y lo registra. Devuelve null si se cancelo o no se pudo.
    Artista registrarArtista() {
        Formulario f = new Formulario();
        JTextField cuenta = f.texto("Nombre de cuenta:");
        JPasswordField clave = f.clave("Contrasena:");
        JTextField nombreArtistico = f.texto("Nombre artistico:");
        JTextField genero = f.texto("Genero principal:");
        JTextField biografia = f.texto("Biografia:");
        JCheckBox verificado = f.marca("Verificado:");

        boolean aceptado = pedir("Registrar artista", f, () -> {
            if (vacio(cuenta)) {
                return "El nombre de cuenta no puede estar vacio.";
            }
            if (gestor.existeNombreUsuario(cuenta.getText())) {
                return "Ese nombre de cuenta ya esta registrado.";
            }
            if (clave.getPassword().length == 0) {
                return "La contrasena no puede estar vacia.";
            }
            if (vacio(nombreArtistico) || vacio(genero)) {
                return "El nombre artistico y el genero son obligatorios.";
            }
            return null;
        });

        if (!aceptado) {
            return null;
        }

        Artista artista = gestor.registrarArtista(
                cuenta.getText().trim(),
                new String(clave.getPassword()),
                nombreArtistico.getText().trim(),
                genero.getText().trim(),
                biografia.getText().trim(),
                verificado.isSelected());

        if (artista == null) {
            errores.accept("Ese nombre de cuenta ya esta registrado.");
        }

        return artista;
    }

    // Pide los datos de un evento nuevo y lo crea. Devuelve null si se cancelo o no se pudo.
    Evento crearEvento() {
        // Un artista crea el evento a su nombre; el administrador elige el
        // artista que va a estar asociado.
        boolean esArtista = gestor.getCuentaActual().getTipoUsuario()
                == TipoUsuario.ARTISTA;

        Formulario f = new Formulario();
        JComboBox<Artista> artistas = null;

        if (!esArtista) {
            List<Artista> disponibles = gestor.listarArtistas();

            if (disponibles.isEmpty()) {
                errores.accept("No hay artistas registrados.");
                return null;
            }

            artistas = f.combo("Artista:", disponibles.toArray(new Artista[0]));
            artistas.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> lista, Object valor, int indice,
                        boolean seleccionado, boolean foco) {

                    String texto = (valor instanceof Artista)
                            ? ((Artista) valor).getNombreArtistico()
                            : String.valueOf(valor);
                    return super.getListCellRendererComponent(
                            lista, texto, indice, seleccionado, foco);
                }
            });
        }

        JTextField titulo = f.texto("Titulo:");
        JTextField descripcion = f.texto("Descripcion:");
        JSpinner minutosInicio = f.numero("Minutos hasta el inicio:", 0, 0, 100000);
        JSpinner duracion = f.numero("Duracion (minutos):", 60, 1, 10000);
        JSpinner capacidad = f.numero("Capacidad maxima:", 50, 1, 1000000);
        JComboBox<PlanSuscripcion> plan = f.combo("Plan minimo:", PlanSuscripcion.values());
        JTextField ubicacion = f.texto("Ubicacion o canal:");
        JCheckBox streaming = f.marca("Por streaming:");
        JCheckBox exclusivo = f.marca("Exclusivo:");

        boolean aceptado = pedir("Crear evento", f, () -> {
            if (vacio(titulo)) {
                return "El titulo no puede estar vacio.";
            }
            if (vacio(ubicacion)) {
                return "La ubicacion o el canal no pueden estar vacios.";
            }
            return null;
        });

        if (!aceptado) {
            return null;
        }

        Artista artista = esArtista
                ? gestor.buscarArtistaPorId(gestor.getCuentaActual().getId())
                : (Artista) artistas.getSelectedItem();

        // El inicio se calcula desde la hora actual.
        LocalDateTime inicio = LocalDateTime.now()
                .plusMinutes((Integer) minutosInicio.getValue());
        LocalDateTime fin = inicio.plusMinutes((Integer) duracion.getValue());

        DatosRecital datosRecital = new DatosRecital(
                titulo.getText().trim(),
                descripcion.getText().trim(),
                inicio,
                fin,
                EstadoEvento.PROGRAMADO,
                (Integer) capacidad.getValue(),
                (PlanSuscripcion) plan.getSelectedItem(),
                ubicacion.getText().trim(),
                streaming.isSelected(),
                exclusivo.isSelected());

        return gestor.crearEvento(datosRecital, artista);
    }
}
