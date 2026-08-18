package app;

import excepciones.AccesoDenegadoException;
import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.Usuario;
import persistencia.PersistenciaArchivos;
import ui.VentanaPrincipal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/*
 * Clase principal del programa.
 *
 * Esta clase funciona como interfaz de consola para probar el modulo.
 * No contiene las reglas de negocio principales: esas reglas estan en
 * GestorEventosEnVivo, AccesoEvento y RecitalEnVivo.
 */
public class App {

    // ID incremental para usuarios nuevos cargados desde el menu.
    private static int siguienteIdUsuario = 4;

    // ID incremental para artistas nuevos cargados desde el menu.
    private static int siguienteIdArtista = 3;

    // ID incremental para eventos nuevos cargados desde el menu.
    private static int siguienteIdEvento = 102;

    // Punto de entrada del programa.
    public static void main(String[] args) {

        // Scanner permite leer datos ingresados por consola.
        Scanner scanner = new Scanner(System.in);

        // Gestor central del modulo: administra eventos, usuarios y registros.
        GestorEventosEnVivo gestor = new GestorEventosEnVivo();

        // Carga usuarios, artistas y recitales iniciales para poder probar.
        cargarDatosIniciales(gestor);

        // Guarda la opcion elegida por el usuario en el menu.
        int opcion;

        // El menu se repite hasta que el usuario elija 0.
        do {
            // Muestra opciones disponibles.
            mostrarMenu();

            // Lee una opcion numerica validada.
            opcion = leerEntero(scanner, "Seleccione una opcion: ");

            // Ejecuta una accion segun la opcion elegida.
            switch (opcion) {
                case 1:
                    // Permite cargar un usuario nuevo.
                    registrarUsuario(scanner, gestor);
                    break;

                case 2:
                    // Permite registrar un artista nuevo.
                    registrarArtista(scanner, gestor);
                    break;

                case 3:
                    // Permite crear un evento y asociarlo a un artista.
                    crearEventoManual(scanner, gestor);
                    break;

                case 4:
                    // Muestra usuarios registrados.
                    listarUsuarios(gestor);
                    break;

                case 5:
                    // Muestra artistas registrados.
                    listarArtistas(gestor);
                    break;

                case 6:
                    // Muestra eventos administrados por el gestor.
                    listarEventos(gestor);
                    break;

                case 7:
                    // Cambia el estado de un evento a EN_CURSO.
                    cambiarEstadoEvento(scanner, gestor, "iniciar");
                    break;

                case 8:
                    // Pide usuario y evento, y solicita ingreso al gestor.
                    solicitarIngreso(scanner, gestor);
                    break;

                case 9:
                    // Lista usuarios conectados a un recital.
                    listarConectados(scanner, gestor);
                    break;

                case 10:
                    // Muestra capacidad usada y maxima de un recital.
                    mostrarCapacidad(scanner, gestor);
                    break;

                case 11:
                    // Expulsa/desconecta un usuario de un recital.
                    expulsarUsuario(scanner, gestor);
                    break;

                case 12:
                    // Cambia el estado de un evento a PAUSADO.
                    cambiarEstadoEvento(scanner, gestor, "pausar");
                    break;

                case 13:
                    // Cambia el estado de un evento a EN_CURSO.
                    cambiarEstadoEvento(scanner, gestor, "reanudar");
                    break;

                case 14:
                    // Cambia el estado de un evento a FINALIZADO.
                    cambiarEstadoEvento(scanner, gestor, "finalizar");
                    break;

                case 15:
                    // Muestra todos los intentos de acceso registrados.
                    listarRegistros(gestor);
                    break;

                case 16:
                    // Guarda informacion relevante en archivos TXT.
                    guardarDatos(gestor);
                    break;

                case 17:
                    // Carga informacion guardada en archivos TXT.
                    cargarDatos(gestor);
                    break;

                case 18:
                    // Abre la interfaz grafica Swing.
                    abrirInterfazGrafica(gestor);
                    break;

                case 0:
                    // Corta el ciclo del menu.
                    System.out.println("Programa finalizado.");
                    break;

                default:
                    // Cualquier numero fuera del menu cae aca.
                    System.out.println("Opcion invalida.");
            }

        } while (opcion != 0);

        // Libera el recurso de lectura por consola.
        scanner.close();
    }

