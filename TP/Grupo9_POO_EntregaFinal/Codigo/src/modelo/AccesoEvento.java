package modelo;

import excepciones.AccesoDenegadoException;

import java.time.LocalDateTime;

/*
 * Representa la autorizacion o ticket de un usuario para un evento.
 *
 * Esta clase no conecta al usuario al recital directamente.
 * Su responsabilidad es validar si el usuario esta habilitado y si su
 * plan alcanza el plan requerido por el evento.
 */
public class AccesoEvento {

    // Identificador del acceso.
    private int id;

    // Usuario que quiere ingresar.
    private Usuario usuario;

    // Evento al que quiere ingresar.
    private Evento evento;

    // Fecha y hora en la que se genero el acceso.
    private LocalDateTime fechaRegistro;

    // Indica si el acceso esta habilitado administrativamente.
    private boolean habilitado;

    // Texto descriptivo del tipo de acceso.
    private String tipoAcceso;

    // Plan minimo requerido para este acceso.
    private PlanSuscripcion planRequerido;

    // Indica si el acceso tiene prioridad, por ejemplo ARTIST_PASS.
    private boolean accesoPrioritario;

    // Constructor completo.
    public AccesoEvento(int id,
                        Usuario usuario,
                        Evento evento,
                        LocalDateTime fechaRegistro,
                        boolean habilitado,
                        String tipoAcceso,
                        PlanSuscripcion planRequerido,
                        boolean accesoPrioritario) {

        this.id = id;
        this.usuario = usuario;
        this.evento = evento;

        this.fechaRegistro = fechaRegistro;

        this.habilitado = habilitado;
        this.tipoAcceso = tipoAcceso;

        this.planRequerido = planRequerido;
        this.accesoPrioritario = accesoPrioritario;
    }

    // Devuelve si el acceso esta habilitado.
    public boolean validarIngreso() {
        return habilitado;
    }

    // Compara el plan del usuario contra el plan requerido.
    public boolean validarPlanUsuario() {
        return usuario.getPlanSuscripcion().cubre(planRequerido);
    }

    // Lanza excepcion si el usuario no esta habilitado.
    public void validarAutorizacion() throws AccesoDenegadoException {
        if (!habilitado || !usuario.isActivo()) {
            throw new AccesoDenegadoException(
                    "Usuario sin autorizacion para ingresar."
            );
        }
    }

    // Lanza excepcion si el plan del usuario es insuficiente.
    public void validarPlan() throws AccesoDenegadoException {
        if (!validarPlanUsuario()) {
            throw new AccesoDenegadoException(
                    "Plan insuficiente. Requiere "
                            + planRequerido
                            + " y el usuario posee "
                            + usuario.getPlanSuscripcion()
                            + "."
            );
        }
    }

    // Genera o habilita manualmente el acceso.
    public void generarAcceso() {
        habilitado = true;
    }

    // Cancela o deshabilita manualmente el acceso.
    public void cancelarAcceso() {
        habilitado = false;
    }

    // Getters de consulta.
    public int getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getTipoAcceso() {
        return tipoAcceso;
    }

    public PlanSuscripcion getPlanRequerido() {
        return planRequerido;
    }

    public boolean isAccesoPrioritario() {
        return accesoPrioritario;
    }
}
