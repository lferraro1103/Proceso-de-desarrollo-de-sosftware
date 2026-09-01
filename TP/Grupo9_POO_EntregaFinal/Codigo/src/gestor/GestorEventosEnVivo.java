package gestor;

import excepciones.AccesoDenegadoException;
import modelo.AccesoEvento;
import modelo.Artista;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.Usuario;

import java.time.LocalDateTime;
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

    // Registros de intentos de acceso exitosos o fallidos.
    private List<RegistroAcceso> registros;

    // Usuarios registrados para la simulacion.
    private List<Usuario> usuariosRegistrados;

    // Artistas disponibles para asociar a eventos.
    private List<Artista> artistasRegistrados;

    // ID incremental para los objetos AccesoEvento.
    private int siguienteIdAcceso;

    // Constructor: inicializa todas las colecciones.
    public GestorEventosEnVivo() {

        eventos = new ArrayList<>();
        registros = new ArrayList<>();
        usuariosRegistrados = new ArrayList<>();
        artistasRegistrados = new ArrayList<>();
        siguienteIdAcceso = 1;
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
        return new ArrayList<>(registros);
    }

    /*
     * Reemplaza los datos principales al cargar desde archivos.
     *
     * Se copian las listas para mantener encapsulamiento.
     */
    public void reemplazarDatos(List<Usuario> usuarios,
                                List<Artista> artistas,
                                List<Evento> eventosCargados) {

        usuariosRegistrados = new ArrayList<>(usuarios);
        artistasRegistrados = new ArrayList<>(artistas);
        eventos = new ArrayList<>(eventosCargados);
        registros = new ArrayList<>();
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
        return buscarUsuarioPorNombreUsuario(nombreUsuario) != null;
    }

    // Busca un usuario por nombre de cuenta. Si no existe, devuelve null.
    public Usuario buscarUsuarioPorNombreUsuario(String nombreUsuario) {
        for (Usuario usuario : usuariosRegistrados) {
            if (usuario.getNombreUsuario().equalsIgnoreCase(
                    nombreUsuario.trim())) {
                return usuario;
            }
        }

        return null;
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

    // Cancela un evento si existe.
    public void cancelarEvento(Evento evento) {
        if (evento != null) {
            evento.cancelarEvento();
        }
    }

    // Agrega un registro a la trazabilidad del gestor.
    public void registrarAcceso(
            RegistroAcceso registro) {

        registros.add(registro);
    }

    /*
     * Caso de uso principal: solicitar ingreso a un evento.
     *
     * Flujo:
     * 1. Busca usuario y evento.
     * 2. Crea un AccesoEvento.
     * 3. Valida autorizacion y plan.
     * 4. Pide al RecitalEnVivo que permita el ingreso.
     * 5. Guarda un RegistroAcceso con resultado exitoso o fallido.
     */
    public RegistroAcceso solicitarIngreso(int idUsuario, int idEvento) {
        // Se buscan las entidades involucradas.
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        Evento evento = buscarEventoPorId(idEvento);

        // Si el usuario no existe, se registra fallo.
        if (usuario == null) {
            return registrarFalloSinUsuario(evento, "Usuario inexistente.");
        }

        // Si el evento no existe, se registra fallo asociado al usuario.
        if (evento == null) {
            RegistroAcceso registro = RegistroAcceso.registrarIntentoFallido(
                    usuario,
                    eventoDesconocido(),
                    "Evento inexistente."
            );
            registrarAcceso(registro);
            return registro;
        }

        // Este modulo opera con RecitalEnVivo.
        if (!(evento instanceof RecitalEnVivo)) {
            RegistroAcceso registro = RegistroAcceso.registrarIntentoFallido(
                    usuario,
                    evento,
                    "Tipo de evento no soportado por este modulo."
            );
            registrarAcceso(registro);
            return registro;
        }

        // Se castea porque ya se valido que el evento es un RecitalEnVivo.
        RecitalEnVivo recital = (RecitalEnVivo) evento;

        // Se crea el acceso con el plan requerido por el recital.
        AccesoEvento acceso = new AccesoEvento(
                siguienteIdAcceso++,
                usuario,
                recital,
                LocalDateTime.now(),
                usuario.isActivo(),
                "GENERAL",
                recital.getPlanMinimoRequerido(),
                usuario.tieneAccesoPrioritario()
        );

        try {
            // Valida si el usuario esta activo y autorizado.
            acceso.validarAutorizacion();

            // Valida si el plan del usuario alcanza el requerido.
            acceso.validarPlan();

            // Valida estado, horario, capacidad y duplicado; si pasa, conecta.
            recital.permitirIngreso(usuario);

            // Si todo salio bien, se crea registro exitoso.
            RegistroAcceso registro = RegistroAcceso.generarRegistro(
                    usuario,
                    recital,
                    true,
                    ""
            );
            registrarAcceso(registro);
            return registro;

        } catch (AccesoDenegadoException e) {
            // Si alguna validacion falla, se registra el motivo exacto.
            RegistroAcceso registro = RegistroAcceso.registrarIntentoFallido(
                    usuario,
                    recital,
                    e.getMessage()
            );
            registrarAcceso(registro);
            return registro;
        }
    }

    /*
     * Registra un fallo cuando no hay usuario valido.
     *
     * Se crea un usuario "desconocido" solo para mantener trazabilidad
     * sin romper el tipo esperado por RegistroAcceso.
     */
    private RegistroAcceso registrarFalloSinUsuario(Evento evento,
                                                    String motivo) {

        Usuario usuarioDesconocido = new Usuario(
                -1,
                "desconocido",
                "Usuario",
                "Desconocido",
                "sin-email",
                "",
                PlanSuscripcion.FREE,
                false
        );

        // Si tampoco hay evento, se reutiliza el mismo evento "placeholder"
        // que usa solicitarIngreso para el caso evento==null (antes se
        // duplicaba esta misma construccion en los dos lugares).
        Evento eventoRegistrado = (evento != null) ? evento : eventoDesconocido();

        RegistroAcceso registro = RegistroAcceso.registrarIntentoFallido(
                usuarioDesconocido,
                eventoRegistrado,
                motivo
        );
        registrarAcceso(registro);
        return registro;
    }

    /*
     * Evento "placeholder" usado unicamente para poder generar un
     * RegistroAcceso cuando no hay un evento real asociado al intento
     * (usuario o evento inexistente). No se agrega a la lista de eventos.
     */
    private RecitalEnVivo eventoDesconocido() {
        DatosRecital datos = new DatosRecital(
                "Evento desconocido",
                "Evento no encontrado",
                LocalDateTime.now(),
                LocalDateTime.now(),
                EstadoEvento.CANCELADO,
                0,
                PlanSuscripcion.FREE,
                "",
                true,
                false
        );

        return new RecitalEnVivo(-1, datos);
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
        Evento evento = buscarEventoPorId(idEvento);
        Usuario usuario = buscarUsuarioPorId(idUsuario);

        // Solo se puede expulsar desde un RecitalEnVivo en curso.
        if (evento instanceof RecitalEnVivo
                && evento.getEstado() == EstadoEvento.EN_CURSO
                && usuario != null) {
            RecitalEnVivo recital = (RecitalEnVivo) evento;
            if (recital.getUsuariosConectados().contains(usuario)) {
                recital.expulsarUsuario(usuario);

                registrarAcceso(RegistroAcceso.generarRegistro(
                        usuario, evento, false, "Expulsado del evento."
                ));

                return usuario;
            }
        }

        return null;
    }
}
