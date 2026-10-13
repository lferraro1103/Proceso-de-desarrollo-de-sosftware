package gestor;

/*
 * Acciones que una cuenta puede intentar ejecutar sobre el sistema.
 *
 * Cada accion lleva la etiqueta con la que se muestra en el menu, de modo
 * que los menus se arman a partir de los permisos del rol y no de listas
 * escritas a mano para cada pantalla.
 */
public enum Accion {

    REGISTRAR_USUARIO("Registrar usuario"),
    REGISTRAR_ARTISTA("Registrar artista"),
    ACTIVAR_DESACTIVAR_USUARIO("Activar o desactivar usuario"),
    CREAR_EVENTO("Crear evento"),
    VER_USUARIOS("Ver usuarios registrados"),
    VER_ARTISTAS("Ver artistas registrados"),
    VER_EVENTOS("Ver eventos"),
    INICIAR_EVENTO("Iniciar evento"),
    PAUSAR_EVENTO("Pausar evento"),
    REANUDAR_EVENTO("Reanudar evento"),
    FINALIZAR_EVENTO("Finalizar evento"),
    CANCELAR_EVENTO("Cancelar evento"),
    SOLICITAR_INGRESO("Solicitar ingreso a evento"),
    VER_CONECTADOS("Ver usuarios conectados a un evento"),
    VER_CAPACIDAD("Ver capacidad de un evento"),
    AGREGAR_A_CALENDARIO("Agregar evento a Google o Apple Calendar"),
    VINCULAR_CALENDARIO("Vincular mi calendario (Google o Apple)"),
    EXPULSAR_USUARIO("Expulsar usuario de un evento"),
    VER_REGISTROS("Ver registros de acceso"),
    VER_MIS_REGISTROS("Ver mis registros de acceso"),
    GUARDAR_DATOS("Guardar datos en archivos TXT"),
    CARGAR_DATOS("Cargar datos desde archivos TXT"),
    ABRIR_INTERFAZ_GRAFICA("Abrir interfaz grafica Swing");

    // Texto que se muestra en el menu.
    private final String etiqueta;

    Accion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
