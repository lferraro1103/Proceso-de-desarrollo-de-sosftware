package ui;

import excepciones.AccesoDenegadoException;
import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.DatosRecital;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.Usuario;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import persistencia.PersistenciaArchivos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.LocalDateTime;
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

    // Los DefaultListModel guardan los textos que se ven en cada JList.
    // Cuando se modifica el modelo, Swing actualiza la lista visual.
    private DefaultListModel<String> modeloUsuarios;
    private DefaultListModel<String> modeloArtistas;
    private DefaultListModel<String> modeloEventos;

    // Guardo esta lista como atributo porque necesito saber que evento
    // selecciono el usuario para iniciar, pausar, reanudar o finalizar.
    private JList<String> listaEventos;

    // Etiqueta inferior para mostrar el resultado de la ultima accion.
    private JLabel estado;

    public VentanaPrincipal(GestorEventosEnVivo gestor) {
        // Recibo el gestor desde afuera para no crear datos separados.
        this.gestor = gestor;

        // Creo un modelo para cada lista de la ventana.
        modeloUsuarios = new DefaultListModel<>();
        modeloArtistas = new DefaultListModel<>();
        modeloEventos = new DefaultListModel<>();

        // Configuracion basica de la ventana.
        setTitle("Eventos en vivo - UADE Beats");
        setSize(850, 520);
        setLocationRelativeTo(null);

        // Cierra solo esta ventana, no corta toda la aplicacion.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        construirInterfaz();
        actualizarListas();
    }

    private void construirInterfaz() {
        // BorderLayout permite poner contenido principal al centro
        // y una barra de estado abajo.
        setLayout(new BorderLayout());

        // JTabbedPane arma las pestanias: Usuarios, Artistas, Eventos, etc.
        JTabbedPane pestanias = new JTabbedPane();
        pestanias.addTab("Usuarios", crearPanelUsuarios());
        pestanias.addTab("Artistas", crearPanelArtistas());
        pestanias.addTab("Eventos", crearPanelEventos());
        pestanias.addTab("Persistencia", crearPanelPersistencia());

        estado = new JLabel("Sistema iniciado.");
        estado.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(pestanias, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> lista = new JList<>(modeloUsuarios);

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
        JList<String> lista = new JList<>(modeloArtistas);

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
        listaEventos = new JList<>(modeloEventos);

        // Panel de botones para operar sobre eventos.
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton iniciar = new JButton("Iniciar");
        JButton pausar = new JButton("Pausar");
        JButton reanudar = new JButton("Reanudar");
        JButton finalizar = new JButton("Finalizar");
        JButton crear = new JButton("Crear evento");
        JButton ingresar = new JButton("Dar acceso");
        JButton expulsar = new JButton("Expulsar usuario");
        JButton actualizar = new JButton("Actualizar");

        // Cada boton ejecuta una accion cuando se hace click.
        iniciar.addActionListener(e -> cambiarEstadoSeleccionado("iniciar"));
        pausar.addActionListener(e -> cambiarEstadoSeleccionado("pausar"));
        reanudar.addActionListener(e -> cambiarEstadoSeleccionado("reanudar"));
        finalizar.addActionListener(e -> cambiarEstadoSeleccionado("finalizar"));
        crear.addActionListener(e -> crearEvento());
        ingresar.addActionListener(e -> solicitarIngreso());
        expulsar.addActionListener(e -> expulsarUsuario());
        actualizar.addActionListener(e -> actualizarListas());

        acciones.add(crear);
        acciones.add(ingresar);
        acciones.add(expulsar);
        acciones.add(iniciar);
        acciones.add(pausar);
        acciones.add(reanudar);
        acciones.add(finalizar);
        acciones.add(actualizar);

        panel.add(new JScrollPane(listaEventos), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
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

        Usuario usuario = new Usuario(
                // El ID se calcula mirando los usuarios ya cargados.
                siguienteIdUsuario(),
                nombreUsuario,
                nombre,
                apellido,
                email,
                contrasena,
                plan,
                true
        );

        if (gestor.registrarUsuario(usuario)) {
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
        String nombreArtistico = leerTexto("Nombre artistico:");
        String genero = leerTexto("Genero principal:");
        String biografia = leerTexto("Biografia:");

        if (nombreArtistico == null || genero == null || biografia == null) {
            return;
        }

        boolean verificado = leerBooleano("El artista esta verificado?");

        Artista artista = new Artista(
                // Igual que usuarios, el ID se calcula automaticamente.
                siguienteIdArtista(),
                nombreArtistico,
                genero,
                biografia,
                verificado
        );

        gestor.registrarArtista(artista);
        mostrarMensaje("Artista registrado correctamente.");
        actualizarListas();

        // Persiste de una para no perder el alta si se cierra sin guardar.
        guardarSilencioso();
    }

    private void crearEvento() {
        // El evento necesita un artista asociado, por eso se elige primero.
        Artista artista = seleccionarArtista();

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

        RecitalEnVivo recital = new RecitalEnVivo(
                siguienteIdEvento(),
                datosRecital
        );

        gestor.crearEvento(recital, artista);
        mostrarMensaje("Evento creado correctamente.");
        actualizarListas();
    }

    private void solicitarIngreso() {
        // Equivale a la opcion "Solicitar ingreso a evento" del menu.
        Usuario usuario = seleccionarUsuario();
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

        if (!(evento instanceof RecitalEnVivo)) {
            mostrarMensaje("Seleccione un recital valido.");
            return;
        }

        RecitalEnVivo recital = (RecitalEnVivo) evento;

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

    private Usuario seleccionarUsuarioConectado(RecitalEnVivo recital) {
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

        for (Evento evento : gestor.listarEventos()) {
            opciones.add(evento.toString());
        }

        String seleccionado = seleccionarOpcion(
                "Seleccione evento",
                opciones
        );

        if (seleccionado == null) {
            return null;
        }

        return gestor.buscarEventoPorId(obtenerIdDesdeTexto(seleccionado));
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

    private int siguienteIdUsuario() {
        // Busco el mayor ID existente y devuelvo el siguiente.
        int mayor = 0;

        for (Usuario usuario : gestor.listarUsuarios()) {
            if (usuario.getId() > mayor) {
                mayor = usuario.getId();
            }
        }

        return mayor + 1;
    }

    private int siguienteIdArtista() {
        // Misma idea que con usuarios, pero para artistas.
        int mayor = 0;

        for (Artista artista : gestor.listarArtistas()) {
            if (artista.getId() > mayor) {
                mayor = artista.getId();
            }
        }

        return mayor + 1;
    }

    private int siguienteIdEvento() {
        // Misma idea que con usuarios, pero para eventos.
        int mayor = 0;

        for (Evento evento : gestor.listarEventos()) {
            if (evento.getId() > mayor) {
                mayor = evento.getId();
            }
        }

        return mayor + 1;
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
        Integer idEvento = obtenerIdSeleccionado(listaEventos);

        if (idEvento == null) {
            mostrarMensaje("Seleccione un evento.");
            return;
        }

        Evento evento = gestor.buscarEventoPorId(idEvento);

        if (evento == null) {
            mostrarMensaje("Evento inexistente.");
            return;
        }

        try {
            // Segun la accion, llamo al metodo correspondiente del evento.
            if ("iniciar".equals(accion)) {
                evento.iniciarEvento();
            } else if ("pausar".equals(accion)) {
                evento.pausarEvento();
            } else if ("reanudar".equals(accion)) {
                evento.reanudarEvento();
            } else if ("finalizar".equals(accion)) {
                evento.finalizarEvento();
            }

            mostrarMensaje("Estado actualizado.");
            actualizarListas();
        } catch (AccesoDenegadoException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void guardarDatos() {
        try {
            // Guarda usuarios, artistas, eventos y registros en archivos TXT.
            PersistenciaArchivos.guardarDatos(gestor);
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
            PersistenciaArchivos.guardarDatos(gestor);
        } catch (IOException e) {
            mostrarMensaje("No se pudieron guardar los datos: "
                    + e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            // Carga usuarios, artistas y eventos desde los TXT.
            PersistenciaArchivos.cargarDatos(gestor);

            // Despues de cargar, refresco lo que se ve en pantalla.
            actualizarListas();
            mostrarMensaje("Datos cargados desde carpeta datos.");
        } catch (IOException | RuntimeException e) {
            mostrarMensaje("No se pudieron cargar los datos: "
                    + e.getMessage());
        }
    }

    private void actualizarListas() {
        // Limpio primero para no repetir elementos visuales.
        modeloUsuarios.clear();
        modeloArtistas.clear();
        modeloEventos.clear();

        for (Usuario usuario : gestor.listarUsuarios()) {
            // Uso toString() porque cada clase ya sabe como mostrarse.
            modeloUsuarios.addElement(usuario.toString());
        }

        for (Artista artista : gestor.listarArtistas()) {
            modeloArtistas.addElement(artista.toString());
        }

        for (Evento evento : gestor.listarEventos()) {
            modeloEventos.addElement(evento.toString());
        }
    }

    private Integer obtenerIdSeleccionado(JList<String> lista) {
        // Los textos empiezan con "ID: 100 | ...".
        // Este metodo obtiene el numero para buscar el objeto real.
        String seleccionado = lista.getSelectedValue();

        if (seleccionado == null || !seleccionado.startsWith("ID: ")) {
            return null;
        }

        int finId = seleccionado.indexOf(" |");

        if (finId == -1) {
            return null;
        }

        return Integer.parseInt(seleccionado.substring(4, finId));
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
