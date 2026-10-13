package api;

/*
 * Pedido para crear un evento a nombre de un artista.
 */
public record NuevoEventoDto(int idArtista, String titulo, String descripcion, String inicio,
                           String fin, String estado, int capacidadMaxima,
                           String planMinimo, String ubicacion, boolean streaming,
                           boolean exclusivo) {
}
