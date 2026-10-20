package transmision;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/*
 * Inicia el servicio de transmisiones (Node.js) junto con el programa y lo
 * cierra cuando el programa termina.
 *
 * Solo arranca el proceso: el servicio no hace nada hasta que alguien pide una
 * transmision. Si ya hay uno funcionando (por ejemplo, iniciado a mano) lo
 * usa y no lo toca. Si no encuentra Node o el script, avisa y sigue: la
 * transmision mostrara que el servicio no responde.
 *
 * Propiedades (todas opcionales):
 *  - uadebeats.node:      ruta del ejecutable de Node.
 *  - uadebeats.servicio:  ruta de servidor.js.
 */
public final class ProcesoServicio {

    private static final String[] POSIBLES_NODE = {
        "node", "/opt/homebrew/bin/node", "/usr/local/bin/node", "/usr/bin/node"
    };

    private ProcesoServicio() {
    }

    public static void iniciar() {
        URI base = URI.create(System.getProperty(
                "uadebeats.transmision", "http://localhost:8766"));

        // Un servicio en otra maquina lo maneja quien lo administre alla.
        if (!base.getHost().equals("localhost") && !base.getHost().equals("127.0.0.1")) {
            return;
        }

        if (respondeSalud(base)) {
            System.out.println("Servicio de transmisiones ya en marcha en " + base + ".");
            return;
        }

        Path script = localizarScript();
        String node = localizarNode();

        if (script == null || node == null) {
            System.out.println("No se inicio el servicio de transmisiones: "
                    + (script == null ? "no se encontro servidor.js" : "no se encontro Node.js")
                    + ". Inicielo a mano con: node servidor.js");
            return;
        }

        try {
            Path registro = Paths.get(System.getProperty("user.home"),
                    ".uadebeats", "transmision.log");
            Files.createDirectories(registro.getParent());

            ProcessBuilder proceso = new ProcessBuilder(node, script.getFileName().toString())
                    .directory(script.getParent().toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(registro.toFile()));

            proceso.environment().put("PORT", String.valueOf(base.getPort() < 0 ? 8766 : base.getPort()));
            // Si este programa termina de golpe, el servicio se da cuenta y se cierra.
            proceso.environment().put("PADRE", String.valueOf(ProcessHandle.current().pid()));

            Process servicio = proceso.start();
            Runtime.getRuntime().addShutdownHook(new Thread(servicio::destroy));

            System.out.println("Servicio de transmisiones iniciado (" + script + ").");
        } catch (IOException e) {
            System.out.println("No se pudo iniciar el servicio de transmisiones: "
                    + e.getMessage());
        }
    }

    private static boolean respondeSalud(URI base) {
        try {
            HttpResponse<String> respuesta = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(base.resolve("/salud"))
                            .timeout(Duration.ofSeconds(1)).build(),
                    HttpResponse.BodyHandlers.ofString());
            return respuesta.statusCode() == 200;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // Busca servidor.js: por propiedad o subiendo por las carpetas desde la actual.
    private static Path localizarScript() {
        String propiedad = System.getProperty("uadebeats.servicio");

        if (propiedad != null) {
            Path ruta = Paths.get(propiedad);
            return Files.isRegularFile(ruta) ? ruta.toAbsolutePath() : null;
        }

        for (Path dir = Paths.get("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            for (String candidato : new String[] {
                "TP/Grupo9_Servicio_Transmision/servidor.js",
                "Grupo9_Servicio_Transmision/servidor.js"}) {

                Path ruta = dir.resolve(candidato);

                if (Files.isRegularFile(ruta)) {
                    return ruta;
                }
            }
        }

        return null;
    }

    /*
     * Un programa abierto desde el escritorio puede no tener Node en el PATH,
     * por eso tambien se prueban las carpetas habituales.
     */
    private static String localizarNode() {
        String propiedad = System.getProperty("uadebeats.node");

        if (propiedad != null) {
            return funciona(propiedad) ? propiedad : null;
        }

        for (String candidato : POSIBLES_NODE) {
            if (funciona(candidato)) {
                return candidato;
            }
        }

        return null;
    }

    private static boolean funciona(String node) {
        try {
            Process proceso = new ProcessBuilder(node, "--version")
                    .redirectErrorStream(true).start();
            proceso.getInputStream().readAllBytes();
            return proceso.waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
