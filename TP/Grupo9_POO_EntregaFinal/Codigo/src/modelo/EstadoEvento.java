package modelo;

/*
 * Enum con los estados posibles de un evento en vivo.
 *
 * Sirve para evitar usar strings sueltos como "activo", "pausado", etc.
 * De esta forma el codigo solo acepta estados validos.
 */
public enum EstadoEvento {

    // Evento creado, pero todavia no disponible para ingresar.
    PROGRAMADO,

    // Evento preparado o en sala de espera previa.
    EN_ESPERA,

    // Unico estado en el que se permite el ingreso de usuarios.
    EN_CURSO,

    // Evento detenido temporalmente.
    PAUSADO,

    // Evento terminado.
    FINALIZADO,

    // Evento cancelado.
    CANCELADO;

    // Regla de negocio: solo se puede entrar si el evento esta en curso.
    public boolean admiteIngreso() {
        return this == EN_CURSO;
    }
}
