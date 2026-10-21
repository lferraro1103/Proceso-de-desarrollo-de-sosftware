import cliente.GestorRemoto;
import com.sun.net.httpserver.HttpServer;
import excepciones.PermisoDenegadoException;
import gestor.*;
import puertos.*;
import seguridad.*;
import servicio.*;
import modelo.*;
import notificacion.NotificadorInactivo;
import persistencia.PersistenciaSqlite;
import servidor.ManejadorApi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.EnumSet;
import java.util.Set;

/** Regresiones ejecutables sin JUnit ni servicios externos: lanzar con java -ea. */
public final class PruebasIteracion4 {
    private static int comprobaciones;
    private static final String HASH = Contrasenas.hashear("prueba-123");

    public static void main(String[] args) throws Exception {
        politicas();
        servicioLocal();
        servicioRemoto();
        persistencia();
        System.out.println("OK: " + comprobaciones + " comprobaciones (Strategy, permisos, HTTP y SQLite).");
    }

    private static Usuario usuario(int id, TipoUsuario rol) {
        return new Usuario(id, "cuenta" + id, "Nombre", "Apellido", "test@local",
                HASH, PlanSuscripcion.PREMIUM, true, rol);
    }

    private static Artista artista(int id) {
        return new Artista(id, "artista" + id, HASH, "Artista " + id, "Rock", "Bio", true);
    }

    private static RecitalEnVivo evento(int id) {
        return new RecitalEnVivo(id, new DatosRecital("Evento " + id, "Descripcion",
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                EstadoEvento.EN_CURSO, 5, PlanSuscripcion.FREE, "Sala", true, false));
    }

    private static void verificar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static void politicas() {
        PoliticasVisibilidad contexto = new PoliticasVisibilidad();
        Artista propio = artista(10);
        RecitalEnVivo recital = evento(100);
        recital.agregarArtista(propio);
        verificar(contexto.esVisible(usuario(1, TipoUsuario.USUARIO), recital), "Usuario ve catalogo");
        verificar(contexto.esVisible(usuario(2, TipoUsuario.ADMINISTRADOR), recital), "Admin ve catalogo");
        verificar(contexto.esVisible(propio, recital), "Artista ve evento propio");
        verificar(contexto.esVisible(artista(10), recital), "Se compara ID, no referencia");
        verificar(!contexto.esVisible(artista(11), recital), "Artista no ve evento ajeno");
        verificar(!contexto.esVisible(propio, evento(101)), "Evento sin artistas no es propio");
        verificar(contexto.esVisible(usuario(10, TipoUsuario.USUARIO), recital), "ID compartido no cambia rol");
        verificar(!contexto.esVisible(null, recital), "Sin cuenta no hay visibilidad");
        verificar(!contexto.esVisible(propio, null), "Sin evento no hay visibilidad");
        verificar(!new VisibilidadGeneral().esVisible(null, recital), "Estrategia general rechaza null");
        verificar(!new VisibilidadArtista().esVisible(propio, null), "Estrategia artista rechaza null");
        // Matriz completa de acciones: la extraccion no cambia las concesiones por rol.
        Permisos permisos = new Permisos();
        Set<Accion> usuario = EnumSet.of(Accion.SOLICITAR_INGRESO, Accion.VER_EVENTOS,
                Accion.VER_CAPACIDAD, Accion.VINCULAR_CALENDARIO, Accion.VER_MIS_REGISTROS,
                Accion.VER_TRANSMISION, Accion.ABRIR_INTERFAZ_GRAFICA);
        Set<Accion> artista = EnumSet.of(Accion.CREAR_EVENTO, Accion.VER_EVENTOS,
                Accion.INICIAR_EVENTO, Accion.PAUSAR_EVENTO, Accion.REANUDAR_EVENTO,
                Accion.FINALIZAR_EVENTO, Accion.CANCELAR_EVENTO, Accion.VER_CONECTADOS,
                Accion.VER_CAPACIDAD, Accion.VINCULAR_CALENDARIO, Accion.VER_TRANSMISION,
                Accion.ABRIR_INTERFAZ_GRAFICA);
        for (Accion accion : Accion.values()) {
            verificar(permisos.permite(TipoUsuario.ADMINISTRADOR, accion)
                    == (accion != Accion.VER_MIS_REGISTROS), "Permisos admin: " + accion);
            verificar(permisos.permite(TipoUsuario.USUARIO, accion) == usuario.contains(accion),
                    "Matriz UML usuario: " + accion);
            verificar(permisos.permite(TipoUsuario.ARTISTA, accion) == artista.contains(accion),
                    "Matriz UML artista: " + accion);
        }
        verificar(!permisos.permite(TipoUsuario.ARTISTA, Accion.SOLICITAR_INGRESO), "Artista no ingresa");
        verificar(!permisos.permite(TipoUsuario.USUARIO, Accion.CREAR_EVENTO), "Usuario no crea");
    }

