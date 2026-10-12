package ui;

import excepciones.AccesoDenegadoException;
import excepciones.PermisoDenegadoException;
import gestor.Accion;
import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/*
 * Interfaz grafica con Swing.
 *
 * Complementa el menu de consola: muestra solo las pestanias y los botones
 * que permite el rol de la cuenta, y trabaja con el mismo gestor.
 */
public class VentanaPrincipal extends JFrame {

    // Colores de la barra de estado.
    private static final Color COLOR_EXITO = new Color(31, 122, 61);
    private static final Color COLOR_ERROR = new Color(176, 58, 46);

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Uso el mismo gestor que usa el menu por consola.
    // Asi la ventana trabaja con los mismos usuarios, artistas y eventos.
    private GestorEventosEnVivo gestor;

    // Cada tabla muestra una entidad en columnas. Los modelos guardan las
    // filas y Swing actualiza la tabla cuando se modifican.
    private ModeloTabla modeloUsuarios;
    private ModeloTabla modeloArtistas;
    private ModeloTabla modeloEventos;
    private ModeloTabla modeloRegistros;

    // Guardo esta tabla como atributo porque necesito saber que evento
    // selecciono la persona para iniciar, pausar, reanudar o finalizar.
    private JTable tablaEventos;

    // Tabla de usuarios y el boton que activa o desactiva el seleccionado.
    private JTable tablaUsuarios;
    private final JButton alternarActivo = new JButton("Activar / Desactivar");

    // Botones que dependen del evento seleccionado y de su estado.
    private final JButton iniciar = new JButton("Iniciar");
    private final JButton pausar = new JButton("Pausar");
    private final JButton reanudar = new JButton("Reanudar");
    private final JButton finalizar = new JButton("Finalizar");
    private final JButton cancelar = new JButton("Cancelar");
    private final JButton verConectados = new JButton("Ver conectados");

    // Etiqueta inferior para mostrar el resultado de la ultima accion.
    private JLabel estado;

    // Indica si la ventana puede cerrar la sesion y volver al login.
    private boolean permiteCerrarSesion;

    /*
     * Si la ventana se abre desde el login, puede cerrar la sesion y volver
     * a esa pantalla. Si se abre desde el menu de consola, la sesion es la
     * del menu y no la cierra.
     */
    public VentanaPrincipal(GestorEventosEnVivo gestor,
                            boolean permiteCerrarSesion) {
        // Recibo el gestor desde afuera para no crear datos separados.
        this.gestor = gestor;
        this.permiteCerrarSesion = permiteCerrarSesion;

        // Creo un modelo para cada tabla de la ventana.
        modeloUsuarios = new ModeloTabla(
                new String[] {"ID", "Usuario", "Nombre", "Plan", "Activo"},
                new Class<?>[] {Integer.class, String.class, String.class,
                        String.class, String.class});
        modeloArtistas = new ModeloTabla(
                new String[] {"ID", "Cuenta", "Artista", "Genero", "Verificado"},
                new Class<?>[] {Integer.class, String.class, String.class,
                        String.class, String.class});
        modeloEventos = new ModeloTabla(
                new String[] {"ID", "Titulo", "Estado", "Inicio",
                        "Conectados", "Plan minimo", "Artistas"},
                new Class<?>[] {Integer.class, String.class, String.class,
                        String.class, String.class, String.class, String.class});
        modeloRegistros = new ModeloTabla(
                new String[] {"Fecha", "Usuario", "Evento", "Resultado"},
                new Class<?>[] {String.class, String.class, String.class,
                        String.class});

        // Configuracion basica de la ventana.
        setTitle("Eventos en vivo - UADE Beats: "
                + gestor.getCuentaActual().getNombreUsuario()
                + " (" + gestor.getCuentaActual().getTipoUsuario() + ")");
        setSize(950, 540);
        setLocationRelativeTo(null);

        // Cierra solo esta ventana, no corta toda la aplicacion.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Logo.aplicar(this);

        construirInterfaz();
        actualizarListas();

        mostrarInfo("Sesion iniciada como "
                + gestor.getCuentaActual().getNombreUsuario()
                + " (" + gestor.getCuentaActual().getTipoUsuario() + ").");
    }

