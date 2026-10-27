package calendario;

import puertos.VinculoCalendario;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
 * Suscribe Google Calendar a la direccion de un evento.
 *
 * Abre en el navegador el enlace de Google para agregar un calendario por
 * direccion; la persona solo confirma. Google lee el calendario desde sus
 * servidores, por eso la direccion tiene que ser publica.
 */
public class VinculoGoogle implements VinculoCalendario {

    private static final String BASE = "https://calendar.google.com/calendar/r?cid=";

    @Override
    public String getNombre() {
        return "Google Calendar";
    }

    @Override
    public void vincular(String urlFeed) throws IOException {
        // Google lee desde internet: una direccion de la red local no le llega.
        if (!urlFeed.startsWith("https://")) {
            throw new IOException("Google Calendar necesita una direccion "
                    + "publica con https y esta es de la red local. Instale "
                    + "cloudflared para que el programa abra un tunel solo, o "
                    + "ejecute con -Duadebeats.url=https://su-direccion.");
        }

        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Este equipo no puede abrir el navegador. "
                    + "Agregue por URL: " + urlFeed);
        }

        String webcal = urlFeed.replaceFirst("^https?://", "webcal://");

        Desktop.getDesktop().browse(URI.create(BASE
                + URLEncoder.encode(webcal, StandardCharsets.UTF_8)));
    }
}
