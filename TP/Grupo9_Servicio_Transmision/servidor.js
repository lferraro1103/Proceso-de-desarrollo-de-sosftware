// Servicio de transmisiones de UADE Beats.
//
// Microservicio aparte del programa principal: dado el nombre de un artista
// devuelve la direccion de un recital suyo en YouTube. No tiene dependencias:
// usa el servidor HTTP y fetch que trae Node.
//
//   GET /salud                    -> { "estado": "ok" }
//   GET /transmision?q=<artista>  -> { titulo, canal, duracion, enVivo, url, alternativas }
//                                    404 si no encuentra ningun recital
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

async function buscarTransmision(artista) {
  const clave = sinAcentos(artista);
  const guardado = cache.get(clave);

  if (guardado && Date.now() - guardado.hora < MINUTOS_CACHE * 60000) {
    return guardado.resultado;
  }

  const unicos = new Map();
  for (const consulta of [`${artista} concierto completo en vivo`, `${artista} live concert full show`]) {
    for (const video of await buscarEnYoutube(consulta)) {
      if (!unicos.has(video.id)) unicos.set(video.id, video);
    }
  }

  // Solo se consideran videos que nombran al artista: sin eso, una busqueda
  // sin resultados devolveria cualquier transmision en vivo.
  const ordenados = [...unicos.values()]
    .filter((video) => nombraAlArtista(video, artista))
    .map((video) => ({ video, puntos: puntuar(video) }))
    .sort((a, b) => b.puntos - a.puntos)
    .map(({ video }) => ({
      titulo: video.titulo,
      canal: video.canal,
      duracion: video.enVivo ? "EN VIVO" : video.duracion,
      enVivo: video.enVivo,
      url: "https://www.youtube.com/watch?v=" + video.id,
    }));

  const resultado = ordenados.length
    ? { ...ordenados[0], alternativas: ordenados.slice(1, 4) }
    : null;

  cache.set(clave, { hora: Date.now(), resultado });
  return resultado;
}

// ---------- servidor ----------

function responder(res, codigo, cuerpo) {
  res.writeHead(codigo, { "Content-Type": "application/json; charset=utf-8" });
  res.end(JSON.stringify(cuerpo));
}

const servidor = http.createServer(async (req, res) => {
  const url = new URL(req.url, "http://localhost");

  if (url.pathname === "/salud") {
    return responder(res, 200, { estado: "ok" });
  }

  if (url.pathname === "/transmision") {
    const artista = (url.searchParams.get("q") || "").trim();

    if (!artista) {
      return responder(res, 400, { error: "Falta el parametro q con el nombre del artista." });
    }

    try {
      const resultado = await buscarTransmision(artista);
      console.log(`[${new Date().toLocaleTimeString()}] "${artista}" -> ${resultado ? resultado.url : "sin resultados"}`);

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
  console.log(`Prueba: http://localhost:${PUERTO}/transmision?q=Sabrina+Carpenter`);
});
