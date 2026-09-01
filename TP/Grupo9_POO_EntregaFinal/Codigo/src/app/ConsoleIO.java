package app;

import java.util.Scanner;

/*
 * Utilidades de lectura por consola.
 *
 * Se separo de App para que la lectura/validacion de datos ingresados
 * por teclado no se mezcle con la logica de menu ni con las acciones
 * de negocio (antes todo vivia junto en una unica clase).
 */
public final class ConsoleIO {

    private ConsoleIO() {
    }

    /*
     * Lee un numero entero de forma segura.
     *
     * Si el usuario escribe texto u otro dato invalido, no rompe el
     * programa: muestra un mensaje y vuelve a pedir el numero.
     */
    public static int leerEntero(Scanner scanner, String mensaje) {
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
    public static boolean leerBooleano(Scanner scanner, String mensaje) {
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
