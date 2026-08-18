package modelo;

/*
 * Enum con los planes de suscripcion obligatorios de la consigna.
 *
 * Cada plan tiene un nivel numerico para poder comparar permisos.
 * Un nivel mas alto puede acceder a eventos que pidan niveles mas bajos.
 */
public enum PlanSuscripcion {

    // Plan basico.
    FREE(1),

    // Plan intermedio.
    PREMIUM(2),

    // Plan de mayor nivel segun la estructura base.
    ARTIST_PASS(3);

    // Valor usado para comparar jerarquia de planes.
    private final int nivel;

    // Constructor interno del enum.
    PlanSuscripcion(int nivel) {
        this.nivel = nivel;
    }

    // Devuelve el nivel numerico del plan.
    public int getNivel() {
        return nivel;
    }

    // Indica si este plan alcanza o supera el plan requerido.
    public boolean cubre(PlanSuscripcion planRequerido) {
        return nivel >= planRequerido.nivel;
    }
}
