package gestor;

import java.io.IOException;

/*
 * Contrato para obtener la transmision de un evento a partir de una busqueda
 * (normalmente el nombre del artista).
 *
 * El gestor depende de esta abstraccion y no del servicio concreto que
 * encuentra los videos.
 */
public interface ProveedorTransmision {

    // Busca la transmision. Falla con IOException si no hay o el servicio no responde.
    Transmision buscar(String consulta) throws IOException;
}
