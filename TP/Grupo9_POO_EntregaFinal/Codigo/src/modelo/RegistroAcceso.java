package modelo;

import java.time.LocalDateTime;

/*
 * Guarda la trazabilidad de cada intento de ingreso.
 *
 * Puede representar:
 * - un ingreso autorizado;
 * - un ingreso rechazado;
 * - el motivo del rechazo.
 */
public class RegistroAcceso {

    // Usuario que intento ingresar.
    private Usuario usuario;

    // Evento al que intento ingresar.
    private Evento evento;

    // Momento del intento o ingreso.
    private LocalDateTime fechaHoraIngreso;

    // Momento de salida, si luego se registra.
    private LocalDateTime fechaHoraSalida;

    // true si ingreso, false si fue rechazado.
    private boolean exitoso;

    // Motivo cuando el ingreso fue rechazado.
    private String motivoRechazo;

    // Constructor principal del registro.
    public RegistroAcceso(Usuario usuario,
                          Evento evento,
                          boolean exitoso,
                          String motivoRechazo) {

        this.usuario = usuario;
        this.evento = evento;

        this.exitoso = exitoso;
        this.motivoRechazo = motivoRechazo;

        this.fechaHoraIngreso =
                LocalDateTime.now();
    }

    // Registra el horario de salida del usuario.
    public void registrarSalida() {
        fechaHoraSalida = LocalDateTime.now();
    }

    // Metodo de fabrica para crear registros exitosos o fallidos.
    public static RegistroAcceso generarRegistro(Usuario usuario,
                                                 Evento evento,
                                                 boolean exitoso,
                                                 String motivoRechazo) {

        return new RegistroAcceso(usuario, evento, exitoso, motivoRechazo);
    }

    // Metodo de fabrica especifico para intentos fallidos.
    public static RegistroAcceso registrarIntentoFallido(Usuario usuario,
                                                         Evento evento,
                                                         String motivoRechazo) {

        return new RegistroAcceso(usuario, evento, false, motivoRechazo);
    }

    // Reconstruye un registro persistido sin modificar su fecha original.
    public static RegistroAcceso reconstruirRegistro(Usuario usuario,
                                                      Evento evento,
                                                      LocalDateTime fechaHoraIngreso,
                                                      boolean exitoso,
                                                      String motivoRechazo) {

        RegistroAcceso registro =
                new RegistroAcceso(usuario, evento, exitoso, motivoRechazo);
        registro.fechaHoraIngreso = fechaHoraIngreso;
        return registro;
    }

    // Getters de consulta.
    public Usuario getUsuario() {
        return usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    // Texto legible para listar registros desde el menu.
    @Override
    public String toString() {
        String resultado = exitoso
                ? "AUTORIZADO"
                : "RECHAZADO - " + motivoRechazo;

        return fechaHoraIngreso
                + " | Usuario: " + usuario.getNombreUsuario()
                + " | Evento: " + evento.getTitulo()
                + " | Resultado: " + resultado;
    }
}

