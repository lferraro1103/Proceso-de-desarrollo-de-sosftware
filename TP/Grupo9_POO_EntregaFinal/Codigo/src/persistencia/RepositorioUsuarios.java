package persistencia;

import modelo.PlanSuscripcion;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 * Repositorio de usuarios (incluye al administrador): traduce entre la
 * tabla usuarios y los objetos Usuario.
 */
final class RepositorioUsuarios {

    void reemplazarTodos(Connection conexion, List<Usuario> usuarios)
            throws SQLException {

        try (PreparedStatement insertar = conexion.prepareStatement(
                "INSERT INTO usuarios (id, nombre_usuario, nombre, apellido, "
                        + "email, contrasena, plan, activo, tipo) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            for (Usuario usuario : usuarios) {
                insertar.setInt(1, usuario.getId());
                insertar.setString(2, usuario.getNombreUsuario());
                insertar.setString(3, usuario.getNombre());
                insertar.setString(4, usuario.getApellido());
                insertar.setString(5, usuario.getEmail());
                insertar.setString(6, usuario.getContrasena());
                insertar.setString(7, usuario.getPlanSuscripcion().name());
                insertar.setInt(8, usuario.isActivo() ? 1 : 0);
                insertar.setString(9, usuario.getTipoUsuario().name());
                insertar.executeUpdate();
            }
        }
    }

    List<Usuario> cargarTodos(Connection conexion) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(
                     "SELECT id, nombre_usuario, nombre, apellido, email, "
                             + "contrasena, plan, activo, tipo "
                             + "FROM usuarios ORDER BY id")) {

            while (filas.next()) {
                usuarios.add(new Usuario(
                        filas.getInt("id"),
                        filas.getString("nombre_usuario"),
                        filas.getString("nombre"),
                        filas.getString("apellido"),
                        filas.getString("email"),
                        filas.getString("contrasena"),
                        PlanSuscripcion.valueOf(filas.getString("plan")),
                        filas.getInt("activo") == 1,
                        TipoUsuario.valueOf(filas.getString("tipo"))));
            }
        }

        return usuarios;
    }
}
