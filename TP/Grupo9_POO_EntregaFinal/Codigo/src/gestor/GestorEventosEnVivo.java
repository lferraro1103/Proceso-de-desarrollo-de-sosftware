package gestor;

import excepciones.AccesoDenegadoException;
import excepciones.PermisoDenegadoException;
import excepciones.PersistenciaException;
import modelo.Artista;
import modelo.Contrasenas;
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
 *
 * Sus operaciones publicas son synchronized porque pueden llegar desde la
 * ventana local y desde los hilos del servidor al mismo tiempo.
 */
public class GestorEventosEnVivo implements ServicioEventos {

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

    // Direccion donde se publica el calendario de cada evento.
    private PublicadorCalendario publicador;

    // Servicio que encuentra la transmision de cada evento.
    private ProveedorTransmision proveedorTransmision;

    // Constructor: inicializa las colecciones y recibe sus colaboradores.
    public GestorEventosEnVivo(Persistencia persistencia,
                               Notificador notificador,
                               PublicadorCalendario publicador,
                               ProveedorTransmision proveedorTransmision) {

        this.persistencia = persistencia;
        this.publicador = publicador;
        this.proveedorTransmision = proveedorTransmision;
        eventos = new ArrayList<>();
        usuariosRegistrados = new ArrayList<>();
        artistasRegistrados = new ArrayList<>();
        controlAcceso = new ControlAcceso(notificador);
        sesion = new ControlSesion();
    }

    // Carga los datos usando la persistencia recibida. Se usa al arrancar.
    public synchronized void cargarDatos() throws IOException {
        persistencia.cargar(this);
    }

    // Guarda los datos. Cada operacion que modifica el sistema lo invoca.
    public synchronized void guardarDatos() {
        try {
            persistencia.guardar(this);
        } catch (IOException e) {
            throw new PersistenciaException(
                    "No se pudieron guardar los datos: " + e.getMessage(), e);
        }
    }

    // Agrega un recital al gestor.
    public synchronized void crearEvento(
            RecitalEnVivo evento) {

        sesion.exigirSiHaySesion(Accion.CREAR_EVENTO);

        eventos.add(evento);
    }

    /*
     * Crea un evento asociado a un artista.
     *
     * Se llama explicitamente a artista.crearEvento(evento) para usar
     * el comportamiento propio de Artista y mantener la trazabilidad.
     */
    public synchronized void crearEvento(RecitalEnVivo evento,
                            Artista artista) {

        sesion.exigirSiHaySesion(Accion.CREAR_EVENTO);

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
    public synchronized boolean registrarUsuario(Usuario usuario) {
        sesion.exigirSiHaySesion(Accion.REGISTRAR_USUARIO);

        if (existeNombreUsuario(usuario.getNombreUsuario())) {
            return false;
        }

        usuariosRegistrados.add(usuario);
        return true;
    }

    // Registra un artista en la lista del gestor.
    public synchronized void registrarArtista(Artista artista) {
        sesion.exigirSiHaySesion(Accion.REGISTRAR_ARTISTA);

        artistasRegistrados.add(artista);
    }

    /*
     * Da de alta un usuario nuevo a partir de los datos crudos que junta
     * la vista. El gestor es quien conoce los usuarios existentes, por lo
     * tanto es quien asigna el ID y construye el objeto del modelo.
     *
     * Devuelve el usuario creado o null si el nombre de usuario ya existe.
     */
    @Override
    public synchronized Usuario registrarUsuario(String nombreUsuario,
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
                Contrasenas.hashear(contrasena),
                plan,
                true
        );

        if (!registrarUsuario(usuario)) {
            return null;
        }

        guardarDatos();
        return usuario;
    }

    /*
     * Da de alta un artista nuevo con sus credenciales de acceso.
     *
     * Devuelve el artista creado o null si el nombre de cuenta ya existe
     * (los nombres de cuenta son unicos entre usuarios y artistas).
     */
    @Override
    public synchronized Artista registrarArtista(String nombreUsuario,
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
                Contrasenas.hashear(contrasena),
                nombreArtistico,
                generoPrincipal,
                biografia,
                verificado
        );

