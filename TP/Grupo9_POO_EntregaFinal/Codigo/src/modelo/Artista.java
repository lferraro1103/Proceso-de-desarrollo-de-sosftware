package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Representa a un artista o banda dentro de UADE Beats.
 *
 * Hereda de Cuenta para tener credenciales propias (nombreUsuario y
 * contrasena) y poder loguearse, en vez de ser un dato publico sin login
 * como antes (CU04-01).
 */
public class Artista extends Cuenta {

    // Nombre publico del artista o banda.
    private String nombreArtistico;

    // Genero musical principal.
    private String generoPrincipal;

    // Texto descriptivo del artista.
    private String biografia;

    // Indica si la plataforma valido al artista.
    private boolean verificado;

    // Eventos en los que participa o que creo.
    private List<Evento> eventosCreados;

    // Obras o canciones principales.
    private List<String> obras;

    // Redes sociales: clave = nombre de red, valor = URL.
    private Map<String, String> redesSociales;

    /*
     * Constructor completo con credenciales propias (CU04-01). Un artista
     * siempre esta activo y su tipoUsuario siempre es ARTISTA: son
     * invariantes de esta clase, no se reciben como parametro.
     */
    public Artista(int id,
                   String nombreUsuario,
                   String contrasena,
                   String nombreArtistico,
                   String generoPrincipal,
                   String biografia,
                   boolean verificado) {

        super(id, nombreUsuario, contrasena, true, TipoUsuario.ARTISTA);
        this.nombreArtistico = nombreArtistico;
        this.generoPrincipal = generoPrincipal;
        this.biografia = biografia;
        this.verificado = verificado;
        this.eventosCreados = new ArrayList<>();
        this.obras = new ArrayList<>();
        this.redesSociales = new HashMap<>();
    }

    /*
     * Constructor de compatibilidad con la firma actual de 5 parametros,
     * usada hoy por MenuAcciones.registrarArtista y
     * VentanaPrincipal.registrarArtista (que todavia no piden credenciales
     * en esta fase; eso es CU04-02, fuera de alcance de la Fase 1). Genera
     * un nombre de cuenta a partir del ID y deja la contrasena vacia.
     */
    public Artista(int id,
                   String nombreArtistico,
                   String generoPrincipal,
                   String biografia,
                   boolean verificado) {

        this(id, "artista" + id, "", nombreArtistico, generoPrincipal,
                biografia, verificado);
    }

    // Asocia un evento al artista.
    public void crearEvento(Evento evento) {
        if (!eventosCreados.contains(evento)) {
            eventosCreados.add(evento);
        }
    }

    // Actualiza la biografia y devuelve el nuevo texto.
    public String actualizarInformacionArtistica(String nuevaBiografia) {
        biografia = nuevaBiografia;
        return biografia;
    }

    // Version simple que solo devuelve la biografia actual.
    public String actualizarInformacionArtistica() {
        return biografia;
    }

    // Registra una obra o cancion del artista.
    public void agregarObra(String titulo) {
        obras.add(titulo);
    }

    // Registra una red social del artista.
    public void agregarRedSocial(String red, String url) {
        redesSociales.put(red, url);
    }

    // Getters de consulta.
    public String getNombreArtistico() {
        return nombreArtistico;
    }

    public String getGeneroPrincipal() {
        return generoPrincipal;
    }

    public String getBiografia() {
        return biografia;
    }

    public boolean isVerificado() {
        return verificado;
    }

    // Devuelve una copia para no exponer la lista interna modificable.
    public List<Evento> getEventosCreados() {
        return new ArrayList<>(eventosCreados);
    }

    // Texto legible para listar artistas en consola.
    @Override
    public String toString() {
        return "ID: " + getId()
                + " | Artista: " + nombreArtistico
                + " | Genero: " + generoPrincipal
                + " | Verificado: " + verificado;
    }
}
