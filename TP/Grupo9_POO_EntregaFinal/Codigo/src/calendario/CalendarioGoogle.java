package calendario;

import gestor.Calendario;
import modelo.Evento;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/*
 * Agrega el evento a Google Calendar.
 *
 * Abre en el navegador el enlace oficial "agregar evento" de Google con el
 * titulo, las fechas y el lugar ya cargados; la persona solo confirma con su
 * cuenta de Google. No guarda credenciales ni usa claves de API.
 */
public class CalendarioGoogle implements Calendario {

    private static final String BASE =
            "https://calendar.google.com/calendar/render?action=TEMPLATE";

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @Override
    public String getNombre() {
        return "Google Calendar";
    }

    @Override
    public void agregar(Evento evento) throws IOException {
        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Este equipo no puede abrir el navegador. "
                    + "Enlace: " + crearEnlace(evento));
        }

        Desktop.getDesktop().browse(URI.create(crearEnlace(evento)));
    }

    // Arma el enlace con los datos del evento. Las fechas van en hora local.
    public static String crearEnlace(Evento evento) {
        return BASE
                + "&text=" + codificar(evento.getTitulo())
                + "&dates=" + formatear(evento.getFechaHoraInicio())
                + "/" + formatear(evento.getFechaHoraFin())
                + "&ctz=" + codificar(ZoneId.systemDefault().getId())
                + "&details=" + codificar(evento.getDescripcion())
                + "&location=" + codificar(evento.getUbicacion());
    }

    private static String formatear(LocalDateTime fecha) {
        return fecha.format(FECHA);
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto == null ? "" : texto, StandardCharsets.UTF_8);
    }
}
