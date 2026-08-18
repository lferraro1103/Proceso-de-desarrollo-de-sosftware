/*
 * Lanzador de compatibilidad.
 *
 * La clase principal real esta organizada en el paquete app.App.
 * Este archivo permite ejecutar el proyecto, usando Main,
 * sin romper la organizacion por paquetes del resto del codigo.
 */
public class Main {

    public static void main(String[] args) {
        // Mantengo este Main para que el proyecto pueda ejecutarse como antes.
        // La aplicacion real esta en app.App.
        app.App.main(args);
    }
}
