package app;

import gestor.GestorEventosEnVivo;
import persistencia.PersistenciaArchivos;

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
    public static void main(String[] args) {

        // Scanner permite leer datos ingresados por consola.
        Scanner scanner = new Scanner(System.in);

        // Gestor central del modulo: administra eventos, usuarios y registros.
        GestorEventosEnVivo gestor = new GestorEventosEnVivo(new PersistenciaArchivos());

        // Carga usuarios, artistas y recitales iniciales para poder probar.
        DatosIniciales.cargar(gestor);

        // La primera pantalla es el login: sin sesion no hay menu de accion.
        while (iniciarSesion(scanner, gestor)) {
            // El menu que se muestra depende del rol de la cuenta.
            MenuPorRol.ejecutar(scanner, gestor);
            gestor.cerrarSesion();
        }

        System.out.println("Programa finalizado.");
        scanner.close();
    }

    /*
     * Pantalla de login. Repite hasta que se inicia sesion o se elige salir.
     *
     * Devuelve true si hay una cuenta con sesion iniciada y false si la
     * persona eligio salir del programa.
     */
    private static boolean iniciarSesion(Scanner scanner,
                                         GestorEventosEnVivo gestor) {

        while (true) {
            System.out.println("\n===== UADE BEATS: EVENTOS EN VIVO =====");
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
