package servidor;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

/*
 * Servidor HTTP del programa, basado en el que trae el JDK (sin
 * dependencias). Atiende la API de los clientes y los calendarios.
 */
public final class ServidorHttp {

    private ServidorHttp() {
    }

    // Inicia el servidor con un manejador por cada ruta indicada.
    public static void iniciar(ConfiguracionServidor configuracion,
                               Map<String, HttpHandler> manejadores)
            throws IOException {

        HttpServer servidor = HttpServer.create(new InetSocketAddress(
                configuracion.getDireccionDeEscucha(),
                configuracion.getPuerto()), 0);

        manejadores.forEach(servidor::createContext);

        // Hilos daemon: no impiden que el programa termine.
        servidor.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread hilo = new Thread(r, "servidor-http");
            hilo.setDaemon(true);
            return hilo;
        }));

        iniciarComoDaemon(servidor);
    }

    /*
     * El hilo que atiende conexiones hereda la condicion de daemon del hilo
     * que inicia el servidor. Se inicia desde un hilo daemon para que, al
     * cerrar el programa, no quede esperando conexiones.
     */
    private static void iniciarComoDaemon(HttpServer servidor)
            throws IOException {

        Thread inicio = new Thread(servidor::start, "servidor-http-inicio");
        inicio.setDaemon(true);
        inicio.start();

        try {
            inicio.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Se interrumpio el inicio del servidor.", e);
        }
    }
}
