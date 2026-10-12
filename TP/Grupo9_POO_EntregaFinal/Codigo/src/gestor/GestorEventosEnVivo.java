package gestor;

import excepciones.AccesoDenegadoException;
import excepciones.PermisoDenegadoException;
import modelo.Artista;
import modelo.Cuenta;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Clase gestora del modulo.
 *
 * Su funcion es coordinar operaciones generales:
 * - administrar eventos;
 * - administrar usuarios;
 * - buscar por ID;
 * - solicitar ingresos;
 * - guardar registros de acceso.
 *
 * Con esto se evita que Main concentre reglas de negocio.
 */
public class GestorEventosEnVivo {

    // Eventos administrados por el modulo.
    private List<Evento> eventos;

    // Usuarios registrados para la simulacion.
    private List<Usuario> usuariosRegistrados;

    // Artistas disponibles para asociar a eventos.
    private List<Artista> artistasRegistrados;

    // Resuelve los ingresos y guarda la trazabilidad de los intentos.
    private ControlAcceso controlAcceso;

    // Persistencia usada para guardar y cargar los datos.
    private Persistencia persistencia;

    // Sesion actual y permisos de la cuenta.
    private ControlSesion sesion;

    // Constructor: inicializa las colecciones y recibe sus colaboradores.
    public GestorEventosEnVivo(Persistencia persistencia,
                               Notificador notificador) {

        this.persistencia = persistencia;
        eventos = new ArrayList<>();
        usuariosRegistrados = new ArrayList<>();
        artistasRegistrados = new ArrayList<>();
        controlAcceso = new ControlAcceso(notificador);
        sesion = new ControlSesion();
    }

    // Guarda los datos usando la persistencia recibida.
    public void guardarDatos() throws IOException {
        persistencia.guardar(this);
    }

    // Carga los datos usando la persistencia recibida.
    public void cargarDatos() throws IOException {
        persistencia.cargar(this);
    }

    // Agrega un recital al gestor.
    public void crearEvento(
            RecitalEnVivo evento) {

        eventos.add(evento);
    }

    /*
     * Crea un evento asociado a un artista.
     *
     * Se llama explicitamente a artista.crearEvento(evento) para usar
     * el comportamiento propio de Artista y mantener la trazabilidad.
     */
    public void crearEvento(RecitalEnVivo evento,
                            Artista artista) {

        if (artista != null) {
            artista.crearEvento(evento);
            evento.agregarArtista(artista);
        }

        eventos.add(evento);
    }

    /*
     * Registra un usuario si el nombre de usuario no existe.
     *
     * Devuelve true si pudo registrarlo y false si ya habia otro usuario
     * con el mismo nombre de usuario.
     */
    public boolean registrarUsuario(Usuario usuario) {
        if (existeNombreUsuario(usuario.getNombreUsuario())) {
            return false;
        }

        usuariosRegistrados.add(usuario);
        return true;
    }

    // Registra un artista en la lista del gestor.
    public void registrarArtista(Artista artista) {
        artistasRegistrados.add(artista);
    }

    /*
     * Da de alta un usuario nuevo a partir de los datos crudos que junta
     * la vista. El gestor es quien conoce los usuarios existentes, por lo
     * tanto es quien asigna el ID y construye el objeto del modelo.
     *
     * Devuelve el usuario creado o null si el nombre de usuario ya existe.
     */
    public Usuario registrarUsuario(String nombreUsuario,
                                    String nombre,
                                    String apellido,
                                    String email,
                                    String contrasena,
                                    PlanSuscripcion plan) {

        sesion.exigir(Accion.REGISTRAR_USUARIO);

        Usuario usuario = new Usuario(
                siguienteIdUsuario(),
                nombreUsuario,
                nombre,
                apellido,
                email,
                contrasena,
                plan,
                true
        );

        return registrarUsuario(usuario) ? usuario : null;
    }

