package gestor;

import modelo.Usuario;

/*
 * Contrato para avisarle a un usuario de algo que le ocurrio en el sistema.
 *
 * El gestor depende de esta abstraccion y no del medio concreto (push,
 * correo, consola), de modo que el medio puede cambiarse sin tocarlo.
 */
public interface Notificador {

    // Envia el aviso al usuario. No debe interrumpir el flujo si falla.
    void notificar(Usuario destinatario, String titulo, String mensaje);
}
