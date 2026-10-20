package gestor;

import java.time.LocalDateTime;

/*
 * Lo que hace falta para buscar la transmision de un evento: el artista a
 * buscar y cuando empezo el evento, para que el video arranque en el minuto
 * que corresponde.
 */
public record ConsultaTransmision(String artista, LocalDateTime inicio) {
}