    /*
     * Da de alta un artista nuevo con sus credenciales de acceso.
     *
     * Devuelve el artista creado o null si el nombre de cuenta ya existe
     * (los nombres de cuenta son unicos entre usuarios y artistas).
     */
    public Artista registrarArtista(String nombreUsuario,
                                    String contrasena,
                                    String nombreArtistico,
                                    String generoPrincipal,
                                    String biografia,
                                    boolean verificado) {

        sesion.exigir(Accion.REGISTRAR_ARTISTA);

        if (existeNombreUsuario(nombreUsuario)) {
            return null;
        }

        Artista artista = new Artista(
                siguienteIdArtista(),
                nombreUsuario.trim(),
                contrasena,
                nombreArtistico,
                generoPrincipal,
                biografia,
                verificado
        );

        registrarArtista(artista);
        return artista;
    }

    // Da de alta un recital nuevo asociado a un artista y lo devuelve.
    public RecitalEnVivo crearEvento(DatosRecital datos, Artista artista) {
        sesion.exigir(Accion.CREAR_EVENTO);

        // Un artista solo puede crear eventos a su propio nombre.
        Cuenta actual = sesion.getCuentaActual();
        if (actual.getTipoUsuario() == TipoUsuario.ARTISTA
                && artista.getId() != actual.getId()) {
            throw new PermisoDenegadoException(
                    "Un artista solo puede crear eventos propios.");
        }

        RecitalEnVivo recital = new RecitalEnVivo(siguienteIdEvento(), datos);
        crearEvento(recital, artista);
        return recital;
    }

    // El proximo ID de usuario es el mayor existente mas uno.
    private int siguienteIdUsuario() {
        int mayor = 0;
        for (Usuario usuario : usuariosRegistrados) {
            mayor = Math.max(mayor, usuario.getId());
        }
        return mayor + 1;
    }

    // El proximo ID de artista es el mayor existente mas uno.
    private int siguienteIdArtista() {
        int mayor = 0;
        for (Artista artista : artistasRegistrados) {
            mayor = Math.max(mayor, artista.getId());
        }
        return mayor + 1;
    }

    // El proximo ID de evento es el mayor existente mas uno.
    private int siguienteIdEvento() {
        int mayor = 0;
        for (Evento evento : eventos) {
            mayor = Math.max(mayor, evento.getId());
        }
        return mayor + 1;
    }

    /*
     * Devuelve eventos disponibles para operar.
     *
     * Excluye cancelados y finalizados porque ya no deberian aparecer
     * como alternativas de acceso disponibles.
     */
    public List<Evento>
    listarEventosDisponibles() {

        List<Evento> disponibles = new ArrayList<>();

        for (Evento evento : eventos) {
            if (evento.getEstado() != EstadoEvento.CANCELADO
                    && evento.getEstado() != EstadoEvento.FINALIZADO) {
                disponibles.add(evento);
            }
        }

        return disponibles;
    }

    /*
     * Eventos que la cuenta actual puede consultar: el administrador y los
     * usuarios ven todos; un artista ve solamente los suyos.
     */
    public List<Evento> listarEventosVisibles() {
        List<Evento> visibles = new ArrayList<>();

        for (Evento evento : eventos) {
            if (esVisible(evento)) {
                visibles.add(evento);
            }
        }

        return visibles;
    }

    // Busca un evento por ID entre los visibles para la cuenta actual.
    public Evento buscarEventoVisible(int id) {
        Evento evento = buscarEventoPorId(id);
        return (evento != null && esVisible(evento)) ? evento : null;
    }

    // Registros de acceso de la cuenta actual.
    public List<RegistroAcceso> listarMisRegistros() {
        sesion.exigir(Accion.VER_MIS_REGISTROS);

        List<RegistroAcceso> propios = new ArrayList<>();
        Cuenta actual = sesion.getCuentaActual();

        if (actual == null) {
            return propios;
        }

        for (RegistroAcceso registro : controlAcceso.listar()) {
            if (registro.getUsuario().getId() == actual.getId()) {
                propios.add(registro);
            }
        }

        return propios;
    }

