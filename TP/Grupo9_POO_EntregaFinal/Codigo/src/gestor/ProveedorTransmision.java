package gestor;

import java.io.IOException;

/*
 * Contrato para obtener la transmision de un evento a partir del artista y
 * de cuando empezo el evento.
 *
 * El gestor depende de esta abstraccion y no del servicio concreto que
 * encuentra los videos.
 */
public interface ProveedorTransmision {

    /*
     * Busca la transmision y devuelve la direccion de la pagina donde se ve.
     * Falla con IOException si no hay ninguna o el servicio no responde.
     */
    Transmision buscar(ConsultaTransmision consulta) throws IOException;
}
