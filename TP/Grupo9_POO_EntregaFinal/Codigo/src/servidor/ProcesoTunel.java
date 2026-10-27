package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Abre un tunel de Cloudflare hacia los calendarios junto con el programa y
 * lo cierra cuando el programa termina.
 *
 * Con el tunel, los calendarios tienen una direccion publica con https y
 * Google Calendar tambien puede suscribirse. Solo se expone el servidor de
 * los calendarios, no la API. Si no encuentra cloudflared, sigue sin tunel:
 * Apple Calendar funciona igual en la red local.
 *
 * La direccion del tunel cambia en cada ejecucion: una suscripcion hecha con
 * ella deja de actualizarse cuando se cierra el programa.
 *
 * Propiedades (opcionales):
 *  - uadebeats.cloudflared: ruta del ejecutable de cloudflared.
 *  - uadebeats.tunel:       "no" para no abrir el tunel.
 */
public final class ProcesoTunel {

    private static final String[] POSIBLES_CLOUDFLARED = {
        "cloudflared", "/opt/homebrew/bin/cloudflared",
        "/usr/local/bin/cloudflared",
        "C:\\Program Files (x86)\\cloudflared\\cloudflared.exe",
        "C:\\Program Files\\cloudflared\\cloudflared.exe"
    };

    // Direccion que cloudflared informa al crear un tunel rapido.
    private static final Pattern DIRECCION =
            Pattern.compile("https://[a-z0-9-]+\\.trycloudflare\\.com");

    private ProcesoTunel() {
    }

    /*
     * Inicia el tunel en segundo plano. Cuando cloudflared informa la
     * direccion, se pasa a usar en los enlaces de los calendarios.
     */
    public static void iniciar(ConfiguracionServidor configuracion) {
        if ("no".equalsIgnoreCase(System.getProperty("uadebeats.tunel"))
                || configuracion.esPublica()) {
            return;
        }

        String cloudflared = localizarCloudflared();

        if (cloudflared == null) {
            System.out.println("Calendarios en la red local: "
                    + configuracion.getUrlBase() + ". Sin cloudflared no hay "
                    + "direccion publica para Google Calendar.");
            return;
        }

        try {
            Process tunel = new ProcessBuilder(cloudflared, "tunnel",
                    "--no-autoupdate", "--url",
                    "http://localhost:" + configuracion.getPuertoCalendario())
                    .redirectErrorStream(true)
                    .start();

            Runtime.getRuntime().addShutdownHook(
                    new Thread(tunel::destroy, "tunel-cierre"));

            Thread lector = new Thread(() -> leerDireccion(tunel, configuracion),
                    "tunel-salida");
            lector.setDaemon(true);
            lector.start();

            System.out.println("Abriendo un tunel para los calendarios...");
        } catch (IOException e) {
            System.out.println("No se pudo abrir el tunel: " + e.getMessage()
                    + ". Los calendarios siguen en la red local.");
        }
    }

    /*
     * Lee la salida de cloudflared hasta encontrar la direccion del tunel y
     * despues la sigue leyendo para que el proceso no se bloquee.
     */
    private static void leerDireccion(Process tunel,
                                      ConfiguracionServidor configuracion) {
        try (BufferedReader salida = new BufferedReader(new InputStreamReader(
                tunel.getInputStream(), StandardCharsets.UTF_8))) {

            boolean encontrada = false;
            String linea;

            while ((linea = salida.readLine()) != null) {
                Matcher direccion = DIRECCION.matcher(linea);

                if (!encontrada && direccion.find()) {
                    encontrada = true;
                    configuracion.usarUrlPublica(direccion.group());
                    System.out.println("Calendarios publicados en "
                            + direccion.group() + ".");
                }
            }
        } catch (IOException e) {
            // El proceso termino: no queda salida que leer.
        }
    }

    private static String localizarCloudflared() {
        String propiedad = System.getProperty("uadebeats.cloudflared");

        if (propiedad != null) {
            return funciona(propiedad) ? propiedad : null;
        }

        for (String candidato : POSIBLES_CLOUDFLARED) {
            if (funciona(candidato)) {
                return candidato;
            }
        }

        return null;
    }

    private static boolean funciona(String cloudflared) {
        try {
            Process proceso = new ProcessBuilder(cloudflared, "--version")
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
