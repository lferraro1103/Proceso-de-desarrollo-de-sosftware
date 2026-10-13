package api;

/*
 * Pedido para registrar un artista.
 */
public record NuevoArtistaDto(String nombreUsuario, String contrasena, String nombreArtistico,
                             String generoPrincipal, String biografia,
                             boolean verificado) {
}
