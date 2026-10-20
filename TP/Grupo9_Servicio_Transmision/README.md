# Servicio de transmisiones

Microservicio aparte del programa principal de UADE Beats. Dado el nombre de un artista y la hora en que empezó el evento, busca un recital suyo en YouTube y lo muestra en una página con el reproductor, como si fuera una transmisión en vivo. Está hecho en Node.js, sin dependencias.

## Ejecución

Requiere Node.js 18 o superior. Normalmente no hace falta iniciarlo: el programa principal lo arranca solo en los modos completo y servidor, y lo cierra al terminar. Para usarlo por separado:

```
node servidor.js
```

Escucha en el puerto 8766 (se cambia con la variable `PORT`). Si ya hay uno funcionando, el programa principal lo usa y no lo toca. Si el programa que lo inició desaparece (variable `PADRE`), se cierra solo.

Mientras nadie pide una transmisión no hace nada ni guarda nada: es solo un proceso esperando.

## Rutas

| Ruta | Respuesta |
|---|---|
| `GET /salud` | `{ "estado": "ok" }` |
| `GET /transmision?q=Sabrina+Carpenter&inicio=2026-10-20T22:00:00` | JSON con `titulo`, `canal`, `duracion`, `enVivo`, `url` (YouTube), `ruta` (la página del reproductor) y `alternativas`. 404 si no hay ningún recital del artista. |
| `GET /reproductor?v=<id>&alt=<ids>&inicio=<ms>&titulo=<texto>` | Página con el reproductor |
| `GET /hora` | Hora del servidor en milisegundos |
| `GET /latido?e=<espectador>` | `{ vigente }`. La página lo pide cada 4 segundos. |
| `GET /salida?e=<espectador>` | `{ cerrados }`. Cierra espectadores; el nombre acepta `*` como comodín. |

## El reproductor

La página de `/reproductor` simula una transmisión en vivo:

- **Minuto del directo:** arranca en el minuto que corresponde. Si el evento empezó a las 22:00 y son las 22:30, el video empieza en el minuto 30. Usa la hora del servidor, no la de la PC de quien mira.
- **Sin controles:** no muestra la barra de tiempo, tiene una capa que bloquea los clics y desactiva el teclado.
- **Sin adelantar, volver ni pausar:** cada segundo compara el video con el minuto esperado. Si se atrasó, se adelantó o se pausó, lo devuelve al minuto del directo y lo reanuda.
- **Sonido:** arranca silenciado porque los navegadores no permiten reproducir con sonido sin un clic. Un botón lo activa.
- **Fin:** si el video termina antes de lo esperado, avisa que la transmisión finalizó.
- **Reserva:** si un video no permite reproducirse en páginas ajenas, pasa solo al siguiente. Si ninguno se puede, ofrece abrir el primero en YouTube en el minuto actual.

## Espectadores

Cada persona que mira una transmisión es un espectador, con un nombre como `USUARIO1:106` (cuenta y evento). Se registra al pedir la transmisión y se da de baja cuando:

| Motivo | Cómo |
|---|---|
| Sale del evento, cierra sesión, lo expulsan, lo desactivan o el evento termina | El programa llama a `/salida` (`USUARIO1:*` cierra todo de una cuenta, `*:106` todo de un evento) |
| Cierra la pestaña | La página deja de mandar latidos y a los 15 segundos se da de baja |
| El servicio deja de responder | La página, tras 3 latidos sin respuesta, se detiene |

Al darse de baja, la página detiene y elimina el reproductor y avisa que la transmisión se cerró. Cuando no queda ningún espectador, el servicio libera la memoria de las búsquedas.

## Cómo elige el video

1. Busca en YouTube `<artista> concierto completo en vivo` y `<artista> live concert full show`.
2. Descarta los videos cuyo título o canal no nombran al artista, para no devolver cualquier transmisión.
3. Puntúa el resto: palabras de recital, duración larga y un bonus si está en vivo en ese momento.
4. Prefiere los que duran más que lo transcurrido desde el inicio del evento. Si ninguno alcanza, el más largo.
5. Devuelve el mejor y hasta siete de reserva. Guarda los resultados diez minutos.

## Limitaciones

- Usa la página de resultados de YouTube sin clave de API. Si YouTube cambia su formato hay que ajustar la lectura.
- Devuelve un recital del artista, que puede ser grabado y no necesariamente transmitirse en ese momento.
- Muchos recitales subidos por terceros no permiten reproducirse fuera de YouTube; para eso están los videos de reserva. YouTube también puede pedir iniciar sesión según la red desde la que se mire.
