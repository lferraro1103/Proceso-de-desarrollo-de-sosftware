package servidor;

import api.ArtistaDto;
import api.CuentaDto;
import api.EventoDto;
import api.RegistroDto;
import api.UsuarioDto;
import modelo.Artista;
import modelo.Cuenta;
import modelo.Evento;
import modelo.RegistroAcceso;
import modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

/*
 * Convierte los objetos del modelo en los objetos de la API que viajan al
 * cliente. Ningun DTO lleva contrasenas.
 */
final class Conversor {

    private Conversor() {
    }

    static CuentaDto aDto(Cuenta cuenta) {
        return new CuentaDto(cuenta.getId(), cuenta.getNombreUsuario(),
                cuenta.getTipoUsuario().name(), cuenta.isActivo());
    }

    static UsuarioDto aDto(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getNombreUsuario(),
                usuario.getNombre(), usuario.getApellido(), usuario.getEmail(),
                usuario.getPlanSuscripcion().name(), usuario.isActivo(),
                usuario.getTipoUsuario().name());
    }

    /*
     * Usuario sin datos personales, para quien puede ver que hay alguien
     * conectado pero no quien es.
     */
    static UsuarioDto anonimo(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), "", "", "", "",
                usuario.getPlanSuscripcion().name(), usuario.isActivo(),
                usuario.getTipoUsuario().name());
    }

    static ArtistaDto aDto(Artista artista) {
        return new ArtistaDto(artista.getId(), artista.getNombreUsuario(),
                artista.getNombreArtistico(), artista.getGeneroPrincipal(),
                artista.getBiografia(), artista.isVerificado());
    }

    static EventoDto aDto(Evento evento) {
        List<Integer> artistas = new ArrayList<>();
        for (Artista artista : evento.getArtistas()) {
            artistas.add(artista.getId());
        }

        List<Integer> conectados = new ArrayList<>();
        for (Usuario usuario : evento.getUsuariosConectados()) {
            conectados.add(usuario.getId());
        }

        return new EventoDto(evento.getId(), evento.getTitulo(),
                evento.getDescripcion(), evento.getFechaHoraInicio().toString(),
                evento.getFechaHoraFin().toString(), evento.getEstado().name(),
                evento.getCapacidadMaxima(), evento.getPlanMinimoRequerido().name(),
                evento.getUbicacion(), evento.isStreaming(), evento.isExclusivo(),
                artistas, conectados);
    }

    static RegistroDto aDto(RegistroAcceso registro) {
        return new RegistroDto(registro.getUsuario().getId(),
                registro.getEvento().getId(),
                registro.getFechaHoraIngreso().toString(),
                registro.isExitoso(),
                registro.getMotivoRechazo() == null
                        ? "" : registro.getMotivoRechazo());
    }
}
