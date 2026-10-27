package puertos;

import modelo.Cuenta;
import modelo.Evento;

import java.io.IOException;

/*
 * Contrato para obtener la direccion donde se publica el calendario de un
 * evento.
 *
 * El gestor depende de esta abstraccion y no del servidor HTTP concreto que
 * publica los archivos .ics.
 */
public interface PublicadorCalendario {

    /*
     * Direccion http(s) del calendario del evento para la cuenta. Puede ser
     * de la red local: cada VinculoCalendario decide si le alcanza (Google
     * necesita https porque lee la suscripcion desde sus servidores). Falla
     * con IOException si no se puede armar ninguna.
     */
    String urlFeed(Cuenta cuenta, Evento evento) throws IOException;
}
