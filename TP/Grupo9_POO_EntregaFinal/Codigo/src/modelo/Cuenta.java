package modelo;

/*
 * Clase base comun para toda cuenta con login en UADE Beats.
 *
 * Concentra los datos y el comportamiento de credenciales/sesion que antes
 * vivian duplicados en Usuario (y que Artista no tenia). Usuario y Artista
 * heredan de aca; un Administrador es simplemente un Usuario con
 * tipoUsuario=ADMINISTRADOR, no hay una clase Java propia para ese rol.
 */
public abstract class Cuenta {

    // Identificador unico de la cuenta.
    private int id;

    // Nombre de cuenta dentro de la plataforma.
    private String nombreUsuario;

    // Hash de la contrasena (ver Contrasenas); nunca el texto original.
    private String hashContrasena;

    // Indica si la cuenta esta habilitada en la plataforma.
    private boolean activo;

    // Indica si inicio sesion.
    private boolean sesionIniciada;

    // Rol de la cuenta (Usuario, Administrador o Artista).
    private TipoUsuario tipoUsuario;

    protected Cuenta(int id,
                      String nombreUsuario,
                      String hashContrasena,
                      boolean activo,
                      TipoUsuario tipoUsuario) {

        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
        this.activo = activo;
        this.tipoUsuario = tipoUsuario;
        this.sesionIniciada = false;
    }

    // Inicia sesion solo si la cuenta esta activa.
    public void iniciarSesion() {
        if (activo) {
            sesionIniciada = true;
        }
    }

    /*
     * Valida la contrasena ingresada para el login.
     *
     * Se compara contra el hash guardado: la contrasena original no se
     * conserva en ningun lugar.
     */
    public boolean validarContrasena(String contrasenaIngresada) {
        // Una cuenta sin contrasena (datos guardados antes de pedir
        // credenciales) no puede iniciar sesion hasta que se le asignen.
        return !hashContrasena.isEmpty()
                && Contrasenas.verificar(contrasenaIngresada, hashContrasena);
    }

    // Deshabilita la cuenta: no puede iniciar sesion hasta que se la active.
    public void desactivar() {
        activo = false;
        sesionIniciada = false;
    }

    // Vuelve a habilitar la cuenta.
    public void activar() {
        activo = true;
    }

    // Cierra la sesion de la cuenta.
    public void cerrarSesion() {
        sesionIniciada = false;
    }

    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    // Getter usado solo por la capa de persistencia para guardar la cuenta.
    public String getHashContrasena() {
        return hashContrasena;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isSesionIniciada() {
        return sesionIniciada;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
}
