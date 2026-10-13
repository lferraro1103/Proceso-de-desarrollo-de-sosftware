package cliente;

import api.ArtistaDto;
import api.EstadoDto;
import api.EventoDto;
import api.RegistroDto;
import api.UsuarioDto;
import modelo.Artista;
import modelo.Cuenta;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Copia de solo lectura de lo que el servidor deja ver a la cuenta: arma los
 * objetos del modelo a partir de la respuesta de la API.
 *
 * Los objetos no llevan contrasenas (el servidor nunca las envia), por lo
 * que las cuentas reconstruidas no pueden iniciar sesion por si mismas.
 */
class EstadoLocal {

    private final Cuenta cuenta;
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Artista> artistas = new ArrayList<>();
    private final List<Evento> eventos = new ArrayList<>();
    private final List<RegistroAcceso> registros = new ArrayList<>();

    EstadoLocal(EstadoDto estado) {
        Map<Integer, Usuario> usuariosPorId = new HashMap<>();
        for (UsuarioDto dto : estado.usuarios()) {
            Usuario usuario = new Usuario(dto.id(), dto.nombreUsuario(),
                    dto.nombre(), dto.apellido(), dto.email(), "",
                    PlanSuscripcion.valueOf(dto.plan()), dto.activo(),
                    TipoUsuario.valueOf(dto.tipo()));
            usuarios.add(usuario);
            usuariosPorId.put(usuario.getId(), usuario);
        }

        Map<Integer, Artista> artistasPorId = new HashMap<>();
        for (ArtistaDto dto : estado.artistas()) {
            Artista artista = new Artista(dto.id(), dto.nombreUsuario(), "",
                    dto.nombreArtistico(), dto.generoPrincipal(),
                    dto.biografia(), dto.verificado());
            artistas.add(artista);
            artistasPorId.put(artista.getId(), artista);
        }

        Map<Integer, Evento> eventosPorId = new HashMap<>();
        for (EventoDto dto : estado.eventos()) {
            RecitalEnVivo recital = new RecitalEnVivo(dto.id(), new DatosRecital(
                    dto.titulo(), dto.descripcion(),
                    LocalDateTime.parse(dto.inicio()), LocalDateTime.parse(dto.fin()),
                    EstadoEvento.valueOf(dto.estado()), dto.capacidadMaxima(),
                    PlanSuscripcion.valueOf(dto.planMinimo()), dto.ubicacion(),
                    dto.streaming(), dto.exclusivo()));

            for (int id : dto.artistas()) {
                recital.agregarArtista(artistasPorId.get(id));
            }

            for (int id : dto.conectados()) {
                recital.restaurarConectado(usuariosPorId.get(id));
            }

            eventos.add(recital);
            eventosPorId.put(recital.getId(), recital);
        }

        for (RegistroDto dto : estado.registros()) {
            registros.add(construirRegistro(dto, usuariosPorId, eventosPorId));
        }

        cuenta = estado.cuenta().tipo().equals(TipoUsuario.ARTISTA.name())
                ? artistasPorId.get(estado.cuenta().id())
                : usuariosPorId.get(estado.cuenta().id());
    }

    /*
     * Registro a partir de su DTO. Si el usuario o el evento no estan en la
     * copia (un intento con un ID inexistente), se usan los "placeholder"
     * del modelo, igual que hace el gestor local.
     */
    RegistroAcceso construirRegistro(RegistroDto dto,
                                     Map<Integer, Usuario> usuariosPorId,
                                     Map<Integer, Evento> eventosPorId) {

        Usuario usuario = usuariosPorId.get(dto.idUsuario());
        Evento evento = eventosPorId.get(dto.idEvento());

        return RegistroAcceso.reconstruirRegistro(
                usuario != null ? usuario : Usuario.desconocido(),
                evento != null ? evento : RecitalEnVivo.desconocido(),
                LocalDateTime.parse(dto.fechaHoraIngreso()),
                dto.exitoso(),
                dto.motivoRechazo());
    }

    // Registro de un solo intento, buscando el usuario y el evento en la copia.
    RegistroAcceso construirRegistro(RegistroDto dto) {
        Map<Integer, Usuario> usuariosPorId = new HashMap<>();
        for (Usuario usuario : usuarios) {
            usuariosPorId.put(usuario.getId(), usuario);
        }

        Map<Integer, Evento> eventosPorId = new HashMap<>();
        for (Evento evento : eventos) {
            eventosPorId.put(evento.getId(), evento);
        }

        return construirRegistro(dto, usuariosPorId, eventosPorId);
    }

    Cuenta getCuenta() {
        return cuenta;
    }

    List<Usuario> getUsuarios() {
        return new ArrayList<>(usuarios);
    }

    List<Artista> getArtistas() {
        return new ArrayList<>(artistas);
    }

    List<Evento> getEventos() {
        return new ArrayList<>(eventos);
    }

    List<RegistroAcceso> getRegistros() {
        return new ArrayList<>(registros);
    }
}
