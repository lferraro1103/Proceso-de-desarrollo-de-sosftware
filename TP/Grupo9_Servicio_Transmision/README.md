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

- **Minuto del directo:** arranca en el minuto que corresponde. Si el evento empezó a las 22:00 y son las 22:30, el video empieza en el minuto 30. Usa la hora del servidor, no la de la PC de quien mira. La página no muestra cuánto va.
- **Bucle:** si el video dura menos que el evento, vuelve a empezar solo, en el minuto que corresponde.
- **Pantalla completa:** el video ocupa toda la ventana. El botón «Activar sonido y pantalla completa» activa el sonido y pasa el navegador a pantalla completa (los navegadores exigen un clic para ambas cosas).
- **Sin controles:** no muestra la barra de tiempo, tiene una capa que bloquea los clics y desactiva el teclado.
- **Sin adelantar, volver ni pausar:** en un video propio, cualquier movimiento del tiempo se deshace en el acto. En YouTube, cada segundo se compara el video con el minuto esperado y, si se atrasó, se adelantó o se pausó, se lo devuelve y se lo reanuda.
- **Sonido:** arranca silenciado porque los navegadores no permiten reproducir con sonido sin un clic. Un botón lo activa.
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

Siempre es un recital real. En este orden:

1. **Archivo propio.** Si en la carpeta `media/` hay un archivo con el nombre del artista, por ejemplo `sabrina-carpenter.mp4` (también `.webm` y `.m4v`), el servicio lo transmite él mismo, con soporte de `Range`. La carpeta está ignorada por git. No viene ningún archivo: hay que poner un recital que se pueda usar (propio, con licencia o autorizado). Para una demostración sirve un recital con licencia abierta de archive.org, por ejemplo «Sunrust | Live Concert» (`https://archive.org/details/sunrust-2023-08-17`, dedicado al dominio público con CC0; 114 minutos, 480p), guardado como `media/sunrust.mp4` y usado con un artista de nombre artístico «Sunrust».
2. **Recital de YouTube**, en el reproductor incrustado que ofrece YouTube.
   - **Con clave de la API de YouTube** (recomendado): busca primero un **directo en este momento** y después recitales largos, y solo considera los videos que sus dueños dejan reproducir incrustados. Así casi siempre hay uno que se reproduce.
   - **Sin clave:** lee la página de resultados y prueba hasta quince videos. Muchos recitales no permiten reproducirse fuera de YouTube; los que fallan se recuerdan y no se vuelven a ofrecer, pero puede que haya que esperar a que pase por varios.

Los videos que sus dueños no dejan reproducir fuera de YouTube **no se descargan ni se retransmiten**.

### Cómo conseguir la clave (gratis)

1. En <https://console.cloud.google.com> crear un proyecto.
2. Activar «YouTube Data API v3» en Biblioteca de APIs.
3. En Credenciales, crear una clave de API.
4. Guardarla en el archivo `~/.uadebeats/youtube.key` (solo la clave, sin comillas), o definirla en la variable `YOUTUBE_API_KEY`. No la subas al repositorio.

La búsqueda con la API cuesta 200 de las 10.000 unidades diarias gratuitas, y los resultados se guardan diez minutos.

## Limitaciones

- Usa la página de resultados de YouTube sin clave de API. Si YouTube cambia su formato hay que ajustar la lectura.
- Devuelve un recital del artista, que puede ser grabado y no necesariamente transmitirse en ese momento.
- YouTube puede mostrar «Sign in to confirm you're not a bot» dentro del reproductor incrustado según la red, el navegador y si hay sesión iniciada (suele pasar con VPN o proxy). Es una verificación de YouTube y el servicio no la puede saltear: hay que mirar desde una red sin VPN y con la sesión de YouTube iniciada en ese navegador.
- Sin clave de la API, muchos recitales no permiten reproducirse fuera de YouTube (o YouTube pide iniciar sesión según la red) y hay que probar varios. La clave de la API filtra solo los que sí se pueden incrustar. La búsqueda con la API no se probó con una clave real.
