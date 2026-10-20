// Servicio de transmisiones de UADE Beats.
//
// Microservicio aparte del programa principal. Dado el nombre de un artista y
// la hora en que empezo el evento, busca un recital suyo en YouTube y lo
// muestra en una pagina con el reproductor: sin controles para adelantar,
// volver ni pausar, y arrancando en el minuto que corresponde, como si fuera
// una transmision en vivo. No tiene dependencias: usa el servidor HTTP y
// fetch que trae Node.
//
//   GET /salud
//   GET /transmision?q=<artista>&inicio=<fecha ISO>
//        -> { titulo, canal, duracion, enVivo, url, ruta, alternativas }
//           ruta es la pagina del reproductor; 404 si no hay ningun recital
//   GET /reproductor?v=<id>&alt=<ids>&inicio=<ms>&titulo=<texto>
//        -> pagina HTML con el reproductor sincronizado
//
// Busca en la pagina de resultados de YouTube (sin clave de API) y elige el
// resultado que mas parece un recital completo del artista. Si hay una
// transmision en vivo en ese momento, la prefiere.

const http = require("http");

const PUERTO = process.env.PORT || 8766;
const MINUTOS_CACHE = 10;

// Palabras que suelen tener los videos de recitales.
const PALABRAS_RECITAL = ["concierto", "concert", "recital", "full show",
  "full set", "completo", "en vivo", "live", "festival", "tour"];

const cache = new Map();

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

// Videos del artista ordenados de mejor a peor. Se guardan unos minutos.
async function buscarCandidatos(artista) {
  const clave = sinAcentos(artista);
  const guardado = cache.get(clave);

  if (guardado && Date.now() - guardado.hora < MINUTOS_CACHE * 60000) {
    return guardado.candidatos;
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
async function buscarTransmision(artista, inicioMs) {
  const candidatos = await buscarCandidatos(artista);

  if (!candidatos.length) {
    return null;
  }

  const transcurrido = Math.max(0, (Date.now() - inicioMs) / 60000);
  const alcanzan = candidatos.filter((c) => c.enVivo || c.minutos > transcurrido + 3);
  const lista = alcanzan.length
    ? alcanzan
    : [...candidatos].sort((a, b) => b.minutos - a.minutos);

  const [mejor, ...resto] = lista;
  // Varias de reserva: algunos videos no permiten reproducirse incrustados.
  const alternativas = resto.slice(0, 7);

  const ruta = "/reproductor?v=" + mejor.id
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

const fs = require("fs");
const path = require("path");

const servidor = http.createServer(async (req, res) => {
  const url = new URL(req.url, "http://localhost");

  if (url.pathname === "/salud") {
    return responder(res, 200, { estado: "ok" });
  }

  // Hora del servidor, para que la pagina no dependa del reloj de la PC.
  if (url.pathname === "/hora") {
    return responder(res, 200, { ahora: Date.now() });
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

    try {
      const resultado = await buscarTransmision(artista, inicioMs);
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
