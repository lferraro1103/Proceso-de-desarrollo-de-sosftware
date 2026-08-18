package modelo;

import excepciones.AccesoDenegadoException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Clase abstracta base para cualquier evento de UADE Beats.
 *
 * Contiene los datos comunes de un evento y define operaciones
 * generales del ciclo de vida. RecitalEnVivo hereda de esta clase.
 */
public abstract class Evento {

    // Identificador unico del evento.
    private int id;

    // Titulo visible del evento.
    private String titulo;

    // Descripcion del evento.
    private String descripcion;

    // Fecha y hora desde la cual se considera valido el evento.
    private LocalDateTime fechaHoraInicio;

    // Fecha y hora hasta la cual se considera valido el evento.
    private LocalDateTime fechaHoraFin;

    // Estado actual del evento.
    private EstadoEvento estado;

    // Cantidad maxima de usuarios permitidos.
    private int capacidadMaxima;

    // Artistas asociados al evento.
    private List<Artista> artistas;

    // Constructor con todos los datos base del evento.
    public Evento(int id,
                  String titulo,
                  String descripcion,
                  LocalDateTime fechaHoraInicio,
                  LocalDateTime fechaHoraFin,
                  EstadoEvento estado,
                  int capacidadMaxima) {

        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;

        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;

        this.estado = estado;
        this.capacidadMaxima = capacidadMaxima;
        this.artistas = new ArrayList<>();
    }

    // Inicia el evento solamente si todavia no esta en curso.
    public void iniciarEvento() throws AccesoDenegadoException {
        if (estado == EstadoEvento.PROGRAMADO
                || estado == EstadoEvento.EN_ESPERA) {
            estado = EstadoEvento.EN_CURSO;
            return;
        }

        throw new AccesoDenegadoException(
                "No se puede iniciar un evento en estado " + estado + "."
        );
    }

    // Finaliza el evento solamente si ya fue iniciado.
    public void finalizarEvento() throws AccesoDenegadoException {
        if (estado == EstadoEvento.EN_CURSO
                || estado == EstadoEvento.PAUSADO) {
            estado = EstadoEvento.FINALIZADO;
            return;
        }

        throw new AccesoDenegadoException(
                "No se puede finalizar un evento en estado " + estado + "."
        );
    }

    // Cambia el estado del evento a cancelado.
    public void cancelarEvento() {
        estado = EstadoEvento.CANCELADO;
    }

    // Pausa el evento solamente si esta en curso.
    public void pausarEvento() throws AccesoDenegadoException {
        if (estado == EstadoEvento.EN_CURSO) {
            estado = EstadoEvento.PAUSADO;
            return;
        }

        throw new AccesoDenegadoException(
                "Solo se puede pausar un evento EN_CURSO. Estado actual: "
                        + estado + "."
        );
    }

    // Reanuda el evento solamente si esta pausado.
    public void reanudarEvento() throws AccesoDenegadoException {
        if (estado == EstadoEvento.PAUSADO) {
            estado = EstadoEvento.EN_CURSO;
            return;
        }

        throw new AccesoDenegadoException(
                "Solo se puede reanudar un evento PAUSADO. Estado actual: "
                        + estado + "."
        );
    }

    // Devuelve el estado como texto.
    public String mostrarEstado() {
        return estado.toString();
    }

    // Verifica si un momento esta dentro del rango de inicio y fin.
    public boolean estaDentroDelHorario(LocalDateTime momento) {
        return !momento.isBefore(fechaHoraInicio)
                && !momento.isAfter(fechaHoraFin);
    }

    // Asocia un artista al evento y tambien registra el evento en el artista.
    public void agregarArtista(Artista artista) {
        if (!artistas.contains(artista)) {
            artistas.add(artista);
        }

        artista.crearEvento(this);
    }

    // Getters y setter necesarios para operar con el evento.
    public int getId() {
        return id;
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

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public List<Artista> getArtistas() {
        return new ArrayList<>(artistas);
    }

    // Arma una cadena con los nombres de artistas asociados.
    public String getNombresArtistas() {
        if (artistas.isEmpty()) {
            return "Sin artistas asignados";
        }

        List<String> nombres = new ArrayList<>();
        for (Artista artista : artistas) {
            nombres.add(artista.getNombreArtistico());
        }

        return String.join(", ", nombres);
    }

    // Texto legible para listar eventos en consola.
    @Override
    public String toString() {
        return "ID: " + id
                + " | Titulo: " + titulo
                + " | Estado: " + estado
                + " | Capacidad: " + capacidadMaxima
                + " | Artistas: " + getNombresArtistas();
    }
}
