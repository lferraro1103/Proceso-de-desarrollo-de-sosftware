package ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/*
 * Abre una direccion web en el navegador del equipo.
 */
public final class Navegador {

    private Navegador() {
    }

    public static void abrir(String url) throws IOException {
        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Este equipo no puede abrir el navegador. "
                    + "Abra manualmente: " + url);
        }

        Desktop.getDesktop().browse(URI.create(url));
    }
}
