package puertos;

import java.time.LocalDateTime;

/*
 * Lo que hace falta para buscar la transmision de un evento: el artista a
 * buscar y cuando empezo el evento, para que el video arranque en el minuto
 * que corresponde. El espectador identifica a quien mira (cuenta y evento),
 * para poder cerrar su transmision cuando sale.
 */
public record ConsultaTransmision(String artista, LocalDateTime inicio, String espectador) {
}
