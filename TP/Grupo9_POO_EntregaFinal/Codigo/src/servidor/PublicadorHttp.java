package servidor;

import puertos.PublicadorCalendario;
import modelo.Cuenta;
import modelo.Evento;

import java.io.IOException;

/*
 * Arma las direcciones donde ManejadorCalendario publica cada evento.
 */
public class PublicadorHttp implements PublicadorCalendario {

    static final String PREFIJO = "/calendario/";
    static final String SUFIJO = ".ics";

    private final ConfiguracionServidor configuracion;
    private final CodigosCalendario codigos;

    public PublicadorHttp(ConfiguracionServidor configuracion,
                          CodigosCalendario codigos) {
        this.configuracion = configuracion;
        this.codigos = codigos;
    }

    @Override
    public String urlFeed(Cuenta cuenta, Evento evento) throws IOException {
        // Calendar convierte webcal:// en https:// y Google lee la direccion
        // desde sus servidores: con una direccion local nunca se completa.
        if (!configuracion.esPublica()) {
            throw new IOException("Google y Apple Calendar necesitan una "
                    + "direccion publica con https y esta es local. Exponga "
                    + "el puerto con un tunel y ejecute con "
                    + "-Duadebeats.url=https://su-direccion.");
        }

        return configuracion.getUrlBase() + PREFIJO
                + codigos.codigoDe(cuenta) + "/" + evento.getId() + SUFIJO;
    }
}
