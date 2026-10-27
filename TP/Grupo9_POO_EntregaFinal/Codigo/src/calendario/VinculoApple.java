package calendario;

import puertos.VinculoCalendario;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/*
 * Suscribe Apple Calendar a la direccion de un evento.
 *
 * Abre un enlace webcal://; el sistema lo entrega a Calendario, que muestra
 * la ventana de suscripcion. Despues se actualiza solo.
 *
 * Calendario lee la direccion desde este equipo, por lo que alcanza con la
 * de la red local (webcal:// sobre http); con un tunel se usa la https.
 */
public class VinculoApple implements VinculoCalendario {

    @Override
    public String getNombre() {
        return "Apple Calendar";
    }

    @Override
    public void vincular(String urlFeed) throws IOException {
        String enlace = urlFeed.replaceFirst("^https?://", "webcal://");

        // En Mac se llama a la app Calendario de forma explicita: el
        // navegador predeterminado puede quedarse con los enlaces webcal.
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            new ProcessBuilder("open", "-a", "Calendar", enlace).start();
            return;
        }

        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Este equipo no puede abrir el calendario. "
                    + "Suscribase manualmente a: " + enlace);
        }

        Desktop.getDesktop().browse(URI.create(enlace));
    }
}
