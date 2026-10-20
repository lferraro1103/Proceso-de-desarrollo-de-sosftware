# Servicio de transmisiones

Microservicio aparte del programa principal de UADE Beats. Dado el nombre de un artista y la hora en que empezó el evento, muestra un recital suyo en una página a pantalla completa, como si fuera una transmisión en vivo. Está hecho en Node.js, sin dependencias.

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
| `GET /reproductor?src=<archivo>` o `?v=<id>&alt=<ids>` y `inicio=<ms>&titulo=<texto>&e=<espectador>` | Página con el reproductor (video propio o de YouTube) |
| `GET /media/<archivo>` | El video de la carpeta `media/`, con `Range` |
| `GET /hora` | Hora del servidor en milisegundos |
| `GET /latido?e=<espectador>` | `{ vigente }`. La página lo pide cada 4 segundos. |
| `GET /salida?e=<espectador>` | `{ cerrados }`. Cierra espectadores; el nombre acepta `*` como comodín. |

## El reproductor

La página de `/reproductor` simula una transmisión en vivo:

- **Minuto del directo:** arranca en el minuto que corresponde. Si el evento empezó a las 22:00 y son las 22:30, el video empieza en el minuto 30. Usa la hora del servidor, no la de la PC de quien mira.
- **Pantalla completa:** el video ocupa toda la ventana. El botón «Activar sonido y pantalla completa» activa el sonido y pasa el navegador a pantalla completa (los navegadores exigen un clic para ambas cosas).
- **Sin controles:** no muestra la barra de tiempo, tiene una capa que bloquea los clics y desactiva el teclado.
- **Sin adelantar, volver ni pausar:** cada segundo compara el video con el minuto esperado. Si se atrasó, se adelantó o se pausó, lo devuelve al minuto del directo y lo reanuda.
- **Sonido:** arranca silenciado porque los navegadores no permiten reproducir con sonido sin un clic. Un botón lo activa.
- **Fin:** si el video termina antes de lo esperado, avisa que la transmisión finalizó.
- **Reserva:** con YouTube, si un video no permite reproducirse en páginas ajenas, pasa solo al siguiente. Si ninguno se puede, avisa que hace falta un video propio en `media/`.

## Espectadores

Cada persona que mira una transmisión es un espectador, con un nombre como `USUARIO1:106` (cuenta y evento). Se registra al pedir la transmisión y se da de baja cuando:

| Motivo | Cómo |
|---|---|
| Sale del evento, cierra sesión, lo expulsan, lo desactivan o el evento termina | El programa llama a `/salida` (`USUARIO1:*` cierra todo de una cuenta, `*:106` todo de un evento) |
| Cierra la pestaña | La página deja de mandar latidos y a los 15 segundos se da de baja |
| El servicio deja de responder | La página, tras 3 latidos sin respuesta, se detiene |

Al darse de baja, la página detiene y elimina el reproductor y avisa que la transmisión se cerró. Cuando no queda ningún espectador, el servicio libera la memoria de las búsquedas.

## De dónde sale el video

1. **Video propio (recomendado).** Si en la carpeta `media/` hay un archivo con el nombre del artista, por ejemplo `sabrina-carpenter.mp4` (también `.webm` y `.m4v`), el servicio lo transmite él mismo, con soporte de `Range` para que el navegador lo reproduzca. No depende de ningún sitio externo. El nombre se arma con el artista en minúsculas, sin acentos y con guiones. La carpeta está ignorada por git: los videos no se suben al repositorio.
2. **YouTube incrustado.** Si no hay archivo, busca recitales del artista y los muestra con el reproductor incrustado que ofrece YouTube:
   - Busca `<artista> concierto completo en vivo` y `<artista> live concert full show`.
   - Descarta los videos cuyo título o canal no nombran al artista.
   - Prefiere los de recital, los largos y los que duran más que lo transcurrido desde el inicio del evento.
   - Prueba hasta ocho. Los videos que sus dueños no permiten reproducir fuera de YouTube **no se descargan ni se retransmiten**: la página pasa al siguiente y, si ninguno sirve, avisa que hace falta un video propio.

Los videos que se agreguen a `media/` tienen que ser contenido que se pueda usar (propio, con licencia o autorizado).

## Limitaciones

- Usa la página de resultados de YouTube sin clave de API. Si YouTube cambia su formato hay que ajustar la lectura.
- Devuelve un recital del artista, que puede ser grabado y no necesariamente transmitirse en ese momento.
- Muchos recitales no permiten reproducirse fuera de YouTube (o YouTube pide iniciar sesión según la red). Para una demostración segura, usar un video propio en `media/`.
