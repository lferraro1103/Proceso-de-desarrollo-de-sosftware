package gestor;

import modelo.Cuenta;

import java.io.IOException;

/*
 * Contrato para vincular un calendario externo a una cuenta.
 *
 * A diferencia de Calendario, que lleva un evento una sola vez, el vinculo
 * suscribe el calendario a todos los eventos de la cuenta y los mantiene
 * actualizados.
 */
public interface VinculoCalendario {

    // Nombre con el que se muestra en los menus.
    String getNombre();

    // Inicia la vinculacion para la cuenta. Falla con IOException si no se pudo.
    void vincular(Cuenta cuenta) throws IOException;
}
