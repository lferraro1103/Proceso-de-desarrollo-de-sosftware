package calendario;

import gestor.GestorEventosEnVivo;
import gestor.VinculoCalendario;
import modelo.Cuenta;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/*
 * Suscribe Apple Calendar al calendario de la cuenta.
 *
 * Abre un enlace webcal://; el sistema lo entrega a Calendario, que muestra
 * la ventana de suscripcion. Despues se actualiza solo.
 */
public class VinculoApple implements VinculoCalendario {

    private final GestorEventosEnVivo gestor;

    public VinculoApple(GestorEventosEnVivo gestor) {
        this.gestor = gestor;
    }

    @Override
    public String getNombre() {
        return "Apple Calendar";
    }

    @Override
    public void vincular(Cuenta cuenta) throws IOException {
        String enlace = ServidorCalendario.obtener(gestor).urlWebcal(cuenta);

        // Calendar convierte webcal:// en https:// y el servidor local solo
        // habla http, por lo que la suscripcion nunca se completa.
        if (ServidorCalendario.obtener(gestor).esLocal()) {
            throw new IOException("Apple Calendar se suscribe por https y esta "
                    + "direccion es local. Exponga el puerto con un tunel y "
                    + "ejecute con -Duadebeats.url=https://su-direccion.");
        }

        // En Mac se llama a la app Calendario de forma explicita: el
        // navegador predeterminado puede quedarse con los enlaces webcal.
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            new ProcessBuilder("open", "-a", "Calendar", enlace).start();
            return;
        }

        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Este equipo no puede abrir el calendario. "
                    + "Suscribase manualmente a: " + enlace);
        }

        Desktop.getDesktop().browse(URI.create(enlace));
    }
}
