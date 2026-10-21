package gestor;

import modelo.Cuenta;
import modelo.Evento;

/** Usuarios y administradores consultan el catalogo completo. No otorga permisos de escritura. */
public final class VisibilidadGeneral implements PoliticaVisibilidadEvento {
    @Override
    public boolean esVisible(Cuenta cuenta, Evento evento) {
        return cuenta != null && evento != null;
    }
}
