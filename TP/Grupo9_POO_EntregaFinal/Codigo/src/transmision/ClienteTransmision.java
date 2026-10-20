package transmision;

import com.google.gson.Gson;
import gestor.ConsultaTransmision;
import gestor.ProveedorTransmision;
import gestor.Transmision;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/*
 * Pide las transmisiones al servicio aparte (TP/Grupo9_Servicio_Transmision).
 *
 * Propiedades:
 *  - uadebeats.transmision:         direccion con la que este programa llama
 *                                   al servicio (http://localhost:8766).
 *  - uadebeats.transmision.publica: direccion con la que las personas abren la
 *                                   pagina del reproductor. Por defecto es la
 *                                   misma; hay que cambiarla si los clientes
 *                                   estan en otra PC y no ven "localhost".
 */
public class ClienteTransmision implements ProveedorTransmision {

    // Lo que responde el servicio; los campos que sobran se ignoran.
    private record Respuesta(String titulo, String ruta, String error) {
    }

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final String urlBase = System.getProperty(
            "uadebeats.transmision", "http://localhost:8766").replaceAll("/+$", "");

    private final String urlPublica = System.getProperty(
            "uadebeats.transmision.publica", urlBase).replaceAll("/+$", "");

    @Override
    public Transmision buscar(ConsultaTransmision consulta) throws IOException {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(urlBase
                        + "/transmision?q="
                        + URLEncoder.encode(consulta.artista(), StandardCharsets.UTF_8)
                        + "&inicio=" + consulta.inicio()
                        + "&espectador=" + URLEncoder.encode(
                                consulta.espectador(), StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> respuesta;

        try {
            respuesta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Se interrumpio la busqueda de la transmision.", e);
        } catch (IOException e) {
            throw new IOException("El servicio de transmisiones no responde en "
                    + urlBase + ". Inicielo con: node servidor.js", e);
        }

        Respuesta datos = new Gson().fromJson(respuesta.body(), Respuesta.class);

        if (respuesta.statusCode() != 200 || datos == null || datos.ruta() == null) {
            throw new IOException(datos != null && datos.error() != null
                    ? datos.error() : "El servicio de transmisiones no pudo responder.");
        }

        return new Transmision(datos.titulo(), urlPublica + datos.ruta());
    }

    @Override
    public void cerrar(String espectador) {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(urlBase
                        + "/salida?e=" + URLEncoder.encode(espectador, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(3))
                .build();

        // Asincrono: quien sale no tiene que esperar al servicio.
        cliente.sendAsync(pedido, HttpResponse.BodyHandlers.discarding())
                .exceptionally(e -> null);
    }
}
