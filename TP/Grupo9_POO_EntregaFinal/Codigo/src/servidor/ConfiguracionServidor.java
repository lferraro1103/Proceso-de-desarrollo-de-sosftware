package servidor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;

/*
 * Parametros de red del servidor, tomados de propiedades del sistema para no
 * modificar el codigo al cambiar de equipo.
 *
 *  - uadebeats.puerto:            puerto de escucha (8765 por defecto).
 *  - uadebeats.escuchar:          "local" solo acepta conexiones de este
 *                                 equipo; "todas" acepta tambien las de la red.
 *  - uadebeats.url:               direccion publica (https) por la que se
 *                                 alcanza el servidor desde afuera, por
 *                                 ejemplo la de un tunel.
 *  - uadebeats.puerto.calendario: puerto con el que se publican los
 *                                 calendarios en la red cuando la API solo
 *                                 escucha este equipo (8767 por defecto).
 *
 * Sin uadebeats.url, los calendarios se publican con la direccion de este
 * equipo en la red local, que alcanza para Apple Calendar en la misma red.
 */
public final class ConfiguracionServidor {

    private static final int PUERTO_POR_DEFECTO = 8765;
    private static final int PUERTO_CALENDARIO_POR_DEFECTO = 8767;

    private int puerto;
    private final boolean puertoElegido;
    private final boolean aceptaRed;
    private int puertoCalendario;

    // La puede fijar el tunel despues de arrancar, desde otro hilo.
    private volatile String urlPublica;

    // Por defecto solo se acepta el equipo local; el modo servidor acepta la red.
    public ConfiguracionServidor(boolean aceptaRedPorDefecto) {
        this.puertoElegido = System.getProperty("uadebeats.puerto") != null;
        this.puerto = Integer.getInteger("uadebeats.puerto", PUERTO_POR_DEFECTO);

        String escuchar = System.getProperty("uadebeats.escuchar");
        this.aceptaRed = (escuchar == null)
                ? aceptaRedPorDefecto : "todas".equalsIgnoreCase(escuchar);

        // Si la API ya escucha la red, los calendarios usan su mismo puerto.
        this.puertoCalendario = aceptaRed ? puerto : Integer.getInteger(
                "uadebeats.puerto.calendario", PUERTO_CALENDARIO_POR_DEFECTO);

        String url = System.getProperty("uadebeats.url");
        this.urlPublica = (url == null || url.isBlank())
                ? null : url.replaceAll("/+$", "");
    }

    public int getPuerto() {
        return puerto;
    }

    // Indica si el puerto se eligio con uadebeats.puerto (entonces no se cambia).
    boolean isPuertoElegido() {
        return puertoElegido;
    }

    // Puerto en el que quedo escuchando la API, si hubo que usar otro.
    void usarPuerto(int puerto) {
        if (aceptaRed) {
            puertoCalendario = puerto;
        }
        this.puerto = puerto;
    }

    public boolean aceptaRed() {
        return aceptaRed;
    }

    public int getPuertoCalendario() {
        return puertoCalendario;
    }

    // Puerto en el que quedaron los calendarios, si hubo que usar otro.
    void usarPuertoCalendario(int puerto) {
        this.puertoCalendario = puerto;
    }

    // Direccion en la que se escucha: la local o todas las interfaces.
    public InetAddress getDireccionDeEscucha() throws UnknownHostException {
        return aceptaRed
                ? InetAddress.getByName("0.0.0.0")
                : InetAddress.getLoopbackAddress();
    }

    /*
     * Direccion base con la que se arman los enlaces de los calendarios: la
     * publica si hay una (propiedad o tunel) y si no la de la red local. Si
     * el equipo no esta en ninguna red, la local, que solo sirve en el mismo
     * equipo.
     */
    public String getUrlBase() {
        if (urlPublica != null) {
            return urlPublica;
        }

        String ip = ipDeLaRed();
        return "http://" + (ip != null ? ip : "localhost") + ":" + puertoCalendario;
    }

    // Indica si la direccion base se puede alcanzar desde afuera.
    public boolean esPublica() {
        return urlPublica != null;
    }

    // Direccion publica obtenida despues de arrancar (la del tunel).
    void usarUrlPublica(String url) {
        this.urlPublica = url.replaceAll("/+$", "");
    }

    /*
     * Direccion IPv4 privada de este equipo en la red local, o null.
     *
     * Primero se pregunta por que interfaz saldria un paquete hacia internet:
     * conectar un socket UDP no envia nada y evita elegir adaptadores
     * virtuales. Sin salida a internet, se usa la primera direccion privada
     * de una interfaz activa.
     */
    static String ipDeLaRed() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress local = socket.getLocalAddress();

            if (local instanceof Inet4Address && local.isSiteLocalAddress()) {
                return local.getHostAddress();
            }
        } catch (IOException | RuntimeException e) {
            // Sin ruta hacia afuera: se prueba con las interfaces.
        }

        try {
            for (NetworkInterface interfaz
                    : Collections.list(NetworkInterface.getNetworkInterfaces())) {

                if (!interfaz.isUp() || interfaz.isLoopback() || interfaz.isVirtual()) {
                    continue;
                }

                for (InetAddress direccion
                        : Collections.list(interfaz.getInetAddresses())) {

                    if (direccion instanceof Inet4Address
                            && direccion.isSiteLocalAddress()) {
                        return direccion.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            // Sin acceso a las interfaces: no hay direccion de red.
        }

        return null;
    }
}
