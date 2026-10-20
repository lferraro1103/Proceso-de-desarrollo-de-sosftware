// Servicio de transmisiones de UADE Beats.
//
// Microservicio aparte del programa principal. Dado el nombre de un artista y
// la hora en que empezo el evento, muestra su recital en una pagina a pantalla
// completa, sin controles para adelantar, volver ni pausar, y arrancando en el
// minuto que corresponde, como si fuera una transmision en vivo. No tiene
// dependencias: usa el servidor HTTP y fetch que trae Node.
//
// De donde sale el video, en este orden:
//   1. Un archivo de la carpeta media/ cuyo nombre sea el del artista
//      (opcional: por ejemplo sabrina-carpenter.mp4). El servicio lo transmite
//      el mismo, con soporte de Range.
//   2. Un recital real de YouTube, mostrado en el reproductor incrustado que
//      ofrece YouTube:
//        - Con una clave (variable YOUTUBE_API_KEY o archivo
//          ~/.uadebeats/youtube.key) usa la API oficial: busca primero un
//          directo en este momento y luego recitales largos, y solo considera
//          los videos que sus duenos dejan reproducir incrustados.
//        - Sin clave, lee la pagina de resultados y prueba los videos uno por
//          uno; los que no se pueden reproducir se recuerdan y se saltean.
//      Los videos que sus duenos no dejan reproducir fuera de YouTube no se
//      descargan ni se retransmiten.
//
//   GET /salud                    -> { estado, espectadores }
//   GET /transmision?q=<artista>&inicio=<fecha ISO>
//        -> { titulo, canal, duracion, enVivo, url, ruta, alternativas }
//           ruta es la pagina del reproductor; 404 si no hay ningun recital
//   GET /media/<archivo>          -> el video de la carpeta media/ (con Range)
//   GET /reproductor?src=<archivo>|v=<id>&alt=<ids>&inicio=<ms>&titulo=<texto>&e=<espectador>
//        -> pagina HTML con el reproductor sincronizado
//   GET /fallo?v=<id>             -> el reproductor no pudo con ese video: se saltea
//   GET /exito?v=<id>             -> ese video si se reproduce: se ofrece primero
//   GET /latido?e=<espectador>    -> { vigente }   (la pagina lo pide cada pocos segundos)
//   GET /salida?e=<patron>        -> { cerrados }  cierra espectadores (ver abajo)
//
// Espectadores: cada persona que mira una transmision es un espectador, con
// un nombre como "USUARIO1:106" (cuenta y evento). Mientras su pagina este
// abierta manda latidos; si deja de mandarlos (cerro la pestana) o el
// programa pide su salida (cerro sesion, lo expulsaron, termino el evento),
// se lo da de baja y la pagina detiene el video. El patron de /salida acepta
// * como comodin: "USUARIO1:*" cierra todo de una cuenta, "*:106" todo de un
// evento. Sin espectadores el servicio no hace nada ni guarda resultados.
//
// Entre los videos del artista elige el que mas parece un recital completo y,
// si hay una transmision en vivo en ese momento, la prefiere.

const http = require("http");

const PUERTO = process.env.PORT || 8766;
const MINUTOS_CACHE = 10;

// Palabras que suelen tener los videos de recitales.
const PALABRAS_RECITAL = ["concierto", "concert", "recital", "full show",
  "full set", "completo", "en vivo", "live", "festival", "tour"];

const cache = new Map();

// Lo que se aprendio de los videos: cuales el reproductor incrustado pudo
// reproducir y cuales no. Se guarda en ~/.uadebeats para no repetir pruebas.
const ARCHIVO_VIDEOS = require("path").join(require("os").homedir(), ".uadebeats", "transmision-videos.json");
const noReproducibles = new Set();
const reproducibles = new Set();

try {
  const guardado = JSON.parse(require("fs").readFileSync(ARCHIVO_VIDEOS, "utf8"));
  (guardado.no || []).forEach((id) => noReproducibles.add(id));
  (guardado.si || []).forEach((id) => reproducibles.add(id));
} catch (e) {
  // Primera vez: no hay nada guardado.
}

function guardarVideos() {
  try {
    require("fs").mkdirSync(require("path").dirname(ARCHIVO_VIDEOS), { recursive: true });
    require("fs").writeFileSync(ARCHIVO_VIDEOS,
      JSON.stringify({ si: [...reproducibles], no: [...noReproducibles] }));
  } catch (e) {
    console.error("No se pudo guardar la lista de videos:", e.message);
  }
}

