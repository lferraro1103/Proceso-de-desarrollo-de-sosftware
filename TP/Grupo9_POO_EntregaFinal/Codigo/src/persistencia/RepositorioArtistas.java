package persistencia;

import modelo.Artista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 * Repositorio de artistas: traduce entre la tabla artistas y los objetos
 * Artista.
 */
final class RepositorioArtistas {

    void reemplazarTodos(Connection conexion, List<Artista> artistas)
            throws SQLException {

        try (PreparedStatement insertar = conexion.prepareStatement(
                "INSERT INTO artistas (id, nombre_usuario, contrasena_hash, "
                        + "nombre_artistico, genero_principal, biografia, "
                        + "verificado) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            for (Artista artista : artistas) {
                insertar.setInt(1, artista.getId());
                insertar.setString(2, artista.getNombreUsuario());
                insertar.setString(3, artista.getHashContrasena());
                insertar.setString(4, artista.getNombreArtistico());
                insertar.setString(5, artista.getGeneroPrincipal());
                insertar.setString(6, artista.getBiografia());
                insertar.setInt(7, artista.isVerificado() ? 1 : 0);
                insertar.executeUpdate();
            }
        }
    }

    List<Artista> cargarTodos(Connection conexion) throws SQLException {
        List<Artista> artistas = new ArrayList<>();

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(
                     "SELECT id, nombre_usuario, contrasena_hash, nombre_artistico, "
                             + "genero_principal, biografia, verificado "
                             + "FROM artistas ORDER BY id")) {

            while (filas.next()) {
                artistas.add(new Artista(
                        filas.getInt("id"),
                        filas.getString("nombre_usuario"),
                        filas.getString("contrasena_hash"),
                        filas.getString("nombre_artistico"),
                        filas.getString("genero_principal"),
                        filas.getString("biografia"),
                        filas.getInt("verificado") == 1));
            }
        }

        return artistas;
    }
}
