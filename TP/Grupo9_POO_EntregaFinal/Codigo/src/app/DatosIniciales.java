package app;

import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.Contrasenas;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Datos de prueba con los que arranca el programa la primera vez.
 *
 * Se cargan solo si la base esta vacia. Incluyen usuarios de cada plan, uno
 * inactivo, un administrador, artistas, eventos en todos los estados y
 * registros de acceso coherentes con los usuarios conectados, de modo que
 * se pueden probar todos los casos sin cargar nada a mano.
 */
public final class DatosIniciales {

    private DatosIniciales() {
    }

    // Carga los datos de prueba en el gestor y los guarda en la base.
    public static void sembrar(GestorEventosEnVivo gestor) {
        List<Usuario> usuarios = new ArrayList<>();
        List<Artista> artistas = new ArrayList<>();
        List<RecitalEnVivo> eventos = new ArrayList<>();
        List<RegistroAcceso> registros = new ArrayList<>();

        // Usuarios: lgabian a sdiaz tienen distintos planes; tgomez esta inactivo.
        usuarios.add(new Usuario(1, "lgabian", "Lucia", "Gabian",
                "lucia.gabian@uadebeats.com", Contrasenas.hashear("lgabian2024"),
                PlanSuscripcion.PREMIUM, true, TipoUsuario.USUARIO));
        usuarios.add(new Usuario(2, "lferraro", "Leandro", "Ferraro",
                "leandro.ferraro@uadebeats.com", Contrasenas.hashear("lferraro2024"),
                PlanSuscripcion.ARTIST_PASS, true, TipoUsuario.USUARIO));
        usuarios.add(new Usuario(3, "ncaggia", "Nicolas", "Caggia",
                "nicolas.caggia@uadebeats.com", Contrasenas.hashear("ncaggia2024"),
                PlanSuscripcion.FREE, true, TipoUsuario.USUARIO));
        usuarios.add(new Usuario(4, "mrossi", "Martina", "Rossi",
                "martina.rossi@uadebeats.com", Contrasenas.hashear("mrossi2024"),
                PlanSuscripcion.PREMIUM, true, TipoUsuario.USUARIO));
        usuarios.add(new Usuario(5, "jperez", "Juan", "Perez",
                "juan.perez@uadebeats.com", Contrasenas.hashear("jperez2024"),
                PlanSuscripcion.FREE, true, TipoUsuario.USUARIO));
        usuarios.add(new Usuario(6, "sdiaz", "Sofia", "Diaz",
                "sofia.diaz@uadebeats.com", Contrasenas.hashear("sdiaz2024"),
                PlanSuscripcion.ARTIST_PASS, true, TipoUsuario.USUARIO));
        usuarios.add(new Usuario(7, "tgomez", "Tomas", "Gomez",
                "tomas.gomez@uadebeats.com", Contrasenas.hashear("tgomez2024"),
                PlanSuscripcion.FREE, false, TipoUsuario.USUARIO));
        usuarios.add(new Usuario(8, "admin", "Administrador", "UADE Beats",
                "admin@uadebeats.com", Contrasenas.hashear("admin123"),
                PlanSuscripcion.PREMIUM, true, TipoUsuario.ADMINISTRADOR));

        artistas.add(new Artista(1, "bandahorizonte", Contrasenas.hashear("horizonte2024"),
                "Banda Horizonte", "Rock",
                "Banda argentina de rock alternativo.", true));
        artistas.add(new Artista(2, "djprisma", Contrasenas.hashear("prisma2024"),
                "DJ Prisma", "Electronica",
                "Productora de shows audiovisuales.", true));
        artistas.add(new Artista(3, "lunanocturna", Contrasenas.hashear("luna2024"),
                "Luna Nocturna", "Pop",
                "Cantautora de pop indie con tres discos editados.", false));
        artistas.add(new Artista(4, "losdelsur", Contrasenas.hashear("sur2024"),
                "Los del Sur", "Folklore",
                "Conjunto de folklore con giras por todo el pais.", true));

        RecitalEnVivo evento100 = new RecitalEnVivo(100, new DatosRecital(
                "Rock en Vivo",
                "Recital exclusivo online",
                LocalDateTime.of(2026, 10, 10, 20, 0),
                LocalDateTime.of(2026, 10, 10, 23, 0),
                EstadoEvento.EN_CURSO, 5, PlanSuscripcion.PREMIUM,
                "Buenos Aires", true, true));
        evento100.agregarArtista(artistas.get(0));
        evento100.restaurarConectado(usuarios.get(0));
        evento100.restaurarConectado(usuarios.get(3));
        evento100.restaurarConectado(usuarios.get(1));
        eventos.add(evento100);

        RecitalEnVivo evento101 = new RecitalEnVivo(101, new DatosRecital(
                "Backstage Prisma Live",
                "Presentacion en vivo para usuarios Artist Pass",
                LocalDateTime.of(2026, 10, 20, 21, 0),
                LocalDateTime.of(2026, 10, 20, 23, 30),
                EstadoEvento.EN_ESPERA, 2, PlanSuscripcion.ARTIST_PASS,
                "Streaming", true, true));
        evento101.agregarArtista(artistas.get(1));
        eventos.add(evento101);

        RecitalEnVivo evento102 = new RecitalEnVivo(102, new DatosRecital(
                "Noche de Folklore",
                "Pena con el conjunto Los del Sur",
                LocalDateTime.of(2026, 10, 11, 19, 0),
                LocalDateTime.of(2026, 10, 11, 22, 30),
                EstadoEvento.PAUSADO, 20, PlanSuscripcion.FREE,
                "Sala Principal", false, false));
        evento102.agregarArtista(artistas.get(3));
        evento102.restaurarConectado(usuarios.get(2));
        eventos.add(evento102);

        RecitalEnVivo evento103 = new RecitalEnVivo(103, new DatosRecital(
                "Luna en Vivo (Acustico)",
                "Show acustico de Luna Nocturna",
                LocalDateTime.of(2026, 11, 5, 20, 30),
                LocalDateTime.of(2026, 11, 5, 22, 30),
                EstadoEvento.PROGRAMADO, 30, PlanSuscripcion.FREE,
                "Teatro UADE", true, false));
        evento103.agregarArtista(artistas.get(2));
        eventos.add(evento103);

        RecitalEnVivo evento104 = new RecitalEnVivo(104, new DatosRecital(
                "Festival de Verano 2025",
                "Evento de la temporada pasada",
                LocalDateTime.of(2025, 12, 20, 18, 0),
                LocalDateTime.of(2025, 12, 20, 23, 59),
                EstadoEvento.FINALIZADO, 100, PlanSuscripcion.FREE,
                "Costanera", true, false));
        evento104.agregarArtista(artistas.get(0));
        evento104.agregarArtista(artistas.get(1));
        eventos.add(evento104);

        RecitalEnVivo evento105 = new RecitalEnVivo(105, new DatosRecital(
                "Fiesta Electronica Cancelada",
                "Cancelado por clima",
                LocalDateTime.of(2026, 9, 30, 22, 0),
                LocalDateTime.of(2026, 10, 1, 4, 0),
                EstadoEvento.CANCELADO, 50, PlanSuscripcion.PREMIUM,
                "Club Prisma", false, true));
        evento105.agregarArtista(artistas.get(1));
        eventos.add(evento105);

        // Registros de acceso: ingresos autorizados, rechazos y expulsiones.
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(0), evento100, LocalDateTime.of(2026, 10, 10, 20, 5),
                true, ""));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(3), evento100, LocalDateTime.of(2026, 10, 10, 20, 11),
                true, ""));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(2), evento100, LocalDateTime.of(2026, 10, 10, 20, 15),
                false, "Plan insuficiente. Requiere PREMIUM y el usuario posee FREE."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(4), evento100, LocalDateTime.of(2026, 10, 10, 20, 20),
                false, "Plan insuficiente. Requiere PREMIUM y el usuario posee FREE."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(5), evento100, LocalDateTime.of(2026, 10, 10, 20, 31),
                true, ""));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(5), evento100, LocalDateTime.of(2026, 10, 10, 21, 2),
                false, "Expulsado del evento."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(2), evento102, LocalDateTime.of(2026, 10, 11, 19, 10),
                true, ""));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(4), evento102, LocalDateTime.of(2026, 10, 11, 19, 25),
                true, ""));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(4), evento102, LocalDateTime.of(2026, 10, 11, 19, 40),
                false, "Expulsado del evento."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(6), evento102, LocalDateTime.of(2026, 10, 11, 19, 52),
                false, "Usuario sin autorizacion para ingresar."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(0), evento101, LocalDateTime.of(2026, 10, 11, 20, 5),
                false, "Plan insuficiente. Requiere ARTIST_PASS y el usuario posee PREMIUM."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(1), evento101, LocalDateTime.of(2026, 10, 11, 20, 7),
                false, "Evento no disponible: debe estar EN_CURSO."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(1), evento104, LocalDateTime.of(2026, 10, 12, 9, 0),
                false, "Evento no disponible: ya finalizo."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(3), evento105, LocalDateTime.of(2026, 10, 12, 9, 5),
                false, "Evento no disponible: se encuentra cancelado."));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(1), evento100, LocalDateTime.of(2026, 10, 12, 10, 15),
                true, ""));
        registros.add(RegistroAcceso.reconstruirRegistro(
                usuarios.get(0), evento100, LocalDateTime.of(2026, 10, 12, 10, 20),
                false, "El usuario ya se encuentra conectado."));

        gestor.reemplazarDatos(usuarios, artistas,
                new ArrayList<>(eventos), registros);
        gestor.guardarDatos();
    }
}