// Espectadores activos: nombre -> hora del ultimo latido.
const espectadores = new Map();
const SEGUNDOS_SIN_LATIDO = 15;

function cerrarEspectadores(patron) {
  const regla = new RegExp("^" + patron.split("*").map((p) => p.replace(/[.+?^${}()|[\]\\]/g, "\\$&")).join(".*") + "$");
  let cerrados = 0;

  for (const nombre of espectadores.keys()) {
    if (regla.test(nombre)) {
      espectadores.delete(nombre);
      cerrados++;
    }
  }

  liberarSiNadieMira();
  return cerrados;
}

// Sin espectadores no se guarda nada: se libera la memoria de las busquedas.
function liberarSiNadieMira() {
  if (espectadores.size === 0) {
    cache.clear();
  }
}

// Da de baja a quien dejo de mandar latidos (por ejemplo, cerro la pestana).
setInterval(() => {
  const limite = Date.now() - SEGUNDOS_SIN_LATIDO * 1000;

  for (const [nombre, ultimo] of espectadores) {
    if (ultimo < limite) {
      espectadores.delete(nombre);
      console.log(`[${new Date().toLocaleTimeString()}] espectador ${nombre} sin latidos: baja`);
    }
  }

  liberarSiNadieMira();
}, 5000).unref();

// Si el programa que lo inicio ya no existe, este servicio termina.
if (process.env.PADRE) {
  setInterval(() => {
    try {
      process.kill(Number(process.env.PADRE), 0);
    } catch (e) {
      console.log("El programa principal termino: cierro el servicio.");
      process.exit(0);
    }
  }, 5000).unref();
}

// ---------- busqueda en YouTube ----------

async function buscarEnYoutube(consulta) {
  const url = "https://www.youtube.com/results?search_query=" + encodeURIComponent(consulta);
  const respuesta = await fetch(url, {
    headers: {
      "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36",
      "Accept-Language": "es-AR,es;q=0.9",
      // Evita la pantalla de consentimiento de cookies.
      "Cookie": "CONSENT=YES+1; SOCS=CAI",
    },
  });

  const html = await respuesta.text();
  const datos = /var ytInitialData = (\{.*?\});<\/script>/s.exec(html);

  if (!datos) {
    throw new Error("YouTube no devolvio resultados legibles");
  }

  const videos = [];
  recorrer(JSON.parse(datos[1]), (nodo) => {
    const v = nodo.videoRenderer;
    if (v && v.videoId) {
      videos.push({
        id: v.videoId,
        titulo: (v.title?.runs || []).map((r) => r.text).join(""),
        canal: (v.ownerText?.runs || []).map((r) => r.text).join(""),
        duracion: v.lengthText?.simpleText || "",
        enVivo: (v.badges || []).some((b) =>
          (b.metadataBadgeRenderer?.style || "").includes("LIVE")),
      });
    }
  });

  return videos;
}

function recorrer(nodo, accion) {
  if (Array.isArray(nodo)) {
    nodo.forEach((n) => recorrer(n, accion));
  } else if (nodo && typeof nodo === "object") {
    accion(nodo);
    Object.values(nodo).forEach((n) => recorrer(n, accion));
  }
}

// ---------- videos propios (carpeta media/) ----------

const fs = require("fs");
const path = require("path");

const CARPETA_MEDIA = path.join(__dirname, "media");
const TIPOS = { ".mp4": "video/mp4", ".m4v": "video/mp4", ".webm": "video/webm" };

// Nombre de archivo de un artista: "Sabrina Carpenter" -> "sabrina-carpenter".
const aNombreDeArchivo = (t) => sinAcentos(t).replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

// Archivo de media/ que corresponde al artista, o null.
function buscarArchivoPropio(artista) {
  if (!fs.existsSync(CARPETA_MEDIA)) return null;

  const buscado = aNombreDeArchivo(artista);
  return fs.readdirSync(CARPETA_MEDIA).find((archivo) =>
    TIPOS[path.extname(archivo).toLowerCase()]
      && aNombreDeArchivo(path.basename(archivo, path.extname(archivo))) === buscado) || null;
}

