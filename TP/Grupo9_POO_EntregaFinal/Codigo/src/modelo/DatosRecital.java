package modelo;

import java.time.LocalDateTime;

/*
 * Agrupa los datos propios de un RecitalEnVivo (todo menos el id).
 *
 * Se creo para evitar que el constructor de RecitalEnVivo tuviera 11
 * parametros posicionales: cualquier campo nuevo o reordenado rompia
 * a la vez a App, VentanaPrincipal, PersistenciaArchivos y
 * GestorEventosEnVivo. Agrupando estos datos en un solo objeto, el
 * constructor de RecitalEnVivo queda en (id, DatosRecital).
 */
public class DatosRecital {

    private String titulo;
    private String descripcion;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private EstadoEvento estado;
    private int capacidadMaxima;
    private PlanSuscripcion planMinimoRequerido;
    private String ubicacion;
    private boolean esStreaming;
    private boolean exclusivo;

    public DatosRecital(String titulo,
                        String descripcion,
                        LocalDateTime fechaHoraInicio,
                        LocalDateTime fechaHoraFin,
                        EstadoEvento estado,
                        int capacidadMaxima,
                        PlanSuscripcion planMinimoRequerido,
                        String ubicacion,
                        boolean esStreaming,
                        boolean exclusivo) {

        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.estado = estado;
        this.capacidadMaxima = capacidadMaxima;
        this.planMinimoRequerido = planMinimoRequerido;
        this.ubicacion = ubicacion;
        this.esStreaming = esStreaming;
        this.exclusivo = exclusivo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public PlanSuscripcion getPlanMinimoRequerido() {
        return planMinimoRequerido;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public boolean isEsStreaming() {
        return esStreaming;
    }

    public boolean isExclusivo() {
        return exclusivo;
    }
}
