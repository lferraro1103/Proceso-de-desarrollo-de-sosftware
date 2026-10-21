package gestor;

import modelo.Artista;
import modelo.Cuenta;
import modelo.Evento;

/** Un artista consulta solamente eventos en los que participa, comparando identidad persistente. */
public final class VisibilidadArtista implements PoliticaVisibilidadEvento {
    @Override
    public boolean esVisible(Cuenta cuenta, Evento evento) {
        if (cuenta == null || evento == null) {
            return false;
        }
        for (Artista artista : evento.getArtistas()) {
            if (artista.getId() == cuenta.getId()) {
                return true;
            }
        }
        return false;
    }
}
