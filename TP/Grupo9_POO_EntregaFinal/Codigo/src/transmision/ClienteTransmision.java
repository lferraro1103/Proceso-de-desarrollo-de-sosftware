package transmision;

import com.google.gson.Gson;
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
 * La direccion del servicio se toma de la propiedad uadebeats.transmision
 * (por defecto http://localhost:8766).
 */
public class ClienteTransmision implements ProveedorTransmision {

    // Lo que responde el servicio; los campos que sobran se ignoran.
    private record Respuesta(String titulo, String url, String error) {
    }

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final String urlBase = System.getProperty(
            "uadebeats.transmision", "http://localhost:8766").replaceAll("/+$", "");

    @Override
    public Transmision buscar(String consulta) throws IOException {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(urlBase
                        + "/transmision?q="
                        + URLEncoder.encode(consulta, StandardCharsets.UTF_8)))
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

        if (respuesta.statusCode() != 200 || datos == null || datos.url() == null) {
            throw new IOException(datos != null && datos.error() != null
                    ? datos.error() : "El servicio de transmisiones no pudo responder.");
        }

        return new Transmision(datos.titulo(), datos.url());
    }
}
