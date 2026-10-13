package app;

import gestor.ServicioEventos;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/*
 * Clase principal del programa.
 *
 * Esta clase funciona como interfaz de consola para probar el modulo.
 * No contiene las reglas de negocio principales: esas reglas estan en
 * GestorEventosEnVivo, AccesoEvento y RecitalEnVivo.
 *
 * Tampoco implementa como se ejecuta cada opcion del menu (eso vive en
 * MenuAcciones y MenuPorRol) ni la carga de datos de prueba (eso vive en
 * DatosIniciales). App solo arranca el programa y pide iniciar sesion
 * antes de mostrar cualquier menu de accion.
 */
public class App {

    // Punto de entrada del programa.
    public static void main(String[] args) throws IOException {

        // Scanner permite leer datos ingresados por consola.
        Scanner scanner = new Scanner(System.in);

        // Gestor central del modulo: administra eventos, usuarios y registros.
        ServicioEventos gestor = Arranque.iniciarServidor(false);

        try {
            int modo = elegirModo(scanner);

            if (modo == MODO_GRAFICO) {
                // La ventana de login se encarga del resto.
                AppSwing.abrir(gestor);
                return;
            }

            if (modo == MODO_CONSOLA) {
                // La primera pantalla es el login: sin sesion no hay menu.
                while (iniciarSesion(scanner, gestor)) {
                    // El menu que se muestra depende del rol de la cuenta.
                    MenuPorRol.ejecutar(scanner, gestor);
                    gestor.cerrarSesion();
                }
            }

            System.out.println("Programa finalizado.");
        } catch (NoSuchElementException e) {
            // La entrada se cerro (por ejemplo, Ctrl+D): termina sin error.
            System.out.println("\nEntrada finalizada.");
        }

        scanner.close();
    }

    private static final int MODO_GRAFICO = 1;
    private static final int MODO_CONSOLA = 2;

    // Pregunta con que interfaz se usa el programa. Devuelve 0 para salir.
    private static int elegirModo(Scanner scanner) {
        while (true) {
            System.out.println("\n===== UADE BEATS: EVENTOS EN VIVO =====");
            System.out.println("1. Interfaz grafica (Swing)");
            System.out.println("2. Consola (CLI)");
            System.out.println("0. Salir");

            int opcion = ConsoleIO.leerEntero(scanner, "Seleccione una opcion: ");

            if (opcion == 0 || opcion == MODO_GRAFICO || opcion == MODO_CONSOLA) {
                return opcion;
            }

            System.out.println("Opcion invalida.");
        }
    }

    /*
     * Pantalla de login. Repite hasta que se inicia sesion o se elige salir.
     *
     * Devuelve true si hay una cuenta con sesion iniciada y false si la
     * persona eligio salir del programa.
     */
    private static boolean iniciarSesion(Scanner scanner,
                                         ServicioEventos gestor) {

        while (true) {
            System.out.println("\n===== INICIAR SESION =====");
            System.out.println("1. Iniciar sesion");
            System.out.println("0. Salir");

            int opcion = ConsoleIO.leerEntero(scanner, "Seleccione una opcion: ");

            if (opcion == 0) {
                return false;
            }

            if (opcion == 1 && MenuAcciones.login(scanner, gestor) != null) {
                return true;
            }

            if (opcion != 1) {
                System.out.println("Opcion invalida.");
            }
        }
    }
}
