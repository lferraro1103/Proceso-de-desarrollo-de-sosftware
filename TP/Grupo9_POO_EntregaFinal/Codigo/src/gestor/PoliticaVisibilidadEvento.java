package gestor;

import modelo.Cuenta;
import modelo.Evento;

/** Strategy: determina el alcance de consulta de una cuenta sobre un evento. */
public interface PoliticaVisibilidadEvento {
    boolean esVisible(Cuenta cuenta, Evento evento);
}
