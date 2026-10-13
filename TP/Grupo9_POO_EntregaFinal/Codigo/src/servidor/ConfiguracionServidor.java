package servidor;

import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * Parametros de red del servidor, tomados de propiedades del sistema para no
 * modificar el codigo al cambiar de equipo.
 *
 *  - uadebeats.puerto:   puerto de escucha (8765 por defecto).
 *  - uadebeats.escuchar: "local" solo acepta conexiones de este equipo;
 *                        "todas" acepta tambien las de la red.
 *  - uadebeats.url:      direccion publica (https) por la que se alcanza el
 *                        servidor desde afuera, por ejemplo la de un tunel.
 */
public final class ConfiguracionServidor {

    private final int puerto;
    private final boolean aceptaRed;
    private final String urlPublica;

    // Por defecto solo se acepta el equipo local; el modo servidor acepta la red.
    public ConfiguracionServidor(boolean aceptaRedPorDefecto) {
        this.puerto = Integer.getInteger("uadebeats.puerto", 8765);

        String escuchar = System.getProperty("uadebeats.escuchar");
        this.aceptaRed = (escuchar == null)
                ? aceptaRedPorDefecto : "todas".equalsIgnoreCase(escuchar);

        String url = System.getProperty("uadebeats.url");
        this.urlPublica = (url == null || url.isBlank())
                ? null : url.replaceAll("/+$", "");
    }

    public int getPuerto() {
        return puerto;
    }

    // Direccion en la que se escucha: la local o todas las interfaces.
    public InetAddress getDireccionDeEscucha() throws UnknownHostException {
        return aceptaRed
                ? InetAddress.getByName("0.0.0.0")
                : InetAddress.getLoopbackAddress();
    }

    // Direccion base con la que se arman los enlaces que se entregan.
    public String getUrlBase() {
        return urlPublica != null ? urlPublica : "http://localhost:" + puerto;
    }

    // Indica si la direccion base se puede alcanzar desde afuera.
    public boolean esPublica() {
        return urlPublica != null;
    }
}
