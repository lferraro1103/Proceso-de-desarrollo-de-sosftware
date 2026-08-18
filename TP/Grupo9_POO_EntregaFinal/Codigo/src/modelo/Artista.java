package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Representa a un artista o banda dentro de UADE Beats.
 *
 * En este modulo se usa para asociar artistas a eventos en vivo,
 * corrigiendo la observacion del profesor sobre la falta de esta relacion.
 */
public class Artista {

    // Identificador unico del artista.
    private int id;

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

    // Constructor con los datos principales definidos en la consigna.
    public Artista(int id,
                   String nombreArtistico,
                   String generoPrincipal,
                   String biografia,
                   boolean verificado) {

        this.id = id;
        this.nombreArtistico = nombreArtistico;
        this.generoPrincipal = generoPrincipal;
        this.biografia = biografia;
        this.verificado = verificado;
        this.eventosCreados = new ArrayList<>();
        this.obras = new ArrayList<>();
        this.redesSociales = new HashMap<>();
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
    public int getId() {
        return id;
    }

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
        return "ID: " + id
                + " | Artista: " + nombreArtistico
                + " | Genero: " + generoPrincipal
                + " | Verificado: " + verificado;
    }
}
