package servidor;

import modelo.Cuenta;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/*
 * Codigo secreto de cada cuenta para las direcciones de calendario.
 *
 * Es un HMAC-SHA256 del nombre de la cuenta con una clave guardada en la
 * carpeta del usuario, fuera del repositorio. No se puede adivinar y evita
 * pedir login a Google o Apple al leer la suscripcion.
 */
public class CodigosCalendario {

    private final byte[] clave;

    public CodigosCalendario() throws IOException {
        Path archivo = Paths.get(System.getProperty("user.home"),
                ".uadebeats", "calendario.key");

        if (Files.exists(archivo)) {
            clave = Files.readAllBytes(archivo);
            return;
        }

        clave = new byte[32];
        new SecureRandom().nextBytes(clave);
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, clave);
    }

    public String codigoDe(Cuenta cuenta) {
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
}
