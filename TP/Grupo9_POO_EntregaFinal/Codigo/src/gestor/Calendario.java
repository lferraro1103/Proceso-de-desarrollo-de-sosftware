package gestor;

import modelo.Evento;

import java.io.IOException;

/*
 * Contrato para llevar un evento a un calendario externo.
 *
 * El gestor depende de esta abstraccion y no del calendario concreto
 * (Google, Apple u otro), de modo que se puede agregar uno nuevo sin
 * modificarlo.
 */
public interface Calendario {

    // Nombre con el que se muestra el calendario en los menus.
    String getNombre();

    // Agrega el evento al calendario. Falla con IOException si no se pudo.
    void agregar(Evento evento) throws IOException;
}
