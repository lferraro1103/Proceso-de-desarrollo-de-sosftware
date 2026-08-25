package modelo;

import java.util.Objects;

/*
 * Entidad comun Usuario.
 *
 * Esta clase fue ampliada para respetar la estructura base obligatoria
 * de la consigna. Se usa en el modulo para validar planes, usuarios
 * activos y usuarios conectados a eventos.
 */
public class Usuario {

    // Identificador unico del usuario.
    private int id;

    // Nombre de cuenta dentro de la plataforma.
    private String nombreUsuario;

    // Nombre real.
    private String nombre;

    // Apellido real.
    private String apellido;

    // Email de contacto.
    private String email;

    // Contrasena simplificada para el prototipo.
    private String contrasena;

    // Plan contratado por el usuario.
    private PlanSuscripcion planSuscripcion;

    // Indica si el usuario esta habilitado en la plataforma.
    private boolean activo;

    // Indica si inicio sesion.
    private boolean sesionIniciada;

    // Constructor completo alineado con la estructura base.
    public Usuario(int id,
                   String nombreUsuario,
                   String nombre,
                   String apellido,
                   String email,
                   String contrasena,
                   PlanSuscripcion planSuscripcion,
                   boolean activo) {

        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasena = contrasena;
        this.planSuscripcion = planSuscripcion;
        this.activo = activo;
        this.sesionIniciada = false;
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

    // Inicia sesion solo si el usuario esta activo.
    public void iniciarSesion() {
        if (activo) {
            sesionIniciada = true;
        }
    }

    /*
     * Valida la contrasena ingresada para el login.
     *
     * No expone la contrasena con un getter: la comparacion se hace
     * adentro de Usuario para mantener encapsulado ese dato.
     */
    public boolean validarContrasena(String contrasenaIngresada) {
        return contrasena.equals(contrasenaIngresada);
    }

    // Cierra la sesion del usuario.
    public void cerrarSesion() {
        sesionIniciada = false;
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
        activo = true;
    }

    // Getters de consulta.
    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

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

    public boolean isActivo() {
        return activo;
    }

    public boolean isSesionIniciada() {
        return sesionIniciada;
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
        return id == usuario.id;
    }

    // hashCode debe ser coherente con equals para funcionar bien en HashSet.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Texto legible para imprimir usuarios en el menu.
    @Override
    public String toString() {
        return "ID: " + id
                + " | Usuario: " + nombreUsuario
                + " | Nombre: " + getNombreCompleto()
                + " | Plan: " + planSuscripcion
                + " | Activo: " + activo;
    }
}