    // Un artista solo ve los eventos en los que participa.
    private boolean esVisible(Evento evento) {
        Cuenta actual = sesion.getCuentaActual();

        if (actual == null) {
            return false;
        }

        if (actual.getTipoUsuario() != TipoUsuario.ARTISTA) {
            return true;
        }

        for (Artista artista : evento.getArtistas()) {
            if (artista.getId() == actual.getId()) {
                return true;
            }
        }

        return false;
    }

    // Devuelve copia de todos los eventos administrados.
    public List<Evento> listarEventos() {
        return new ArrayList<>(eventos);
    }

    // Devuelve copia de los usuarios registrados.
    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuariosRegistrados);
    }

    // Devuelve copia de los artistas registrados.
    public List<Artista> listarArtistas() {
        return new ArrayList<>(artistasRegistrados);
    }

    // Devuelve copia de los registros de acceso.
    public List<RegistroAcceso> listarRegistros() {
        return controlAcceso.listar();
    }

    /*
     * Reemplaza los datos principales al cargar desde archivos.
     *
     * Se copian las listas para mantener encapsulamiento.
     */
    public void reemplazarDatos(List<Usuario> usuarios,
                                List<Artista> artistas,
                                List<Evento> eventosCargados,
                                List<RegistroAcceso> registrosCargados) {

        usuariosRegistrados = new ArrayList<>(usuarios);
        artistasRegistrados = new ArrayList<>(artistas);
        eventos = new ArrayList<>(eventosCargados);
        controlAcceso.reemplazar(registrosCargados);
    }

    // Busca un evento por ID. Si no existe, devuelve null.
    public Evento buscarEventoPorId(int id) {
        for (Evento evento : eventos) {
            if (evento.getId() == id) {
                return evento;
            }
        }

        return null;
    }

    // Busca un usuario por ID. Si no existe, devuelve null.
    public Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : usuariosRegistrados) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }

        return null;
    }

    // Verifica si ya existe un usuario con ese nombre de cuenta.
    public boolean existeNombreUsuario(String nombreUsuario) {
        return buscarCuentaPorNombreUsuario(nombreUsuario) != null;
    }

    /*
     * Busca una cuenta por nombre de cuenta entre usuarios y artistas.
     * Si no existe, devuelve null.
     */
    public Cuenta buscarCuentaPorNombreUsuario(String nombreUsuario) {
        String buscado = nombreUsuario.trim();

        for (Usuario usuario : usuariosRegistrados) {
            if (usuario.getNombreUsuario().equalsIgnoreCase(buscado)) {
                return usuario;
            }
        }

        for (Artista artista : artistasRegistrados) {
            if (artista.getNombreUsuario().equalsIgnoreCase(buscado)) {
                return artista;
            }
        }

        return null;
    }

    /*
     * Inicia sesion con nombre de cuenta y contrasena. Sirve para cualquier
     * tipo de cuenta: usuario, administrador o artista.
     *
     * Devuelve la cuenta autenticada o lanza la excepcion con el motivo.
     */
    public Cuenta iniciarSesion(String nombreUsuario, String contrasena)
            throws AccesoDenegadoException {

        Cuenta cuenta = buscarCuentaPorNombreUsuario(nombreUsuario);

        if (cuenta == null) {
            throw new AccesoDenegadoException("Cuenta inexistente.");
        }

        if (!cuenta.validarContrasena(contrasena)) {
            throw new AccesoDenegadoException("Contrasena incorrecta.");
        }

        if (!cuenta.isActivo()) {
            throw new AccesoDenegadoException("La cuenta no esta activa.");
        }

        sesion.iniciar(cuenta);
        return cuenta;
    }

    // Cierra la sesion actual.
    public void cerrarSesion() {
        sesion.cerrar();
    }

    // Cuenta con sesion iniciada, o null.
    public Cuenta getCuentaActual() {
        return sesion.getCuentaActual();
    }

    // Indica si la cuenta actual puede ejecutar la accion.
    public boolean puede(Accion accion) {
        return sesion.permite(accion);
    }

    // Acciones que puede ejecutar la cuenta actual.
    public List<Accion> accionesPermitidas() {
        return sesion.accionesPermitidas();
    }

    // Busca un artista por ID. Si no existe, devuelve null.
    public Artista buscarArtistaPorId(int id) {
        for (Artista artista : artistasRegistrados) {
            if (artista.getId() == id) {
                return artista;
            }
        }

        return null;
    }

    /*
     * Centraliza las transiciones de estado pedidas por las interfaces.
     *
     * Consola y Swing llaman a estos metodos en vez de invocar
     * iniciarEvento/pausarEvento/etc directamente sobre el Evento: el
     * gestor es quien recibe el pedido, busca el evento y coordina.
     */
    public void iniciarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.INICIAR_EVENTO).iniciarEvento();
    }

    public void pausarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.PAUSAR_EVENTO).pausarEvento();
    }

    public void reanudarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.REANUDAR_EVENTO).reanudarEvento();
    }

    public void finalizarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.FINALIZAR_EVENTO).finalizarEvento();
    }

    public void cancelarEvento(int idEvento) throws AccesoDenegadoException {
        Evento evento = buscarEventoParaCambio(idEvento, Accion.CANCELAR_EVENTO);

        if (!evento.getEstado().permiteCancelar()) {
            throw new AccesoDenegadoException(
                    "No se puede cancelar un evento en estado "
                            + evento.getEstado() + "."
            );
        }

        evento.cancelarEvento();
    }

    private Evento buscarEventoParaCambio(int idEvento, Accion accion)
            throws AccesoDenegadoException {

        // El rol debe permitir la accion y, para un artista, el evento
        // debe ser uno de los suyos.
        sesion.exigir(accion);

        Evento evento = buscarEventoPorId(idEvento);

        if (evento == null) {
            throw new AccesoDenegadoException("Evento inexistente.");
        }

        if (!esVisible(evento)) {
            throw new PermisoDenegadoException(
                    "El evento no pertenece a su cuenta.");
        }

        return evento;
    }

    // Agrega un registro a la trazabilidad.
    public void registrarAcceso(RegistroAcceso registro) {
        controlAcceso.registrar(registro);
    }

    // Caso de uso principal: solicitar ingreso a un evento.
    public RegistroAcceso solicitarIngreso(int idUsuario, int idEvento) {
        sesion.exigir(Accion.SOLICITAR_INGRESO);

        // Un usuario solo puede solicitar el ingreso a su propio nombre.
        Cuenta actual = sesion.getCuentaActual();
        if (actual.getTipoUsuario() == TipoUsuario.USUARIO
                && idUsuario != actual.getId()) {
            throw new PermisoDenegadoException(
                    "Un usuario solo puede solicitar ingreso a su nombre.");
        }

        return controlAcceso.procesarIngreso(
                buscarUsuarioPorId(idUsuario),
                buscarEventoPorId(idEvento));
    }

    /*
     * Expulsa un usuario conectado a un evento.
     *
     * Devuelve el usuario expulsado si pudo hacerlo, para que quien llama
     * pueda notificarlo. Devuelve null si no existia el evento, el usuario,
     * el evento no estaba en curso o el usuario no estaba conectado.
     *
     * Ademas deja trazabilidad de la expulsion en RegistroAcceso, igual
     * que se hace con los intentos de ingreso.
     */
    public Usuario expulsarUsuario(int idEvento, int idUsuario) {
        sesion.exigir(Accion.EXPULSAR_USUARIO);

        Evento evento = buscarEventoPorId(idEvento);
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        // Solo se puede expulsar desde un evento en curso.
        if (evento != null
                && evento.getEstado() == EstadoEvento.EN_CURSO
                && usuario != null
                && evento.getUsuariosConectados().contains(usuario)) {

            evento.expulsarUsuario(usuario);
            controlAcceso.registrarExpulsion(usuario, evento);
            return usuario;
        }

        return null;
    }
}
