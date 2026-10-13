package gestor;

import modelo.TipoUsuario;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Unica fuente de verdad sobre que puede hacer cada rol.
 *
 * Los menus y el gestor consultan esta clase: ninguno guarda su propia
 * lista de opciones permitidas. Para cambiar los permisos de un rol alcanza
 * con modificar la tabla de este constructor.
 */
public class Permisos {

    // Acciones permitidas para cada tipo de cuenta.
    private final Map<TipoUsuario, Set<Accion>> permitidas;

    public Permisos() {
        permitidas = new EnumMap<>(TipoUsuario.class);

        // Usuario: solo consumo.
        permitidas.put(TipoUsuario.USUARIO, EnumSet.of(
                Accion.SOLICITAR_INGRESO,
                Accion.VER_EVENTOS,
                Accion.VER_CAPACIDAD,
                Accion.VINCULAR_CALENDARIO,
                Accion.VER_MIS_REGISTROS,
                Accion.ABRIR_INTERFAZ_GRAFICA
        ));

        // Artista: crea y gestiona sus propios eventos y los ve en lectura.
        permitidas.put(TipoUsuario.ARTISTA, EnumSet.of(
                Accion.CREAR_EVENTO,
                Accion.VER_EVENTOS,
                Accion.INICIAR_EVENTO,
                Accion.PAUSAR_EVENTO,
                Accion.REANUDAR_EVENTO,
                Accion.FINALIZAR_EVENTO,
                Accion.CANCELAR_EVENTO,
                Accion.VER_CONECTADOS,
                Accion.VER_CAPACIDAD,
                Accion.VINCULAR_CALENDARIO,
                Accion.ABRIR_INTERFAZ_GRAFICA
        ));

        // Administrador: gestion total (todo salvo "mis registros", que es
        // una vista propia de quien consume).
        Set<Accion> administrador = EnumSet.allOf(Accion.class);
        administrador.remove(Accion.VER_MIS_REGISTROS);
        permitidas.put(TipoUsuario.ADMINISTRADOR, administrador);
    }

    // Indica si el rol puede ejecutar la accion.
    public boolean permite(TipoUsuario tipo, Accion accion) {
        return permitidas.get(tipo).contains(accion);
    }

    // Devuelve las acciones del rol, en el orden en que se declaran.
    public List<Accion> accionesPermitidas(TipoUsuario tipo) {
        List<Accion> acciones = new ArrayList<>();

        for (Accion accion : Accion.values()) {
            if (permite(tipo, accion)) {
                acciones.add(accion);
            }
        }

        return acciones;
    }
}
