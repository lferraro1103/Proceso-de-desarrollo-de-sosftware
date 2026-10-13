package persistencia;

import modelo.Artista;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Repositorio de eventos: traduce entre las tablas eventos,
 * evento_artistas y evento_conectados y los objetos RecitalEnVivo.
 */
final class RepositorioEventos {

    void reemplazarTodos(Connection conexion, List<Evento> eventos)
            throws SQLException {

        try (PreparedStatement insertar = conexion.prepareStatement(
                "INSERT INTO eventos (id, titulo, descripcion, "
                        + "fecha_hora_inicio, fecha_hora_fin, estado, "
                        + "capacidad_maxima, plan_minimo, ubicacion, "
                        + "streaming, exclusivo) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
             PreparedStatement artista = conexion.prepareStatement(
                     "INSERT INTO evento_artistas (evento_id, artista_id) "
                             + "VALUES (?, ?)");
             PreparedStatement conectado = conexion.prepareStatement(
                     "INSERT INTO evento_conectados (evento_id, usuario_id) "
                             + "VALUES (?, ?)")) {

            for (Evento evento : eventos) {
                insertar.setInt(1, evento.getId());
                insertar.setString(2, evento.getTitulo());
                insertar.setString(3, evento.getDescripcion());
                insertar.setString(4, evento.getFechaHoraInicio().toString());
                insertar.setString(5, evento.getFechaHoraFin().toString());
                insertar.setString(6, evento.getEstado().name());
                insertar.setInt(7, evento.getCapacidadMaxima());
                insertar.setString(8, evento.getPlanMinimoRequerido().name());
                insertar.setString(9, evento.getUbicacion());
                insertar.setInt(10, evento.isStreaming() ? 1 : 0);
                insertar.setInt(11, evento.isExclusivo() ? 1 : 0);
                insertar.executeUpdate();

                for (Artista a : evento.getArtistas()) {
                    artista.setInt(1, evento.getId());
                    artista.setInt(2, a.getId());
                    artista.executeUpdate();
                }

                for (Usuario u : evento.getUsuariosConectados()) {
                    conectado.setInt(1, evento.getId());
                    conectado.setInt(2, u.getId());
                    conectado.executeUpdate();
                }
            }
        }
    }

    // Reconstruye los eventos uniendolos con los artistas y usuarios ya cargados.
    List<Evento> cargarTodos(Connection conexion,
                             Map<Integer, Artista> artistas,
                             Map<Integer, Usuario> usuarios)
            throws SQLException {

        List<Evento> eventos = new ArrayList<>();

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(
                     "SELECT id, titulo, descripcion, fecha_hora_inicio, "
                             + "fecha_hora_fin, estado, capacidad_maxima, "
                             + "plan_minimo, ubicacion, streaming, exclusivo "
                             + "FROM eventos ORDER BY id")) {

            while (filas.next()) {
                DatosRecital datos = new DatosRecital(
                        filas.getString("titulo"),
                        filas.getString("descripcion"),
                        LocalDateTime.parse(filas.getString("fecha_hora_inicio")),
                        LocalDateTime.parse(filas.getString("fecha_hora_fin")),
                        EstadoEvento.valueOf(filas.getString("estado")),
                        filas.getInt("capacidad_maxima"),
                        PlanSuscripcion.valueOf(filas.getString("plan_minimo")),
                        filas.getString("ubicacion"),
                        filas.getInt("streaming") == 1,
                        filas.getInt("exclusivo") == 1);

                eventos.add(new RecitalEnVivo(filas.getInt("id"), datos));
            }
        }

        Map<Integer, RecitalEnVivo> porId = new java.util.HashMap<>();
        for (Evento evento : eventos) {
            porId.put(evento.getId(), (RecitalEnVivo) evento);
        }

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(
                     "SELECT evento_id, artista_id FROM evento_artistas "
                             + "ORDER BY rowid")) {

            while (filas.next()) {
                Artista artista = artistas.get(filas.getInt("artista_id"));

                if (artista != null) {
                    porId.get(filas.getInt("evento_id")).agregarArtista(artista);
                }
            }
        }

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(
                     "SELECT evento_id, usuario_id FROM evento_conectados")) {

            while (filas.next()) {
                Usuario usuario = usuarios.get(filas.getInt("usuario_id"));

                if (usuario != null) {
                    porId.get(filas.getInt("evento_id")).restaurarConectado(usuario);
                }
            }
        }

        return eventos;
    }
}