// Entrega el archivo con soporte de Range, que los navegadores necesitan para reproducir.
function servirMedia(req, res, nombre) {
  const archivo = path.join(CARPETA_MEDIA, path.basename(nombre));

  if (!fs.existsSync(archivo) || !TIPOS[path.extname(archivo).toLowerCase()]) {
    return responder(res, 404, { error: "Archivo inexistente." });
  }

  const total = fs.statSync(archivo).size;
  const tipo = TIPOS[path.extname(archivo).toLowerCase()];
  const rango = /bytes=(\d*)-(\d*)/.exec(req.headers.range || "");

  if (!rango) {
    res.writeHead(200, { "Content-Type": tipo, "Content-Length": total, "Accept-Ranges": "bytes" });
    return fs.createReadStream(archivo).pipe(res);
  }

  const desde = rango[1] ? Number(rango[1]) : Math.max(0, total - Number(rango[2]));
  const hasta = rango[1] && rango[2] ? Math.min(Number(rango[2]), total - 1) : total - 1;

  if (desde > hasta || desde >= total) {
    res.writeHead(416, { "Content-Range": `bytes */${total}` });
    return res.end();
  }

  res.writeHead(206, {
    "Content-Type": tipo,
    "Content-Length": hasta - desde + 1,
    "Content-Range": `bytes ${desde}-${hasta}/${total}`,
    "Accept-Ranges": "bytes",
  });
  fs.createReadStream(archivo, { start: desde, end: hasta }).pipe(res);
}

// ---------- eleccion del mejor resultado ----------

const sinAcentos = (t) => t.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();

function minutos(duracion) {
  if (!duracion) return 0;
  return duracion.split(":").reduce((total, parte) => total * 60 + Number(parte), 0) / 60;
}

// Indica si el titulo o el canal nombran al artista (todas sus palabras).
function nombraAlArtista(video, artista) {
  const texto = sinAcentos(video.titulo + " " + video.canal);
  return sinAcentos(artista).split(/\s+/).filter(Boolean)
    .every((palabra) => texto.includes(palabra));
}

// Puntaje: cuanto parece un recital completo.
function puntuar(video) {
  const texto = sinAcentos(video.titulo + " " + video.canal);
  let puntos = 0;

  if (PALABRAS_RECITAL.some((p) => texto.includes(p))) puntos += 15;
  if (video.enVivo) puntos += 30;

  const largo = minutos(video.duracion);
  if (video.enVivo || largo >= 40) puntos += 25;
  else if (largo >= 20) puntos += 12;
  else if (largo < 8) puntos -= 25;

  return puntos;
}

// ---------- API oficial de YouTube (opcional) ----------

// La clave se toma de la variable YOUTUBE_API_KEY o, si no esta, del archivo
// ~/.uadebeats/youtube.key (queda fuera del repositorio).
function claveApi() {
  if (process.env.YOUTUBE_API_KEY) return process.env.YOUTUBE_API_KEY;

  try {
    return require("fs").readFileSync(
      require("path").join(require("os").homedir(), ".uadebeats", "youtube.key"), "utf8").trim();
  } catch (e) {
    return "";
  }
}

// "PT1H10M19S" -> minutos.
function minutosIso(duracion) {
  const m = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/.exec(duracion || "") || [];
  return Number(m[1] || 0) * 60 + Number(m[2] || 0) + Number(m[3] || 0) / 60;
}

async function llamarApi(recurso, parametros) {
  const url = new URL("https://www.googleapis.com/youtube/v3/" + recurso);
  Object.entries({ ...parametros, key: claveApi() })
    .forEach(([clave, valor]) => url.searchParams.set(clave, valor));

  const respuesta = await fetch(url);
  const datos = await respuesta.json();

  if (!respuesta.ok) {
    throw new Error(datos.error?.message || "error " + respuesta.status);
  }

  return datos;
}

// Directos y recitales largos que YouTube deja reproducir incrustados.
async function buscarConApi(artista) {
  const buscar = async (extra) => (await llamarApi("search", {
    part: "snippet", type: "video", videoEmbeddable: "true", videoSyndicated: "true", maxResults: "15",
    q: artista + " concierto en vivo", ...extra,
  })).items.map((item) => item.id.videoId);

  const ids = [...new Set([
    ...await buscar({ eventType: "live" }),
    ...await buscar({ videoDuration: "long" }),
  ])];

  if (!ids.length) return [];

  const detalles = (await llamarApi("videos", {
    part: "snippet,contentDetails,status", id: ids.join(","),
  })).items;

  return detalles
    .filter((v) => v.status?.embeddable)
    .map((v) => {
      const enVivo = v.snippet.liveBroadcastContent === "live";
      return {
        id: v.id,
        titulo: v.snippet.title,
        canal: v.snippet.channelTitle,
        duracion: enVivo ? "EN VIVO" : "",
        minutos: enVivo ? 0 : minutosIso(v.contentDetails.duration),
        enVivo,
        url: "https://www.youtube.com/watch?v=" + v.id,
      };
    });
}

