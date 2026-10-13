package app;

import gestor.GestorEventosEnVivo;
import notificacion.NotificadorInactivo;
import persistencia.PersistenciaArchivos;
import servidor.CodigosCalendario;
import servidor.ConfiguracionServidor;
import servidor.ManejadorCalendario;
import servidor.PublicadorHttp;
import servidor.ServidorHttp;

import java.io.IOException;
import java.util.Map;

/*
 * Arma el gestor con sus colaboradores y levanta el servidor.
 *
 * Es el unico lugar que conoce las clases concretas (persistencia,
 * notificador, servidor): el gestor las recibe por el constructor.
 */
final class Arranque {

    private Arranque() {
    }

    // Gestor con datos propios y servidor HTTP en marcha.
    static GestorEventosEnVivo iniciarServidor(boolean aceptaRed)
            throws IOException {

        ConfiguracionServidor configuracion = new ConfiguracionServidor(aceptaRed);
        CodigosCalendario codigos = new CodigosCalendario();

        GestorEventosEnVivo gestor = new GestorEventosEnVivo(
                new PersistenciaArchivos(),
                new NotificadorInactivo(),
                new PublicadorHttp(configuracion, codigos));

        DatosIniciales.cargar(gestor);

        ServidorHttp.iniciar(configuracion, Map.of(
                "/calendario/", new ManejadorCalendario(gestor, codigos)));

        return gestor;
    }
}
