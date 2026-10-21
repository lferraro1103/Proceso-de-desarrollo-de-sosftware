package notificacion;

import puertos.Notificador;
import modelo.Usuario;

/*
 * Notificador que no envia nada.
 *
 * Es el que usa hoy el programa: los avisos estan previstos en el gestor,
 * pero todavia no se entregan por ningun medio. Para activarlos alcanza con
 * crear el gestor con otra implementacion de Notificador en lugar de esta.
 */
public class NotificadorInactivo implements Notificador {

    @Override
    public void notificar(Usuario destinatario, String titulo, String mensaje) {
        // No hace nada a proposito.
    }
}
