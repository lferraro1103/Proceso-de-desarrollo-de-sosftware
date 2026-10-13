package calendario;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import gestor.GestorEventosEnVivo;
import modelo.Cuenta;
import modelo.Evento;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Executors;

/*
 * Publica el calendario de cada cuenta en una direccion propia, para que
 * Google Calendar o Apple Calendar se suscriban una vez y reciban solos los
 * cambios de sus eventos.
 *
 * La direccion lleva un codigo secreto por cuenta (HMAC de su nombre con
 * una clave guardada en la carpeta del usuario), asi que no se puede
 * adivinar y no hace falta ningun login. Escucha solo en el equipo local.
 * Para que Google, que lee desde sus servidores, llegue a la direccion, hay
 * que exponerla con un tunel o un servidor y pasar la direccion publica en
 * la propiedad uadebeats.url.
 */
public class ServidorCalendario {

    private static final String PREFIJO = "/calendario/";
    private static final String SUFIJO = ".ics";

    // Una sola instancia por programa: la comparten la consola y la ventana.
    private static ServidorCalendario compartido;

    private final GestorEventosEnVivo gestor;
    private final byte[] clave;
    private final String urlBase;
    private final boolean local;

    private ServidorCalendario(GestorEventosEnVivo gestor) throws IOException {
        this.gestor = gestor;
        this.clave = leerOCrearClave();

        int puerto = Integer.getInteger("uadebeats.puerto", 8765);
        String publica = System.getProperty("uadebeats.url");

        this.local = (publica == null || publica.isBlank());
        this.urlBase = local ? "http://localhost:" + puerto
                : publica.replaceAll("/+$", "");

        HttpServer servidor = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), puerto), 0);
        servidor.createContext(PREFIJO, this::atender);
        // Hilos daemon: no impiden que el programa termine.
        servidor.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread hilo = new Thread(r, "calendario-http");
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
    private static void iniciarComoDaemon(HttpServer servidor) throws IOException {
        Thread inicio = new Thread(servidor::start, "calendario-inicio");
        inicio.setDaemon(true);
        inicio.start();

        try {
            inicio.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Se interrumpio el inicio del servidor.", e);
        }
    }

    // Devuelve la instancia compartida, iniciando el servidor la primera vez.
    public static synchronized ServidorCalendario obtener(GestorEventosEnVivo gestor)
            throws IOException {
        if (compartido == null) {
            compartido = new ServidorCalendario(gestor);
        }

        return compartido;
    }

    // Direccion http(s) del calendario de un evento para la cuenta.
    public String urlFeed(Cuenta cuenta, Evento evento) {
        return urlBase + PREFIJO + token(cuenta) + "/" + evento.getId() + SUFIJO;
    }

    // Direccion con esquema webcal, que Apple Calendar y Google reconocen.
    public String urlWebcal(Cuenta cuenta, Evento evento) {
        return urlFeed(cuenta, evento).replaceFirst("^https?://", "webcal://");
    }

    // Indica si la direccion solo se alcanza desde este equipo.
    public boolean esLocal() {
        return local;
    }

    private void atender(HttpExchange intercambio) throws IOException {
        try (intercambio) {
            Evento evento = null;
            String ruta = intercambio.getRequestURI().getPath();

            // Ruta: /calendario/<codigo>/<idEvento>.ics
            if (ruta.startsWith(PREFIJO) && ruta.endsWith(SUFIJO)) {
                String[] partes = ruta.substring(PREFIJO.length(),
                        ruta.length() - SUFIJO.length()).split("/");

                if (partes.length == 2) {
                    evento = buscarEvento(buscarPorToken(partes[0]), partes[1]);
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
     * Evento pedido si la cuenta esta activa y puede verlo, o null. El
     * gestor no esta pensado para lecturas desde otro hilo: si una lista
     * cambia justo mientras se lee, se reintenta.
     */
    private Evento buscarEvento(Cuenta cuenta, String texto) {
        if (cuenta == null || !cuenta.isActivo()) {
            return null;
        }

        int id;

        try {
            id = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }

        for (int intento = 0; intento < 3; intento++) {
            try {
                return gestor.buscarEventoVisiblePara(cuenta, id);
            } catch (ConcurrentModificationException e) {
                // Reintenta.
            }
        }

        return gestor.buscarEventoVisiblePara(cuenta, id);
    }

    private Cuenta buscarPorToken(String token) {
        List<Cuenta> cuentas = new ArrayList<>(gestor.listarUsuarios());
        cuentas.addAll(gestor.listarArtistas());

        for (Cuenta cuenta : cuentas) {
            if (MessageDigest.isEqual(
                    token(cuenta).getBytes(StandardCharsets.UTF_8),
                    token.getBytes(StandardCharsets.UTF_8))) {
                return cuenta;
            }
        }

        return null;
    }

    // Codigo secreto de la cuenta: HMAC-SHA256 de su nombre con la clave.
    private String token(Cuenta cuenta) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clave, "HmacSHA256"));
            byte[] firma = mac.doFinal((cuenta.getTipoUsuario() + ":"
                    + cuenta.getNombreUsuario().toLowerCase())
                    .getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                hex.append(String.format("%02x", firma[i]));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo generar el codigo.", e);
        }
    }

    // La clave vive en la carpeta del usuario, fuera del repositorio.
    private static byte[] leerOCrearClave() throws IOException {
        Path archivo = Paths.get(System.getProperty("user.home"),
                ".uadebeats", "calendario.key");

        if (Files.exists(archivo)) {
            return Files.readAllBytes(archivo);
        }

        byte[] nueva = new byte[32];
        new SecureRandom().nextBytes(nueva);
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, nueva);
        return nueva;
    }
}
