package gestor;

import modelo.Cuenta;
import modelo.Evento;
import modelo.TipoUsuario;

import java.util.Map;

/**
 * Contexto Strategy: selecciona un algoritmo por rol sin guardar estado de sesion.
 * Compartido por listados, consultas individuales y calendarios a traves del gestor.
 * La autorizacion de acciones sigue siendo responsabilidad de ControlSesion/Permisos.
 */
public final class PoliticasVisibilidad {
    private final Map<TipoUsuario, PoliticaVisibilidadEvento> porRol = Map.of(
            TipoUsuario.USUARIO, new VisibilidadGeneral(),
            TipoUsuario.ADMINISTRADOR, new VisibilidadGeneral(),
            TipoUsuario.ARTISTA, new VisibilidadArtista());

    public boolean esVisible(Cuenta cuenta, Evento evento) {
        if (cuenta == null || evento == null) {
            return false;
        }
        PoliticaVisibilidadEvento politica = porRol.get(cuenta.getTipoUsuario());
        return politica != null && politica.esVisible(cuenta, evento);
    }
}
