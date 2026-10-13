package servidor;

import calendario.FormatoIcs;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gestor.GestorEventosEnVivo;
import modelo.Cuenta;
import modelo.Evento;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/*
 * Atiende las suscripciones de calendario: responde con el .ics de un evento
 * en /calendario/<codigo>/<id del evento>.ics.
 *
 * Responde 404 si el codigo no es de ninguna cuenta, si la cuenta esta
 * desactivada, si el evento no existe o si la cuenta no puede verlo.
 */
public class ManejadorCalendario implements HttpHandler {

    private final GestorEventosEnVivo gestor;
    private final CodigosCalendario codigos;

    public ManejadorCalendario(GestorEventosEnVivo gestor,
                               CodigosCalendario codigos) {
        this.gestor = gestor;
        this.codigos = codigos;
    }

    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        try (intercambio) {
            Evento evento = null;
            String ruta = intercambio.getRequestURI().getPath();

            // Ruta: /calendario/<codigo>/<idEvento>.ics
            if (ruta.endsWith(PublicadorHttp.SUFIJO)) {
                String[] partes = ruta.substring(PublicadorHttp.PREFIJO.length(),
                        ruta.length() - PublicadorHttp.SUFIJO.length()).split("/");

                if (partes.length == 2) {
                    evento = buscarEvento(partes[0], partes[1]);
                }
            }

            if (evento == null) {
                intercambio.sendResponseHeaders(404, -1);
                return;
            }

            byte[] cuerpo = FormatoIcs.calendario(
                    evento.getTitulo(), List.of(evento))
                    .getBytes(StandardCharsets.UTF_8);

            intercambio.getResponseHeaders().set(
                    "Content-Type", "text/calendar; charset=utf-8");
            intercambio.sendResponseHeaders(200, cuerpo.length);

            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(cuerpo);
            }
        }
    }

    /*
     * Evento pedido si el codigo corresponde a una cuenta activa que puede
     * verlo, o null. Se trabaja dentro del bloqueo del gestor porque las
     * peticiones llegan en hilos distintos.
     */
    private Evento buscarEvento(String codigo, String texto) {
        int id;

        try {
            id = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }

        synchronized (gestor) {
            List<Cuenta> cuentas = new ArrayList<>(gestor.listarUsuarios());
            cuentas.addAll(gestor.listarArtistas());

            for (Cuenta cuenta : cuentas) {
                if (MessageDigest.isEqual(
                        codigos.codigoDe(cuenta).getBytes(StandardCharsets.UTF_8),
                        codigo.getBytes(StandardCharsets.UTF_8))) {
                    return cuenta.isActivo()
                            ? gestor.buscarEventoVisiblePara(cuenta, id) : null;
                }
            }
        }

        return null;
    }
}
