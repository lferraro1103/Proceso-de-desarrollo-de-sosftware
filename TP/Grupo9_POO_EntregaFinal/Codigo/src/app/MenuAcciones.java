package app;

import excepciones.AccesoDenegadoException;
import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.DatosRecital;
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
 * Implementacion de cada opcion del menu de consola.
 *
 * Se separo de App para que App se ocupe solo de arrancar el programa
 * y despachar la opcion elegida; aca vive como se ejecuta cada accion.
 */
public final class MenuAcciones {

    // ID incremental para usuarios nuevos cargados desde el menu.
    private static int siguienteIdUsuario = 4;

    // ID incremental para artistas nuevos cargados desde el menu.
    private static int siguienteIdArtista = 3;

    // ID incremental para eventos nuevos cargados desde el menu.
    private static int siguienteIdEvento = 102;

    private MenuAcciones() {
    }

    /*
     * Login simple por consola.
     *
     * Pide nombre de usuario y contrasena, busca el usuario y valida
     * la contrasena. Si es correcta, inicia sesion (Usuario.iniciarSesion
     * ya contempla que el usuario este activo).
     */
    public static void login(Scanner scanner, GestorEventosEnVivo gestor) {
        System.out.print("Ingrese nombre de usuario: ");
        String nombreUsuario = scanner.nextLine().trim();

        Usuario usuario = gestor.buscarUsuarioPorNombreUsuario(nombreUsuario);

        if (usuario == null) {
            System.out.println("Usuario inexistente.");
            return;
        }

        System.out.print("Ingrese contrasena: ");
        String contrasena = scanner.nextLine();

        if (!usuario.validarContrasena(contrasena)) {
            System.out.println("Contrasena incorrecta.");
            return;
        }

        if (!usuario.isActivo()) {
            System.out.println("El usuario no esta activo.");
            return;
        }

        usuario.iniciarSesion();
        System.out.println("Sesion iniciada. Bienvenido, "
                + usuario.getNombreCompleto() + ".");
    }

