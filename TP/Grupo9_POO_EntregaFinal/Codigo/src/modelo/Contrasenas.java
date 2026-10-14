package modelo;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/*
 * Guarda las contrasenas como hash y nunca en texto plano.
 *
 * Usa PBKDF2 con HMAC-SHA256, que trae el JDK: cada contrasena lleva su
 * propia sal aleatoria y se repite el calculo muchas veces para que probar
 * contrasenas por fuerza bruta sea lento. El resultado se guarda en un solo
 * texto con el formato  pbkdf2$iteraciones$sal$hash  (sal y hash en Base64),
 * de modo que las iteraciones pueden aumentar mas adelante sin invalidar los
 * hash ya guardados.
 */
public final class Contrasenas {

    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;

    private static final SecureRandom AZAR = new SecureRandom();

    private Contrasenas() {
    }

    // Devuelve el hash de la contrasena, con una sal nueva en cada llamada.
    public static String hashear(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        AZAR.nextBytes(sal);

        return PREFIJO + "$" + ITERACIONES
                + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(
                        calcular(contrasena, sal, ITERACIONES));
    }

    /*
     * Indica si la contrasena ingresada corresponde al hash guardado. Un
     * texto que no tiene el formato de un hash (por ejemplo una contrasena
     * guardada sin hashear) nunca coincide.
     */
    public static boolean verificar(String contrasena, String hash) {
        String[] partes = hash.split("\\$");

        if (partes.length != 4 || !partes[0].equals(PREFIJO)) {
            return false;
        }

        try {
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = calcular(contrasena,
                    Base64.getDecoder().decode(partes[2]),
                    Integer.parseInt(partes[1]));

            // Comparacion de tiempo constante, para no filtrar por el tiempo.
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] calcular(String contrasena, byte[] sal, int iteraciones) {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(new PBEKeySpec(contrasena.toCharArray(),
                            sal, iteraciones, BITS_HASH))
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No se pudo calcular el hash de la contrasena.", e);
        }
    }
}
