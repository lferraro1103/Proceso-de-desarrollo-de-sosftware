package modelo;

import excepciones.AccesoDenegadoException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/*
 * Clase concreta que representa un recital, show o presentacion en vivo.
 *
 * Hereda de Evento y agrega reglas especificas del modulo:
 * - plan minimo requerido;
 * - capacidad disponible;
 * - usuarios conectados;
 * - validacion de estado y horario.
 */
public class RecitalEnVivo extends Evento {

    // Plan minimo que necesita un usuario para entrar.
    private PlanSuscripcion planMinimoRequerido;

    // Ubicacion fisica o descripcion del canal de streaming.
    private String ubicacion;

    // Indica si el recital se transmite por streaming.
    private boolean esStreaming;

    // Indica si es un evento exclusivo.
    private boolean exclusivo;

    // Set de usuarios conectados. Se usa Set para evitar duplicados.
    private Set<Usuario> usuariosConectados;

    // Constructor completo del recital.
    public RecitalEnVivo(int id,
                         String titulo,
                         String descripcion,
                         LocalDateTime fechaHoraInicio,
                         LocalDateTime fechaHoraFin,
                         EstadoEvento estado,
                         int capacidadMaxima,
                         PlanSuscripcion planMinimoRequerido,
                         String ubicacion,
                         boolean esStreaming,
                         boolean exclusivo) {

        super(id,
                titulo,
                descripcion,
                fechaHoraInicio,
                fechaHoraFin,
                estado,
                capacidadMaxima);

        this.planMinimoRequerido = planMinimoRequerido;
        this.ubicacion = ubicacion;

        this.esStreaming = esStreaming;
        this.exclusivo = exclusivo;

        this.usuariosConectados = new HashSet<>();
    }

    // Devuelve true si todavia hay lugar disponible.
    public boolean verificarCapacidad() {
        return usuariosConectados.size() < getCapacidadMaxima();
    }

    /*
     * Intenta conectar un usuario al recital.
     *
     * Este metodo concentra reglas propias del evento:
     * - evento disponible;
     * - capacidad;
     * - usuario no conectado previamente.
     */
    public void permitirIngreso(Usuario usuario)
            throws AccesoDenegadoException {

        // Primero se valida estado y horario.
        validarDisponibilidad(LocalDateTime.now());

        // Si no hay capacidad, se rechaza el ingreso.
        if (!verificarCapacidad()) {

            throw new AccesoDenegadoException(
                    "Capacidad agotada."
            );
        }

        // El Set usa equals/hashCode de Usuario para detectar duplicados.
        if (usuariosConectados.contains(usuario)) {

            throw new AccesoDenegadoException(
                    "El usuario ya se encuentra conectado."
            );
        }

        // Si todas las validaciones pasan, el usuario queda conectado.
        usuariosConectados.add(usuario);
    }

    /*
     * Valida si el recital esta disponible para recibir usuarios.
     *
     * Diferencia los motivos de rechazo para que el error sea claro.
     */
    public void validarDisponibilidad(LocalDateTime momento)
            throws AccesoDenegadoException {

        // Un evento cancelado no admite ingreso.
        if (getEstado() == EstadoEvento.CANCELADO) {
            throw new AccesoDenegadoException(
                    "Evento no disponible: se encuentra cancelado."
            );
        }

        // Un evento finalizado tampoco admite nuevos ingresos.
        if (getEstado() == EstadoEvento.FINALIZADO) {
            throw new AccesoDenegadoException(
                    "Evento no disponible: ya finalizo."
            );
        }

        // Si esta pausado, queda temporalmente no disponible.
        if (getEstado() == EstadoEvento.PAUSADO) {
            throw new AccesoDenegadoException(
                    "Evento no disponible: se encuentra pausado."
            );
        }

        // Regla central: solo EN_CURSO admite ingreso.
        if (!getEstado().admiteIngreso()) {
            throw new AccesoDenegadoException(
                    "Evento no disponible: debe estar EN_CURSO."
            );
        }

        // Tambien se controla que el intento ocurra dentro del horario.
        if (!estaDentroDelHorario(momento)) {
            throw new AccesoDenegadoException(
                    "Acceso fuera de horario."
            );
        }
    }

    // Expulsa o desconecta un usuario del recital.
    public void expulsarUsuario(Usuario usuario) {
        usuariosConectados.remove(usuario);
    }

    // Devuelve una vista no modificable para proteger el Set interno.
    public Set<Usuario> getUsuariosConectados() {
        return Collections.unmodifiableSet(usuariosConectados);
    }

    // Devuelve la cantidad actual de conectados.
    public int getCantidadConectados() {
        return usuariosConectados.size();
    }

    // Getters de datos propios del recital.
    public PlanSuscripcion getPlanMinimoRequerido() {
        return planMinimoRequerido;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public boolean isStreaming() {
        return esStreaming;
    }

    public boolean isExclusivo() {
        return exclusivo;
    }

    // Texto legible para listar eventos en consola.
    @Override
    public String toString() {
        return super.toString()
                + " | Plan minimo: " + planMinimoRequerido
                + " | Conectados: " + getCantidadConectados()
                + "/" + getCapacidadMaxima();
    }
}
