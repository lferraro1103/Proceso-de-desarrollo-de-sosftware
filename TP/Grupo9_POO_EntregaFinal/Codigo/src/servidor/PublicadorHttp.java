package servidor;

import puertos.PublicadorCalendario;
import modelo.Cuenta;
import modelo.Evento;

import java.io.IOException;

/*
 * Arma las direcciones donde ManejadorCalendario publica cada evento.
 *
 * La direccion es la publica (https) si hay una y si no la de este equipo en
 * la red local. Cada calendario decide si le alcanza: Apple se suscribe en la
 * misma red, Google necesita https porque lee desde sus servidores.
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
        return configuracion.getUrlBase() + PREFIJO
                + codigos.codigoDe(cuenta) + "/" + evento.getId() + SUFIJO;
    }
}
