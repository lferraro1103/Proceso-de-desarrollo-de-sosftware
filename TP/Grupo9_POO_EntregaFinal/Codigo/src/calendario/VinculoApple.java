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

        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Este equipo no puede abrir el calendario. "
                    + "Suscribase manualmente a: " + enlace);
        }

        Desktop.getDesktop().browse(URI.create(enlace));
    }
}
