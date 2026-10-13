package gestor;

/*
 * Todo lo que las vistas (ventana y consola) piden al sistema.
 *
 * Lo implementan GestorEventosEnVivo, que trabaja sobre los datos de este
 * programa, y GestorRemoto, que reenvia cada pedido a un servidor. Las
 * vistas dependen solo de esta interfaz, por lo que no saben cual de las
 * dos estan usando.
 */
public interface ServicioEventos
        extends ServicioSesion, ServicioConsultas, ServicioComandos {
}
