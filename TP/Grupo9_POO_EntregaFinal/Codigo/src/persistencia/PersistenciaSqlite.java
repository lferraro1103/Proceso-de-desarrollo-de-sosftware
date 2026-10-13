package persistencia;

import gestor.GestorEventosEnVivo;
import gestor.Persistencia;
import modelo.Artista;
import modelo.Evento;
import modelo.RegistroAcceso;
import modelo.Usuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Persistencia en una base SQLite.
 *
 * Reparte el trabajo entre un repositorio por cada tipo de dato. Cada
 * guardado reescribe las tablas completas dentro de una sola transaccion: la
 * base queda siempre consistente (si algo falla se deshace todo) y, con los
 * pocos datos del programa, es mas simple que calcular cambios por fila.
 */
public class PersistenciaSqlite implements Persistencia {

    private final RepositorioUsuarios usuarios = new RepositorioUsuarios();
    private final RepositorioArtistas artistas = new RepositorioArtistas();
    private final RepositorioEventos eventos = new RepositorioEventos();
    private final RepositorioRegistros registros = new RepositorioRegistros();

    @Override
    public void guardar(GestorEventosEnVivo gestor) throws IOException {
        try (Connection conexion = ConexionSqlite.abrir()) {
            conexion.setAutoCommit(false);

            try {
                // Primero se borra lo que depende de otras tablas.
                borrar(conexion, "registros_acceso", "evento_conectados",
                        "evento_artistas", "eventos", "artistas", "usuarios");

                List<Usuario> listaUsuarios = gestor.listarUsuarios();
                List<Evento> listaEventos = gestor.listarEventos();

                usuarios.reemplazarTodos(conexion, listaUsuarios);
                artistas.reemplazarTodos(conexion, gestor.listarArtistas());
                eventos.reemplazarTodos(conexion, listaEventos);
                registros.reemplazarTodos(conexion, gestor.listarRegistros(),
                        listaUsuarios.stream().map(Usuario::getId)
                                .collect(Collectors.toSet()),
                        listaEventos.stream().map(Evento::getId)
                                .collect(Collectors.toSet()));

                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IOException("No se pudo guardar en la base: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public void cargar(GestorEventosEnVivo gestor) throws IOException {
        try (Connection conexion = ConexionSqlite.abrir()) {
            List<Usuario> listaUsuarios = usuarios.cargarTodos(conexion);
            List<Artista> listaArtistas = artistas.cargarTodos(conexion);

            Map<Integer, Usuario> usuariosPorId = new HashMap<>();
            for (Usuario usuario : listaUsuarios) {
                usuariosPorId.put(usuario.getId(), usuario);
            }

            Map<Integer, Artista> artistasPorId = new HashMap<>();
            for (Artista artista : listaArtistas) {
                artistasPorId.put(artista.getId(), artista);
            }

            List<Evento> listaEventos = eventos.cargarTodos(
                    conexion, artistasPorId, usuariosPorId);

            Map<Integer, Evento> eventosPorId = new HashMap<>();
            for (Evento evento : listaEventos) {
                eventosPorId.put(evento.getId(), evento);
            }

            List<RegistroAcceso> listaRegistros = registros.cargarTodos(
                    conexion, usuariosPorId, eventosPorId);

            gestor.reemplazarDatos(listaUsuarios, listaArtistas,
                    listaEventos, listaRegistros);
        } catch (SQLException e) {
            throw new IOException("No se pudo leer la base: "
                    + e.getMessage(), e);
        }
    }

    private static void borrar(Connection conexion, String... tablas)
            throws SQLException {

        try (Statement sentencia = conexion.createStatement()) {
            for (String tabla : tablas) {
                sentencia.executeUpdate("DELETE FROM " + tabla);
            }
        }
    }
}
