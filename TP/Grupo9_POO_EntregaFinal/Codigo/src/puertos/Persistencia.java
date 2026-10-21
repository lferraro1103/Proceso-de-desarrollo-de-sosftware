package puertos;

import gestor.GestorEventosEnVivo;
import java.io.IOException;

/*
 * Contrato de persistencia que necesita el gestor.
 *
 * El gestor depende de esta abstraccion y no de una tecnologia concreta de
 * almacenamiento: guardar en archivos de texto es una implementacion, y
 * otra (por ejemplo una base de datos) podria reemplazarla sin modificar
 * el gestor ni las vistas.
 */
public interface Persistencia {

    // Guarda los datos del gestor.
    void guardar(GestorEventosEnVivo gestor) throws IOException;

    // Carga los datos guardados dentro del gestor.
    void cargar(GestorEventosEnVivo gestor) throws IOException;
}