    /*
     * Carga datos iniciales para que el programa ya tenga informacion
     * apenas arranca.
     *
     * Se cargan:
     * - 3 usuarios con planes diferentes;
     * - 2 artistas;
     * - 2 recitales con distintos planes requeridos.
     */
    public static void cargarDatosIniciales(GestorEventosEnVivo gestor) {
        // Usuario con plan PREMIUM.
        Usuario usuario1 = new Usuario(
                1,
                "lgabian",
                "Lucia",
                "Gabian",
                "lucia.gabian@uadebeats.com",
                "1234",
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
                "1234",
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
                "1234",
                PlanSuscripcion.FREE,
                true
        );

        // Se registran los usuarios en el gestor.
        gestor.registrarUsuario(usuario1);
        gestor.registrarUsuario(usuario2);
        gestor.registrarUsuario(usuario3);

        // Primer artista asociado al primer recital.
        Artista artista1 = new Artista(
                1,
                "Banda Horizonte",
                "Rock",
                "Banda argentina de rock alternativo.",
                true
        );

        // Segundo artista asociado al segundo recital.
        Artista artista2 = new Artista(
                2,
                "DJ Prisma",
                "Electronica",
                "Productora de shows audiovisuales.",
                true
        );

        // Primer recital: requiere PREMIUM y tiene capacidad 5.
        RecitalEnVivo recital1 = new RecitalEnVivo(
                100,
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

        // Segundo recital: requiere ARTIST_PASS y tiene capacidad 2.
        RecitalEnVivo recital2 = new RecitalEnVivo(
                101,
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

        // Se cargan los artistas en el gestor.
        gestor.registrarArtista(artista1);
        gestor.registrarArtista(artista2);

        // Se cargan ambos eventos y se asocian usando Artista.crearEvento().
        gestor.crearEvento(recital1, artista1);
        gestor.crearEvento(recital2, artista2);
    }

    // Imprime el menu principal de la aplicacion por consola.
    private static void mostrarMenu() {
        System.out.println("\n===== MENU EVENTOS EN VIVO =====");
        System.out.println("1. Registrar usuario");
        System.out.println("2. Registrar artista");
        System.out.println("3. Crear evento manualmente");
        System.out.println("4. Ver usuarios registrados");
        System.out.println("5. Ver artistas registrados");
        System.out.println("6. Ver eventos administrados");
        System.out.println("7. Iniciar evento");
        System.out.println("8. Solicitar ingreso a evento");
        System.out.println("9. Ver usuarios conectados a un evento");
        System.out.println("10. Ver capacidad de un evento");
        System.out.println("11. Expulsar usuario de un evento");
        System.out.println("12. Pausar evento");
        System.out.println("13. Reanudar evento");
        System.out.println("14. Finalizar evento");
        System.out.println("15. Ver registros de acceso");
        System.out.println("16. Guardar datos en archivos TXT");
        System.out.println("17. Cargar datos desde archivos TXT");
        System.out.println("18. Abrir interfaz grafica Swing");
        System.out.println("0. Salir");
    }

    /*
     * Registra un usuario nuevo desde consola.
     *
     * Pide los datos basicos, permite seleccionar plan y luego lo guarda
     * dentro del gestor.
     */
    private static void registrarUsuario(Scanner scanner,
                                         GestorEventosEnVivo gestor) {

        // Lee nombre de cuenta.
        System.out.print("Ingrese nombre de usuario: ");
        String nombreUsuario = scanner.nextLine().trim();

        if (nombreUsuario.isEmpty()) {
            System.out.println("El nombre de usuario no puede estar vacio.");
            return;
        }

        if (gestor.existeNombreUsuario(nombreUsuario)) {
            System.out.println("Ese nombre de usuario ya esta registrado.");
            return;
        }

        // Lee nombre real.
        System.out.print("Ingrese nombre: ");
        String nombre = scanner.nextLine();

        // Lee apellido.
        System.out.print("Ingrese apellido: ");
        String apellido = scanner.nextLine();

        // Lee email.
        System.out.print("Ingrese email: ");
        String email = scanner.nextLine();

        // Permite elegir FREE, PREMIUM o ARTIST_PASS.
        PlanSuscripcion plan = seleccionarPlan(scanner);

        // Crea el usuario con ID incremental y contrasena simple de prueba.
        Usuario nuevoUsuario = new Usuario(
                siguienteIdUsuario,
                nombreUsuario,
                nombre,
                apellido,
                email,
                "1234",
                plan,
                true
        );

        // Guarda el usuario en el gestor.
        if (!gestor.registrarUsuario(nuevoUsuario)) {
            System.out.println("Ese nombre de usuario ya esta registrado.");
            return;
        }

        // Informa resultado y avanza el contador de IDs.
        System.out.println("Usuario registrado correctamente.");
        System.out.println("ID asignado: " + siguienteIdUsuario);
        siguienteIdUsuario++;
    }

    /*
     * Registra un artista nuevo desde consola.
     *
     * El artista queda disponible para ser seleccionado al crear eventos.
     */
    private static void registrarArtista(Scanner scanner,
                                         GestorEventosEnVivo gestor) {

        System.out.print("Ingrese nombre artistico: ");
        String nombreArtistico = scanner.nextLine();

        System.out.print("Ingrese genero principal: ");
        String generoPrincipal = scanner.nextLine();

        System.out.print("Ingrese biografia: ");
        String biografia = scanner.nextLine();

        boolean verificado = leerBooleano(
                scanner,
                "El artista esta verificado"
        );

        Artista artista = new Artista(
                siguienteIdArtista,
                nombreArtistico,
                generoPrincipal,
                biografia,
                verificado
        );

        gestor.registrarArtista(artista);

        System.out.println("Artista registrado correctamente.");
        System.out.println("ID asignado: " + siguienteIdArtista);
        siguienteIdArtista++;
    }

    /*
     * Crea un evento en vivo desde consola.
     *
     * Primero se elige un artista y luego se arma el RecitalEnVivo.
     * El gestor lo crea usando la relacion con Artista.
     */
    private static void crearEventoManual(Scanner scanner,
                                          GestorEventosEnVivo gestor) {

        if (gestor.listarArtistas().isEmpty()) {
            System.out.println(
                    "No hay artistas registrados. Registre un artista primero."
            );
            return;
        }

        listarArtistas(gestor);
        int idArtista = leerEntero(scanner, "Ingrese ID de artista: ");
        Artista artista = gestor.buscarArtistaPorId(idArtista);

        if (artista == null) {
            System.out.println("Artista inexistente.");
            return;
        }

        System.out.print("Ingrese titulo del evento: ");
        String titulo = scanner.nextLine();

        System.out.print("Ingrese descripcion del evento: ");
        String descripcion = scanner.nextLine();

        int minutosHastaInicio = leerEntero(
                scanner,
                "Minutos hasta el inicio (0 para ahora): "
        );

        int duracionMinutos = leerEntero(
                scanner,
                "Duracion en minutos: "
        );

        if (duracionMinutos <= 0) {
            System.out.println("Duracion invalida. Se asignan 60 minutos.");
            duracionMinutos = 60;
        }

        int capacidadMaxima = leerEntero(
                scanner,
                "Capacidad maxima: "
        );

        if (capacidadMaxima <= 0) {
            System.out.println("Capacidad invalida. Se asigna 1.");
            capacidadMaxima = 1;
        }

        PlanSuscripcion planMinimo = seleccionarPlan(scanner);

        System.out.print("Ingrese ubicacion o canal de streaming: ");
        String ubicacion = scanner.nextLine();

        boolean esStreaming = leerBooleano(
                scanner,
                "El evento es por streaming"
        );

        boolean exclusivo = leerBooleano(
                scanner,
                "El evento es exclusivo"
        );

        LocalDateTime inicio = LocalDateTime.now()
                .plusMinutes(minutosHastaInicio);

        LocalDateTime fin = inicio.plusMinutes(duracionMinutos);

        RecitalEnVivo recital = new RecitalEnVivo(
                siguienteIdEvento,
                titulo,
                descripcion,
                inicio,
                fin,
                EstadoEvento.PROGRAMADO,
                capacidadMaxima,
                planMinimo,
                ubicacion,
                esStreaming,
                exclusivo
        );

        gestor.crearEvento(recital, artista);

        System.out.println("Evento creado correctamente.");
        System.out.println("ID asignado: " + siguienteIdEvento);
        System.out.println("Artista asociado: "
                + artista.getNombreArtistico());
        siguienteIdEvento++;
    }

    // Lista todos los artistas registrados en el gestor.
    private static void listarArtistas(GestorEventosEnVivo gestor) {
        System.out.println("\nArtistas registrados:");

        for (Artista artista : gestor.listarArtistas()) {
            System.out.println(artista);
        }
    }

    // Muestra los planes disponibles y devuelve el plan elegido.
    private static PlanSuscripcion seleccionarPlan(Scanner scanner) {
        System.out.println("Seleccione plan:");
        System.out.println("1 - FREE");
        System.out.println("2 - PREMIUM");
        System.out.println("3 - ARTIST_PASS");

        // Lee la opcion numerica del plan.
        int opcionPlan = leerEntero(scanner, "Plan: ");

        // Convierte la opcion del menu en un valor del enum.
        switch (opcionPlan) {
            case 1:
                return PlanSuscripcion.FREE;

            case 2:
                return PlanSuscripcion.PREMIUM;

            case 3:
                return PlanSuscripcion.ARTIST_PASS;

            default:
                // Si el usuario ingresa algo fuera de rango, se asigna FREE.
                System.out.println("Plan invalido. Se asigna FREE.");
                return PlanSuscripcion.FREE;
        }
    }

    // Lista todos los usuarios registrados en el gestor.
    private static void listarUsuarios(GestorEventosEnVivo gestor) {
        System.out.println("\nUsuarios registrados:");

        // El toString() de Usuario define como se muestra cada usuario.
        for (Usuario usuario : gestor.listarUsuarios()) {
            System.out.println(usuario);
        }
    }

    // Lista todos los eventos administrados por el gestor.
    private static void listarEventos(GestorEventosEnVivo gestor) {
        System.out.println("\nEventos administrados:");

        // El toString() de Evento/RecitalEnVivo define el formato de salida.
        for (Evento evento : gestor.listarEventos()) {
            System.out.println(evento);
        }
    }

    /*
     * Solicita ingreso de un usuario a un evento.
     *
     * Main solo pide IDs y muestra el resultado.
     * La logica real la ejecuta gestor.solicitarIngreso().
     */
    private static void solicitarIngreso(Scanner scanner,
                                         GestorEventosEnVivo gestor) {

        // Muestra usuarios para que el operador elija uno.
        listarUsuarios(gestor);
        int idUsuario = leerEntero(scanner, "Ingrese ID de usuario: ");

        // Muestra eventos para que el operador elija uno.
        listarEventos(gestor);
        int idEvento = leerEntero(scanner, "Ingrese ID de evento: ");

        // El gestor coordina validaciones y devuelve el registro generado.
        RegistroAcceso registro = gestor.solicitarIngreso(idUsuario, idEvento);

        // Imprime una traza basica del registro.
        registro.guardarRegistro();

        // Muestra el resultado final al usuario de consola.
        if (registro.isExitoso()) {
            System.out.println("Ingreso autorizado.");
        } else {
            System.out.println("Ingreso rechazado: "
                    + registro.getMotivoRechazo());
        }
    }

    // Lista usuarios conectados a un evento puntual.
    private static void listarConectados(Scanner scanner,
                                         GestorEventosEnVivo gestor) {

        // Pide ID del evento a consultar.
        int idEvento = leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento en el gestor.
        Evento evento = gestor.buscarEventoPorId(idEvento);

        // Se valida que exista y que sea del tipo RecitalEnVivo.
        if (!(evento instanceof RecitalEnVivo)) {
            System.out.println("Evento inexistente o no compatible.");
            return;
        }

        // Cast: luego del instanceof, Java permite tratarlo como RecitalEnVivo.
        RecitalEnVivo recital = (RecitalEnVivo) evento;
        System.out.println("\nUsuarios conectados:");

        // Si no hay usuarios conectados, se informa y se corta.
        if (recital.getUsuariosConectados().isEmpty()) {
            System.out.println("No hay usuarios conectados.");
            return;
        }

        // Imprime cada usuario conectado.
        for (Usuario usuario : recital.getUsuariosConectados()) {
            System.out.println(usuario);
        }
    }

    // Expulsa un usuario conectado a un evento.
    private static void expulsarUsuario(Scanner scanner,
                                        GestorEventosEnVivo gestor) {

        // Muestra solo eventos activos, porque no se expulsa desde pausados.
        if (!listarEventosParaExpulsion(gestor)) {
            return;
        }

        // Pide el evento sobre el que se quiere operar.
        int idEvento = leerEntero(scanner, "Ingrese ID de evento: ");

        Evento evento = gestor.buscarEventoPorId(idEvento);

        if (!(evento instanceof RecitalEnVivo)) {
            System.out.println("Evento inexistente o no compatible.");
            return;
        }

        if (evento.getEstado() != EstadoEvento.EN_CURSO) {
            System.out.println(
                    "Solo se pueden expulsar usuarios de eventos EN_CURSO."
            );
            return;
        }

        RecitalEnVivo recital = (RecitalEnVivo) evento;

        // Antes de pedir usuario, muestra quienes estan conectados.
        if (!listarUsuariosConectadosParaExpulsion(recital)) {
            return;
        }

        // Pide usuario a expulsar.
        int idUsuario = leerEntero(scanner, "Ingrese ID de usuario a expulsar: ");

        // El gestor devuelve true si pudo expulsarlo.
        if (gestor.expulsarUsuario(idEvento, idUsuario)) {
            System.out.println("Usuario expulsado.");
        } else {
            System.out.println("Usuario no encontrado en el evento.");
        }
    }

    /*
     * Lista eventos en curso para la operacion de expulsion.
     *
     * No incluye eventos pausados, programados, finalizados ni cancelados,
     * porque la expulsion se plantea sobre eventos activos.
     */
    private static boolean listarEventosParaExpulsion(
            GestorEventosEnVivo gestor) {

        System.out.println("\nEventos en curso:");
        boolean hayEventos = false;

        for (Evento evento : gestor.listarEventos()) {
            if (evento instanceof RecitalEnVivo
                    && evento.getEstado() == EstadoEvento.EN_CURSO) {
                System.out.println(evento);
                hayEventos = true;
            }
        }

        if (!hayEventos) {
            System.out.println(
                    "No hay eventos en curso para expulsar usuarios."
            );
        }

        return hayEventos;
    }

    // Lista usuarios conectados al recital elegido para poder seleccionar uno.
    private static boolean listarUsuariosConectadosParaExpulsion(
            RecitalEnVivo recital) {

        System.out.println("\nUsuarios conectados al evento:");

        if (recital.getUsuariosConectados().isEmpty()) {
            System.out.println("No hay usuarios conectados en este evento.");
            return false;
        }

        for (Usuario usuario : recital.getUsuariosConectados()) {
            System.out.println(usuario);
        }

        return true;
    }

    // Muestra capacidad usada y capacidad maxima de un evento.
    private static void mostrarCapacidad(Scanner scanner,
                                         GestorEventosEnVivo gestor) {

        // Pide ID del evento.
        int idEvento = leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento.
        Evento evento = gestor.buscarEventoPorId(idEvento);

        // La capacidad conectada solo existe en RecitalEnVivo.
        if (!(evento instanceof RecitalEnVivo)) {
            System.out.println("Evento inexistente o no compatible.");
            return;
        }

        // Convierte el evento a RecitalEnVivo para usar sus metodos propios.
        RecitalEnVivo recital = (RecitalEnVivo) evento;

        // Imprime conectados/capacidad maxima.
        System.out.println("Capacidad actual: "
                + recital.getCantidadConectados()
                + "/"
                + recital.getCapacidadMaxima());
    }

    /*
     * Cambia el estado de un evento.
     *
     * El parametro accion permite reutilizar el mismo metodo para:
     * - iniciar;
     * - pausar;
     * - reanudar;
     * - finalizar.
     */
    private static void cambiarEstadoEvento(Scanner scanner,
                                            GestorEventosEnVivo gestor,
                                            String accion) {

        // Muestra solo los eventos que pueden recibir esa accion.
        if (!listarEventosParaCambio(gestor, accion)) {
            return;
        }

        // Pide ID del evento a modificar.
        int idEvento = leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento en el gestor.
        Evento evento = gestor.buscarEventoPorId(idEvento);

        // Si no existe, se informa y se corta.
        if (evento == null) {
            System.out.println("Evento inexistente.");
            return;
        }

        try {
            // Ejecuta la accion recibida por parametro.
            if ("iniciar".equals(accion)) {
                evento.iniciarEvento();
                System.out.println("Evento iniciado.");
            } else if ("pausar".equals(accion)) {
                evento.pausarEvento();
                System.out.println("Evento pausado.");
            } else if ("reanudar".equals(accion)) {
                evento.reanudarEvento();
                System.out.println("Evento reanudado.");
            } else if ("finalizar".equals(accion)) {
                evento.finalizarEvento();
                System.out.println("Evento finalizado.");
            }
        } catch (AccesoDenegadoException e) {
            System.out.println("No se pudo cambiar el estado: "
                    + e.getMessage());
        }
    }

    /*
     * Lista solo los eventos que tienen sentido para la accion elegida.
     *
     * Esto evita, por ejemplo, que al pausar aparezcan eventos que todavia
     * estan PROGRAMADO o EN_ESPERA.
     */
    private static boolean listarEventosParaCambio(GestorEventosEnVivo gestor,
                                                   String accion) {

        System.out.println("\nEventos disponibles para "
                + obtenerNombreAccion(accion)
                + ":");

        boolean hayEventos = false;

        for (Evento evento : gestor.listarEventos()) {
            if (puedeAplicarAccion(evento, accion)) {
                System.out.println(evento);
                hayEventos = true;
            }
        }

        if (!hayEventos) {
            System.out.println("No hay eventos disponibles para esta accion.");
        }

        return hayEventos;
    }

    // Indica si el estado actual del evento permite la accion pedida.
    private static boolean puedeAplicarAccion(Evento evento,
                                              String accion) {

        EstadoEvento estado = evento.getEstado();

        if ("iniciar".equals(accion)) {
            return estado == EstadoEvento.PROGRAMADO
                    || estado == EstadoEvento.EN_ESPERA;
        }

        if ("pausar".equals(accion)) {
            return estado == EstadoEvento.EN_CURSO;
        }

        if ("reanudar".equals(accion)) {
            return estado == EstadoEvento.PAUSADO;
        }

        if ("finalizar".equals(accion)) {
            return estado == EstadoEvento.EN_CURSO
                    || estado == EstadoEvento.PAUSADO;
        }

        return false;
    }

    // Devuelve un texto legible para mostrar la accion en consola.
    private static String obtenerNombreAccion(String accion) {
        if ("iniciar".equals(accion)) {
            return "iniciar";
        }

        if ("pausar".equals(accion)) {
            return "pausar";
        }

        if ("reanudar".equals(accion)) {
            return "reanudar";
        }

        if ("finalizar".equals(accion)) {
            return "finalizar";
        }

        return "modificar";
    }

    // Guarda usuarios, artistas, eventos y registros en archivos TXT.
    private static void guardarDatos(GestorEventosEnVivo gestor) {
        try {
            PersistenciaArchivos.guardarDatos(gestor);
            System.out.println("Datos guardados correctamente en carpeta datos.");
        } catch (IOException e) {
            System.out.println("No se pudieron guardar los datos: "
                    + e.getMessage());
        }
    }

    // Carga usuarios, artistas y eventos desde archivos TXT.
    private static void cargarDatos(GestorEventosEnVivo gestor) {
        try {
            PersistenciaArchivos.cargarDatos(gestor);
            actualizarIdsDesdeGestor(gestor);
            System.out.println("Datos cargados correctamente desde carpeta datos.");
        } catch (IOException | RuntimeException e) {
            System.out.println("No se pudieron cargar los datos: "
                    + e.getMessage());
        }
    }

    // Abre la ventana Swing reutilizando el mismo gestor del menu consola.
    private static void abrirInterfazGrafica(GestorEventosEnVivo gestor) {
        SwingUtilities.invokeLater(() ->
                new VentanaPrincipal(gestor).setVisible(true)
        );

        System.out.println("Interfaz grafica abierta.");
    }

    /*
     * Actualiza los IDs incrementales despues de cargar datos.
     *
     * Evita que un nuevo usuario, artista o evento repita un ID ya cargado.
     */
    private static void actualizarIdsDesdeGestor(GestorEventosEnVivo gestor) {
        int mayorUsuario = 0;
        int mayorArtista = 0;
        int mayorEvento = 0;

        for (Usuario usuario : gestor.listarUsuarios()) {
            if (usuario.getId() > mayorUsuario) {
                mayorUsuario = usuario.getId();
            }
        }

        for (Artista artista : gestor.listarArtistas()) {
            if (artista.getId() > mayorArtista) {
                mayorArtista = artista.getId();
            }
        }

        for (Evento evento : gestor.listarEventos()) {
            if (evento.getId() > mayorEvento) {
                mayorEvento = evento.getId();
            }
        }

        siguienteIdUsuario = mayorUsuario + 1;
        siguienteIdArtista = mayorArtista + 1;
        siguienteIdEvento = mayorEvento + 1;
    }

    // Lista todos los registros de acceso guardados por el gestor.
    private static void listarRegistros(GestorEventosEnVivo gestor) {
        // Pide una copia de los registros.
        List<RegistroAcceso> registros = gestor.listarRegistros();

        // Si no hay registros, se informa.
        if (registros.isEmpty()) {
            System.out.println("No hay registros de acceso.");
            return;
        }

        // Imprime todos los registros usando su toString().
        System.out.println("\nRegistros de acceso:");
        for (RegistroAcceso registro : registros) {
            System.out.println(registro);
        }
    }

    /*
     * Lee un numero entero de forma segura.
     *
     * Si el usuario escribe texto u otro dato invalido, no rompe el
     * programa: muestra un mensaje y vuelve a pedir el numero.
     */
    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            // Muestra el mensaje recibido, por ejemplo "Ingrese ID".
            System.out.print(mensaje);

            // hasNextInt verifica si lo ingresado es un entero.
            if (scanner.hasNextInt()) {
                // Lee el numero.
                int valor = scanner.nextInt();

                // Limpia el salto de linea pendiente luego de nextInt().
                scanner.nextLine();

                // Devuelve el numero valido.
                return valor;
            }

            // Si no era entero, se informa el error.
            System.out.println("Debe ingresar un numero entero.");

            // Descarta el texto invalido para volver a intentar.
            scanner.nextLine();
        }
    }

    // Lee una respuesta booleana usando 1 para si y 2 para no.
    private static boolean leerBooleano(Scanner scanner, String mensaje) {
        while (true) {
            int opcion = leerEntero(
                    scanner,
                    mensaje + " (1 - Si / 2 - No): "
            );

            if (opcion == 1) {
                return true;
            }

            if (opcion == 2) {
                return false;
            }

            System.out.println("Opcion invalida.");
        }
    }
}
