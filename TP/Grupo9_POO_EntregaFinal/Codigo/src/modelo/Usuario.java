package modelo;

import java.util.Objects;

/*
 * Entidad comun Usuario.
 *
 * Hereda de Cuenta el id, nombreUsuario, contrasena, activo, sesionIniciada
 * y tipoUsuario. Un Administrador es un Usuario con
 * tipoUsuario=ADMINISTRADOR, no hay una clase Java aparte para ese rol.
 */
public class Usuario extends Cuenta {

    // Nombre real.
    private String nombre;

    // Apellido real.
    private String apellido;

    // Email de contacto.
    private String email;

    // Plan contratado por el usuario.
    private PlanSuscripcion planSuscripcion;

    // Constructor completo con rol explicito.
    public Usuario(int id,
                   String nombreUsuario,
                   String nombre,
                   String apellido,
                   String email,
                   String contrasena,
                   PlanSuscripcion planSuscripcion,
                   boolean activo,
                   TipoUsuario tipoUsuario) {

        super(id, nombreUsuario, contrasena, activo, tipoUsuario);
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.planSuscripcion = planSuscripcion;
    }

    /*
     * Constructor de compatibilidad (mismo orden que el original de 8
     * parametros). Delega con TipoUsuario.USUARIO por defecto para no
     * romper a los call sites existentes (DatosIniciales, MenuAcciones,
     * VentanaPrincipal, GestorEventosEnVivo.registrarFalloSinUsuario).
     */
    public Usuario(int id,
                   String nombreUsuario,
                   String nombre,
                   String apellido,
                   String email,
                   String contrasena,
                   PlanSuscripcion planSuscripcion,
                   boolean activo) {

        this(id, nombreUsuario, nombre, apellido, email, contrasena,
                planSuscripcion, activo, TipoUsuario.USUARIO);
    }

    // Constructor corto usado para pruebas rapidas.
    public Usuario(int id, String nombre, PlanSuscripcion planSuscripcion) {
        this(id,
                nombre.toLowerCase(),
                nombre,
                "",
                nombre.toLowerCase() + "@uadebeats.com",
                "1234",
                planSuscripcion,
                true);
    }

    // Actualiza datos principales del perfil.
    public void actualizarPerfil(String nombre,
                                 String apellido,
                                 String email) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
    }

    // Version simple para cumplir el metodo de la estructura base.
    public void actualizarPerfil() {
        // Nada que hacer aca: activo ahora vive en Cuenta y ya se define
        // en el constructor. Se mantiene el metodo por compatibilidad con
        // la estructura base de la consigna.
    }

    // Getters de consulta.
    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public PlanSuscripcion getPlanSuscripcion() {
        return planSuscripcion;
    }

    // El plan ARTIST_PASS habilita acceso prioritario a los eventos.
    public boolean tieneAccesoPrioritario() {
        return planSuscripcion == PlanSuscripcion.ARTIST_PASS;
    }

    public String getNombreCompleto() {
        return (nombre + " " + apellido).trim();
    }

    /*
     * equals compara usuarios por id.
     *
     * Esto es importante porque RecitalEnVivo usa Set<Usuario>.
     * Si dos objetos Usuario tienen el mismo id, se consideran el mismo
     * usuario y el Set evita conectarlos dos veces.
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Usuario usuario = (Usuario) obj;
        return getId() == usuario.getId();
    }

    // hashCode debe ser coherente con equals para funcionar bien en HashSet.
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    // Texto legible para imprimir usuarios en el menu.
    @Override
    public String toString() {
        return "ID: " + getId()
                + " | Usuario: " + getNombreUsuario()
                + " | Nombre: " + getNombreCompleto()
                + " | Plan: " + planSuscripcion
                + " | Activo: " + isActivo();
    }
}