// Videos del artista ordenados de mejor a peor. Se guardan unos minutos.
async function buscarCandidatos(artista) {
  const clave = sinAcentos(artista);
  const guardado = cache.get(clave);

  if (guardado && Date.now() - guardado.hora < MINUTOS_CACHE * 60000) {
    return guardado.candidatos;
  }

  // Con clave de API: solo videos que se pueden reproducir incrustados.
  if (claveApi()) {
    try {
      const desdeApi = (await buscarConApi(artista))
        .filter((video) => nombraAlArtista(video, artista))
        .map((video) => ({ video, puntos: puntuar(video) }))
        .sort((a, b) => b.puntos - a.puntos)
        .map(({ video }) => video);

      if (desdeApi.length) {
        cache.set(clave, { hora: Date.now(), candidatos: desdeApi });
        return desdeApi;
      }
    } catch (e) {
      console.error("API de YouTube:", e.message, "- se usa la busqueda comun");
    }
  }

  const unicos = new Map();
  for (const consulta of [`${artista} concierto completo en vivo`, `${artista} live concert full show`]) {
    for (const video of await buscarEnYoutube(consulta)) {
      if (!unicos.has(video.id)) unicos.set(video.id, video);
    }
  }

  // Solo se consideran videos que nombran al artista: sin eso, una busqueda
  // sin resultados devolveria cualquier transmision en vivo.
  const candidatos = [...unicos.values()]
    .filter((video) => nombraAlArtista(video, artista))
    .map((video) => ({ video, puntos: puntuar(video) }))
    .sort((a, b) => b.puntos - a.puntos)
    .map(({ video }) => ({
      id: video.id,
      titulo: video.titulo,
      canal: video.canal,
      duracion: video.enVivo ? "EN VIVO" : video.duracion,
      minutos: minutos(video.duracion),
      enVivo: video.enVivo,
      url: "https://www.youtube.com/watch?v=" + video.id,
    }));

  cache.set(clave, { hora: Date.now(), candidatos });
  return candidatos;
}

// Mejor video para el momento actual: si el evento empezo hace 30 minutos,
// el video tiene que durar mas que eso. Si ninguno alcanza, el mas largo.
async function buscarTransmision(artista, inicioMs, espectador) {
  const quien = espectador ? "&e=" + encodeURIComponent(espectador) : "";

  // 1. Video propio: se transmite desde este servicio.
  const propio = buscarArchivoPropio(artista);

  if (propio) {
    return {
      titulo: artista + " (transmision propia)",
      canal: "UADE Beats",
      duracion: "",
      enVivo: true,
      url: "/media/" + encodeURIComponent(propio),
      ruta: "/reproductor?src=" + encodeURIComponent(propio) + quien
        + "&inicio=" + inicioMs + "&titulo=" + encodeURIComponent(artista),
      alternativas: [],
    };
  }

  // 2. Recital de YouTube que permita reproducirse incrustado.
  // Los que ya se vieron reproducir van primero; los que fallaron, no se ofrecen.
  const candidatos = (await buscarCandidatos(artista))
    .filter((c) => !noReproducibles.has(c.id))
    .sort((a, b) => Number(reproducibles.has(b.id)) - Number(reproducibles.has(a.id)));

  if (!candidatos.length) {
    return null;
  }

  const transcurrido = Math.max(0, (Date.now() - inicioMs) / 60000);
  const alcanzan = candidatos.filter((c) => c.enVivo || c.minutos > transcurrido + 3);
  const lista = alcanzan.length
    ? alcanzan
    : [...candidatos].sort((a, b) => b.minutos - a.minutos);

  const [mejor, ...resto] = lista;
  // Varias de reserva: muchos videos no permiten reproducirse incrustados.
  const alternativas = resto.slice(0, 14);

  const ruta = "/reproductor?v=" + mejor.id + quien
    + "&alt=" + alternativas.map((a) => a.id).join(",")
    + "&inicio=" + inicioMs
    + "&titulo=" + encodeURIComponent(mejor.titulo);

  const publico = ({ id, minutos, ...datos }) => datos;
  return { ...publico(mejor), ruta, alternativas: alternativas.map(publico) };
}

