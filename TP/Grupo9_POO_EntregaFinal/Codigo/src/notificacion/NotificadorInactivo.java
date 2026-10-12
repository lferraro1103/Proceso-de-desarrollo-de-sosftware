package notificacion;

import gestor.Notificador;
import modelo.Usuario;

/*
 * Notificador que no envia nada.
 *
 * Es el que usa hoy el programa: los avisos estan previstos en el gestor,
 * pero todavia no se entregan por ningun medio. Para activarlos alcanza con
 * crear el gestor con NotificadorNtfy (u otro medio) en lugar de esta clase.
 */
public class NotificadorInactivo implements Notificador {

    @Override
    public void notificar(Usuario destinatario, String titulo, String mensaje) {
        // No hace nada a proposito.
    }
}