    private void construirInterfaz() {
        // BorderLayout permite poner contenido principal al centro
        // y una barra de estado abajo.
        setLayout(new BorderLayout());

        // JTabbedPane arma las pestanias. Cada una aparece solo si el rol de
        // la cuenta tiene permitida la accion que la habilita.
        JTabbedPane pestanias = new JTabbedPane();

        if (gestor.puede(Accion.VER_USUARIOS)) {
            pestanias.addTab("Usuarios", crearPanelUsuarios());
        }

        if (gestor.puede(Accion.VER_ARTISTAS)) {
            pestanias.addTab("Artistas", crearPanelArtistas());
        }

        if (gestor.puede(Accion.VER_EVENTOS)) {
            pestanias.addTab("Eventos", crearPanelEventos());
        }

        if (gestor.puede(Accion.VER_REGISTROS)
                || gestor.puede(Accion.VER_MIS_REGISTROS)) {
            pestanias.addTab("Registros", crearPanelRegistros());
        }

        if (gestor.puede(Accion.GUARDAR_DATOS)
                || gestor.puede(Accion.CARGAR_DATOS)) {
            pestanias.addTab("Persistencia", crearPanelPersistencia());
        }

        estado = new JLabel(" ");
        estado.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(estado, BorderLayout.CENTER);

        if (permiteCerrarSesion) {
            JButton cerrarSesion = new JButton("Cerrar sesion");
            cerrarSesion.addActionListener(e -> cerrarSesion());
            JPanel lado = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            lado.add(cerrarSesion);
            inferior.add(lado, BorderLayout.EAST);
        }

        add(pestanias, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
    }

    /*
     * Crea una tabla de solo lectura con orden por columna y seleccion de
     * una sola fila. Los anchos indican el ancho preferido de cada columna.
     */
    private JTable crearTabla(ModeloTabla modelo, int... anchos) {
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(22);

        // Swing alinea a la derecha las columnas numericas; el ID se
        // alinea a la izquierda como el resto del texto.
        DefaultTableCellRenderer izquierda = new DefaultTableCellRenderer();
        izquierda.setHorizontalAlignment(SwingConstants.LEFT);
        tabla.setDefaultRenderer(Integer.class, izquierda);

        for (int i = 0; i < anchos.length; i++) {
            TableColumn columna = tabla.getColumnModel().getColumn(i);
            columna.setPreferredWidth(anchos[i]);
        }

        return tabla;
    }

    // Pinta el estado del evento con un color segun su valor.
    private static class RenderizadorEstado extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabla,
                                                       Object valor,
                                                       boolean seleccionada,
                                                       boolean foco,
                                                       int fila,
                                                       int columna) {

            Component celda = super.getTableCellRendererComponent(
                    tabla, valor, seleccionada, foco, fila, columna);

            if (!seleccionada) {
                celda.setForeground(colorDeEstado(String.valueOf(valor)));
            }

            return celda;
        }

