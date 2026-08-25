package app;

import gestor.GestorEventosEnVivo;

import java.util.Scanner;

/*
 * Clase principal del programa.
 *
 * Esta clase funciona como interfaz de consola para probar el modulo.
 * No contiene las reglas de negocio principales: esas reglas estan en
 * GestorEventosEnVivo, AccesoEvento y RecitalEnVivo.
 *
 * Tampoco implementa como se ejecuta cada opcion del menu (eso vive en
 * MenuAcciones) ni la carga de datos de prueba (eso vive en
 * DatosIniciales). App solo arranca el programa y despacha la opcion
 * elegida a quien corresponda.
 */
public class App {

    // Punto de entrada del programa.
    public static void main(String[] args) {

        // Scanner permite leer datos ingresados por consola.
        Scanner scanner = new Scanner(System.in);

        // Gestor central del modulo: administra eventos, usuarios y registros.
        GestorEventosEnVivo gestor = new GestorEventosEnVivo();

        // Carga usuarios, artistas y recitales iniciales para poder probar.
        DatosIniciales.cargar(gestor);

        // Guarda la opcion elegida por el usuario en el menu.
        int opcion;

        // El menu se repite hasta que el usuario elija 0.
        do {
            // Muestra opciones disponibles.
            mostrarMenu();

            // Lee una opcion numerica validada.
            opcion = ConsoleIO.leerEntero(scanner, "Seleccione una opcion: ");

            // Ejecuta una accion segun la opcion elegida.
            switch (opcion) {
                case 1:
                    // Permite cargar un usuario nuevo.
                    MenuAcciones.registrarUsuario(scanner, gestor);
                    break;

                case 2:
                    // Permite registrar un artista nuevo.
                    MenuAcciones.registrarArtista(scanner, gestor);
                    break;

                case 3:
                    // Permite crear un evento y asociarlo a un artista.
                    MenuAcciones.crearEventoManual(scanner, gestor);
                    break;

                case 4:
                    // Muestra usuarios registrados.
                    MenuAcciones.listarUsuarios(gestor);
                    break;

                case 5:
                    // Muestra artistas registrados.
                    MenuAcciones.listarArtistas(gestor);
                    break;

                case 6:
                    // Muestra eventos administrados por el gestor.
                    MenuAcciones.listarEventos(gestor);
                    break;

                case 7:
                    // Cambia el estado de un evento a EN_CURSO.
                    MenuAcciones.cambiarEstadoEvento(scanner, gestor, "iniciar");
                    break;

                case 8:
                    // Pide usuario y evento, y solicita ingreso al gestor.
                    MenuAcciones.solicitarIngreso(scanner, gestor);
                    break;

                case 9:
                    // Lista usuarios conectados a un recital.
                    MenuAcciones.listarConectados(scanner, gestor);
                    break;

                case 10:
                    // Muestra capacidad usada y maxima de un recital.
                    MenuAcciones.mostrarCapacidad(scanner, gestor);
                    break;

                case 11:
                    // Expulsa/desconecta un usuario de un recital.
                    MenuAcciones.expulsarUsuario(scanner, gestor);
                    break;

                case 12:
                    // Cambia el estado de un evento a PAUSADO.
                    MenuAcciones.cambiarEstadoEvento(scanner, gestor, "pausar");
                    break;

                case 13:
                    // Cambia el estado de un evento a EN_CURSO.
                    MenuAcciones.cambiarEstadoEvento(scanner, gestor, "reanudar");
                    break;

                case 14:
                    // Cambia el estado de un evento a FINALIZADO.
                    MenuAcciones.cambiarEstadoEvento(scanner, gestor, "finalizar");
                    break;

                case 15:
                    // Muestra todos los intentos de acceso registrados.
                    MenuAcciones.listarRegistros(gestor);
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
        System.out.println("0. Salir");
    }
}
