package api;

import com.google.gson.Gson;

/*
 * Conversion entre objetos de la API y texto JSON, compartida por el
 * servidor y el cliente para que ambos hablen exactamente el mismo formato.
 */
public final class Json {

    private static final Gson GSON = new Gson();

    private Json() {
    }

    public static String escribir(Object objeto) {
        return GSON.toJson(objeto);
    }

    public static <T> T leer(String texto, Class<T> tipo) {
        return GSON.fromJson(texto, tipo);
    }
}