// ---------- servidor ----------

function responder(res, codigo, cuerpo) {
  res.writeHead(codigo, { "Content-Type": "application/json; charset=utf-8" });
  res.end(JSON.stringify(cuerpo));
}

const servidor = http.createServer(async (req, res) => {
  const url = new URL(req.url, "http://localhost");

  if (url.pathname === "/salud") {
    return responder(res, 200, { estado: "ok", espectadores: espectadores.size });
  }

  // La pagina avisa que sigue abierta y se entera si ya no debe reproducir.
  if (url.pathname === "/latido") {
    const nombre = url.searchParams.get("e") || "";
    const vigente = espectadores.has(nombre);

    if (vigente) {
      espectadores.set(nombre, Date.now());
    }

    return responder(res, 200, { vigente });
  }

  // La pagina avisa que un video no se pudo reproducir incrustado: no se ofrece mas.
  if (url.pathname === "/fallo") {
    const id = url.searchParams.get("v") || "";

    if (/^[\w-]{11}$/.test(id)) {
      noReproducibles.add(id);
      reproducibles.delete(id);
      guardarVideos();
    }

    return responder(res, 200, { omitidos: noReproducibles.size });
  }

  // La pagina avisa que un video si se reproduce: se ofrece primero la proxima vez.
  if (url.pathname === "/exito") {
    const id = url.searchParams.get("v") || "";

    if (/^[\w-]{11}$/.test(id) && !reproducibles.has(id)) {
      reproducibles.add(id);
      noReproducibles.delete(id);
      guardarVideos();
    }

    return responder(res, 200, { conocidos: reproducibles.size });
  }

  // Cierra espectadores: uno exacto o varios con comodin.
  if (url.pathname === "/salida") {
    const cerrados = cerrarEspectadores(url.searchParams.get("e") || "");
    console.log(`[${new Date().toLocaleTimeString()}] salida "${url.searchParams.get("e")}": ${cerrados} cerrado(s)`);
    return responder(res, 200, { cerrados });
  }

  // Hora del servidor, para que la pagina no dependa del reloj de la PC.
  if (url.pathname === "/hora") {
    return responder(res, 200, { ahora: Date.now() });
  }

  // Video propio.
  if (url.pathname.startsWith("/media/")) {
    return servirMedia(req, res, decodeURIComponent(url.pathname.slice("/media/".length)));
  }

  // Pagina con el reproductor.
  if (url.pathname === "/reproductor") {
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    return res.end(fs.readFileSync(path.join(__dirname, "reproductor.html")));
  }

  if (url.pathname === "/transmision") {
    const artista = (url.searchParams.get("q") || "").trim();

    if (!artista) {
      return responder(res, 400, { error: "Falta el parametro q con el nombre del artista." });
    }

    // Cuando empezo el evento (hora local sin zona, como la envia el programa).
    const inicioMs = new Date(url.searchParams.get("inicio") || Date.now()).getTime();

    if (Number.isNaN(inicioMs)) {
      return responder(res, 400, { error: "El parametro inicio no es una fecha valida." });
    }

    const espectador = (url.searchParams.get("espectador") || "").trim();

    try {
      const resultado = await buscarTransmision(artista, inicioMs, espectador);

      // Quien pide la transmision pasa a ser un espectador activo.
      if (resultado && espectador) {
        espectadores.set(espectador, Date.now());
      }

      console.log(`[${new Date().toLocaleTimeString()}] "${artista}" desde ${new Date(inicioMs).toLocaleTimeString()} -> ${resultado ? resultado.url : "sin resultados"}`);

      return resultado
        ? responder(res, 200, resultado)
        : responder(res, 404, { error: `No se encontro un recital de ${artista}.` });
    } catch (e) {
      console.error("Error al buscar:", e.message);
      return responder(res, 502, { error: "No se pudo consultar YouTube: " + e.message });
    }
  }

  responder(res, 404, { error: "Ruta inexistente." });
});

servidor.listen(PUERTO, () => {
  console.log(`Servicio de transmisiones en http://localhost:${PUERTO}`);
  console.log(`Prueba: http://localhost:${PUERTO}/transmision?q=Sabrina+Carpenter&inicio=${new Date(Date.now() - 30 * 60000).toISOString()}`);
});
