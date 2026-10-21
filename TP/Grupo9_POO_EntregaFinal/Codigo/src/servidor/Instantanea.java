package servidor;

import api.ArtistaDto;
import api.EstadoDto;
import api.EventoDto;
import api.RegistroDto;
import api.UsuarioDto;
import seguridad.Accion;
import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.Cuenta;
import modelo.Evento;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Arma lo que ve la cuenta que tiene la sesion abierta en el gestor, segun
 * su rol. El filtro se hace aca, en el servidor: el cliente nunca recibe
 * datos que su rol no puede ver.
 *
 *  - Eventos: los visibles para la cuenta (el artista solo los suyos).
 *  - Usuarios: todos para quien puede verlos; para el resto, solo la propia
 *    cuenta y los conectados a los eventos visibles, con nombre real si el
 *    rol puede ver los conectados y sin datos personales si no.
 *  - Artistas: todos para quien puede verlos; para el resto, los de los
 *    eventos visibles y la propia cuenta.
 *  - Registros: todos, solo los propios o ninguno, segun el permiso.
 */
final class Instantanea {

    private Instantanea() {
    }

    static EstadoDto de(GestorEventosEnVivo gestor) {
        Cuenta cuenta = gestor.getCuentaActual();
        List<Evento> eventos = gestor.listarEventosVisibles();

        Map<Integer, UsuarioDto> usuarios = new LinkedHashMap<>();
        Map<Integer, ArtistaDto> artistas = new LinkedHashMap<>();

        if (gestor.puede(Accion.VER_USUARIOS)) {
            for (Usuario usuario : gestor.listarUsuarios()) {
                usuarios.put(usuario.getId(), Conversor.aDto(usuario));
            }
        } else {
            boolean veConectados = gestor.puede(Accion.VER_CONECTADOS);

            if (cuenta.getTipoUsuario() != TipoUsuario.ARTISTA) {
                usuarios.put(cuenta.getId(),
                        Conversor.aDto(gestor.buscarUsuarioPorId(cuenta.getId())));
            }

            for (Evento evento : eventos) {
                for (Usuario usuario : evento.getUsuariosConectados()) {
                    usuarios.putIfAbsent(usuario.getId(), veConectados
                            ? Conversor.aDto(usuario) : Conversor.anonimo(usuario));
                }
            }
        }

        if (gestor.puede(Accion.VER_ARTISTAS)) {
            for (Artista artista : gestor.listarArtistas()) {
                artistas.put(artista.getId(), Conversor.aDto(artista));
            }
        } else {
            if (cuenta.getTipoUsuario() == TipoUsuario.ARTISTA) {
                artistas.put(cuenta.getId(),
                        Conversor.aDto(gestor.buscarArtistaPorId(cuenta.getId())));
            }

            for (Evento evento : eventos) {
                for (Artista artista : evento.getArtistas()) {
                    artistas.putIfAbsent(artista.getId(), Conversor.aDto(artista));
                }
            }
        }

        List<EventoDto> eventosDto = new ArrayList<>();
        for (Evento evento : eventos) {
            eventosDto.add(Conversor.aDto(evento));
        }

        List<RegistroAcceso> registros = new ArrayList<>();
        if (gestor.puede(Accion.VER_REGISTROS)) {
            registros = gestor.listarRegistros();
        } else if (gestor.puede(Accion.VER_MIS_REGISTROS)) {
            registros = gestor.listarMisRegistros();
        }

        List<RegistroDto> registrosDto = new ArrayList<>();
        for (RegistroAcceso registro : registros) {
            registrosDto.add(Conversor.aDto(registro));
        }

        return new EstadoDto(Conversor.aDto(cuenta),
                new ArrayList<>(usuarios.values()),
                new ArrayList<>(artistas.values()),
                eventosDto, registrosDto);
    }
}
