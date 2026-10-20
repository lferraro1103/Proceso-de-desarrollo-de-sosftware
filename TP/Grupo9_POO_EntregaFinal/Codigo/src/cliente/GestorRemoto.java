package cliente;

import api.EstadoDto;
import api.IdDto;
import api.InicioSesionDto;
import api.Json;
import api.NuevoArtistaDto;
import api.NuevoEventoDto;
import api.NuevoUsuarioDto;
import api.RegistroDto;
import api.SesionDto;
import api.TransmisionDto;
import api.UrlDto;
import api.UsuarioPedidoDto;
import excepciones.AccesoDenegadoException;
import excepciones.ConexionException;
import excepciones.PermisoDenegadoException;
import gestor.Accion;
import gestor.Permisos;
import gestor.Transmision;
import gestor.ServicioEventos;
import gestor.VinculoCalendario;
import modelo.Artista;
import modelo.Cuenta;
import modelo.DatosRecital;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Gestor que no tiene datos propios: reenvia cada pedido a un servidor y
 * muestra lo que este le deja ver.
 *
 * Es un proxy remoto de GestorEventosEnVivo: implementa la misma interfaz
 * (ServicioEventos), por lo que la ventana y la consola funcionan igual con
 * uno o con otro. Las consultas se responden con una copia local que se
 * vuelve a pedir al servidor despues de cada cambio o al actualizar; los
 * cambios se hacen siempre en el servidor, que es quien verifica permisos y
 * reglas (los permisos de aca solo sirven para armar los menus).
 */
public class GestorRemoto implements ServicioEventos {

    private final ClienteHttp http;
    private final Permisos permisos = new Permisos();

    // Copia de lo que el servidor deja ver; null si no hay sesion.
    private EstadoLocal estado;

    // Indica si hay una sesion abierta en el servidor.
    private boolean conSesion;

    public GestorRemoto(String urlServidor) {
        this.http = new ClienteHttp(urlServidor);
    }

    // ---------------- sesion ----------------

    @Override
    public Cuenta iniciarSesion(String nombreUsuario, String contrasena)
            throws AccesoDenegadoException {

        try {
            SesionDto sesion = Json.leer(http.post("/api/sesion",
                    new InicioSesionDto(nombreUsuario, contrasena)),
                    SesionDto.class);

            http.setToken(sesion.token());
            conSesion = true;
            actualizar();
            return estado.getCuenta();
        } catch (IOException e) {
            throw new ConexionException(e.getMessage(), e);
        }
    }

    @Override
    public void cerrarSesion() {
        try {
            http.delete("/api/sesion");
        } catch (AccesoDenegadoException | IOException | RuntimeException e) {
            // Aunque el servidor no responda, la sesion local se descarta.
        }

        http.setToken(null);
        conSesion = false;
        estado = null;
    }

    @Override
    public Cuenta getCuentaActual() {
        return estado == null ? null : estado.getCuenta();
    }

    @Override
    public boolean puede(Accion accion) {
        return estado != null
                && permisos.permite(estado.getCuenta().getTipoUsuario(), accion);
    }

    @Override
    public List<Accion> accionesPermitidas() {
        return estado == null
                ? new ArrayList<>()
                : permisos.accionesPermitidas(estado.getCuenta().getTipoUsuario());
    }

    // ---------------- consultas ----------------

    @Override
    public List<Usuario> listarUsuarios() {
        return estado == null ? new ArrayList<>() : estado.getUsuarios();
    }

    @Override
    public List<Artista> listarArtistas() {
        return estado == null ? new ArrayList<>() : estado.getArtistas();
    }

    @Override
    public List<Evento> listarEventosVisibles() {
        return estado == null ? new ArrayList<>() : estado.getEventos();
    }

    @Override
    public List<RegistroAcceso> listarRegistros() {
        return estado == null ? new ArrayList<>() : estado.getRegistros();
    }

    @Override
    public List<RegistroAcceso> listarMisRegistros() {
        exigir(Accion.VER_MIS_REGISTROS);

        List<RegistroAcceso> propios = new ArrayList<>();
        for (RegistroAcceso registro : estado.getRegistros()) {
            if (registro.getUsuario().getId() == estado.getCuenta().getId()) {
                propios.add(registro);
            }
        }

        return propios;
    }

    @Override
    public Evento buscarEventoVisible(int id) {
        for (Evento evento : listarEventosVisibles()) {
            if (evento.getId() == id) {
                return evento;
            }
        }

        return null;
    }

