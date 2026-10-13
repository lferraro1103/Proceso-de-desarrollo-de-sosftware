package gestor;

import modelo.Artista;
import modelo.Evento;
import modelo.RegistroAcceso;
import modelo.Usuario;

import java.util.List;

/*
 * Consultas de datos que necesitan las vistas.
 *
 * Solo lee: las modificaciones estan en ServicioComandos.
 */
public interface ServicioConsultas {

    List<Usuario> listarUsuarios();

    List<Artista> listarArtistas();

    // Eventos que la cuenta actual puede ver.
    List<Evento> listarEventosVisibles();

    // Todos los registros de acceso (solo administrador).
    List<RegistroAcceso> listarRegistros();

    // Registros de acceso de la cuenta actual.
    List<RegistroAcceso> listarMisRegistros();

    // Evento de ese ID si la cuenta actual puede verlo, o null.
    Evento buscarEventoVisible(int id);

    Usuario buscarUsuarioPorId(int id);

    Artista buscarArtistaPorId(int id);

    boolean existeNombreUsuario(String nombreUsuario);

    /*
     * Trae los datos mas recientes. Cuando el gestor es local los datos ya
     * estan al dia y no hace nada; cuando es remoto vuelve a pedirlos al
     * servidor.
     */
    void actualizar();
}
