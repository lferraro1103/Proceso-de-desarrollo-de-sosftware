package calendario;

import gestor.Calendario;
import modelo.Evento;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/*
 * Agrega el evento a Apple Calendar.
 *
 * Genera un archivo .ics (iCalendar, formato estandar) y lo abre con la
 * aplicacion de calendario del sistema, que ofrece agregarlo. Tambien sirve
 * para cualquier otro calendario que importe .ics.
 */
public class CalendarioApple implements Calendario {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

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

    // Contenido iCalendar del evento. Las fechas van en hora local flotante.
    public static String crearIcs(Evento evento) {
        StringBuilder ics = new StringBuilder();

        linea(ics, "BEGIN:VCALENDAR");
        linea(ics, "VERSION:2.0");
        linea(ics, "PRODID:-//UADE Beats//Eventos//ES");
        linea(ics, "BEGIN:VEVENT");
        linea(ics, "UID:evento-" + evento.getId() + "@uadebeats");
        linea(ics, "DTSTAMP:" + LocalDateTime.now(ZoneOffset.UTC).format(FECHA) + "Z");
        linea(ics, "DTSTART:" + evento.getFechaHoraInicio().format(FECHA));
        linea(ics, "DTEND:" + evento.getFechaHoraFin().format(FECHA));
        linea(ics, "SUMMARY:" + escapar(evento.getTitulo()));
        linea(ics, "DESCRIPTION:" + escapar(evento.getDescripcion()));
        linea(ics, "LOCATION:" + escapar(evento.getUbicacion()));
        linea(ics, "END:VEVENT");
        linea(ics, "END:VCALENDAR");

        return ics.toString();
    }

    // Escapa los caracteres especiales del formato iCalendar.
    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    // Agrega una linea; el formato pide cortar las largas con CRLF + espacio.
    private static void linea(StringBuilder ics, String texto) {
        int largo = 73;

        while (texto.length() > largo) {
            ics.append(texto, 0, largo).append("\r\n ");
            texto = texto.substring(largo);
        }

        ics.append(texto).append("\r\n");
    }
}
