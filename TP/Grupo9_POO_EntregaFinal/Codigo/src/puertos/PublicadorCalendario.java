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
     * Direccion http(s) del calendario del evento para la cuenta. Falla con
     * IOException si la direccion no es alcanzable desde afuera, porque
     * Google y Apple Calendar leen la suscripcion desde sus servidores.
     */
    String urlFeed(Cuenta cuenta, Evento evento) throws IOException;
}
