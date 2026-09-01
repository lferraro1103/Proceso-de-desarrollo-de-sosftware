package modelo;

/*
 * Rol de una cuenta dentro de UADE Beats.
 *
 * Un Administrador es un Usuario con tipoUsuario=ADMINISTRADOR (no una
 * clase Java aparte); este enum es la unica forma de distinguir el rol.
 */
public enum TipoUsuario {
    USUARIO,
    ADMINISTRADOR,
    ARTISTA
}
