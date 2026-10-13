package gestor;

import modelo.Cuenta;
import modelo.Evento;

import java.io.IOException;

/*
 * Contrato para vincular un calendario externo a una cuenta.
 *
 * El vinculo
 * suscribe el calendario a un evento y lo mantiene actualizado.
 */
public interface VinculoCalendario {

    // Nombre con el que se muestra en los menus.
    String getNombre();

    // Suscribe el calendario al evento de la cuenta. Falla con IOException si no se pudo.
    void vincular(Cuenta cuenta, Evento evento) throws IOException;
}
