package cliente;

import api.ErrorDto;
import api.Json;
import excepciones.AccesoDenegadoException;
import excepciones.ConexionException;
import excepciones.PermisoDenegadoException;
import excepciones.PersistenciaException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/*
 * Habla HTTP con el servidor: arma cada pedido con el token de la sesion y
 * convierte las respuestas de error en las mismas excepciones que lanzaria
 * el gestor local, para que las vistas no noten la diferencia.
 */
class ClienteHttp {

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String urlBase;
    private String token;

    ClienteHttp(String urlBase) {
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    void setToken(String token) {
        this.token = token;
    }

    String get(String ruta) throws AccesoDenegadoException, IOException {
        return enviar("GET", ruta, null);
    }

    String post(String ruta, Object cuerpo)
            throws AccesoDenegadoException, IOException {
        return enviar("POST", ruta, cuerpo);
    }

    String delete(String ruta) throws AccesoDenegadoException, IOException {
        return enviar("DELETE", ruta, null);
    }

    /*
     * Envia el pedido y devuelve el cuerpo de la respuesta. Si el servidor
     * responde con un error se lanza la excepcion que corresponde a su tipo;
     * si no se puede conectar, ConexionException.
     */
    private String enviar(String metodo, String ruta, Object cuerpo)
            throws AccesoDenegadoException, IOException {

        HttpRequest.Builder pedido = HttpRequest.newBuilder(
                        URI.create(urlBase + ruta))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json; charset=utf-8");

        if (token != null) {
            pedido.header("Authorization", "Bearer " + token);
        }

        pedido.method(metodo, cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(Json.escribir(cuerpo)));

        HttpResponse<String> respuesta;

        try {
            respuesta = cliente.send(pedido.build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConexionException("Se interrumpio la conexion.", e);
        } catch (IOException e) {
            throw new ConexionException("No se pudo conectar con el servidor ("
                    + urlBase + "): " + e.getMessage(), e);
        }

        if (respuesta.statusCode() >= 200 && respuesta.statusCode() < 300) {
            return respuesta.body();
        }

        throw errorDe(respuesta);
    }

    // Recrea la excepcion que el servidor informo.
    private static RuntimeException errorDe(HttpResponse<String> respuesta)
            throws AccesoDenegadoException, IOException {

        ErrorDto error;

        try {
            error = Json.leer(respuesta.body(), ErrorDto.class);
        } catch (RuntimeException e) {
            error = null;
        }

        if (error == null || error.tipo() == null) {
            return new ConexionException("Respuesta inesperada del servidor ("
                    + respuesta.statusCode() + ").", null);
        }

        switch (error.tipo()) {
            case "acceso":
                throw new AccesoDenegadoException(error.mensaje());
            case "externo":
                throw new IOException(error.mensaje());
            case "permiso":
            case "sesion":
                return new PermisoDenegadoException(error.mensaje());
            case "persistencia":
                return new PersistenciaException(error.mensaje(), null);
            default:
                return new IllegalArgumentException(error.mensaje());
        }
    }
}
