package persistencia;

import modelo.Evento;
import modelo.RegistroAcceso;
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
import java.util.Set;

/*
 * Repositorio de registros de acceso: traduce entre la tabla
 * registros_acceso y los objetos RegistroAcceso.
 */
final class RepositorioRegistros {

    /*
     * Un intento con un usuario o un evento inexistente se rechaza pero no se
     * guarda: no hay fila a la que asociarlo (clave foranea).
     */
    void reemplazarTodos(Connection conexion,
                         List<RegistroAcceso> registros,
                         Set<Integer> idsUsuarios,
                         Set<Integer> idsEventos)
            throws SQLException {

        try (PreparedStatement insertar = conexion.prepareStatement(
                "INSERT INTO registros_acceso (usuario_id, evento_id, "
                        + "fecha_hora_ingreso, exitoso, motivo_rechazo) "
                        + "VALUES (?, ?, ?, ?, ?)")) {

            for (RegistroAcceso registro : registros) {
                if (!idsUsuarios.contains(registro.getUsuario().getId())
                        || !idsEventos.contains(registro.getEvento().getId())) {
                    continue;
                }

                insertar.setInt(1, registro.getUsuario().getId());
                insertar.setInt(2, registro.getEvento().getId());
                insertar.setString(3, registro.getFechaHoraIngreso().toString());
                insertar.setInt(4, registro.isExitoso() ? 1 : 0);
                insertar.setString(5, registro.getMotivoRechazo() == null
                        ? "" : registro.getMotivoRechazo());
                insertar.executeUpdate();
            }
        }
    }

    // El orden de insercion (id) es el orden cronologico en que se guardaron.
    List<RegistroAcceso> cargarTodos(Connection conexion,
                                     Map<Integer, Usuario> usuarios,
                                     Map<Integer, Evento> eventos)
            throws SQLException {

        List<RegistroAcceso> registros = new ArrayList<>();

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(
                     "SELECT usuario_id, evento_id, fecha_hora_ingreso, "
                             + "exitoso, motivo_rechazo "
                             + "FROM registros_acceso ORDER BY id")) {

            while (filas.next()) {
                registros.add(RegistroAcceso.reconstruirRegistro(
                        usuarios.get(filas.getInt("usuario_id")),
                        eventos.get(filas.getInt("evento_id")),
                        LocalDateTime.parse(filas.getString("fecha_hora_ingreso")),
                        filas.getInt("exitoso") == 1,
                        filas.getString("motivo_rechazo")));
            }
        }

        return registros;
    }
}
