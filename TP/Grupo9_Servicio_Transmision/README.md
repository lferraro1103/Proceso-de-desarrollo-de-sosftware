# Servicio de transmisiones

Microservicio aparte del programa principal de UADE Beats. Dado el nombre de un artista, devuelve la dirección de un recital suyo en YouTube. Está hecho en Node.js, sin dependencias, y solo existe para que la aplicación pueda mostrar una transmisión real al conectarse a un evento.

## Ejecución

Requiere Node.js 18 o superior.

```
node servidor.js
```

Escucha en el puerto 8766 (se cambia con la variable `PORT`). Hay que dejarlo corriendo mientras se usa el programa principal.

## Rutas

| Ruta | Respuesta |
|---|---|
| `GET /salud` | `{ "estado": "ok" }` |
| `GET /transmision?q=Sabrina+Carpenter` | `{ titulo, canal, duracion, enVivo, url, alternativas }`, o 404 si no hay ningún recital del artista |

## Cómo elige el video

1. Busca en YouTube `<artista> concierto completo en vivo` y `<artista> live concert full show`.
2. Descarta los videos cuyo título o canal no nombran al artista, para no devolver cualquier transmisión.
3. Puntúa el resto: palabras de recital (concierto, live, tour, festival), duración de más de 20 o 40 minutos, y un bonus si está en vivo en ese momento.
4. Devuelve el mejor y hasta tres alternativas. Guarda el resultado diez minutos para no repetir la búsqueda.

## Limitaciones

- Usa la página de resultados de YouTube sin clave de API. Es lo más simple, pero si YouTube cambia su formato hay que ajustar la lectura.
- Devuelve un recital del artista, que puede ser grabado y no necesariamente transmitirse en ese momento.
