package ui;

import calendario.VinculoApple;
import calendario.VinculoGoogle;
import excepciones.AccesoDenegadoException;
import excepciones.PermisoDenegadoException;
import excepciones.ErrorDeServicioException;
import seguridad.Accion;
import puertos.VinculoCalendario;
import servicio.ServicioEventos;
import puertos.Transmision;
import modelo.Artista;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    private ServicioEventos gestor;

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
    private final JButton verTransmision = new JButton("Ver transmision");
    private final JButton calendario = new JButton("Agregar a calendario \u25BE");

    // Formularios de alta (usuario, artista y evento).
    private final Formularios formularios;

    // Etiqueta inferior para mostrar el resultado de la ultima accion.
    private JLabel estado;

    // Borra el mensaje de la barra de estado a los pocos segundos.
    private final javax.swing.Timer limpiarEstado = new javax.swing.Timer(
            8000, e -> estado.setText(" "));

    // Refresca las tablas solo cada pocos segundos, para ver los cambios de otros.
    private final javax.swing.Timer refrescoAutomatico = new javax.swing.Timer(
            5000, e -> actualizarAutomaticamente());

    // Indica si la ventana puede cerrar la sesion y volver al login.
    private boolean permiteCerrarSesion;

    /*
     * Si la ventana se abre desde el login, puede cerrar la sesion y volver
     * a esa pantalla. Si se abre desde el menu de consola, la sesion es la
     * del menu y no la cierra.
     */
    public VentanaPrincipal(ServicioEventos gestor,
                            boolean permiteCerrarSesion) {
        // Recibo el gestor desde afuera para no crear datos separados.
        this.gestor = gestor;
        this.permiteCerrarSesion = permiteCerrarSesion;
        this.formularios = new Formularios(this, gestor, this::mostrarError);

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

        limpiarEstado.setRepeats(false);
        refrescoAutomatico.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refrescoAutomatico.stop();
                limpiarEstado.stop();
            }
        });

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
        // El tooltip muestra el contenido completo de la celda, por si el
        // texto no entra en el ancho de la columna.
        JTable tabla = new JTable(modelo) {
            // Filas alternadas: con muchas filas se lee mejor.
            @Override
            public Component prepareRenderer(TableCellRenderer renderizador,
                                             int fila, int columna) {
                Component celda = super.prepareRenderer(renderizador, fila, columna);

                if (!isRowSelected(fila)) {
                    celda.setBackground(fila % 2 == 0
                            ? getBackground() : new Color(245, 247, 251));
                }

                return celda;
            }

            @Override
            public String getToolTipText(MouseEvent evento) {
                int fila = rowAtPoint(evento.getPoint());
                int columna = columnAtPoint(evento.getPoint());

                if (fila < 0 || columna < 0) {
                    return null;
                }

                Object valor = getValueAt(fila, columna);
                return (valor == null) ? null : valor.toString();
            }
        };
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

            boolean enVivo = "EN_CURSO".equals(String.valueOf(valor));

            // Un evento en curso se destaca con "EN VIVO" en negrita.
            setText(enVivo ? "\u25CF EN VIVO" : String.valueOf(valor));
            setFont(getFont().deriveFont(enVivo ? Font.BOLD : Font.PLAIN));

            if (!seleccionada) {
                celda.setForeground(colorDeEstado(String.valueOf(valor)));
            }

            return celda;
        }

        private Color colorDeEstado(String estado) {
            switch (estado) {
                case "EN_CURSO":
                    return COLOR_ERROR;
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
        alternarActivo.addActionListener(
                e -> ejecutar(this::alternarActivoSeleccionado));

        JPanel acciones = crearFila("Usuarios");
        agregarSiPermitido(acciones, Accion.REGISTRAR_USUARIO,
                crearBoton("Registrar usuario", this::registrarUsuario));
        agregarSiPermitido(acciones, Accion.ACTIVAR_DESACTIVAR_USUARIO,
                alternarActivo);
        acciones.add(crearBoton("Actualizar", this::actualizarDesdeOrigen));

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
        acciones.add(crearBoton("Actualizar", this::actualizarDesdeOrigen));

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

        // Doble clic: un usuario entra al evento; el resto abre su transmision
        // o, si no esta en curso, ve los conectados.
        tablaEventos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && eventoSeleccionado() != null) {
                    ejecutar(VentanaPrincipal.this::accionPorDefecto);
                }
            }
        });

        iniciar.addActionListener(
                e -> ejecutar(() -> cambiarEstadoSeleccionado("iniciar")));
        pausar.addActionListener(
                e -> ejecutar(() -> cambiarEstadoSeleccionado("pausar")));
        reanudar.addActionListener(
                e -> ejecutar(() -> cambiarEstadoSeleccionado("reanudar")));
        finalizar.addActionListener(
                e -> ejecutar(() -> cambiarEstadoSeleccionado("finalizar")));
        cancelar.addActionListener(
                e -> ejecutar(() -> cambiarEstadoSeleccionado("cancelar")));
        verConectados.addActionListener(e -> ejecutar(this::verConectados));
        verTransmision.addActionListener(e -> ejecutar(this::verTransmision));

        // Primera fila: ciclo de vida del evento.
        JPanel filaEvento = crearFila("Evento");
        agregarSiPermitido(filaEvento, Accion.CREAR_EVENTO,
                crearBoton("Crear evento", this::crearEvento));
        agregarSiPermitido(filaEvento, Accion.INICIAR_EVENTO, iniciar);
        agregarSiPermitido(filaEvento, Accion.PAUSAR_EVENTO, pausar);
        agregarSiPermitido(filaEvento, Accion.REANUDAR_EVENTO, reanudar);
        agregarSiPermitido(filaEvento, Accion.FINALIZAR_EVENTO, finalizar);
        agregarSiPermitido(filaEvento, Accion.CANCELAR_EVENTO, cancelar);

        // Segunda fila: acceso, transmision y calendario del evento seleccionado.
        JPanel filaGestion = crearFila("Acceso, transmision y calendario");
        agregarSiPermitido(filaGestion, Accion.SOLICITAR_INGRESO, crearBoton(
                gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO
                        ? "Solicitar ingreso" : "Dar acceso",
                this::solicitarIngreso));
        agregarSiPermitido(filaGestion, Accion.EXPULSAR_USUARIO,
                crearBoton("Expulsar usuario", this::expulsarUsuario));
        agregarSiPermitido(filaGestion, Accion.VER_CONECTADOS, verConectados);
        agregarSiPermitido(filaGestion, Accion.VER_TRANSMISION, verTransmision);

        // Un solo boton con un menu para elegir el calendario.
        JPopupMenu menuCalendario = new JPopupMenu();
        JMenuItem google = new JMenuItem("Google Calendar");
        JMenuItem apple = new JMenuItem("Apple Calendar");
        google.addActionListener(e -> ejecutar(
                () -> vincularCalendario(new VinculoGoogle())));
        apple.addActionListener(e -> ejecutar(
                () -> vincularCalendario(new VinculoApple())));
        menuCalendario.add(google);
        menuCalendario.add(apple);
        calendario.addActionListener(e ->
                menuCalendario.show(calendario, 0, calendario.getHeight()));
        agregarSiPermitido(filaGestion, Accion.VINCULAR_CALENDARIO, calendario);
        filaGestion.add(crearBoton("Actualizar", this::actualizarDesdeOrigen));

        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));

        // Una fila solo se muestra si el rol tiene algun boton en ella.
        if (filaEvento.getComponentCount() > 0) {
            acciones.add(filaEvento);
        }
        acciones.add(filaGestion);

        // Hasta que se elija un evento, los botones de estado quedan apagados.
        actualizarBotones();

        panel.add(crearBuscador(), BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaEventos), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    // Campo que filtra la tabla de eventos por titulo, estado o artista mientras se escribe.
    private JPanel crearBuscador() {
        JTextField buscador = new JTextField(22);
        buscador.setToolTipText("Filtra por titulo, estado o artista");

        buscador.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarEventos(buscador.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarEventos(buscador.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarEventos(buscador.getText());
            }
        });

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(new JLabel("Buscar:"));
        barra.add(buscador);
        return barra;
    }

    private void filtrarEventos(String texto) {
        TableRowSorter<?> orden = (TableRowSorter<?>) tablaEventos.getRowSorter();

        // Columnas: 1 titulo, 2 estado y 6 artistas. "en vivo" encuentra los
        // eventos en curso, que en la tabla se muestran asi.
        String buscado = texto.trim();
        String patron = Pattern.quote(buscado)
                + (buscado.equalsIgnoreCase("en vivo") ? "|EN_CURSO" : "");

        orden.setRowFilter(buscado.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + patron, 1, 2, 6));
    }

    private JPanel crearPanelRegistros() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel acciones = crearFila("Registros");
        acciones.add(crearBoton("Actualizar", this::actualizarDesdeOrigen));

        panel.add(new JScrollPane(crearTabla(modeloRegistros, 130, 90, 190, 520)),
                BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
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
        boton.addActionListener(e -> ejecutar(accion));
        return boton;
    }

    /*
     * Ejecuta una accion de un boton verificando antes que haya sesion.
     *
     * La sesion puede cerrarse mientras la ventana esta abierta, por
     * ejemplo si al cargar los datos la cuenta ya no existe. En ese caso
     * se cierra la ventana en lugar de fallar. Si el gestor rechaza la
     * accion por permisos, se informa el motivo.
     */
    private void ejecutar(Runnable accion) {
        if (gestor.getCuentaActual() == null) {
            mostrarError("La sesion se cerro. Vuelva a iniciar sesion.");
            dispose();

            if (permiteCerrarSesion) {
                new VentanaLogin(gestor).setVisible(true);
            }

            return;
        }

        try {
            accion.run();
        } catch (PermisoDenegadoException | ErrorDeServicioException e) {
            mostrarError(e.getMessage());
        }
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
        verTransmision.setEnabled(estadoEvento == EstadoEvento.EN_CURSO);
        calendario.setEnabled(evento != null);

        String ayuda = (evento == null)
                ? "Seleccione un evento de la tabla."
                : "No disponible para el estado " + evento.getEstado() + ".";

        for (JButton boton : new JButton[] {iniciar, pausar, reanudar,
                finalizar, cancelar, verConectados, verTransmision,
                calendario}) {
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

        if (seleccionado.isActivo() && !confirmar("\u00BFDesactivar la cuenta de "
                + seleccionado.getNombreUsuario() + "? Se la desconecta de los eventos.")) {
            return;
        }

        try {
            Usuario usuario = gestor.alternarActivo(seleccionado.getId());
            mostrarExito("Usuario " + usuario.getNombreUsuario()
                    + (usuario.isActivo() ? " activado." : " desactivado."));
            actualizarListas();
        } catch (PermisoDenegadoException e) {
            mostrarError(e.getMessage());
        }
    }

    // Suscribe el calendario externo al evento seleccionado.
    private void vincularCalendario(VinculoCalendario vinculo) {
        Evento evento = eventoSeleccionado();

        if (evento == null) {
            mostrarError("Seleccione un evento.");
            return;
        }

        try {
            gestor.vincularCalendario(evento.getId(), vinculo);
            mostrarInfo("Se abrio " + vinculo.getNombre() + " con \""
                    + evento.getTitulo() + "\". Confirme alli la suscripcion; "
                    + "despues se actualiza solo.");
        } catch (IOException e) {
            mostrarError("No se pudo vincular: " + e.getMessage());
        }
    }

    /*
     * Abre en el navegador la transmision del evento seleccionado. Un usuario
     * solo puede verla si ya ingreso al evento; el gestor lo verifica.
     */
    private void verTransmision() {
        Evento evento = eventoSeleccionado();

        if (evento == null) {
            mostrarError("Seleccione un evento.");
            return;
        }

        abrirTransmision(evento);
    }

    // Busca la transmision del evento y la abre en el navegador.
    private void abrirTransmision(Evento evento) {
        // La busqueda puede tardar unos segundos: se avisa y se muestra el cursor de espera.
        mostrarInfo("Buscando la transmision...");
        estado.paintImmediately(estado.getVisibleRect());
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            Transmision transmision = gestor.obtenerTransmision(evento.getId());
            Navegador.abrir(transmision.url());
            mostrarInfo("Se abrio la transmision: " + transmision.titulo());
        } catch (AccesoDenegadoException | IOException e) {
            mostrarError("No se pudo abrir la transmision: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /*
     * Accion del doble clic sobre un evento. Un usuario entra: si ya estaba
     * conectado abre la transmision y si no, pide el ingreso (que la abre al
     * autorizarse). El resto abre la transmision del evento en curso o, si no
     * esta en curso, muestra los conectados.
     */
    private void accionPorDefecto() {
        Evento evento = eventoSeleccionado();

        if (evento == null) {
            return;
        }

        if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO) {
            if (estaConectado(evento)) {
                abrirTransmision(evento);
            } else {
                solicitarIngreso();
            }
        } else if (evento.getEstado() == EstadoEvento.EN_CURSO) {
            abrirTransmision(evento);
        } else if (verConectados.isEnabled()) {
            verConectados();
        }
    }

    // Indica si la cuenta actual ya esta conectada al evento.
    private boolean estaConectado(Evento evento) {
        for (Usuario usuario : evento.getUsuariosConectados()) {
            if (usuario.getId() == gestor.getCuentaActual().getId()) {
                return true;
            }
        }

        return false;
    }

    // Pregunta antes de una accion que no se puede deshacer.
    private boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    /*
     * Refresco periodico de las tablas. Si no hay respuesta (por ejemplo, el
     * servidor se apago) lo informa en la barra de estado, sin ventanas.
     */
    private void actualizarAutomaticamente() {
        if (gestor.getCuentaActual() == null) {
            return;
        }

        try {
            actualizarDesdeOrigen();
        } catch (PermisoDenegadoException | ErrorDeServicioException e) {
            mostrarInfo("Sin actualizar: " + e.getMessage());
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

    private void registrarUsuario() {
        Usuario usuario = formularios.registrarUsuario();

        if (usuario != null) {
            mostrarExito("Usuario registrado: " + usuario.getNombreUsuario()
                    + " (ID " + usuario.getId() + ").");
            actualizarListas();
        }
    }

    private void registrarArtista() {
        Artista artista = formularios.registrarArtista();

        if (artista != null) {
            mostrarExito("Artista registrado: " + artista.getNombreArtistico()
                    + " (ID " + artista.getId() + ").");
            actualizarListas();
        }
    }

    private void crearEvento() {
        Evento evento = formularios.crearEvento();

        if (evento != null) {
            mostrarExito("Evento creado: " + evento.getTitulo()
                    + " (ID " + evento.getId() + ").");
            actualizarListas();
        }
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

        if (registro.isExitoso()) {
            mostrarExito("Ingreso autorizado a \"" + evento.getTitulo() + "\".");

            // Quien ingresa a su nombre entra directamente a la transmision.
            if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO) {
                abrirTransmision(evento);
            }
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

        if (!confirmar("\u00BFExpulsar a " + usuario.getNombreCompleto()
                + " de \"" + evento.getTitulo() + "\"?")) {
            return;
        }

        Usuario usuarioExpulsado = gestor.expulsarUsuario(
                evento.getId(), usuario.getId());

        if (usuarioExpulsado != null) {
            mostrarExito("Usuario expulsado: "
                    + usuarioExpulsado.getNombreCompleto() + ".");
            actualizarListas();
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

        if (("finalizar".equals(accion) || "cancelar".equals(accion))
                && !confirmar("\u00BF" + accion.substring(0, 1).toUpperCase()
                + accion.substring(1) + " el evento \"" + evento.getTitulo()
                + "\"? No se puede deshacer.")) {
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

            mostrarExito("Evento \"" + evento.getTitulo() + "\": " + accion
                    + " realizado. Estado actual: " + evento.getEstado() + ".");
            actualizarListas();
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

    // ---------------- tablas ----------------

    // Pide los datos mas recientes al origen y los muestra.
    private void actualizarDesdeOrigen() {
        gestor.actualizar();
        actualizarListas();
    }

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

                // Si el filtro oculta la fila, no hay nada que seleccionar.
                if (filaVista >= 0) {
                    tabla.setRowSelectionInterval(filaVista, filaVista);
                }

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

    // Muestra un mensaje en la barra de estado y lo borra a los pocos segundos.
    private void mostrarEnBarra(String mensaje, Color color) {
        estado.setForeground(color);
        estado.setText(mensaje);
        limpiarEstado.restart();
    }

    // Resultado correcto: se ve en la barra de estado, sin ventana emergente.
    private void mostrarExito(String mensaje) {
        mostrarEnBarra(mensaje, COLOR_EXITO);
    }

    // Informacion neutra en la barra de estado.
    private void mostrarInfo(String mensaje) {
        mostrarEnBarra(mensaje, Color.DARK_GRAY);
    }

    // Los errores se muestran tambien en una ventana para que no pasen
    // desapercibidos.
    private void mostrarError(String mensaje) {
        mostrarEnBarra(mensaje, COLOR_ERROR);
        JOptionPane.showMessageDialog(this, mensaje, "Atencion",
                JOptionPane.WARNING_MESSAGE);
    }
}
