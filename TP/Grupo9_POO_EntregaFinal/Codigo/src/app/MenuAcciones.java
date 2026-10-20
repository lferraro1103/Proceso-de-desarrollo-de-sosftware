package app;

import excepciones.AccesoDenegadoException;
import excepciones.ErrorDeServicioException;
import calendario.VinculoApple;
import calendario.VinculoGoogle;
import gestor.ServicioEventos;
import gestor.Transmision;
import gestor.VinculoCalendario;
import modelo.Artista;
import modelo.Cuenta;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;
import ui.Navegador;
import ui.VentanaPrincipal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/*
 * Implementacion de cada opcion del menu de consola.
 *
 * Se separo de App para que App se ocupe solo de arrancar el programa
 * y despachar la opcion elegida; aca vive como se ejecuta cada accion.
 */
public final class MenuAcciones {

    private MenuAcciones() {
    }

    /*
     * Login por consola para cualquier tipo de cuenta.
     *
     * Pide nombre de cuenta y contrasena y deja la validacion al gestor.
     * Devuelve la cuenta autenticada, o null si no pudo iniciar sesion.
     */
    public static Cuenta login(Scanner scanner, ServicioEventos gestor) {
        System.out.print("Ingrese nombre de usuario: ");
        String nombreUsuario = scanner.nextLine().trim();

        System.out.print("Ingrese contrasena: ");
        String contrasena = scanner.nextLine();

        try {
            Cuenta cuenta = gestor.iniciarSesion(nombreUsuario, contrasena);
            System.out.println("Sesion iniciada. Bienvenido, "
                    + cuenta.getNombreUsuario() + " ("
                    + cuenta.getTipoUsuario() + ").");
            return cuenta;
        } catch (AccesoDenegadoException | ErrorDeServicioException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /*
     * Registra un usuario nuevo desde consola.
     *
     * Pide los datos basicos, permite seleccionar plan y luego lo guarda
     * dentro del gestor.
     */
    public static void registrarUsuario(Scanner scanner,
                                        ServicioEventos gestor) {

        // Lee y valida el nombre de cuenta.
        String nombreUsuario = leerNombreUsuarioNuevo(scanner, gestor);
        if (nombreUsuario == null) {
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

        // Lee y valida la contrasena para el login.
        String contrasena = leerContrasenaNueva(scanner);
        if (contrasena == null) {
            return;
        }

        // Permite elegir FREE, PREMIUM o ARTIST_PASS.
        PlanSuscripcion plan = seleccionarPlan(scanner);

        // El gestor asigna el ID y construye el usuario.
        Usuario nuevoUsuario = gestor.registrarUsuario(
                nombreUsuario,
                nombre,
                apellido,
                email,
                contrasena,
                plan
        );

        if (nuevoUsuario == null) {
            System.out.println("Ese nombre de usuario ya esta registrado.");
            return;
        }

        // Informa el resultado.
        System.out.println("Usuario registrado correctamente.");
        System.out.println("ID asignado: " + nuevoUsuario.getId());

    }

    /*
     * Pide el nombre de cuenta y valida que no este vacio ni repetido.
     *
     * Devuelve null si la validacion falla; en ese caso ya se informo
     * el motivo por consola.
     */
    private static String leerNombreUsuarioNuevo(Scanner scanner,
                                                 ServicioEventos gestor) {

        System.out.print("Ingrese nombre de usuario: ");
        String nombreUsuario = scanner.nextLine().trim();

        if (nombreUsuario.isEmpty()) {
            System.out.println("El nombre de usuario no puede estar vacio.");
            return null;
        }

        if (gestor.existeNombreUsuario(nombreUsuario)) {
            System.out.println("Ese nombre de usuario ya esta registrado.");
            return null;
        }

        return nombreUsuario;
    }

    // Pide la contrasena para el registro y valida que no este vacia.
    private static String leerContrasenaNueva(Scanner scanner) {
        System.out.print("Ingrese contrasena: ");
        String contrasena = scanner.nextLine();

        if (contrasena.isEmpty()) {
            System.out.println("La contrasena no puede estar vacia.");
            return null;
        }

        return contrasena;
    }

    /*
     * Registra un artista nuevo desde consola.
     *
     * El artista queda disponible para ser seleccionado al crear eventos.
     */
    public static void registrarArtista(Scanner scanner,
                                        ServicioEventos gestor) {

        System.out.print("Ingrese nombre de cuenta del artista: ");
        String nombreUsuario = scanner.nextLine().trim();

        if (nombreUsuario.isEmpty() || gestor.existeNombreUsuario(nombreUsuario)) {
            System.out.println("Ese nombre de cuenta no es valido o ya esta registrado.");
            return;
        }

        String contrasena = leerContrasenaNueva(scanner);
        if (contrasena == null) {
            return;
        }

        System.out.print("Ingrese nombre artistico: ");
        String nombreArtistico = scanner.nextLine();

        System.out.print("Ingrese genero principal: ");
        String generoPrincipal = scanner.nextLine();

        System.out.print("Ingrese biografia: ");
        String biografia = scanner.nextLine();

        boolean verificado = ConsoleIO.leerBooleano(
                scanner,
                "El artista esta verificado"
        );

        Artista artista = gestor.registrarArtista(
                nombreUsuario,
                contrasena,
                nombreArtistico,
                generoPrincipal,
                biografia,
                verificado
        );

        System.out.println("Artista registrado correctamente.");
        System.out.println("ID asignado: " + artista.getId());

    }

    /*
     * Crea un evento en vivo desde consola.
     *
     * Primero se elige un artista y luego se arma el RecitalEnVivo.
     * El gestor lo crea usando la relacion con Artista.
     */
    public static void crearEventoManual(Scanner scanner,
                                         ServicioEventos gestor) {

        // Paso 1: un artista crea el evento a su nombre; el administrador
        // elige el artista que va a estar asociado.
        Artista artista;
        if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.ARTISTA) {
            artista = gestor.buscarArtistaPorId(gestor.getCuentaActual().getId());
        } else {
            artista = seleccionarArtistaParaEvento(scanner, gestor);
        }
        if (artista == null) {
            return;
        }

        // Paso 2: leer titulo, descripcion y horarios del evento.
        System.out.print("Ingrese titulo del evento: ");
        String titulo = scanner.nextLine();

        System.out.print("Ingrese descripcion del evento: ");
        String descripcion = scanner.nextLine();

        int minutosHastaInicio = ConsoleIO.leerEntero(
                scanner,
                "Minutos hasta el inicio (0 para ahora): "
        );

        LocalDateTime inicio = LocalDateTime.now()
                .plusMinutes(minutosHastaInicio);

        LocalDateTime fin = inicio.plusMinutes(leerDuracionMinutos(scanner));

        // Paso 3: leer reglas de acceso del evento.
        int capacidadMaxima = leerCapacidadMaxima(scanner);
        PlanSuscripcion planMinimo = seleccionarPlan(scanner);

        System.out.print("Ingrese ubicacion o canal de streaming: ");
        String ubicacion = scanner.nextLine();

        boolean esStreaming = ConsoleIO.leerBooleano(
                scanner,
                "El evento es por streaming"
        );

        boolean exclusivo = ConsoleIO.leerBooleano(
                scanner,
                "El evento es exclusivo"
        );

        // Paso 4: construir el evento con todos los datos reunidos.
        DatosRecital datosRecital = new DatosRecital(
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

        // Paso 5: el gestor asigna el ID, lo crea y lo asocia al artista.
        RecitalEnVivo recital = gestor.crearEvento(datosRecital, artista);

        System.out.println("Evento creado correctamente.");
        System.out.println("ID asignado: " + recital.getId());
        System.out.println("Artista asociado: "
                + artista.getNombreArtistico());
    }

    /*
     * Muestra los artistas disponibles y devuelve el elegido por ID.
     *
     * Devuelve null si no hay artistas cargados o si el ID no existe;
     * en ambos casos ya se informo el motivo por consola.
     */
    private static Artista seleccionarArtistaParaEvento(
            Scanner scanner, ServicioEventos gestor) {

        if (gestor.listarArtistas().isEmpty()) {
            System.out.println(
                    "No hay artistas registrados. Registre un artista primero."
            );
            return null;
        }

        listarArtistas(gestor);
        int idArtista = ConsoleIO.leerEntero(scanner, "Ingrese ID de artista: ");
        Artista artista = gestor.buscarArtistaPorId(idArtista);

        if (artista == null) {
            System.out.println("Artista inexistente.");
        }

        return artista;
    }

    // Pide la duracion del evento y aplica un valor por defecto si es invalida.
    private static int leerDuracionMinutos(Scanner scanner) {
        int duracionMinutos = ConsoleIO.leerEntero(scanner, "Duracion en minutos: ");

        if (duracionMinutos <= 0) {
            System.out.println("Duracion invalida. Se asignan 60 minutos.");
            return 60;
        }

        return duracionMinutos;
    }

    // Pide la capacidad maxima del evento y aplica un minimo si es invalida.
    private static int leerCapacidadMaxima(Scanner scanner) {
        int capacidadMaxima = ConsoleIO.leerEntero(scanner, "Capacidad maxima: ");

        if (capacidadMaxima <= 0) {
            System.out.println("Capacidad invalida. Se asigna 1.");
            return 1;
        }

        return capacidadMaxima;
    }

    // Lista todos los artistas registrados en el gestor.
    public static void listarArtistas(ServicioEventos gestor) {
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
        int opcionPlan = ConsoleIO.leerEntero(scanner, "Plan: ");

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
    public static void listarUsuarios(ServicioEventos gestor) {
        System.out.println("\nUsuarios registrados:");

        // El toString() de Usuario define como se muestra cada usuario.
        for (Usuario usuario : gestor.listarUsuarios()) {
            System.out.println(usuario);
        }
    }

    // Lista todos los eventos administrados por el gestor.
    public static void listarEventos(ServicioEventos gestor) {
        System.out.println("\nEventos:");

        // El toString() de Evento/RecitalEnVivo define el formato de salida.
        for (Evento evento : gestor.listarEventosVisibles()) {
            System.out.println(evento);
        }
    }

    /*
     * Solicita ingreso de un usuario a un evento.
     *
     * Este metodo solo pide IDs y muestra el resultado.
     * La logica real la ejecuta gestor.solicitarIngreso().
     */
    public static void solicitarIngreso(Scanner scanner,
                                        ServicioEventos gestor) {

        // Un usuario solicita el ingreso con su propia cuenta; el
        // administrador elige a nombre de quien lo hace.
        int idUsuario;
        if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO) {
            idUsuario = gestor.getCuentaActual().getId();
        } else {
            listarUsuarios(gestor);
            idUsuario = ConsoleIO.leerEntero(scanner, "Ingrese ID de usuario: ");
        }

        // Muestra eventos para que el operador elija uno.
        listarEventos(gestor);
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // El gestor coordina validaciones y devuelve el registro generado.
        RegistroAcceso registro = gestor.solicitarIngreso(idUsuario, idEvento);

        // Muestra el resultado final al usuario de consola.
        if (registro.isExitoso()) {
            System.out.println("Ingreso autorizado.");

            // Quien ingresa a su nombre entra directamente a la transmision.
            if (gestor.getCuentaActual().getTipoUsuario() == TipoUsuario.USUARIO) {
                mostrarTransmision(gestor, idEvento);
            }
        } else {
            System.out.println("Ingreso rechazado: "
                    + registro.getMotivoRechazo());
        }

        // El intento, exitoso o no, queda registrado: se guarda.
    }


    // Lista usuarios conectados a un evento puntual.
    public static void listarConectados(Scanner scanner,
                                        ServicioEventos gestor) {

        // Pide ID del evento a consultar.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento por ID.
        Evento recital = gestor.buscarEventoVisible(idEvento);

        if (recital == null) {
            System.out.println("Evento inexistente o no compatible.");
            return;
        }

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
    public static void expulsarUsuario(Scanner scanner,
                                       ServicioEventos gestor) {

        // Muestra solo eventos activos, porque no se expulsa desde pausados.
        // Reutiliza listarEventosParaCambio con la accion "expulsar" en vez
        // de tener un metodo aparte que filtraba lo mismo (EN_CURSO).
        if (!listarEventosParaCambio(gestor, "expulsar")) {
            return;
        }

        // Pide el evento sobre el que se quiere operar.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        Evento recital = gestor.buscarEventoVisible(idEvento);

        if (recital == null) {
            System.out.println("Evento inexistente o no compatible.");
            return;
        }

        if (recital.getEstado() != EstadoEvento.EN_CURSO) {
            System.out.println(
                    "Solo se pueden expulsar usuarios de eventos EN_CURSO."
            );
            return;
        }

        // Antes de pedir usuario, muestra quienes estan conectados.
        if (!listarUsuariosConectadosParaExpulsion(recital)) {
            return;
        }

        // Pide usuario a expulsar.
        int idUsuario = ConsoleIO.leerEntero(
                scanner, "Ingrese ID de usuario a expulsar: ");

        // El gestor devuelve el usuario expulsado, o null si no se pudo.
        Usuario usuarioExpulsado = gestor.expulsarUsuario(idEvento, idUsuario);

        if (usuarioExpulsado != null) {
            System.out.println("Usuario expulsado.");

            // Notificacion simple: en esta consola no hay sesiones
            // separadas por usuario, asi que se informa que el sistema
            // ya genero el aviso para el usuario afectado.
            System.out.println("Se notifico a "
                    + usuarioExpulsado.getNombreCompleto()
                    + " que fue expulsado del evento.");
        } else {
            System.out.println("Usuario no encontrado en el evento.");
        }
    }

    // Lista usuarios conectados al recital elegido para poder seleccionar uno.
    private static boolean listarUsuariosConectadosParaExpulsion(
            Evento recital) {

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
    public static void mostrarCapacidad(Scanner scanner,
                                        ServicioEventos gestor) {

        // Pide ID del evento.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento por ID.
        Evento recital = gestor.buscarEventoVisible(idEvento);

        if (recital == null) {
            System.out.println("Evento inexistente o no compatible.");
            return;
        }

        // Imprime conectados/capacidad maxima.
        System.out.println("Capacidad actual: "
                + recital.getCantidadConectados()
                + "/"
                + recital.getCapacidadMaxima());
    }

    // Muestra y abre la transmision de un evento en curso.
    public static void verTransmision(Scanner scanner,
                                      ServicioEventos gestor) {

        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");
        mostrarTransmision(gestor, idEvento);
    }

    // Imprime la transmision del evento y la abre en el navegador.
    private static void mostrarTransmision(ServicioEventos gestor, int idEvento) {
        try {
            Transmision transmision = gestor.obtenerTransmision(idEvento);
            System.out.println("Transmision: " + transmision.titulo());
            System.out.println(transmision.url());
            Navegador.abrir(transmision.url());
        } catch (AccesoDenegadoException | IOException e) {
            System.out.println("No se pudo ver la transmision: " + e.getMessage());
        }
    }

    // Suscribe un calendario externo a un evento.
    public static void vincularCalendario(Scanner scanner,
                                          ServicioEventos gestor) {

        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        if (gestor.buscarEventoVisible(idEvento) == null) {
            System.out.println("Evento inexistente o no compatible.");
            return;
        }

        System.out.println("1. Google Calendar");
        System.out.println("2. Apple Calendar");
        int opcion = ConsoleIO.leerEntero(scanner, "Calendario: ");

        VinculoCalendario vinculo;

        if (opcion == 1) {
            vinculo = new VinculoGoogle();
        } else if (opcion == 2) {
            vinculo = new VinculoApple();
        } else {
            System.out.println("Opcion invalida.");
            return;
        }

        try {
            gestor.vincularCalendario(idEvento, vinculo);
            System.out.println("Se abrio " + vinculo.getNombre()
                    + " para suscribirse. Confirme alli.");
        } catch (IOException e) {
            System.out.println("No se pudo vincular: " + e.getMessage());
        }
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
    public static void cambiarEstadoEvento(Scanner scanner,
                                           ServicioEventos gestor,
                                           String accion) {

        // Muestra solo los eventos que pueden recibir esa accion.
        if (!listarEventosParaCambio(gestor, accion)) {
            return;
        }

        // Pide ID del evento a modificar.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento entre los que la cuenta puede gestionar.
        Evento evento = gestor.buscarEventoVisible(idEvento);

        // Si no existe, se informa y se corta.
        if (evento == null) {
            System.out.println("Evento inexistente.");
            return;
        }

        try {
            ejecutarCambioDeEstado(gestor, idEvento, accion);
        } catch (AccesoDenegadoException e) {
            System.out.println("No se pudo cambiar el estado: "
                    + e.getMessage());
        }
    }

    /*
     * Pide al gestor que ejecute la transicion de estado.
     *
     * El gestor centraliza las operaciones sobre Evento (patron
     * Controlador): esta clase no llama iniciarEvento/pausarEvento/etc
     * directamente sobre el Evento.
     */
    private static void ejecutarCambioDeEstado(ServicioEventos gestor,
                                               int idEvento,
                                               String accion)
            throws AccesoDenegadoException {

        if ("iniciar".equals(accion)) {
            gestor.iniciarEvento(idEvento);
            System.out.println("Evento iniciado.");
        } else if ("pausar".equals(accion)) {
            gestor.pausarEvento(idEvento);
            System.out.println("Evento pausado.");
        } else if ("reanudar".equals(accion)) {
            gestor.reanudarEvento(idEvento);
            System.out.println("Evento reanudado.");
        } else if ("finalizar".equals(accion)) {
            gestor.finalizarEvento(idEvento);
            System.out.println("Evento finalizado.");
        } else if ("cancelar".equals(accion)) {
            gestor.cancelarEvento(idEvento);
            System.out.println("Evento cancelado.");
        }
    }

    /*
     * Lista solo los eventos que tienen sentido para la accion elegida.
     *
     * Esto evita, por ejemplo, que al pausar aparezcan eventos que todavia
     * estan PROGRAMADO o EN_ESPERA.
     */
    private static boolean listarEventosParaCambio(ServicioEventos gestor,
                                                   String accion) {

        System.out.println("\nEventos disponibles para "
                + obtenerNombreAccion(accion)
                + ":");

        boolean hayEventos = false;

        for (Evento evento : gestor.listarEventosVisibles()) {
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
            return estado.permiteIniciar();
        }

        if ("pausar".equals(accion)) {
            return estado.permitePausar();
        }

        if ("reanudar".equals(accion)) {
            return estado.permiteReanudar();
        }

        if ("finalizar".equals(accion)) {
            return estado.permiteFinalizar();
        }

        // Solo se puede expulsar usuarios de eventos en curso.
        if ("expulsar".equals(accion)) {
            return estado == EstadoEvento.EN_CURSO;
        }

        if ("cancelar".equals(accion)) {
            return estado.permiteCancelar();
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

        if ("expulsar".equals(accion)) {
            return "expulsar";
        }

        if ("cancelar".equals(accion)) {
            return "cancelar";
        }

        return "modificar";
    }

    // Activa o desactiva un usuario elegido por ID.
    public static void cambiarEstadoUsuario(Scanner scanner,
                                            ServicioEventos gestor) {

        listarUsuarios(gestor);
        int idUsuario = ConsoleIO.leerEntero(scanner, "Ingrese ID de usuario: ");

        Usuario usuario = gestor.alternarActivo(idUsuario);

        if (usuario == null) {
            System.out.println("Usuario inexistente.");
            return;
        }

        System.out.println("Usuario " + usuario.getNombreUsuario()
                + (usuario.isActivo() ? " activado." : " desactivado."));
    }

    // Abre la ventana Swing reutilizando el mismo gestor del menu consola.
    public static void abrirInterfazGrafica(ServicioEventos gestor) {
        SwingUtilities.invokeLater(() ->
                new VentanaPrincipal(gestor, false).setVisible(true)
        );

        System.out.println("Interfaz grafica abierta.");
    }

    // Lista los registros de acceso de la cuenta que inicio sesion.
    public static void listarMisRegistros(ServicioEventos gestor) {
        List<RegistroAcceso> registros = gestor.listarMisRegistros();

        if (registros.isEmpty()) {
            System.out.println("No tiene registros de acceso.");
            return;
        }

        System.out.println("\nMis registros de acceso:");
        for (RegistroAcceso registro : registros) {
            System.out.println(registro);
        }
    }

    // Lista todos los registros de acceso guardados por el gestor.
    public static void listarRegistros(ServicioEventos gestor) {
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
}