    private static GestorEventosEnVivo gestor(Persistencia persistencia) {
        GestorEventosEnVivo gestor = new GestorEventosEnVivo(persistencia, new NotificadorInactivo(),
                (cuenta, evento) -> "https://ejemplo.local/calendario/" + evento.getId() + ".ics",
                sinRed());
        // Datos de partida cargados con la misma operacion que usa la persistencia.
        Artista artista10 = artista(10);
        Artista artista11 = artista(11);
        RecitalEnVivo evento100 = evento(100);
        RecitalEnVivo evento101 = evento(101);
        evento100.agregarArtista(artista10);
        evento101.agregarArtista(artista11);
        gestor.reemplazarDatos(
                List.of(usuario(1, TipoUsuario.USUARIO), usuario(2, TipoUsuario.ADMINISTRADOR)),
                List.of(artista10, artista11),
                List.of(evento100, evento101),
                List.of());
        return gestor;
    }

    private static Persistencia sinDisco() {
        return new Persistencia() {
            public void guardar(GestorEventosEnVivo gestor) { }
            public void cargar(GestorEventosEnVivo gestor) { }
        };
    }

    private static ProveedorTransmision sinRed() {
        return new ProveedorTransmision() {
            public Transmision buscar(ConsultaTransmision consulta) {
                return new Transmision(consulta.artista(), "https://ejemplo.local/ver");
            }
            public void cerrar(String espectador) { }
        };
    }

    private static void servicioLocal() throws Exception {
        GestorEventosEnVivo gestor = gestor(sinDisco());
        verificar(gestor.listarEventosVisibles().isEmpty(), "Listado sin sesion vacio");
        gestor.iniciarSesion("artista10", "prueba-123");
        verificar(gestor.listarEventosVisibles().size() == 1, "Artista tiene un evento local");
        verificar(gestor.buscarEventoVisible(101) == null, "Busqueda ajena oculta");
        verificar(gestor.obtenerTransmision(100).titulo().equals("Artista 10"),
                "Merge: artista obtiene transmision propia");
        try {
            gestor.obtenerTransmision(101);
            throw new AssertionError("Transmision ajena debe ocultarse");
        } catch (IllegalArgumentException esperado) {
            verificar(true, "Merge: Strategy impide transmision ajena");
        }
        verificar(gestor.buscarEventoVisiblePara(artista(11), 101) != null, "Calendario usa cuenta indicada");
        verificar(gestor.buscarEventoVisiblePara(artista(11), 100) == null, "Calendario ajeno oculto");
        verificar(gestor.buscarEventoVisiblePara(null, 100) == null, "Calendario sin cuenta oculto");
        gestor.pausarEvento(100);
        verificar(gestor.buscarEventoVisible(100).getEstado() == EstadoEvento.PAUSADO,
                "Artista administra evento propio mediante la misma politica");
        gestor.reanudarEvento(100);
        try {
            gestor.pausarEvento(101);
            throw new AssertionError("Artista no debe administrar evento ajeno");
        } catch (PermisoDenegadoException esperado) {
            verificar(gestor.buscarEventoPorId(101).getEstado() == EstadoEvento.EN_CURSO,
                    "Evento ajeno no fue modificado");
        }
        String[] feed = { null };
        gestor.vincularCalendario(100, vinculo(feed));
        verificar(feed[0].endsWith("/100.ics"), "Strategy calendario recibe URL del evento");
        feed[0] = null;
        try {
            gestor.vincularCalendario(101, vinculo(feed));
            throw new AssertionError("Calendario ajeno debe fallar");
        } catch (IllegalArgumentException esperado) {
            verificar(feed[0] == null, "No invoca Strategy para evento invisible");
        }
        try {
            gestor.vincularCalendario(100, new VinculoCalendario() {
                public String getNombre() { return "Falla controlada"; }
                public void vincular(String url) throws IOException { throw new IOException("controlada"); }
            });
            throw new AssertionError("IOException debe propagarse");
        } catch (IOException esperado) {
            verificar("controlada".equals(esperado.getMessage()), "Propaga fallo del proveedor");
        }
        gestor.cerrarSesion();
        gestor.iniciarSesion("cuenta1", "prueba-123");
        verificar(gestor.listarEventosVisibles().size() == 2, "Usuario ve ambos eventos");
        verificar(gestor.solicitarIngreso(1, 100).isExitoso(), "Ingreso valido");
        verificar(gestor.obtenerTransmision(100).url().endsWith("/ver"),
                "Merge: usuario conectado obtiene transmision");
        verificar(!gestor.solicitarIngreso(1, 100).isExitoso(), "Ingreso duplicado rechazado");
        try {
            gestor.solicitarIngreso(2, 100);
            throw new AssertionError("Suplantacion debe rechazarse");
        } catch (PermisoDenegadoException esperado) {
            verificar(true, "No permite ingresar a nombre ajeno");
        }
        gestor.cerrarSesion();
        try {
            gestor.vincularCalendario(100, vinculo(feed));
            throw new AssertionError("Calendario sin sesion debe rechazarse");
        } catch (PermisoDenegadoException esperado) {
            verificar(true, "Permiso comprobado antes de Strategy");
        }
    }

