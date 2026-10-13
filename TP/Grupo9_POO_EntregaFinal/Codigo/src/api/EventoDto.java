package api;

import java.util.List;

/*
 * Evento con los IDs de sus artistas y de los usuarios conectados. Las fechas viajan como texto ISO.
 */
public record EventoDto(int id, String titulo, String descripcion, String inicio, String fin,
                        String estado, int capacidadMaxima, String planMinimo,
                        String ubicacion, boolean streaming, boolean exclusivo,
                        List<Integer> artistas, List<Integer> conectados) {
}
