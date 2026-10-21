package servicio;

import puertos.VinculoCalendario;
import excepciones.AccesoDenegadoException;
import modelo.Artista;
import modelo.DatosRecital;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.Usuario;

import java.io.IOException;

/*
 * Operaciones que modifican el sistema y que piden las vistas.
 *
 * Cada una verifica el permiso del rol y guarda los cambios.
 */
public interface ServicioComandos {

    // Devuelve el usuario creado o null si el nombre de cuenta ya existe.
    Usuario registrarUsuario(String nombreUsuario,
                             String nombre,
                             String apellido,
                             String email,
                             String contrasena,
                             PlanSuscripcion plan);

    // Devuelve el artista creado o null si el nombre de cuenta ya existe.
    Artista registrarArtista(String nombreUsuario,
                             String contrasena,
                             String nombreArtistico,
                             String generoPrincipal,
                             String biografia,
                             boolean verificado);

    RecitalEnVivo crearEvento(DatosRecital datos, Artista artista);

    void iniciarEvento(int idEvento) throws AccesoDenegadoException;

    void pausarEvento(int idEvento) throws AccesoDenegadoException;

    void reanudarEvento(int idEvento) throws AccesoDenegadoException;

    void finalizarEvento(int idEvento) throws AccesoDenegadoException;

    void cancelarEvento(int idEvento) throws AccesoDenegadoException;

    RegistroAcceso solicitarIngreso(int idUsuario, int idEvento);

    // Devuelve el usuario con su nuevo estado, o null si no existe.
    Usuario alternarActivo(int idUsuario);

    // Devuelve el usuario expulsado, o null si no se pudo expulsar.
    Usuario expulsarUsuario(int idEvento, int idUsuario);

    // Suscribe un calendario externo a un evento visible.
    void vincularCalendario(int idEvento, VinculoCalendario vinculo)
            throws IOException;
}