    private static VinculoCalendario vinculo(String[] feed) {
        return new VinculoCalendario() {
            public String getNombre() { return "Proveedor de prueba"; }
            public void vincular(String url) { feed[0] = url; }
        };
    }

    private static void servicioRemoto() throws Exception {
        GestorEventosEnVivo local = gestor(sinDisco());
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/api/", new ManejadorApi(local, sinRed()));
        servidor.start();
        try {
            String url = "http://127.0.0.1:" + servidor.getAddress().getPort();
            GestorRemoto artista = new GestorRemoto(url);
            artista.iniciarSesion("artista10", "prueba-123");
            verificar(artista.listarEventosVisibles().size() == 1, "HTTP filtra eventos del artista");
            verificar(artista.buscarEventoVisible(101) == null, "HTTP no filtra solo en UI");
            artista.pausarEvento(100);
            verificar(artista.buscarEventoVisible(100).getEstado() == EstadoEvento.PAUSADO,
                    "HTTP permite administrar evento propio");
            artista.reanudarEvento(100);
            try {
                artista.pausarEvento(101);
                throw new AssertionError("HTTP no debe modificar evento ajeno");
            } catch (PermisoDenegadoException esperado) {
                verificar(local.buscarEventoPorId(101).getEstado() == EstadoEvento.EN_CURSO,
                        "HTTP rechaza administracion ajena en el servidor");
            }
            String[] feed = { null };
            artista.vincularCalendario(100, vinculo(feed));
            verificar(feed[0].endsWith("/100.ics"), "Proxy conserva Strategy del calendario");
            try {
                artista.vincularCalendario(101, vinculo(feed));
                throw new AssertionError("HTTP no debe publicar calendario ajeno");
            } catch (IllegalArgumentException esperado) {
                verificar(true, "Servidor deniega calendario ajeno");
            }
            verificar(local.getCuentaActual() == null, "HTTP restaura sesion anterior");
            artista.cerrarSesion();
            GestorRemoto usuario = new GestorRemoto(url);
            usuario.iniciarSesion("cuenta1", "prueba-123");
            verificar(usuario.listarEventosVisibles().size() == 2, "Usuario HTTP ve catalogo");
            verificar(usuario.solicitarIngreso(1, 100).isExitoso(), "Ingreso HTTP conserva negocio");
            verificar(usuario.obtenerTransmision(100).url().endsWith("/ver"),
                    "Merge: contrato de transmision funciona mediante HTTP");
            verificar(usuario.listarMisRegistros().size() == 1, "HTTP devuelve registro propio");
            usuario.cerrarSesion();
            GestorRemoto admin = new GestorRemoto(url);
            admin.iniciarSesion("cuenta2", "prueba-123");
            verificar(admin.listarEventosVisibles().size() == 2, "Admin HTTP ve catalogo");
            verificar(admin.listarRegistros().size() == 1, "Admin HTTP ve registros");
            admin.cerrarSesion();
        } finally {
            servidor.stop(0);
        }
    }

    private static void persistencia() throws Exception {
        Path carpeta = Files.createTempDirectory("uadebeats-iteracion4-");
        Path base = carpeta.resolve("prueba.db");
        String anterior = System.getProperty("uadebeats.db");
        try {
            System.setProperty("uadebeats.db", base.toString());
            GestorEventosEnVivo original = gestor(new PersistenciaSqlite());
            original.guardarDatos();
            GestorEventosEnVivo cargado = new GestorEventosEnVivo(new PersistenciaSqlite(),
                    new NotificadorInactivo(), (cuenta, evento) -> "https://ejemplo.local", sinRed());
            cargado.cargarDatos();
            cargado.iniciarSesion("artista10", "prueba-123");
            List<Evento> visibles = cargado.listarEventosVisibles();
            verificar(visibles.size() == 1 && visibles.get(0).getId() == 100,
                    "Strategy conserva asociaciones tras guardar y cargar SQLite");
            verificar(cargado.buscarEventoVisible(101) == null, "SQLite conserva aislamiento del artista");
        } finally {
            if (anterior == null) System.clearProperty("uadebeats.db");
            else System.setProperty("uadebeats.db", anterior);
            Files.deleteIfExists(base);
            Files.deleteIfExists(carpeta);
        }
    }
}
