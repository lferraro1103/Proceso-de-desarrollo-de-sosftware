package gestor;

import puertos.ConsultaTransmision;
import puertos.ProveedorTransmision;
import puertos.Transmision;
import excepciones.AccesoDenegadoException;
import modelo.Cuenta;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.TipoUsuario;

import java.io.IOException;

/*
 * Reglas de las transmisiones: quien puede verlas, que se busca y como se
 * cierran. El gestor verifica el permiso y la visibilidad del evento; esta
 * clase aplica lo propio de la transmision y habla con el servicio externo.
 */
public class ControlTransmision {

    // Servicio que encuentra la transmision de cada evento.
    private final ProveedorTransmision proveedor;

    public ControlTransmision(ProveedorTransmision proveedor) {
        this.proveedor = proveedor;
    }

    /*
     * Verifica que la cuenta pueda ver la transmision del evento y devuelve
     * que buscar: el artista principal (o, si no tiene, el titulo), cuando
     * empezo el evento y el nombre del espectador.
     *
     * Solo hay transmision con el evento en curso, y un usuario la recibe si
     * ya ingreso al evento.
     */
    public ConsultaTransmision consultaPara(Cuenta cuenta, Evento evento)
            throws AccesoDenegadoException {

        if (evento.getEstado() != EstadoEvento.EN_CURSO) {
            throw new AccesoDenegadoException("La transmision solo esta "
                    + "disponible mientras el evento esta en curso. Estado "
                    + "actual: " + evento.getEstado() + ".");
        }

        if (cuenta.getTipoUsuario() == TipoUsuario.USUARIO
                && !estaConectado(evento, cuenta)) {
            throw new AccesoDenegadoException(
                    "Debe ingresar al evento para ver la transmision.");
        }

        return new ConsultaTransmision(
                evento.getArtistas().isEmpty()
                        ? evento.getTitulo()
                        : evento.getArtistas().get(0).getNombreArtistico(),
                evento.getFechaHoraInicio(),
                espectador(cuenta, evento.getId()));
    }

    // Busca la transmision en el servicio externo. Puede tardar.
    public Transmision buscar(ConsultaTransmision consulta) throws IOException {
        return proveedor.buscar(consulta);
    }

    // Cierra la transmision de una cuenta en un evento.
    public void cerrarDe(Cuenta cuenta, int idEvento) {
        proveedor.cerrar(espectador(cuenta, idEvento));
    }

    // Cierra todas las transmisiones de una cuenta.
    public void cerrarDe(Cuenta cuenta) {
        proveedor.cerrar(espectador(cuenta, "*"));
    }

    // Cierra las transmisiones de todos los que miran un evento.
    public void cerrarDeEvento(int idEvento) {
        proveedor.cerrar("*:" + idEvento);
    }

    // Nombre con el que el servicio identifica a quien mira un evento; "*"
    // en lugar del evento abarca todos los de la cuenta.
    private static String espectador(Cuenta cuenta, Object evento) {
        return cuenta.getTipoUsuario() + "" + cuenta.getId() + ":" + evento;
    }

    private static boolean estaConectado(Evento evento, Cuenta cuenta) {
        return evento.getUsuariosConectados().stream()
                .anyMatch(usuario -> usuario.getId() == cuenta.getId());
    }
}