        private Color colorDeEstado(String estado) {
            switch (estado) {
                case "EN_CURSO":
                    return COLOR_EXITO;
                case "PAUSADO":
                    return new Color(168, 114, 26);
                case "CANCELADO":
                    return COLOR_ERROR;
                case "FINALIZADO":
                    return Color.GRAY;
                default:
                    return Color.BLACK;
            }
        }
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout());
        tablaUsuarios = crearTabla(modeloUsuarios, 40, 120, 200, 110, 60);

        // El boton cambia segun el usuario seleccionado.
        tablaUsuarios.getSelectionModel().addListSelectionListener(
                e -> actualizarBotonUsuario());
        alternarActivo.addActionListener(e -> alternarActivoSeleccionado());

        JPanel acciones = crearFila("Usuarios");
        agregarSiPermitido(acciones, Accion.REGISTRAR_USUARIO,
                crearBoton("Registrar usuario", this::registrarUsuario));
        agregarSiPermitido(acciones, Accion.ACTIVAR_DESACTIVAR_USUARIO,
                alternarActivo);
        acciones.add(crearBoton("Actualizar", this::actualizarListas));

        actualizarBotonUsuario();

        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelArtistas() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable lista = crearTabla(modeloArtistas, 40, 130, 190, 120, 80);

        JPanel acciones = crearFila("Artistas");
        agregarSiPermitido(acciones, Accion.REGISTRAR_ARTISTA,
                crearBoton("Registrar artista", this::registrarArtista));
        acciones.add(crearBoton("Actualizar", this::actualizarListas));

        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelEventos() {
        JPanel panel = new JPanel(new BorderLayout());

        tablaEventos = crearTabla(modeloEventos, 40, 190, 100, 130, 90, 100, 170);
        tablaEventos.getColumnModel().getColumn(2)
                .setCellRenderer(new RenderizadorEstado());

        // Los botones se habilitan segun el evento seleccionado.
        tablaEventos.getSelectionModel().addListSelectionListener(
                e -> actualizarBotones());

        // Doble clic sobre un evento muestra sus usuarios conectados.
        tablaEventos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && verConectados.isEnabled()) {
                    verConectados();
                }
            }
        });

        iniciar.addActionListener(e -> cambiarEstadoSeleccionado("iniciar"));
        pausar.addActionListener(e -> cambiarEstadoSeleccionado("pausar"));
        reanudar.addActionListener(e -> cambiarEstadoSeleccionado("reanudar"));
        finalizar.addActionListener(e -> cambiarEstadoSeleccionado("finalizar"));
        cancelar.addActionListener(e -> cambiarEstadoSeleccionado("cancelar"));
        verConectados.addActionListener(e -> verConectados());

        // Primera fila: ciclo de vida del evento.
        JPanel filaEvento = crearFila("Evento");
        agregarSiPermitido(filaEvento, Accion.CREAR_EVENTO,
                crearBoton("Crear evento", this::crearEvento));
        agregarSiPermitido(filaEvento, Accion.INICIAR_EVENTO, iniciar);
        agregarSiPermitido(filaEvento, Accion.PAUSAR_EVENTO, pausar);
        agregarSiPermitido(filaEvento, Accion.REANUDAR_EVENTO, reanudar);
        agregarSiPermitido(filaEvento, Accion.FINALIZAR_EVENTO, finalizar);
        agregarSiPermitido(filaEvento, Accion.CANCELAR_EVENTO, cancelar);

        // Segunda fila: gestion de usuarios en el evento.
        JPanel filaGestion = crearFila("Gestion");
        agregarSiPermitido(filaGestion, Accion.SOLICITAR_INGRESO, crearBoton(
                gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO
                        ? "Solicitar ingreso" : "Dar acceso",
                this::solicitarIngreso));
        agregarSiPermitido(filaGestion, Accion.EXPULSAR_USUARIO,
                crearBoton("Expulsar usuario", this::expulsarUsuario));
        agregarSiPermitido(filaGestion, Accion.VER_CONECTADOS, verConectados);
        filaGestion.add(crearBoton("Actualizar", this::actualizarListas));

        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));

        // Una fila solo se muestra si el rol tiene algun boton en ella.
        if (filaEvento.getComponentCount() > 0) {
            acciones.add(filaEvento);
        }
        acciones.add(filaGestion);

        // Hasta que se elija un evento, los botones de estado quedan apagados.
        actualizarBotones();

        panel.add(new JScrollPane(tablaEventos), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelRegistros() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel acciones = crearFila("Registros");
        acciones.add(crearBoton("Actualizar", this::actualizarListas));

        panel.add(new JScrollPane(crearTabla(modeloRegistros, 130, 110, 230, 330)),
                BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelPersistencia() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Botones que llaman a la clase de persistencia con archivos TXT.
        agregarSiPermitido(panel, Accion.GUARDAR_DATOS,
                crearBoton("Guardar TXT", this::guardarDatos));
        agregarSiPermitido(panel, Accion.CARGAR_DATOS,
                crearBoton("Cargar TXT", this::cargarDatos));
        return panel;
    }

    // Fila de botones con un borde que indica a que grupo pertenecen.
    private JPanel crearFila(String titulo) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.setBorder(BorderFactory.createTitledBorder(titulo));
        return fila;
    }

    private JButton crearBoton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    // Agrega el boton al panel solo si el rol permite la accion.
    private void agregarSiPermitido(JPanel panel, Accion accion, JButton boton) {
        if (gestor.puede(accion)) {
            panel.add(boton);
        }
    }

    // Cierra la sesion y vuelve a la ventana de login.
    private void cerrarSesion() {
        gestor.cerrarSesion();
        dispose();
        new VentanaLogin(gestor).setVisible(true);
    }

    /*
     * Habilita cada boton solo cuando la accion es posible: hace falta un
     * evento seleccionado y que su estado permita la transicion. Las reglas
     * son las del propio EstadoEvento, no se repiten aca.
     */
    private void actualizarBotones() {
        Evento evento = eventoSeleccionado();
        EstadoEvento estadoEvento = (evento == null) ? null : evento.getEstado();

        iniciar.setEnabled(estadoEvento != null && estadoEvento.permiteIniciar());
        pausar.setEnabled(estadoEvento != null && estadoEvento.permitePausar());
        reanudar.setEnabled(estadoEvento != null && estadoEvento.permiteReanudar());
        finalizar.setEnabled(estadoEvento != null && estadoEvento.permiteFinalizar());
        cancelar.setEnabled(estadoEvento != null && estadoEvento.permiteCancelar());
        verConectados.setEnabled(evento != null);

        String ayuda = (evento == null)
                ? "Seleccione un evento de la tabla."
                : "No disponible para el estado " + evento.getEstado() + ".";

        for (JButton boton : new JButton[] {iniciar, pausar, reanudar,
                finalizar, cancelar, verConectados}) {
            boton.setToolTipText(boton.isEnabled() ? null : ayuda);
        }
    }

    // Evento de la fila seleccionada, o null si no hay ninguna.
    private Evento eventoSeleccionado() {
        if (tablaEventos == null) {
            return null;
        }

        Integer id = obtenerIdSeleccionado(tablaEventos);
        return (id == null) ? null : gestor.buscarEventoVisible(id);
    }

    // Usuario de la fila seleccionada en la tabla de usuarios, o null.
    private Usuario usuarioSeleccionado() {
        if (tablaUsuarios == null) {
            return null;
        }

        Integer id = obtenerIdSeleccionado(tablaUsuarios);
        return (id == null) ? null : gestor.buscarUsuarioPorId(id);
    }

    // El boton dice "Desactivar" o "Activar" segun el usuario elegido.
    private void actualizarBotonUsuario() {
        Usuario usuario = usuarioSeleccionado();

        alternarActivo.setEnabled(usuario != null);
        alternarActivo.setText(usuario == null
                ? "Activar / Desactivar"
                : (usuario.isActivo() ? "Desactivar" : "Activar"));
        alternarActivo.setToolTipText(usuario == null
                ? "Seleccione un usuario de la tabla." : null);
    }

    private void alternarActivoSeleccionado() {
        Usuario seleccionado = usuarioSeleccionado();

        if (seleccionado == null) {
            mostrarError("Seleccione un usuario.");
            return;
        }

        try {
            Usuario usuario = gestor.alternarActivo(seleccionado.getId());
            mostrarExito("Usuario " + usuario.getNombreUsuario()
                    + (usuario.isActivo() ? " activado." : " desactivado."));
            actualizarListas();
            guardarSilencioso();
        } catch (PermisoDenegadoException e) {
            mostrarError(e.getMessage());
        }
    }

    private void verConectados() {
        Evento evento = eventoSeleccionado();

        if (evento == null) {
            mostrarError("Seleccione un evento.");
            return;
        }

        if (evento.getUsuariosConectados().isEmpty()) {
            mostrarInfo("No hay usuarios conectados en \"" + evento.getTitulo() + "\".");
            return;
        }

        StringBuilder texto = new StringBuilder("Usuarios conectados:\n");
        for (Usuario usuario : evento.getUsuariosConectados()) {
            texto.append(usuario.getNombreUsuario()).append(" - ")
                    .append(usuario.getNombreCompleto()).append("\n");
        }

        JOptionPane.showMessageDialog(this, texto.toString(),
                evento.getTitulo(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- formularios ----------------

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
            int respuesta = JOptionPane.showConfirmDialog(this,
                    formulario.panel(), titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (respuesta != JOptionPane.OK_OPTION) {
                return false;
            }

            String error = validador.get();

            if (error == null) {
                return true;
            }

            mostrarError(error);
        }
    }

    private static boolean vacio(JTextField campo) {
        return campo.getText().trim().isEmpty();
    }

    private void registrarUsuario() {
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
            return;
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
            mostrarError("Ese nombre de usuario ya esta registrado.");
            return;
        }

        mostrarExito("Usuario registrado: " + usuario.getNombreUsuario()
                + " (ID " + usuario.getId() + ").");
        actualizarListas();

        // Persiste de una para no perder el alta si se cierra sin guardar.
        guardarSilencioso();
    }

    private void registrarArtista() {
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
            return;
        }

        Artista artista = gestor.registrarArtista(
                cuenta.getText().trim(),
                new String(clave.getPassword()),
                nombreArtistico.getText().trim(),
                genero.getText().trim(),
                biografia.getText().trim(),
                verificado.isSelected());

        if (artista == null) {
            mostrarError("Ese nombre de cuenta ya esta registrado.");
            return;
        }

        mostrarExito("Artista registrado: " + artista.getNombreArtistico()
                + " (ID " + artista.getId() + ").");
        actualizarListas();

        // Persiste de una para no perder el alta si se cierra sin guardar.
        guardarSilencioso();
    }

    private void crearEvento() {
        // Un artista crea el evento a su nombre; el administrador elige el
        // artista que va a estar asociado.
        boolean esArtista = gestor.getCuentaActual().getTipoUsuario()
                == TipoUsuario.ARTISTA;

        Formulario f = new Formulario();
        JComboBox<Artista> artistas = null;

        if (!esArtista) {
            List<Artista> disponibles = gestor.listarArtistas();

            if (disponibles.isEmpty()) {
                mostrarError("No hay artistas registrados.");
                return;
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
            return;
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

        Evento evento = gestor.crearEvento(datosRecital, artista);
        mostrarExito("Evento creado: " + evento.getTitulo()
                + " (ID " + evento.getId() + ").");
        actualizarListas();
        guardarSilencioso();
    }

    // ---------------- acciones sobre eventos y usuarios ----------------

    private void solicitarIngreso() {
        // Un usuario solicita el ingreso con su propia cuenta; el
        // administrador elige a nombre de quien lo hace.
        Usuario usuario;
        if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO) {
            usuario = gestor.buscarUsuarioPorId(gestor.getCuentaActual().getId());
        } else {
            usuario = seleccionarUsuario();
        }

        // Si hay un evento seleccionado en la tabla se usa ese.
        Evento evento = eventoSeleccionado();
        if (evento == null) {
            evento = seleccionarEvento();
        }

        if (usuario == null || evento == null) {
            return;
        }

        RegistroAcceso registro = gestor.solicitarIngreso(
                // El gestor trabaja con IDs, por eso paso idUsuario e idEvento.
                usuario.getId(),
                evento.getId()
        );

        actualizarListas();

        // El intento, exitoso o no, queda registrado: se guarda.
        guardarSilencioso();

        if (registro.isExitoso()) {
            mostrarExito("Ingreso autorizado a \"" + evento.getTitulo() + "\".");
        } else {
            mostrarError("Ingreso rechazado: " + registro.getMotivoRechazo());
        }
    }

    private void expulsarUsuario() {
        // Si hay un evento seleccionado en la tabla se usa ese.
        Evento evento = eventoSeleccionado();
        if (evento == null) {
            evento = seleccionarEvento();
        }

        if (evento == null) {
            return;
        }

        if (evento.getUsuariosConectados().isEmpty()) {
            mostrarError("No hay usuarios conectados en este evento.");
            return;
        }

        Usuario usuario = seleccionarUsuarioConectado(evento);

        if (usuario == null) {
            return;
        }

        Usuario usuarioExpulsado = gestor.expulsarUsuario(
                evento.getId(), usuario.getId());

        if (usuarioExpulsado != null) {
            mostrarExito("Usuario expulsado: "
                    + usuarioExpulsado.getNombreCompleto() + ".");
            actualizarListas();
            guardarSilencioso();
        } else {
            mostrarError("No se pudo expulsar al usuario.");
        }
    }

    private void cambiarEstadoSeleccionado(String accion) {
        Evento evento = eventoSeleccionado();

        if (evento == null) {
            mostrarError("Seleccione un evento.");
            return;
        }

        int idEvento = evento.getId();

        try {
            // El gestor centraliza las transiciones de estado (patron
            // Controlador): esta clase no llama a los metodos de Evento.
            if ("iniciar".equals(accion)) {
                gestor.iniciarEvento(idEvento);
            } else if ("pausar".equals(accion)) {
                gestor.pausarEvento(idEvento);
            } else if ("reanudar".equals(accion)) {
                gestor.reanudarEvento(idEvento);
            } else if ("finalizar".equals(accion)) {
                gestor.finalizarEvento(idEvento);
            } else if ("cancelar".equals(accion)) {
                gestor.cancelarEvento(idEvento);
            }

            mostrarExito("Evento \"" + evento.getTitulo() + "\": " + accion
                    + " realizado. Estado actual: " + evento.getEstado() + ".");
            actualizarListas();
            guardarSilencioso();
        } catch (AccesoDenegadoException | PermisoDenegadoException e) {
            mostrarError(e.getMessage());
        }
    }

    // ---------------- selectores ----------------

    private Usuario seleccionarUsuario() {
        // Armo una lista de textos para que la persona elija desde un combo.
        List<String> opciones = new ArrayList<>();

        for (Usuario usuario : gestor.listarUsuarios()) {
            opciones.add(usuario.toString());
        }

        String seleccionado = seleccionarOpcion(
                "Seleccione usuario",
                opciones
        );

        if (seleccionado == null) {
            return null;
        }

        // Del texto elegido extraigo el ID y busco el objeto real en el gestor.
        return gestor.buscarUsuarioPorId(obtenerIdDesdeTexto(seleccionado));
    }

    private Usuario seleccionarUsuarioConectado(Evento evento) {
        // Solo muestro los usuarios conectados a este evento.
        List<String> opciones = new ArrayList<>();

        for (Usuario usuario : evento.getUsuariosConectados()) {
            opciones.add(usuario.toString());
        }

        String seleccionado = seleccionarOpcion(
                "Seleccione usuario conectado",
                opciones
        );

        if (seleccionado == null) {
            return null;
        }

        return gestor.buscarUsuarioPorId(obtenerIdDesdeTexto(seleccionado));
    }

    private Evento seleccionarEvento() {
        // Lista de eventos visibles para la cuenta.
        List<String> opciones = new ArrayList<>();

        for (Evento evento : gestor.listarEventosVisibles()) {
            opciones.add(evento.toString());
        }

        String seleccionado = seleccionarOpcion(
                "Seleccione evento",
                opciones
        );

        if (seleccionado == null) {
            return null;
        }

        return gestor.buscarEventoVisible(obtenerIdDesdeTexto(seleccionado));
    }

    private String seleccionarOpcion(String titulo, List<String> opciones) {
        if (opciones.isEmpty()) {
            mostrarError("No hay datos disponibles para seleccionar.");
            return null;
        }

        // showInputDialog con opciones funciona como una lista desplegable.
        return (String) JOptionPane.showInputDialog(
                this,
                titulo,
                titulo,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones.toArray(),
                opciones.get(0)
        );
    }

    // ---------------- persistencia ----------------

    private void guardarDatos() {
        try {
            // Guarda usuarios, artistas, eventos y registros en archivos TXT.
            gestor.guardarDatos();
            mostrarExito("Datos guardados en la carpeta datos.");
        } catch (IOException e) {
            mostrarError("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    /*
     * Igual que guardarDatos(), pero sin mensaje de confirmacion.
     *
     * Se usa despues de un alta (usuario/artista) para no perder el dato
     * si se cierra la ventana sin apretar "Guardar TXT", sin pisar el
     * mensaje de "registrado".
     */
    private void guardarSilencioso() {
        try {
            gestor.guardarDatos();
        } catch (IOException e) {
            mostrarError("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            // Carga usuarios, artistas y eventos desde los TXT.
            gestor.cargarDatos();

            // Despues de cargar, refresco lo que se ve en pantalla.
            actualizarListas();
            mostrarExito("Datos cargados desde la carpeta datos.");
        } catch (IOException | RuntimeException e) {
            mostrarError("No se pudieron cargar los datos: " + e.getMessage());
        }
    }

    // ---------------- tablas ----------------

    private void actualizarListas() {
        // Recuerdo las filas seleccionadas para volver a seleccionarlas.
        Integer idSeleccionado = (tablaEventos == null)
                ? null : obtenerIdSeleccionado(tablaEventos);
        Integer idUsuarioSeleccionado = (tablaUsuarios == null)
                ? null : obtenerIdSeleccionado(tablaUsuarios);

        // Limpio primero las filas para no repetir elementos.
        modeloUsuarios.setRowCount(0);
        modeloArtistas.setRowCount(0);
        modeloEventos.setRowCount(0);
        modeloRegistros.setRowCount(0);

        if (gestor.puede(Accion.VER_USUARIOS)) {
            for (Usuario usuario : gestor.listarUsuarios()) {
                modeloUsuarios.addRow(new Object[] {
                        usuario.getId(),
                        usuario.getNombreUsuario(),
                        usuario.getNombreCompleto(),
                        usuario.getPlanSuscripcion(),
                        usuario.isActivo() ? "Si" : "No"
                });
            }
        }

        if (gestor.puede(Accion.VER_ARTISTAS)) {
            for (Artista artista : gestor.listarArtistas()) {
                modeloArtistas.addRow(new Object[] {
                        artista.getId(),
                        artista.getNombreUsuario(),
                        artista.getNombreArtistico(),
                        artista.getGeneroPrincipal(),
                        artista.isVerificado() ? "Si" : "No"
                });
            }
        }

        if (gestor.puede(Accion.VER_EVENTOS)) {
            for (Evento evento : gestor.listarEventosVisibles()) {
                modeloEventos.addRow(new Object[] {
                        evento.getId(),
                        evento.getTitulo(),
                        evento.getEstado(),
                        evento.getFechaHoraInicio().format(FORMATO_FECHA),
                        evento.getCantidadConectados() + "/"
                                + evento.getCapacidadMaxima(),
                        evento.getPlanMinimoRequerido(),
                        evento.getNombresArtistas()
                });
            }
        }

        // El administrador ve todos los registros; un usuario, los suyos.
        if (gestor.puede(Accion.VER_REGISTROS)) {
            agregarRegistros(gestor.listarRegistros());
        } else if (gestor.puede(Accion.VER_MIS_REGISTROS)) {
            agregarRegistros(gestor.listarMisRegistros());
        }

        reseleccionar(tablaEventos, modeloEventos, idSeleccionado);
        reseleccionar(tablaUsuarios, modeloUsuarios, idUsuarioSeleccionado);
        actualizarBotones();
        actualizarBotonUsuario();
    }

    private void agregarRegistros(List<RegistroAcceso> registros) {
        for (RegistroAcceso registro : registros) {
            modeloRegistros.addRow(new Object[] {
                    registro.getFechaHoraIngreso().format(FORMATO_FECHA),
                    registro.getUsuario().getNombreUsuario(),
                    registro.getEvento().getTitulo(),
                    registro.isExitoso()
                            ? "AUTORIZADO"
                            : "RECHAZADO: " + registro.getMotivoRechazo()
            });
        }
    }

    // Vuelve a seleccionar la fila que estaba elegida antes de refrescar.
    private void reseleccionar(JTable tabla, ModeloTabla modelo, Integer id) {
        if (tabla == null || id == null) {
            return;
        }

        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            if (id.equals(modelo.getValueAt(fila, 0))) {
                int filaVista = tabla.convertRowIndexToView(fila);
                tabla.setRowSelectionInterval(filaVista, filaVista);
                return;
            }
        }
    }

    // El ID es la primera columna de la fila seleccionada.
    private Integer obtenerIdSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            return null;
        }

        // La tabla puede estar ordenada: convierto al indice del modelo.
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return (Integer) tabla.getModel().getValueAt(filaModelo, 0);
    }

    private int obtenerIdDesdeTexto(String texto) {
        // Los textos de los combos empiezan con "ID: 100 | ...".
        int finId = texto.indexOf(" |");
        return Integer.parseInt(texto.substring(4, finId));
    }

    // ---------------- mensajes ----------------

    // Resultado correcto: se ve en la barra de estado, sin ventana emergente.
    private void mostrarExito(String mensaje) {
        estado.setForeground(COLOR_EXITO);
        estado.setText(mensaje);
    }

    // Informacion neutra en la barra de estado.
    private void mostrarInfo(String mensaje) {
        estado.setForeground(Color.DARK_GRAY);
        estado.setText(mensaje);
    }

    // Los errores se muestran tambien en una ventana para que no pasen
    // desapercibidos.
    private void mostrarError(String mensaje) {
        estado.setForeground(COLOR_ERROR);
        estado.setText(mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Atencion",
                JOptionPane.WARNING_MESSAGE);
    }
}
