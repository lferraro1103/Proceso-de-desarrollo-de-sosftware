package calendario;

import gestor.Calendario;
import modelo.Evento;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/*
 * Agrega el evento a Apple Calendar.
 *
 * Genera un archivo .ics (iCalendar, formato estandar) y lo abre con la
 * aplicacion de calendario del sistema, que ofrece agregarlo. Tambien sirve
 * para cualquier otro calendario que importe .ics.
 */
public class CalendarioApple implements Calendario {

    @Override
    public String getNombre() {
        return "Apple Calendar";
    }

    @Override
    public void agregar(Evento evento) throws IOException {
        File archivo = File.createTempFile("uadebeats-evento-" + evento.getId() + "-", ".ics");
        archivo.deleteOnExit();
        Files.write(archivo.toPath(), crearIcs(evento).getBytes(StandardCharsets.UTF_8));

        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            throw new IOException("Este equipo no puede abrir el calendario. "
                    + "Archivo generado: " + archivo.getAbsolutePath());
        }

        Desktop.getDesktop().open(archivo);
    }

    // Contenido iCalendar del evento.
    public static String crearIcs(Evento evento) {
        return FormatoIcs.calendario(evento.getTitulo(), List.of(evento));
    }
}
