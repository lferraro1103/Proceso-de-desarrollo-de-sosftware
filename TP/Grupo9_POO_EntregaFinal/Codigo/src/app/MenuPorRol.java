package app;

import gestor.Accion;
import excepciones.PermisoDenegadoException;
import gestor.GestorEventosEnVivo;

import java.util.List;
import java.util.Scanner;

/*
 * Menu de consola de la cuenta que inicio sesion.
 *
 * Es el unico menu del programa: sus opciones no estan escritas por rol,
 * se arman con las acciones que el gestor informa como permitidas para esa
 * cuenta. Asi el menu del usuario, del artista y del administrador salen
 * de la misma tabla de permisos.
 */
public final class MenuPorRol {

    private MenuPorRol() {
    }

    // Muestra el menu y ejecuta opciones hasta que se cierra la sesion.
    public static void ejecutar(Scanner scanner, GestorEventosEnVivo gestor) {

        while (true) {
            List<Accion> acciones = gestor.accionesPermitidas();

            mostrarMenu(gestor, acciones);

            int opcion = ConsoleIO.leerEntero(scanner, "Seleccione una opcion: ");

            if (opcion == 0) {
                System.out.println("Sesion cerrada.");
                return;
            }

            if (opcion < 1 || opcion > acciones.size()) {
                System.out.println("Opcion invalida.");
                continue;
            }

            Accion accion = acciones.get(opcion - 1);

            // El gestor vuelve a verificar el permiso aunque la opcion
            // haya salido del menu: la autorizacion no depende de la vista.
            try {
                if (!gestor.puede(accion)) {
                    throw new PermisoDenegadoException(
                            "Su rol no permite: " + accion.getEtiqueta() + ".");
                }

                ejecutarAccion(accion, scanner, gestor);
            } catch (PermisoDenegadoException e) {
                System.out.println("Accion no permitida: " + e.getMessage());
            }
        }
    }

    // Imprime las acciones permitidas numeradas, mas la salida de sesion.
    private static void mostrarMenu(GestorEventosEnVivo gestor,
                                    List<Accion> acciones) {

        System.out.println("\n===== MENU "
                + gestor.getCuentaActual().getTipoUsuario()
                + ": " + gestor.getCuentaActual().getNombreUsuario()
                + " =====");

        for (int i = 0; i < acciones.size(); i++) {
            System.out.println((i + 1) + ". " + acciones.get(i).getEtiqueta());
        }

        System.out.println("0. Cerrar sesion");
    }

    // Deriva cada accion a la operacion de consola que la implementa.
    private static void ejecutarAccion(Accion accion,
                                       Scanner scanner,
                                       GestorEventosEnVivo gestor) {

        switch (accion) {
            case REGISTRAR_USUARIO:
                MenuAcciones.registrarUsuario(scanner, gestor);
                break;
            case REGISTRAR_ARTISTA:
                MenuAcciones.registrarArtista(scanner, gestor);
                break;
            case CREAR_EVENTO:
                MenuAcciones.crearEventoManual(scanner, gestor);
                break;
            case VER_USUARIOS:
                MenuAcciones.listarUsuarios(gestor);
                break;
            case VER_ARTISTAS:
                MenuAcciones.listarArtistas(gestor);
                break;
            case VER_EVENTOS:
                MenuAcciones.listarEventos(gestor);
                break;
            case INICIAR_EVENTO:
                MenuAcciones.cambiarEstadoEvento(scanner, gestor, "iniciar");
                break;
            case PAUSAR_EVENTO:
                MenuAcciones.cambiarEstadoEvento(scanner, gestor, "pausar");
                break;
            case REANUDAR_EVENTO:
                MenuAcciones.cambiarEstadoEvento(scanner, gestor, "reanudar");
                break;
            case FINALIZAR_EVENTO:
                MenuAcciones.cambiarEstadoEvento(scanner, gestor, "finalizar");
                break;
            case CANCELAR_EVENTO:
                MenuAcciones.cambiarEstadoEvento(scanner, gestor, "cancelar");
                break;
            case SOLICITAR_INGRESO:
                MenuAcciones.solicitarIngreso(scanner, gestor);
                break;
            case VER_CONECTADOS:
                MenuAcciones.listarConectados(scanner, gestor);
                break;
            case VER_CAPACIDAD:
                MenuAcciones.mostrarCapacidad(scanner, gestor);
                break;
            case EXPULSAR_USUARIO:
                MenuAcciones.expulsarUsuario(scanner, gestor);
                break;
            case VER_REGISTROS:
                MenuAcciones.listarRegistros(gestor);
                break;
            case VER_MIS_REGISTROS:
                MenuAcciones.listarMisRegistros(gestor);
                break;
            case GUARDAR_DATOS:
                MenuAcciones.guardarDatos(gestor);
                break;
            case CARGAR_DATOS:
                MenuAcciones.cargarDatos(gestor);
                break;
            case ABRIR_INTERFAZ_GRAFICA:
                MenuAcciones.abrirInterfazGrafica(gestor);
                break;
        }
    }
}
