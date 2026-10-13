package servidor;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Sesiones abiertas por los clientes remotos: cada token aleatorio
 * corresponde al nombre de una cuenta. Viven en memoria: al reiniciar el
 * servidor hay que volver a iniciar sesion.
 */
class Sesiones {

    private final Map<String, String> cuentasPorToken = new ConcurrentHashMap<>();
    private final SecureRandom azar = new SecureRandom();

    // Abre una sesion para la cuenta y devuelve su token.
    String abrir(String nombreUsuario) {
        byte[] bytes = new byte[24];
        azar.nextBytes(bytes);

        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }

        cuentasPorToken.put(token.toString(), nombreUsuario);
        return token.toString();
    }

    // Nombre de la cuenta de ese token, o null si no hay sesion abierta.
    String cuentaDe(String token) {
        return token == null ? null : cuentasPorToken.get(token);
    }

    void cerrar(String token) {
        if (token != null) {
            cuentasPorToken.remove(token);
        }
    }
}
