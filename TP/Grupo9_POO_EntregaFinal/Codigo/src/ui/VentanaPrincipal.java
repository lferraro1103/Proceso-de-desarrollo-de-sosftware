package ui;

import excepciones.AccesoDenegadoException;
import excepciones.PermisoDenegadoException;
import gestor.Accion;
import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.DatosRecital;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.Usuario;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*
 * Interfaz grafica minima con Swing.
 *
 * Complementa el menu de consola y demuestra la estructura recomendada
 * de Clase 12: separar la interfaz grafica en un paquete ui.
 */
public class VentanaPrincipal extends JFrame {

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

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

        // Creo un modelo para cada lista de la ventana.
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
        setSize(850, 520);
        setLocationRelativeTo(null);

        // Cierra solo esta ventana, no corta toda la aplicacion.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Logo.aplicar(this);

        construirInterfaz();
        actualizarListas();
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

        estado = new JLabel("Sistema iniciado.");
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
                    return new Color(31, 122, 61);
                case "PAUSADO":
                    return new Color(168, 114, 26);
                case "CANCELADO":
                    return new Color(176, 58, 46);
                case "FINALIZADO":
                    return Color.GRAY;
                default:
                    return Color.BLACK;
            }
        }
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable lista = crearTabla(modeloUsuarios, 40, 120, 200, 110, 60);

        // Refresca la informacion visible.
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> actualizarListas());

        // Abre ventanas para cargar los datos del usuario.
        JButton registrar = new JButton("Registrar usuario");
        registrar.addActionListener(e -> registrarUsuario());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(registrar);
        acciones.add(actualizar);

        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelArtistas() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable lista = crearTabla(modeloArtistas, 40, 130, 190, 120, 80);

        // Refresca la informacion visible.
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> actualizarListas());

        // Abre ventanas para cargar un artista nuevo.
        JButton registrar = new JButton("Registrar artista");
        registrar.addActionListener(e -> registrarArtista());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(registrar);
        acciones.add(actualizar);

        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelEventos() {
        JPanel panel = new JPanel(new BorderLayout());
        tablaEventos = crearTabla(modeloEventos, 40, 190, 100, 130, 90, 100, 170);
        tablaEventos.getColumnModel().getColumn(2)
                .setCellRenderer(new RenderizadorEstado());

        // Panel de botones para operar sobre eventos.
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton iniciar = new JButton("Iniciar");
        JButton pausar = new JButton("Pausar");
        JButton reanudar = new JButton("Reanudar");
        JButton finalizar = new JButton("Finalizar");
        JButton cancelar = new JButton("Cancelar");
        JButton crear = new JButton("Crear evento");
        JButton ingresar = new JButton(
                gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO
                        ? "Solicitar ingreso" : "Dar acceso");
        JButton expulsar = new JButton("Expulsar usuario");
        JButton actualizar = new JButton("Actualizar");

        // Cada boton ejecuta una accion cuando se hace click.
        iniciar.addActionListener(e -> cambiarEstadoSeleccionado("iniciar"));
        pausar.addActionListener(e -> cambiarEstadoSeleccionado("pausar"));
        reanudar.addActionListener(e -> cambiarEstadoSeleccionado("reanudar"));
        finalizar.addActionListener(e -> cambiarEstadoSeleccionado("finalizar"));
        cancelar.addActionListener(e -> cambiarEstadoSeleccionado("cancelar"));
        crear.addActionListener(e -> crearEvento());
        ingresar.addActionListener(e -> solicitarIngreso());
        expulsar.addActionListener(e -> expulsarUsuario());
        actualizar.addActionListener(e -> actualizarListas());

        JButton verConectados = new JButton("Ver conectados");
        verConectados.addActionListener(e -> verConectados());

        // Cada boton se agrega solo si el rol permite su accion.
        agregarSiPermitido(acciones, Accion.CREAR_EVENTO, crear);
        agregarSiPermitido(acciones, Accion.SOLICITAR_INGRESO, ingresar);
        agregarSiPermitido(acciones, Accion.EXPULSAR_USUARIO, expulsar);
        agregarSiPermitido(acciones, Accion.INICIAR_EVENTO, iniciar);
        agregarSiPermitido(acciones, Accion.PAUSAR_EVENTO, pausar);
        agregarSiPermitido(acciones, Accion.REANUDAR_EVENTO, reanudar);
        agregarSiPermitido(acciones, Accion.FINALIZAR_EVENTO, finalizar);
        agregarSiPermitido(acciones, Accion.CANCELAR_EVENTO, cancelar);
        agregarSiPermitido(acciones, Accion.VER_CONECTADOS, verConectados);
        acciones.add(actualizar);

        panel.add(new JScrollPane(tablaEventos), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    // Cierra la sesion y vuelve a la ventana de login.
    private void cerrarSesion() {
        gestor.cerrarSesion();
        dispose();
        new VentanaLogin(gestor).setVisible(true);
    }

    // Agrega el boton al panel solo si el rol permite la accion.
    private void agregarSiPermitido(JPanel panel, Accion accion, JButton boton) {
        if (gestor.puede(accion)) {
            panel.add(boton);
        }
    }

    private JPanel crearPanelRegistros() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> actualizarListas());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(actualizar);

        panel.add(new JScrollPane(crearTabla(modeloRegistros, 130, 110, 230, 330)),
                BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void verConectados() {
        Integer idEvento = obtenerIdSeleccionado(tablaEventos);

        if (idEvento == null) {
            mostrarMensaje("Seleccione un evento.");
            return;
        }

        Evento evento = gestor.buscarEventoVisible(idEvento);

        if (evento == null) {
            mostrarMensaje("Evento inexistente.");
            return;
        }

        if (evento.getUsuariosConectados().isEmpty()) {
            mostrarMensaje("No hay usuarios conectados.");
            return;
        }

        StringBuilder texto = new StringBuilder("Usuarios conectados:\n");
        for (Usuario usuario : evento.getUsuariosConectados()) {
            texto.append(usuario).append("\n");
        }

        JOptionPane.showMessageDialog(this, texto.toString());
    }

    private void registrarUsuario() {
        // Primero pido el nombre de usuario porque no puede repetirse.
        String nombreUsuario = leerTexto("Nombre de usuario:");

        if (nombreUsuario == null) {
            return;
        }

        nombreUsuario = nombreUsuario.trim();

        if (nombreUsuario.isEmpty()) {
            mostrarMensaje("El nombre de usuario no puede estar vacio.");
            return;
        }

        if (gestor.existeNombreUsuario(nombreUsuario)) {
            mostrarMensaje("Ese nombre de usuario ya esta registrado.");
            return;
        }

        // Si el nombre de usuario es valido, pido el resto de los datos.
        String nombre = leerTexto("Nombre:");
        String apellido = leerTexto("Apellido:");
        String email = leerTexto("Email:");
        String contrasena = leerTexto("Contrasena:");
        PlanSuscripcion plan = seleccionarPlan();

        if (nombre == null || apellido == null || email == null
                || contrasena == null || contrasena.isEmpty()
                || plan == null) {
            return;
        }

        // El gestor asigna el ID y construye el usuario.
        Usuario usuario = gestor.registrarUsuario(
                nombreUsuario,
                nombre,
                apellido,
                email,
                contrasena,
                plan
        );

        if (usuario != null) {
            mostrarMensaje("Usuario registrado correctamente.");

            // Refresco para que aparezca en la lista de la ventana.
            actualizarListas();

            // Persiste de una para no perder el alta si se cierra sin guardar.
            guardarSilencioso();
        } else {
            mostrarMensaje("Ese nombre de usuario ya esta registrado.");
        }
    }

    private void registrarArtista() {
        // Carga manual de artista para despues poder crear eventos asociados.
        String nombreUsuario = leerTexto("Nombre de cuenta del artista:");
        String contrasena = leerTexto("Contrasena del artista:");
        String nombreArtistico = leerTexto("Nombre artistico:");
        String genero = leerTexto("Genero principal:");
        String biografia = leerTexto("Biografia:");

        if (nombreUsuario == null || contrasena == null
                || contrasena.isEmpty() || nombreArtistico == null
                || genero == null || biografia == null) {
            return;
        }

        if (gestor.existeNombreUsuario(nombreUsuario)) {
            mostrarMensaje("Ese nombre de usuario ya esta registrado.");
            return;
        }

        boolean verificado = leerBooleano("El artista esta verificado?");

        gestor.registrarArtista(
                nombreUsuario,
                contrasena,
                nombreArtistico,
                genero,
                biografia,
                verificado
        );
        mostrarMensaje("Artista registrado correctamente.");
        actualizarListas();

        // Persiste de una para no perder el alta si se cierra sin guardar.
        guardarSilencioso();
    }

    private void crearEvento() {
        // Un artista crea el evento a su nombre; el administrador elige el
        // artista que va a estar asociado.
        Artista artista;
        if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.ARTISTA) {
            artista = gestor.buscarArtistaPorId(gestor.getCuentaActual().getId());
        } else {
            artista = seleccionarArtista();
        }

        if (artista == null) {
            return;
        }

        String titulo = leerTexto("Titulo del evento:");
        String descripcion = leerTexto("Descripcion:");
        Integer minutosInicio = leerEntero("Minutos hasta inicio:");
        Integer duracion = leerEntero("Duracion en minutos:");
        Integer capacidad = leerEntero("Capacidad maxima:");
        PlanSuscripcion plan = seleccionarPlan();
        String ubicacion = leerTexto("Ubicacion o canal:");

        if (titulo == null || descripcion == null || minutosInicio == null
                || duracion == null || capacidad == null || plan == null
                || ubicacion == null) {
            return;
        }

        if (duracion <= 0) {
            // Si ingresan una duracion invalida, uso un valor por defecto.
            duracion = 60;
        }

        if (capacidad <= 0) {
            // Evito crear eventos con capacidad cero o negativa.
            capacidad = 1;
        }

        boolean streaming = leerBooleano("El evento es por streaming?");
        boolean exclusivo = leerBooleano("El evento es exclusivo?");

        // El inicio se calcula desde la hora actual.
        LocalDateTime inicio = LocalDateTime.now()
                .plusMinutes(minutosInicio);
        LocalDateTime fin = inicio.plusMinutes(duracion);

        DatosRecital datosRecital = new DatosRecital(
                titulo,
                descripcion,
                inicio,
                fin,
                modelo.EstadoEvento.PROGRAMADO,
                capacidad,
                plan,
                ubicacion,
                streaming,
                exclusivo
        );

        gestor.crearEvento(datosRecital, artista);
        mostrarMensaje("Evento creado correctamente.");
        actualizarListas();
    }

    private void solicitarIngreso() {
        // Equivale a la opcion "Solicitar ingreso a evento" del menu.
        // Un usuario solicita el ingreso con su propia cuenta; el
        // administrador elige a nombre de quien lo hace.
        Usuario usuario;
        if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO) {
            usuario = gestor.buscarUsuarioPorId(gestor.getCuentaActual().getId());
        } else {
            usuario = seleccionarUsuario();
        }
        Evento evento = seleccionarEvento();

        if (usuario == null || evento == null) {
            return;
        }

        RegistroAcceso registro = gestor.solicitarIngreso(
                // El gestor trabaja con IDs, por eso paso idUsuario e idEvento.
                usuario.getId(),
                evento.getId()
        );

        actualizarListas();

        if (registro.isExitoso()) {
            mostrarMensaje("Ingreso autorizado.");
        } else {
            mostrarMensaje("Ingreso rechazado: "
                    + registro.getMotivoRechazo());
        }
    }

    private void expulsarUsuario() {
        // Primero elijo el evento y despues un usuario conectado a ese evento.
        Evento evento = seleccionarEvento();

        if (evento == null) {
            mostrarMensaje("Seleccione un evento valido.");
            return;
        }

        Evento recital = evento;

        if (recital.getUsuariosConectados().isEmpty()) {
            mostrarMensaje("No hay usuarios conectados en este evento.");
            return;
        }

        Usuario usuario = seleccionarUsuarioConectado(recital);

        if (usuario == null) {
            return;
        }

        Usuario usuarioExpulsado = gestor.expulsarUsuario(
                evento.getId(), usuario.getId());

        if (usuarioExpulsado != null) {
            mostrarMensaje("Usuario expulsado. Se notifico a "
                    + usuarioExpulsado.getNombreCompleto() + ".");
            actualizarListas();
        } else {
            mostrarMensaje("No se pudo expulsar al usuario.");
        }
    }

    private Usuario seleccionarUsuario() {
        // Armo una lista de textos para que el usuario elija desde un combo.
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

    private Usuario seleccionarUsuarioConectado(Evento recital) {
        // Solo muestro los usuarios conectados a este recital.
        List<String> opciones = new ArrayList<>();

        for (Usuario usuario : recital.getUsuariosConectados()) {
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

    private Artista seleccionarArtista() {
        // Lista de artistas disponibles para asociar a un evento.
        List<String> opciones = new ArrayList<>();

        for (Artista artista : gestor.listarArtistas()) {
            opciones.add(artista.toString());
        }

        String seleccionado = seleccionarOpcion(
                "Seleccione artista",
                opciones
        );

        if (seleccionado == null) {
            return null;
        }

        return gestor.buscarArtistaPorId(obtenerIdDesdeTexto(seleccionado));
    }

    private Evento seleccionarEvento() {
        // Lista de eventos administrados por el gestor.
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
            mostrarMensaje("No hay datos disponibles para seleccionar.");
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

    private PlanSuscripcion seleccionarPlan() {
        // Uso los valores del enum para no escribir planes a mano.
        Object seleccion = JOptionPane.showInputDialog(
                this,
                "Seleccione plan",
                "Plan",
                JOptionPane.QUESTION_MESSAGE,
                null,
                PlanSuscripcion.values(),
                PlanSuscripcion.FREE
        );

        return (PlanSuscripcion) seleccion;
    }

    private String leerTexto(String mensaje) {
        // Ventana simple para ingresar texto.
        return JOptionPane.showInputDialog(this, mensaje);
    }

    private Integer leerEntero(String mensaje) {
        // JOptionPane devuelve texto, por eso lo convierto a Integer.
        String valor = JOptionPane.showInputDialog(this, mensaje);

        if (valor == null) {
            return null;
        }

        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            mostrarMensaje("Debe ingresar un numero entero.");
            return null;
        }
    }

    private boolean leerBooleano(String mensaje) {
        // Confirm dialog devuelve si el usuario eligio Si o No.
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                mensaje,
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        return respuesta == JOptionPane.YES_OPTION;
    }

    private JPanel crearPanelPersistencia() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Botones que llaman a la clase de persistencia con archivos TXT.
        JButton guardar = new JButton("Guardar TXT");
        JButton cargar = new JButton("Cargar TXT");

        guardar.addActionListener(e -> guardarDatos());
        cargar.addActionListener(e -> cargarDatos());

        panel.add(guardar);
        panel.add(cargar);
        return panel;
    }

    private void cambiarEstadoSeleccionado(String accion) {
        // Primero necesito saber que evento esta seleccionado en la lista.
        Integer idEvento = obtenerIdSeleccionado(tablaEventos);

        if (idEvento == null) {
            mostrarMensaje("Seleccione un evento.");
            return;
        }

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

            mostrarMensaje("Estado actualizado.");
            actualizarListas();
        } catch (AccesoDenegadoException | PermisoDenegadoException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void guardarDatos() {
        try {
            // Guarda usuarios, artistas, eventos y registros en archivos TXT.
            gestor.guardarDatos();
            mostrarMensaje("Datos guardados en carpeta datos.");
        } catch (IOException e) {
            mostrarMensaje("No se pudieron guardar los datos: "
                    + e.getMessage());
        }
    }

    /*
     * Igual que guardarDatos(), pero sin popup de confirmacion.
     *
     * Se usa despues de un alta (usuario/artista) para no perder el dato
     * si se cierra la ventana sin apretar "Guardar TXT", sin interrumpir
     * al operador con un segundo mensaje encima del de "registrado".
     */
    private void guardarSilencioso() {
        try {
            gestor.guardarDatos();
        } catch (IOException e) {
            mostrarMensaje("No se pudieron guardar los datos: "
                    + e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            // Carga usuarios, artistas y eventos desde los TXT.
            gestor.cargarDatos();

            // Despues de cargar, refresco lo que se ve en pantalla.
            actualizarListas();
            mostrarMensaje("Datos cargados desde carpeta datos.");
        } catch (IOException | RuntimeException e) {
            mostrarMensaje("No se pudieron cargar los datos: "
                    + e.getMessage());
        }
    }

    private void actualizarListas() {
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
        // Mismo formato que arriba, pero cuando el texto viene de un combo.
        int finId = texto.indexOf(" |");
        return Integer.parseInt(texto.substring(4, finId));
    }

    private void mostrarMensaje(String mensaje) {
        // Muestro el mensaje abajo y tambien en una ventana emergente.
        estado.setText(mensaje);
        JOptionPane.showMessageDialog(this, mensaje);
    }
}