    /*
     * Registra un usuario nuevo desde consola.
     *
     * Pide los datos basicos, permite seleccionar plan y luego lo guarda
     * dentro del gestor.
     */
    public static void registrarUsuario(Scanner scanner,
                                        GestorEventosEnVivo gestor) {

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

        // Crea el usuario con ID incremental y la contrasena ingresada.
        Usuario nuevoUsuario = new Usuario(
                siguienteIdUsuario,
                nombreUsuario,
                nombre,
                apellido,
                email,
                contrasena,
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

        // Persiste de una para no perder el alta si se cierra sin guardar.
        guardarDatos(gestor);
    }

    /*
     * Pide el nombre de cuenta y valida que no este vacio ni repetido.
     *
     * Devuelve null si la validacion falla; en ese caso ya se informo
     * el motivo por consola.
     */
    private static String leerNombreUsuarioNuevo(Scanner scanner,
                                                 GestorEventosEnVivo gestor) {

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
                                        GestorEventosEnVivo gestor) {

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

        // Persiste de una para no perder el alta si se cierra sin guardar.
        guardarDatos(gestor);
    }

    /*
     * Crea un evento en vivo desde consola.
     *
     * Primero se elige un artista y luego se arma el RecitalEnVivo.
     * El gestor lo crea usando la relacion con Artista.
     */
    public static void crearEventoManual(Scanner scanner,
                                         GestorEventosEnVivo gestor) {

        // Paso 1: elegir el artista que va a estar asociado al evento.
        Artista artista = seleccionarArtistaParaEvento(scanner, gestor);
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
        RecitalEnVivo recital = new RecitalEnVivo(siguienteIdEvento, datosRecital);

        // Paso 5: darlo de alta en el gestor e informar el resultado.
        gestor.crearEvento(recital, artista);

        System.out.println("Evento creado correctamente.");
        System.out.println("ID asignado: " + siguienteIdEvento);
        System.out.println("Artista asociado: "
                + artista.getNombreArtistico());
        siguienteIdEvento++;
    }

    /*
     * Muestra los artistas disponibles y devuelve el elegido por ID.
     *
     * Devuelve null si no hay artistas cargados o si el ID no existe;
     * en ambos casos ya se informo el motivo por consola.
     */
    private static Artista seleccionarArtistaParaEvento(
            Scanner scanner, GestorEventosEnVivo gestor) {

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
    public static void listarArtistas(GestorEventosEnVivo gestor) {
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
    public static void listarUsuarios(GestorEventosEnVivo gestor) {
        System.out.println("\nUsuarios registrados:");

        // El toString() de Usuario define como se muestra cada usuario.
        for (Usuario usuario : gestor.listarUsuarios()) {
            System.out.println(usuario);
        }
    }

    // Lista todos los eventos administrados por el gestor.
    public static void listarEventos(GestorEventosEnVivo gestor) {
        System.out.println("\nEventos administrados:");

        // El toString() de Evento/RecitalEnVivo define el formato de salida.
        for (Evento evento : gestor.listarEventos()) {
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
                                        GestorEventosEnVivo gestor) {

        // Muestra usuarios para que el operador elija uno.
        listarUsuarios(gestor);
        int idUsuario = ConsoleIO.leerEntero(scanner, "Ingrese ID de usuario: ");

        // Muestra eventos para que el operador elija uno.
        listarEventos(gestor);
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // El gestor coordina validaciones y devuelve el registro generado.
        RegistroAcceso registro = gestor.solicitarIngreso(idUsuario, idEvento);

        // Muestra el resultado final al usuario de consola.
        if (registro.isExitoso()) {
            System.out.println("Ingreso autorizado.");
        } else {
            System.out.println("Ingreso rechazado: "
                    + registro.getMotivoRechazo());
        }
    }

    /*
     * Busca un evento por ID y lo devuelve solo si es un RecitalEnVivo.
     *
     * Centraliza una validacion que antes estaba repetida igual en
     * listarConectados, expulsarUsuario y mostrarCapacidad.
     */
    private static RecitalEnVivo buscarRecitalPorId(GestorEventosEnVivo gestor,
                                                     int idEvento) {

        Evento evento = gestor.buscarEventoPorId(idEvento);

        if (!(evento instanceof RecitalEnVivo)) {
            return null;
        }

        return (RecitalEnVivo) evento;
    }

    // Lista usuarios conectados a un evento puntual.
    public static void listarConectados(Scanner scanner,
                                        GestorEventosEnVivo gestor) {

        // Pide ID del evento a consultar.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento y valida que sea del tipo RecitalEnVivo.
        RecitalEnVivo recital = buscarRecitalPorId(gestor, idEvento);

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
                                       GestorEventosEnVivo gestor) {

        // Muestra solo eventos activos, porque no se expulsa desde pausados.
        // Reutiliza listarEventosParaCambio con la accion "expulsar" en vez
        // de tener un metodo aparte que filtraba lo mismo (EN_CURSO).
        if (!listarEventosParaCambio(gestor, "expulsar")) {
            return;
        }

        // Pide el evento sobre el que se quiere operar.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        RecitalEnVivo recital = buscarRecitalPorId(gestor, idEvento);

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
    public static void mostrarCapacidad(Scanner scanner,
                                        GestorEventosEnVivo gestor) {

        // Pide ID del evento.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento y valida que sea del tipo RecitalEnVivo.
        RecitalEnVivo recital = buscarRecitalPorId(gestor, idEvento);

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
                                           GestorEventosEnVivo gestor,
                                           String accion) {

        // Muestra solo los eventos que pueden recibir esa accion.
        if (!listarEventosParaCambio(gestor, accion)) {
            return;
        }

        // Pide ID del evento a modificar.
        int idEvento = ConsoleIO.leerEntero(scanner, "Ingrese ID de evento: ");

        // Busca el evento en el gestor.
        Evento evento = gestor.buscarEventoPorId(idEvento);

        // Si no existe, se informa y se corta.
        if (evento == null) {
            System.out.println("Evento inexistente.");
            return;
        }

        try {
            ejecutarCambioDeEstado(evento, accion);
        } catch (AccesoDenegadoException e) {
            System.out.println("No se pudo cambiar el estado: "
                    + e.getMessage());
        }
    }

    // Ejecuta sobre el evento la transicion de estado pedida por accion.
    private static void ejecutarCambioDeEstado(Evento evento, String accion)
            throws AccesoDenegadoException {

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
        } else if ("cancelar".equals(accion)) {
            evento.cancelarEvento();
            System.out.println("Evento cancelado.");
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

        // Solo se puede expulsar usuarios de eventos en curso.
        if ("expulsar".equals(accion)) {
            return estado == EstadoEvento.EN_CURSO;
        }

        // Se puede cancelar cualquier evento que no haya terminado su
        // ciclo de vida (ya finalizado o ya cancelado).
        if ("cancelar".equals(accion)) {
            return estado != EstadoEvento.FINALIZADO
                    && estado != EstadoEvento.CANCELADO;
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

    // Guarda usuarios, artistas, eventos y registros en archivos TXT.
    public static void guardarDatos(GestorEventosEnVivo gestor) {
        try {
            PersistenciaArchivos.guardarDatos(gestor);
            System.out.println("Datos guardados correctamente en carpeta datos.");
        } catch (IOException e) {
            System.out.println("No se pudieron guardar los datos: "
                    + e.getMessage());
        }
    }

    // Carga usuarios, artistas y eventos desde archivos TXT.
    public static void cargarDatos(GestorEventosEnVivo gestor) {
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
    public static void abrirInterfazGrafica(GestorEventosEnVivo gestor) {
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
    public static void listarRegistros(GestorEventosEnVivo gestor) {
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
