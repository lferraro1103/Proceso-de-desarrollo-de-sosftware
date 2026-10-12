package gestor;

import excepciones.AccesoDenegadoException;
import modelo.AccesoEvento;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Resuelve las solicitudes de ingreso a un evento y lleva la trazabilidad
 * de cada intento, exitoso o rechazado.
 *
 * Se separo del gestor para que este se ocupe de administrar usuarios,
 * artistas y eventos, y esta clase de una unica responsabilidad: decidir
 * si un usuario puede ingresar y dejar constancia del resultado.
 */
public class ControlAcceso {

    // Registros de intentos de acceso exitosos o fallidos.
    private List<RegistroAcceso> registros;

    // ID incremental para los objetos AccesoEvento.
    private int siguienteIdAcceso;

    // Medio por el que se avisa a los usuarios.
    private Notificador notificador;

    public ControlAcceso(Notificador notificador) {
        this.notificador = notificador;
        registros = new ArrayList<>();
        siguienteIdAcceso = 1;
    }

    /*
     * Procesa un intento de ingreso.
     *
     * 1. Si falta el usuario o el evento, registra el rechazo.
     * 2. Crea un AccesoEvento con el plan que pide el evento.
     * 3. Valida autorizacion y plan.
     * 4. Pide al evento que permita el ingreso.
     * 5. Guarda un RegistroAcceso con el resultado.
     */
    public RegistroAcceso procesarIngreso(Usuario usuario, Evento evento) {

        if (usuario == null) {
            return rechazar(usuarioDesconocido(),
                    evento != null ? evento : eventoDesconocido(),
                    "Usuario inexistente.");
        }

        if (evento == null) {
            return rechazar(usuario, eventoDesconocido(),
                    "Evento inexistente.");
        }

        AccesoEvento acceso = new AccesoEvento(
                siguienteIdAcceso++,
                usuario,
                evento,
                LocalDateTime.now(),
                usuario.isActivo(),
                "GENERAL",
                evento.getPlanMinimoRequerido(),
                usuario.tieneAccesoPrioritario()
        );

        try {
            acceso.validarAutorizacion();
            acceso.validarPlan();
            evento.permitirIngreso(usuario);

            RegistroAcceso registro = RegistroAcceso.generarRegistro(
                    usuario, evento, true, "");
            registrar(registro);
            return registro;

        } catch (AccesoDenegadoException e) {
            return rechazar(usuario, evento, e.getMessage());
        }
    }

    // Deja constancia de la expulsion de un usuario.
    public void registrarExpulsion(Usuario usuario, Evento evento) {
        registrar(RegistroAcceso.generarRegistro(
                usuario, evento, false, "Expulsado del evento."));

        avisar(usuario, "Expulsado del evento", evento.getTitulo());
    }

    // Agrega un registro a la trazabilidad.
    public void registrar(RegistroAcceso registro) {
        registros.add(registro);
    }

    // Devuelve una copia de los registros.
    public List<RegistroAcceso> listar() {
        return new ArrayList<>(registros);
    }

    // Reemplaza los registros al cargar datos guardados.
    public void reemplazar(List<RegistroAcceso> registrosCargados) {
        registros = new ArrayList<>(registrosCargados);
    }

    private RegistroAcceso rechazar(Usuario usuario,
                                    Evento evento,
                                    String motivo) {

        RegistroAcceso registro = RegistroAcceso.registrarIntentoFallido(
                usuario, evento, motivo);
        registrar(registro);

        avisar(usuario, "Ingreso rechazado", evento.getTitulo() + ": " + motivo);
        return registro;
    }

    /*
     * Avisa solo a las cuentas con rol Usuario. Los administradores y los
     * artistas no reciben estos avisos, y tampoco el usuario "placeholder"
     * que se usa cuando el intento no tiene un usuario real.
     */
    private void avisar(Usuario usuario, String titulo, String mensaje) {
        if (usuario.getTipoUsuario() == TipoUsuario.USUARIO
                && usuario.getId() > 0) {
            notificador.notificar(usuario, titulo, mensaje);
        }
    }

    /*
     * Usuario "placeholder" para poder registrar un intento cuando no hay
     * un usuario real asociado.
     */
    private Usuario usuarioDesconocido() {
        return new Usuario(
                -1,
                "desconocido",
                "Usuario",
                "Desconocido",
                "sin-email",
                "",
                PlanSuscripcion.FREE,
                false
        );
    }

    /*
     * Evento "placeholder" para poder registrar un intento cuando no hay
     * un evento real asociado. No se agrega a la lista de eventos.
     */
    private Evento eventoDesconocido() {
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
}