        registrarArtista(artista);
        guardarDatos();
        return artista;
    }

    // Da de alta un recital nuevo asociado a un artista y lo devuelve.
    @Override
    public synchronized RecitalEnVivo crearEvento(DatosRecital datos, Artista artista) {
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
        guardarDatos();
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
    @Override
    public synchronized List<Evento> listarEventosVisibles() {
        List<Evento> visibles = new ArrayList<>();

        for (Evento evento : eventos) {
            if (esVisible(evento)) {
                visibles.add(evento);
            }
        }

        return visibles;
    }

    // Busca un evento por ID entre los visibles para la cuenta actual.
    @Override
    public synchronized Evento buscarEventoVisible(int id) {
        Evento evento = buscarEventoPorId(id);
        return (evento != null && esVisible(evento)) ? evento : null;
    }

    // Registros de acceso de la cuenta actual.
    @Override
    public synchronized List<RegistroAcceso> listarMisRegistros() {
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
        return esVisiblePara(sesion.getCuentaActual(), evento);
    }

    private boolean esVisiblePara(Cuenta actual, Evento evento) {
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

    // Los datos de un gestor local siempre estan al dia.
    @Override
    public synchronized void actualizar() {
    }

    // Devuelve copia de todos los eventos administrados.
    public synchronized List<Evento> listarEventos() {
        return new ArrayList<>(eventos);
    }

    // Devuelve copia de los usuarios registrados.
    @Override
    public synchronized List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuariosRegistrados);
    }

    // Devuelve copia de los artistas registrados.
    @Override
    public synchronized List<Artista> listarArtistas() {
        return new ArrayList<>(artistasRegistrados);
    }

    // Devuelve copia de los registros de acceso.
    @Override
    public synchronized List<RegistroAcceso> listarRegistros() {
        return controlAcceso.listar();
    }

    /*
     * Reemplaza los datos principales al cargar desde archivos.
     *
     * Se copian las listas para mantener encapsulamiento.
     */
    public synchronized void reemplazarDatos(List<Usuario> usuarios,
                                List<Artista> artistas,
                                List<Evento> eventosCargados,
                                List<RegistroAcceso> registrosCargados) {

        usuariosRegistrados = new ArrayList<>(usuarios);
        artistasRegistrados = new ArrayList<>(artistas);
        eventos = new ArrayList<>(eventosCargados);
        controlAcceso.reemplazar(registrosCargados);

        reenlazarSesion();
    }

    /*
     * Despues de cargar datos los objetos son nuevos: la sesion pasa a la
     * cuenta equivalente recien cargada. Si esa cuenta ya no existe, esta
     * inactiva o cambio de rol, la sesion se cierra.
     */
    private void reenlazarSesion() {
        Cuenta actual = sesion.getCuentaActual();

        if (actual == null) {
            return;
        }

        Cuenta cargada = buscarCuentaPorNombreUsuario(actual.getNombreUsuario());

        if (cargada == null || !cargada.isActivo()
                || cargada.getTipoUsuario() != actual.getTipoUsuario()) {
            sesion.cerrar();
        } else {
            sesion.iniciar(cargada);
        }
    }

    // Busca un evento por ID. Si no existe, devuelve null.
    public synchronized Evento buscarEventoPorId(int id) {
        for (Evento evento : eventos) {
            if (evento.getId() == id) {
                return evento;
            }
        }

        return null;
    }

    // Busca un usuario por ID. Si no existe, devuelve null.
    @Override
    public synchronized Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : usuariosRegistrados) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }

        return null;
    }

    // Verifica si ya existe un usuario con ese nombre de cuenta.
    @Override
    public synchronized boolean existeNombreUsuario(String nombreUsuario) {
        return buscarCuentaPorNombreUsuario(nombreUsuario) != null;
    }

    /*
     * Busca una cuenta por nombre de cuenta entre usuarios y artistas.
     * Si no existe, devuelve null.
     */
    public synchronized Cuenta buscarCuentaPorNombreUsuario(String nombreUsuario) {
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
     * Verifica nombre de cuenta y contrasena sin abrir ninguna sesion.
     * Sirve para cualquier tipo de cuenta: usuario, administrador o artista.
     *
     * Devuelve la cuenta autenticada o lanza la excepcion con el motivo.
     * Lo usa el servidor para iniciar la sesion de un cliente remoto.
     */
    public synchronized Cuenta autenticar(String nombreUsuario, String contrasena)
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

        return cuenta;
    }

    // Inicia sesion con nombre de cuenta y contrasena.
    @Override
    public synchronized Cuenta iniciarSesion(String nombreUsuario, String contrasena)
            throws AccesoDenegadoException {

        Cuenta cuenta = autenticar(nombreUsuario, contrasena);
        sesion.iniciar(cuenta);
        return cuenta;
    }

    /*
     * Abre la sesion de una cuenta ya autenticada por otro medio, sin pedir
     * la contrasena. Lo usa el servidor, que valida el token de cada pedido,
     * y la copia local de un cliente remoto. No esta en ServicioEventos: las
     * vistas no pueden saltarse el login.
     */
    public synchronized void asumirSesion(Cuenta cuenta) {
        sesion.iniciar(cuenta);
    }

    // Cierra la sesion actual.
    @Override
    public synchronized void cerrarSesion() {
        sesion.cerrar();
    }

    // Cuenta con sesion iniciada, o null.
    @Override
    public synchronized Cuenta getCuentaActual() {
        return sesion.getCuentaActual();
    }

    // Indica si la cuenta actual puede ejecutar la accion.
    @Override
    public synchronized boolean puede(Accion accion) {
        return sesion.permite(accion);
    }

    // Acciones que puede ejecutar la cuenta actual.
    @Override
    public synchronized List<Accion> accionesPermitidas() {
        return sesion.accionesPermitidas();
    }

    /*
     * Suscribe un calendario externo a un evento visible para la cuenta
     * actual. El permiso se verifica aca; el calendario concreto llega como
     * parametro.
     */
    @Override
    public synchronized void vincularCalendario(int idEvento, VinculoCalendario vinculo)
            throws IOException {
        vinculo.vincular(urlCalendario(idEvento));
    }

    /*
     * Direccion donde se publica el calendario de un evento visible para la
     * cuenta actual. Tambien la usa el servidor cuando un cliente remoto
     * pide suscribirse.
     */
    public synchronized String urlCalendario(int idEvento) throws IOException {
        sesion.exigir(Accion.VINCULAR_CALENDARIO);

        Evento evento = buscarEventoVisible(idEvento);

        if (evento == null) {
            throw new IllegalArgumentException("Evento inexistente.");
        }

        return publicador.urlFeed(sesion.getCuentaActual(), evento);
    }

    /*
     * Transmision de un evento en curso. Se decide con el bloqueo tomado, pero
     * la busqueda en el servicio externo se hace sin el: puede tardar y no
     * debe frenar al resto de las operaciones.
     */
    @Override
    public Transmision obtenerTransmision(int idEvento)
            throws AccesoDenegadoException, IOException {
        return proveedorTransmision.buscar(consultaDeTransmision(idEvento));
    }

    /*
     * Verifica que la cuenta actual pueda ver la transmision del evento y
     * devuelve que buscar: el artista principal o, si no tiene, el titulo.
     * Tambien la usa el servidor antes de buscar fuera del bloqueo.
     */
    public synchronized String consultaDeTransmision(int idEvento)
            throws AccesoDenegadoException {

        sesion.exigir(Accion.VER_TRANSMISION);

        Evento evento = buscarEventoVisible(idEvento);

        if (evento == null) {
            throw new IllegalArgumentException("Evento inexistente.");
        }

        if (evento.getEstado() != EstadoEvento.EN_CURSO) {
            throw new AccesoDenegadoException("La transmision solo esta "
                    + "disponible mientras el evento esta en curso. Estado "
                    + "actual: " + evento.getEstado() + ".");
        }

        Cuenta actual = sesion.getCuentaActual();

        if (actual.getTipoUsuario() == TipoUsuario.USUARIO
                && !estaConectado(evento, actual)) {
            throw new AccesoDenegadoException(
                    "Debe ingresar al evento para ver la transmision.");
        }

        return evento.getArtistas().isEmpty()
                ? evento.getTitulo()
                : evento.getArtistas().get(0).getNombreArtistico();
    }

    private boolean estaConectado(Evento evento, Cuenta cuenta) {
        for (Usuario usuario : evento.getUsuariosConectados()) {
            if (usuario.getId() == cuenta.getId()) {
                return true;
            }
        }

        return false;
    }

    /*
     * Evento de ese ID si la cuenta puede verlo, sin depender de la sesion.
     * Lo usa el servidor del calendario, que atiende a cuentas sin sesion
     * abierta en el programa. Si no existe o no lo puede ver, devuelve null.
     */
    public synchronized Evento buscarEventoVisiblePara(Cuenta cuenta, int id) {
        Evento evento = buscarEventoPorId(id);
        return (evento != null && esVisiblePara(cuenta, evento)) ? evento : null;
    }

    // Busca un artista por ID. Si no existe, devuelve null.
    @Override
    public synchronized Artista buscarArtistaPorId(int id) {
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
    @Override
    public synchronized void iniciarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.INICIAR_EVENTO).iniciarEvento();
        guardarDatos();
    }

    @Override
    public synchronized void pausarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.PAUSAR_EVENTO).pausarEvento();
        guardarDatos();
    }

    @Override
    public synchronized void reanudarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.REANUDAR_EVENTO).reanudarEvento();
        guardarDatos();
    }

    @Override
    public synchronized void finalizarEvento(int idEvento) throws AccesoDenegadoException {
        buscarEventoParaCambio(idEvento, Accion.FINALIZAR_EVENTO).finalizarEvento();
        guardarDatos();
    }

    @Override
    public synchronized void cancelarEvento(int idEvento) throws AccesoDenegadoException {
        Evento evento = buscarEventoParaCambio(idEvento, Accion.CANCELAR_EVENTO);

        if (!evento.getEstado().permiteCancelar()) {
            throw new AccesoDenegadoException(
                    "No se puede cancelar un evento en estado "
                            + evento.getEstado() + "."
            );
        }

        evento.cancelarEvento();
        guardarDatos();
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
    public synchronized void registrarAcceso(RegistroAcceso registro) {
        controlAcceso.registrar(registro);
    }

    // Caso de uso principal: solicitar ingreso a un evento.
    @Override
    public synchronized RegistroAcceso solicitarIngreso(int idUsuario, int idEvento) {
        sesion.exigir(Accion.SOLICITAR_INGRESO);

        // Un usuario solo puede solicitar el ingreso a su propio nombre.
        Cuenta actual = sesion.getCuentaActual();
        if (actual.getTipoUsuario() == TipoUsuario.USUARIO
                && idUsuario != actual.getId()) {
            throw new PermisoDenegadoException(
                    "Un usuario solo puede solicitar ingreso a su nombre.");
        }

        RegistroAcceso registro = controlAcceso.procesarIngreso(
                buscarUsuarioPorId(idUsuario),
                buscarEventoPorId(idEvento));

        // Tambien los intentos rechazados quedan registrados.
        guardarDatos();
        return registro;
    }

    /*
     * Activa o desactiva un usuario segun su estado actual.
     *
     * Un usuario desactivado no puede iniciar sesion ni ingresar a eventos,
     * y se lo desconecta de los eventos en los que estaba. Un administrador
     * no puede desactivar su propia cuenta para no quedar sin acceso.
     *
     * Devuelve el usuario con su nuevo estado, o null si no existe.
     */
    @Override
    public synchronized Usuario alternarActivo(int idUsuario) {
        sesion.exigir(Accion.ACTIVAR_DESACTIVAR_USUARIO);

        Usuario usuario = buscarUsuarioPorId(idUsuario);

        if (usuario == null) {
            return null;
        }

        if (!usuario.isActivo()) {
            usuario.activar();
            guardarDatos();
            return usuario;
        }

        Cuenta actual = sesion.getCuentaActual();
        if (actual.getId() == usuario.getId()
                && actual.getTipoUsuario() == usuario.getTipoUsuario()) {
            throw new PermisoDenegadoException(
                    "No puede desactivar su propia cuenta.");
        }

        usuario.desactivar();

        for (Evento evento : eventos) {
            if (evento.getUsuariosConectados().contains(usuario)) {
                evento.expulsarUsuario(usuario);
                controlAcceso.registrarExpulsion(usuario, evento);
            }
        }

        guardarDatos();
        return usuario;
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
    @Override
    public synchronized Usuario expulsarUsuario(int idEvento, int idUsuario) {
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
            guardarDatos();
            return usuario;
        }

        return null;
    }
}
