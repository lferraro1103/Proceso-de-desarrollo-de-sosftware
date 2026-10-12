package notificacion;

import gestor.Notificador;
import modelo.Usuario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/*
 * Envia los avisos como notificaciones push mediante ntfy.sh.
 *
 * Cada usuario recibe los avisos en su propio tema, "uadebeats-" seguido de
 * su nombre de cuenta. Para recibirlos, el usuario se suscribe a ese tema
 * desde la aplicacion o la pagina de ntfy.
 *
 * Los temas de ntfy.sh son publicos: por eso el mensaje solo lleva el
 * titulo del evento y el motivo, sin datos personales.
 */
public class NotificadorNtfy implements Notificador {

    private static final String SERVIDOR = "https://ntfy.sh/";
    private static final String PREFIJO_TEMA = "uadebeats-";

    // Tiempo maximo de espera para no demorar el programa si no hay red.
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(3);

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(ESPERA_MAXIMA)
            .build();

    @Override
    public void notificar(Usuario destinatario, String titulo, String mensaje) {
        try {
            // El tema solo admite letras, numeros, guion y guion bajo.
            String tema = PREFIJO_TEMA
                    + destinatario.getNombreUsuario()
                            .replaceAll("[^A-Za-z0-9_-]", "");

            HttpRequest pedido = HttpRequest.newBuilder(
                            URI.create(SERVIDOR + tema))
                    .timeout(ESPERA_MAXIMA)
                    .header("Title", titulo)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mensaje, StandardCharsets.UTF_8))
                    .build();

            // Envio asincronico: el programa no espera la respuesta y un
            // error de red se descarta sin afectar la operacion.
            cliente.sendAsync(pedido, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(error -> null);

        } catch (RuntimeException e) {
            // Un aviso fallido nunca debe romper la operacion de negocio.
        }
    }
}
