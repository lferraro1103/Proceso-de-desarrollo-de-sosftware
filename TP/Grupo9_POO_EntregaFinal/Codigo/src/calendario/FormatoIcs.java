package calendario;

import modelo.EstadoEvento;
import modelo.Evento;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
 * Convierte eventos al formato iCalendar (.ics), que leen Google Calendar,
 * Apple Calendar y la mayoria de los calendarios.
 *
 * Lo usan el archivo de un solo evento y el calendario suscripto, para que
 * ambos describan el evento de la misma manera.
 */
public final class FormatoIcs {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private FormatoIcs() {
    }

    // Calendario completo con los eventos indicados.
    public static String calendario(String nombre, List<Evento> eventos) {
        StringBuilder ics = new StringBuilder();

        linea(ics, "BEGIN:VCALENDAR");
        linea(ics, "VERSION:2.0");
        linea(ics, "PRODID:-//UADE Beats//Eventos//ES");
        linea(ics, "CALSCALE:GREGORIAN");
        linea(ics, "METHOD:PUBLISH");
        linea(ics, "X-WR-CALNAME:" + escapar(nombre));

        for (Evento evento : eventos) {
            agregarEvento(ics, evento);
        }

        linea(ics, "END:VCALENDAR");
        return ics.toString();
    }

    /*
     * Un evento. El UID es fijo, de modo que el calendario lo reconoce como
     * el mismo evento aunque se vuelva a importar. SEQUENCE crece con el
     * tiempo para que la copia mas nueva reemplace a la anterior, y un
     * evento cancelado se marca como tal en lugar de desaparecer.
     */
    private static void agregarEvento(StringBuilder ics, Evento evento) {
        linea(ics, "BEGIN:VEVENT");
        linea(ics, "UID:evento-" + evento.getId() + "@uadebeats");
        linea(ics, "DTSTAMP:" + LocalDateTime.now(ZoneOffset.UTC).format(FECHA) + "Z");
        linea(ics, "SEQUENCE:" + Instant.now().getEpochSecond() / 60);
        linea(ics, "DTSTART:" + evento.getFechaHoraInicio().format(FECHA));
        linea(ics, "DTEND:" + evento.getFechaHoraFin().format(FECHA));
        linea(ics, "SUMMARY:" + escapar(evento.getTitulo()));
        linea(ics, "DESCRIPTION:" + escapar(evento.getDescripcion()
                + " (Estado: " + evento.getEstado() + ")"));
        linea(ics, "LOCATION:" + escapar(evento.getUbicacion()));
        linea(ics, "STATUS:" + (evento.getEstado() == EstadoEvento.CANCELADO
                ? "CANCELLED" : "CONFIRMED"));
        linea(ics, "END:VEVENT");
    }

    // Escapa los caracteres especiales del formato.
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
