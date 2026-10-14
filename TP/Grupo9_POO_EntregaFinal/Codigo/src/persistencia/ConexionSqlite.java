package persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Abre la base SQLite y crea las tablas si todavia no existen.
 *
 * La base es un unico archivo. Por defecto queda en
 * ~/.uadebeats/uadebeats.db y se cambia con la propiedad uadebeats.db.
 */
final class ConexionSqlite {

    private static final String[] TABLAS = {
        "CREATE TABLE IF NOT EXISTS usuarios ("
            + "id INTEGER PRIMARY KEY,"
            + "nombre_usuario TEXT NOT NULL UNIQUE COLLATE NOCASE,"
            + "nombre TEXT NOT NULL,"
            + "apellido TEXT NOT NULL,"
            + "email TEXT NOT NULL,"
            + "contrasena_hash TEXT NOT NULL,"
            + "plan TEXT NOT NULL,"
            + "activo INTEGER NOT NULL,"
            + "tipo TEXT NOT NULL)",

        "CREATE TABLE IF NOT EXISTS artistas ("
            + "id INTEGER PRIMARY KEY,"
            + "nombre_usuario TEXT NOT NULL UNIQUE COLLATE NOCASE,"
            + "contrasena_hash TEXT NOT NULL,"
            + "nombre_artistico TEXT NOT NULL,"
            + "genero_principal TEXT NOT NULL,"
            + "biografia TEXT NOT NULL,"
            + "verificado INTEGER NOT NULL)",

        "CREATE TABLE IF NOT EXISTS eventos ("
            + "id INTEGER PRIMARY KEY,"
            + "titulo TEXT NOT NULL,"
            + "descripcion TEXT NOT NULL,"
            + "fecha_hora_inicio TEXT NOT NULL,"
            + "fecha_hora_fin TEXT NOT NULL,"
            + "estado TEXT NOT NULL,"
            + "capacidad_maxima INTEGER NOT NULL,"
            + "plan_minimo TEXT NOT NULL,"
            + "ubicacion TEXT NOT NULL,"
            + "streaming INTEGER NOT NULL,"
            + "exclusivo INTEGER NOT NULL)",

        "CREATE TABLE IF NOT EXISTS evento_artistas ("
            + "evento_id INTEGER NOT NULL REFERENCES eventos(id),"
            + "artista_id INTEGER NOT NULL REFERENCES artistas(id),"
            + "PRIMARY KEY (evento_id, artista_id))",

        "CREATE TABLE IF NOT EXISTS evento_conectados ("
            + "evento_id INTEGER NOT NULL REFERENCES eventos(id),"
            + "usuario_id INTEGER NOT NULL REFERENCES usuarios(id),"
            + "PRIMARY KEY (evento_id, usuario_id))",

        "CREATE TABLE IF NOT EXISTS registros_acceso ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "usuario_id INTEGER NOT NULL REFERENCES usuarios(id),"
            + "evento_id INTEGER NOT NULL REFERENCES eventos(id),"
            + "fecha_hora_ingreso TEXT NOT NULL,"
            + "exitoso INTEGER NOT NULL,"
            + "motivo_rechazo TEXT NOT NULL)"
    };

    private ConexionSqlite() {
    }

    // Abre una conexion nueva con las claves foraneas activadas.
    static Connection abrir() throws IOException {
        Path archivo = Paths.get(System.getProperty("uadebeats.db",
                Paths.get(System.getProperty("user.home"), ".uadebeats",
                        "uadebeats.db").toString()));

        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }

            Connection conexion = DriverManager.getConnection(
                    "jdbc:sqlite:" + archivo);

            try (Statement sentencia = conexion.createStatement()) {
                sentencia.execute("PRAGMA foreign_keys = ON");

                for (String tabla : TABLAS) {
                    sentencia.execute(tabla);
                }
            }

            return conexion;
        } catch (SQLException e) {
            throw new IOException("No se pudo abrir la base de datos "
                    + archivo + ": " + e.getMessage(), e);
        }
    }
}
