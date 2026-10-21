package gestor;

import java.io.IOException;

/*
 * Strategy para suscribir un calendario externo a la direccion de un evento.
 *
 * Cada implementacion (Google, Apple) sabe como abrir su aplicacion con la
 * suscripcion ya armada.
 * GestorEventosEnVivo y GestorRemoto reciben la estrategia por parametro:
 * el algoritmo se elige en la vista sin condicionar el gestor por proveedor.
 */
public interface VinculoCalendario {

    // Nombre con el que se muestra en los menus.
    String getNombre();

    // Abre el calendario para suscribirse a la direccion del evento.
    void vincular(String urlFeed) throws IOException;
}
