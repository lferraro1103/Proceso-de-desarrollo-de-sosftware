package calendario;

import gestor.GestorEventosEnVivo;
import gestor.VinculoCalendario;
import modelo.Cuenta;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
 * Suscribe Google Calendar al calendario de la cuenta.
 *
 * Abre en el navegador el enlace de Google para agregar un calendario por
 * direccion; la persona solo confirma. Google lee el calendario desde sus
 * servidores, por eso la direccion tiene que ser publica.
 */
public class VinculoGoogle implements VinculoCalendario {

    private static final String BASE = "https://calendar.google.com/calendar/r?cid=";

    private final GestorEventosEnVivo gestor;

    public VinculoGoogle(GestorEventosEnVivo gestor) {
        this.gestor = gestor;
    }

    @Override
    public String getNombre() {
        return "Google Calendar";
    }

    @Override
    public void vincular(Cuenta cuenta) throws IOException {
        ServidorCalendario servidor = ServidorCalendario.obtener(gestor);

        if (servidor.esLocal()) {
            throw new IOException("Google Calendar necesita una direccion "
                    + "publica y esta es local. Exponga el puerto con un "
                    + "tunel y ejecute con -Duadebeats.url=https://su-direccion.");
        }

        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Este equipo no puede abrir el navegador. "
                    + "Agregue por URL: " + servidor.urlFeed(cuenta));
        }

        Desktop.getDesktop().browse(URI.create(BASE
                + URLEncoder.encode(servidor.urlWebcal(cuenta), StandardCharsets.UTF_8)));
    }
}
