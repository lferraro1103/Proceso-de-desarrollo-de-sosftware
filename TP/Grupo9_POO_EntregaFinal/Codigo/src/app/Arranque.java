package app;

import gestor.GestorEventosEnVivo;
import puertos.ProveedorTransmision;
import notificacion.NotificadorInactivo;
import persistencia.PersistenciaSqlite;
import transmision.ClienteTransmision;
import transmision.ProcesoServicio;
import servidor.CodigosCalendario;
import servidor.ConfiguracionServidor;
import servidor.ManejadorApi;
import servidor.ManejadorCalendario;
import servidor.ProcesoTunel;
import servidor.PublicadorHttp;
import servidor.ServidorHttp;

import java.io.IOException;
import java.net.BindException;
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

        ProveedorTransmision proveedorTransmision = new ClienteTransmision();

        GestorEventosEnVivo gestor = new GestorEventosEnVivo(
                new PersistenciaSqlite(),
                new NotificadorInactivo(),
                new PublicadorHttp(configuracion, codigos),
                proveedorTransmision);

        gestor.cargarDatos();

        // La primera vez la base esta vacia: se cargan los datos de prueba.
        if (gestor.listarUsuarios().isEmpty()) {
            DatosIniciales.sembrar(gestor);
        }

        ManejadorCalendario calendarios = new ManejadorCalendario(gestor, codigos);

        try {
            ServidorHttp.iniciar(configuracion, Map.of(
                    "/api/", new ManejadorApi(gestor, proveedorTransmision),
                    "/calendario/", calendarios));
        } catch (BindException e) {
            throw new IOException("El puerto " + configuracion.getPuerto()
                    + " ya esta en uso (¿hay otro programa abierto?). "
                    + "Use -Duadebeats.puerto=<otro puerto>.", e);
        }

        // Si la API solo escucha este equipo, los calendarios se publican
        // aparte en la red local para que Apple Calendar se suscriba sin
        // arrancar el modo servidor. Con cloudflared se abre ademas un tunel
        // con https para Google Calendar.
        if (!configuracion.aceptaRed()) {
            ServidorHttp.iniciarCalendarios(configuracion, calendarios);
            ProcesoTunel.iniciar(configuracion);
        }

        // El servicio de transmisiones arranca con el programa y no hace nada
        // hasta que alguien pide una transmision.
        ProcesoServicio.iniciar();

        return gestor;
    }
}
