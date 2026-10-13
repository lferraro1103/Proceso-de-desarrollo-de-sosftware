package app;

import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.io.IOException;
import java.time.LocalDateTime;

/*
 * Carga de datos de prueba (usuarios, artistas y recitales) para que
 * el programa no arranque vacio.
 *
 * Se separo de App porque cargar datos semilla es una responsabilidad
 * distinta a la de correr el menu de consola.
 */
public final class  DatosIniciales {

    private DatosIniciales() {
    }

    /*
     * Carga los datos con los que arranca el programa.
     *
     * Si ya existen archivos guardados (de una corrida anterior, por
     * ejemplo despues de registrar usuarios/artistas), se cargan esos
     * en vez de pisarlos con los datos fijos de prueba. Solo se siembran
     * los datos fijos si todavia no hay nada guardado (primera corrida).
     */
    public static void cargar(GestorEventosEnVivo gestor) {
        cargarDesdeArchivos(gestor);

        // Solo se siembra lo que falta, para no duplicar datos cuando los
        // archivos guardados estan incompletos.
        if (gestor.listarUsuarios().isEmpty()) {
            cargarUsuariosIniciales(gestor);
        }

        // Sin un administrador nadie podria dar de alta cuentas, por eso
        // se garantiza que siempre exista uno (por ejemplo, con archivos
        // guardados antes de que existieran los roles).
        asegurarAdministrador(gestor);

        if (gestor.listarArtistas().isEmpty()) {
            // Cada artista queda asociado a "su" recital dentro del arreglo,
            // en el mismo orden en que se crean.
            Artista[] artistas = cargarArtistasIniciales(gestor);

            if (gestor.listarEventos().isEmpty()) {
                cargarEventosIniciales(gestor, artistas[0], artistas[1]);
            }
        }
    }

    /*
     * Intenta cargar usuarios/artistas/eventos desde la carpeta datos.
     *
     * Si un archivo no existe o esta vacio no hay error: simplemente no
     * se carga nada y se siembran los datos de prueba. Si un archivo esta
     * danado, se informa y se usan los datos de prueba en su lugar.
     */
    private static void cargarDesdeArchivos(GestorEventosEnVivo gestor) {
        try {
            gestor.cargarDatos();
        } catch (IOException e) {
            // Sin carpeta de datos legible se arranca con los datos de prueba.
        } catch (RuntimeException e) {
            System.out.println("No se pudieron leer los datos guardados ("
                    + e.getMessage() + "). Se usan los datos de prueba.");
        }
    }

    // Proximo identificador libre: el mayor existente mas uno.
    private static int siguienteIdUsuario(GestorEventosEnVivo gestor) {
        int mayor = 0;

        for (Usuario usuario : gestor.listarUsuarios()) {
            mayor = Math.max(mayor, usuario.getId());
        }

        return mayor + 1;
    }

    // Crea el administrador de arranque si todavia no hay ninguno.
    private static void asegurarAdministrador(GestorEventosEnVivo gestor) {
        for (Usuario usuario : gestor.listarUsuarios()) {
            if (usuario.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
                return;
            }
        }

        cargarAdministradorInicial(gestor);
    }

    // Crea y registra los 3 usuarios de prueba, uno por cada plan.
    private static void cargarUsuariosIniciales(GestorEventosEnVivo gestor) {
        // Usuario con plan PREMIUM.
        Usuario usuario1 = new Usuario(
                1,
                "lgabian",
                "Lucia",
                "Gabian",
                "lucia.gabian@uadebeats.com",
                "lgabian2024",
                PlanSuscripcion.PREMIUM,
                true
        );

        // Usuario con plan ARTIST_PASS, el nivel mas alto.
        Usuario usuario2 = new Usuario(
                2,
                "lferraro",
                "Leandro",
                "Ferraro",
                "leandro.ferraro@uadebeats.com",
                "lferraro2024",
                PlanSuscripcion.ARTIST_PASS,
                true
        );

        // Usuario con plan FREE, usado para probar plan insuficiente.
        Usuario usuario3 = new Usuario(
                3,
                "ncaggia",
                "Nicolas",
                "Caggia",
                "nicolas.caggia@uadebeats.com",
                "ncaggia2024",
                PlanSuscripcion.FREE,
                true
        );

        gestor.registrarUsuario(usuario1);
        gestor.registrarUsuario(usuario2);
        gestor.registrarUsuario(usuario3);
    }

    /*
     * Crea el Administrador de arranque para poder loguear ese rol desde el
     * primer arranque del programa (SEED-01), sin depender de que alguien
     * lo de de alta a mano primero.
     */
    private static void cargarAdministradorInicial(GestorEventosEnVivo gestor) {
        Usuario administrador = new Usuario(
                siguienteIdUsuario(gestor),
                "admin",
                "Administrador",
                "UADE Beats",
                "admin@uadebeats.com",
                "admin123",
                PlanSuscripcion.PREMIUM,
                true,
                TipoUsuario.ADMINISTRADOR
        );

        gestor.registrarUsuario(administrador);
    }

    // Crea y registra los 2 artistas de prueba. Devuelve ambos ya registrados.
    private static Artista[] cargarArtistasIniciales(
            GestorEventosEnVivo gestor) {

        Artista artista1 = new Artista(
                1,
                "bandahorizonte",
                "horizonte2024",
                "Banda Horizonte",
                "Rock",
                "Banda argentina de rock alternativo.",
                true
        );

        Artista artista2 = new Artista(
                2,
                "djprisma",
                "prisma2024",
                "DJ Prisma",
                "Electronica",
                "Productora de shows audiovisuales.",
                true
        );

        gestor.registrarArtista(artista1);
        gestor.registrarArtista(artista2);

        return new Artista[] { artista1, artista2 };
    }

    // Crea los 2 recitales de prueba y los asocia a sus artistas.
    private static void cargarEventosIniciales(GestorEventosEnVivo gestor,
                                               Artista artista1,
                                               Artista artista2) {

        // Primer recital: requiere PREMIUM y tiene capacidad 5.
        DatosRecital datosRecital1 = new DatosRecital(
                "Rock en Vivo",
                "Recital exclusivo online",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusHours(2),
                EstadoEvento.PROGRAMADO,
                5,
                PlanSuscripcion.PREMIUM,
                "Buenos Aires",
                true,
                true
        );
        RecitalEnVivo recital1 = new RecitalEnVivo(100, datosRecital1);

        // Segundo recital: requiere ARTIST_PASS y tiene capacidad 2.
        DatosRecital datosRecital2 = new DatosRecital(
                "Backstage Prisma Live",
                "Presentacion en vivo para usuarios Artist Pass",
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1),
                EstadoEvento.EN_ESPERA,
                2,
                PlanSuscripcion.ARTIST_PASS,
                "Streaming",
                true,
                true
        );
        RecitalEnVivo recital2 = new RecitalEnVivo(101, datosRecital2);

        // Se cargan ambos eventos y se asocian usando Artista.crearEvento().
        gestor.crearEvento(recital1, artista1);
        gestor.crearEvento(recital2, artista2);
    }
}
