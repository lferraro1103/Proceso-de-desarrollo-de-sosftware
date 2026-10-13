package api;

/*
 * Artista. Nunca lleva la contrasena.
 */
public record ArtistaDto(int id, String nombreUsuario, String nombreArtistico,
                         String generoPrincipal, String biografia, boolean verificado) {
}
