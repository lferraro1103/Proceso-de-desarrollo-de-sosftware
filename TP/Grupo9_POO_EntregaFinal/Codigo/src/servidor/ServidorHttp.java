package servidor;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
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

    /*
     * Inicia el servidor con un manejador por cada ruta indicada.
     *
     * En el modo completo, si el puerto por defecto esta ocupado (por
     * ejemplo, por otro programa abierto) se usa uno libre. Un puerto elegido
     * con uadebeats.puerto o el del modo servidor no se cambian, porque los
     * clientes lo esperan ahi: en ese caso falla con BindException.
     */
    public static void iniciar(ConfiguracionServidor configuracion,
                               Map<String, HttpHandler> manejadores)
            throws IOException {

        HttpServer servidor;

        try {
            servidor = crear(configuracion.getDireccionDeEscucha(),
                    configuracion.getPuerto());
        } catch (BindException e) {
            if (configuracion.aceptaRed() || configuracion.isPuertoElegido()) {
                throw e;
            }

            servidor = crear(configuracion.getDireccionDeEscucha(), 0);
            configuracion.usarPuerto(servidor.getAddress().getPort());

            System.out.println("El puerto de la API estaba en uso; se usa el "
                    + configuracion.getPuerto() + ".");
        }

        ponerEnMarcha(servidor, manejadores);
    }

    /*
     * Publica los calendarios en la red local cuando la API solo escucha este
     * equipo. Este servidor atiende unicamente los .ics, que no piden login
     * pero llevan un codigo que no se puede adivinar: la API no queda
     * expuesta a la red.
     */
    public static void iniciarCalendarios(ConfiguracionServidor configuracion,
                                          HttpHandler manejador)
            throws IOException {

        InetAddress todas = InetAddress.getByName("0.0.0.0");
        HttpServer servidor;

        try {
            servidor = crear(todas, configuracion.getPuertoCalendario());
        } catch (BindException e) {
            servidor = crear(todas, 0);
            configuracion.usarPuertoCalendario(servidor.getAddress().getPort());

            // Las suscripciones guardan la direccion completa, puerto incluido.
            System.out.println("El puerto de los calendarios estaba en uso; se "
                    + "usa el " + configuracion.getPuertoCalendario() + ". Las "
                    + "suscripciones que se hagan ahora dejan de actualizarse "
                    + "si el puerto cambia en otra ejecucion.");
        }

        ponerEnMarcha(servidor, Map.of(PublicadorHttp.PREFIJO, manejador));
    }

    private static HttpServer crear(InetAddress direccion, int puerto)
            throws IOException {
        return HttpServer.create(new InetSocketAddress(direccion, puerto), 0);
    }

    private static void ponerEnMarcha(HttpServer servidor,
                                      Map<String, HttpHandler> manejadores)
            throws IOException {

        manejadores.forEach(servidor::createContext);

        // Hilos daemon: no impiden que el programa termine.
        servidor.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread hilo = new Thread(r, "servidor-http");
            hilo.setDaemon(true);
            return hilo;
        }));

        iniciarComoDaemon(servidor);

        // Al salir del programa se libera el puerto.
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> servidor.stop(0), "servidor-http-cierre"));
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