    @Override
    public Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : listarUsuarios()) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }

        return null;
    }

    @Override
    public Artista buscarArtistaPorId(int id) {
        for (Artista artista : listarArtistas()) {
            if (artista.getId() == id) {
                return artista;
            }
        }

        return null;
    }

    /*
     * Solo ve los nombres que el servidor le deja ver: el administrador, que
     * es quien da de alta cuentas, los ve todos. El servidor rechaza igual un
     * nombre repetido.
     */
    @Override
    public boolean existeNombreUsuario(String nombreUsuario) {
        String buscado = nombreUsuario.trim();

        for (Usuario usuario : listarUsuarios()) {
            if (usuario.getNombreUsuario().equalsIgnoreCase(buscado)) {
                return true;
            }
        }

        for (Artista artista : listarArtistas()) {
            if (artista.getNombreUsuario().equalsIgnoreCase(buscado)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Transmision obtenerTransmision(int idEvento)
            throws AccesoDenegadoException, IOException {

        TransmisionDto dto = Json.leer(
                http.get("/api/eventos/" + idEvento + "/transmision"),
                TransmisionDto.class);

        return new Transmision(dto.titulo(), dto.url());
    }

    // Vuelve a pedir al servidor lo que la cuenta puede ver.
    @Override
    public void actualizar() {
        if (!conSesion) {
            return;
        }

        try {
            estado = new EstadoLocal(Json.leer(http.get("/api/estado"),
                    EstadoDto.class));
        } catch (AccesoDenegadoException | IOException e) {
            throw new ConexionException(e.getMessage(), e);
        }
    }

    // ---------------- comandos ----------------

    @Override
    public Usuario registrarUsuario(String nombreUsuario,
                                    String nombre,
                                    String apellido,
                                    String email,
                                    String contrasena,
                                    PlanSuscripcion plan) {

        Integer id = pedirId("/api/usuarios", new NuevoUsuarioDto(
                nombreUsuario, nombre, apellido, email, contrasena, plan.name()));

        return id == null ? null : buscarUsuarioPorId(id);
    }

    @Override
    public Artista registrarArtista(String nombreUsuario,
                                    String contrasena,
                                    String nombreArtistico,
                                    String generoPrincipal,
                                    String biografia,
                                    boolean verificado) {

        Integer id = pedirId("/api/artistas", new NuevoArtistaDto(
                nombreUsuario, contrasena, nombreArtistico, generoPrincipal,
                biografia, verificado));

        return id == null ? null : buscarArtistaPorId(id);
    }

    @Override
    public RecitalEnVivo crearEvento(DatosRecital datos, Artista artista) {
        Integer id = pedirId("/api/eventos", new NuevoEventoDto(
                artista.getId(), datos.getTitulo(), datos.getDescripcion(),
                datos.getFechaHoraInicio().toString(),
                datos.getFechaHoraFin().toString(), datos.getEstado().name(),
                datos.getCapacidadMaxima(), datos.getPlanMinimoRequerido().name(),
                datos.getUbicacion(), datos.isEsStreaming(), datos.isExclusivo()));

        return (RecitalEnVivo) buscarEventoVisible(id);
    }

    @Override
    public void iniciarEvento(int idEvento) throws AccesoDenegadoException {
        cambiarEstado(idEvento, "iniciar");
    }

    @Override
    public void pausarEvento(int idEvento) throws AccesoDenegadoException {
        cambiarEstado(idEvento, "pausar");
    }

    @Override
    public void reanudarEvento(int idEvento) throws AccesoDenegadoException {
        cambiarEstado(idEvento, "reanudar");
    }

    @Override
    public void finalizarEvento(int idEvento) throws AccesoDenegadoException {
        cambiarEstado(idEvento, "finalizar");
    }

    @Override
    public void cancelarEvento(int idEvento) throws AccesoDenegadoException {
        cambiarEstado(idEvento, "cancelar");
    }

    @Override
    public RegistroAcceso solicitarIngreso(int idUsuario, int idEvento) {
        try {
            RegistroDto registro = Json.leer(
                    http.post("/api/eventos/" + idEvento + "/ingresos",
                            new UsuarioPedidoDto(idUsuario)),
                    RegistroDto.class);

            // Tambien un ingreso rechazado cambia los registros del servidor.
            actualizar();
            return estado.construirRegistro(registro);
        } catch (AccesoDenegadoException | IOException e) {
            throw new ConexionException(e.getMessage(), e);
        }
    }

    @Override
    public Usuario alternarActivo(int idUsuario) {
        Integer id = pedirId("/api/usuarios/" + idUsuario + "/alternar-activo", null);
        return id == null ? null : buscarUsuarioPorId(id);
    }

    @Override
    public Usuario expulsarUsuario(int idEvento, int idUsuario) {
        Integer id = pedirId("/api/eventos/" + idEvento + "/expulsiones",
                new UsuarioPedidoDto(idUsuario));

        return id == null ? null : buscarUsuarioPorId(id);
    }

    @Override
    public void vincularCalendario(int idEvento, VinculoCalendario vinculo)
            throws IOException {

        try {
            UrlDto url = Json.leer(
                    http.get("/api/eventos/" + idEvento + "/calendario"),
                    UrlDto.class);

            vinculo.vincular(url.url());
        } catch (AccesoDenegadoException e) {
            throw new ConexionException(e.getMessage(), e);
        }
    }

    // ---------------- auxiliares ----------------

    private void exigir(Accion accion) {
        if (estado == null) {
            throw new PermisoDenegadoException(
                    "Debe iniciar sesion para realizar esta accion.");
        }

        if (!puede(accion)) {
            throw new PermisoDenegadoException(
                    "Su rol no permite: " + accion.getEtiqueta() + ".");
        }
    }

    // Envia un cambio de estado y refresca la copia local.
    private void cambiarEstado(int idEvento, String accion)
            throws AccesoDenegadoException {

        try {
            http.post("/api/eventos/" + idEvento + "/" + accion, null);
            actualizar();
        } catch (IOException e) {
            throw new ConexionException(e.getMessage(), e);
        }
    }

    // Envia un pedido que responde con un ID, refresca y devuelve el ID.
    private Integer pedirId(String ruta, Object cuerpo) {
        try {
            Integer id = Json.leer(http.post(ruta, cuerpo), IdDto.class).id();
            actualizar();
            return id;
        } catch (AccesoDenegadoException | IOException e) {
            throw new ConexionException(e.getMessage(), e);
        }
    }
}
