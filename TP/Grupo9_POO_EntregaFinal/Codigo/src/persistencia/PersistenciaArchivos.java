package persistencia;

import gestor.GestorEventosEnVivo;
import modelo.Artista;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.Evento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.RegistroAcceso;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * Persistencia simple con archivos TXT.
 *
 * No usa base de datos. Guarda informacion relevante en la carpeta datos
 * para demostrar que usuarios, artistas, eventos y registros pueden quedar
 * disponibles fuera de la ejecucion del programa.
 */
public class PersistenciaArchivos {

    // Carpeta datos del proyecto (ver resolverCarpetaDatos): no depende de
    // desde donde se lance el programa, siempre es la del proyecto.
    private static final Path CARPETA_DATOS = resolverCarpetaDatos();

    /*
     * Ubica la carpeta "datos" del proyecto (Grupo9_POO_EntregaFinal/datos)
     * a partir de donde esta el .class compilado, en vez de usar una ruta
     * relativa al directorio actual del proceso.
     *
     * Antes se usaba Paths.get("datos"), relativa al working directory con
     * el que se lanza el programa. Eso hacia que, segun la configuracion de
     * ejecucion, los datos se guardaran en una carpeta "datos" distinta a
     * la real del proyecto (por ejemplo, en IntelliJ con
     * inherit-compiler-output, los .class terminan bajo
     * out/production/<nombre-del-proyecto>/..., no dentro de
     * Grupo9_POO_EntregaFinal). Por eso no se busca solo el nombre de
     * carpeta "Grupo9_POO_EntregaFinal": se sube desde la ubicacion del
     * .class buscando, en cada nivel, un ancestro que ya sea esa carpeta
     * o que la contenga como "TP/Grupo9_POO_EntregaFinal".
     */
    private static Path resolverCarpetaDatos() {
        try {
            Path ubicacion = Paths.get(PersistenciaArchivos.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());

            Path actual = ubicacion;
            while (actual != null) {
                // Caso 1: ya estamos dentro (o en) Grupo9_POO_EntregaFinal.
                if (actual.getFileName() != null
                        && "Grupo9_POO_EntregaFinal"
                                .equals(actual.getFileName().toString())) {
                    return actual.resolve("datos");
                }

                // Caso 2: este ancestro contiene TP/Grupo9_POO_EntregaFinal
                // (por ejemplo, la raiz del proyecto/repositorio).
                Path candidato = actual.resolve("TP")
                        .resolve("Grupo9_POO_EntregaFinal");
                if (Files.isDirectory(candidato)) {
                    return candidato.resolve("datos");
                }

                actual = actual.getParent();
            }
        } catch (URISyntaxException | SecurityException e) {
            // Si por algun motivo no se puede determinar la ubicacion del
            // .class, se cae al comportamiento anterior como respaldo.
        }

        return Paths.get("datos");
    }

    // Cada tipo de informacion se guarda en su propio archivo.
    private static final Path ARCHIVO_USUARIOS =
            CARPETA_DATOS.resolve("usuarios.txt");
    private static final Path ARCHIVO_ARTISTAS =
            CARPETA_DATOS.resolve("artistas.txt");
    private static final Path ARCHIVO_EVENTOS =
            CARPETA_DATOS.resolve("eventos.txt");
    private static final Path ARCHIVO_REGISTROS =
            CARPETA_DATOS.resolve("registros_acceso.txt");

    // Guarda todos los datos relevantes del gestor en archivos TXT.
    public static void guardarDatos(GestorEventosEnVivo gestor)
            throws IOException {

        // Si la carpeta datos no existe, Java la crea.
        Files.createDirectories(CARPETA_DATOS);

        guardarUsuarios(gestor.listarUsuarios());
        guardarArtistas(gestor.listarArtistas());
        guardarEventos(gestor.listarEventos());
        guardarRegistros(gestor.listarRegistros());
    }

    // Carga usuarios, artistas y eventos desde TXT al gestor.
    public static void cargarDatos(GestorEventosEnVivo gestor)
            throws IOException {

        // Tambien la creo al cargar, para evitar error si todavia no existe.
        Files.createDirectories(CARPETA_DATOS);

        List<Usuario> usuarios = cargarUsuarios();
        List<Artista> artistas = cargarArtistas();
        List<Evento> eventos = cargarEventos(artistas);
        List<RegistroAcceso> registros = cargarRegistros(usuarios, eventos);

        // Reemplazo los datos en memoria por los datos leidos del TXT.
        gestor.reemplazarDatos(usuarios, artistas, eventos, registros);
    }

    private static void guardarUsuarios(List<Usuario> usuarios)
            throws IOException {

        List<String> lineas = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            // Uso | como separador para despues poder separar cada campo.
            lineas.add(usuario.getId()
                    + "|" + limpiar(usuario.getNombreUsuario())
                    + "|" + limpiar(usuario.getNombre())
                    + "|" + limpiar(usuario.getApellido())
                    + "|" + limpiar(usuario.getEmail())
                    + "|" + limpiar(usuario.getContrasena())
                    + "|" + usuario.getPlanSuscripcion()
                    + "|" + usuario.isActivo()
                    + "|" + usuario.getTipoUsuario());
        }

        // Files.write escribe todas las lineas en el archivo indicado.
        Files.write(ARCHIVO_USUARIOS, lineas);
    }

    private static void guardarArtistas(List<Artista> artistas)
            throws IOException {

        List<String> lineas = new ArrayList<>();

        for (Artista artista : artistas) {
            // Cada artista queda en una linea del archivo artistas.txt.
            lineas.add(artista.getId()
                    + "|" + limpiar(artista.getNombreArtistico())
                    + "|" + limpiar(artista.getGeneroPrincipal())
                    + "|" + limpiar(artista.getBiografia())
                    + "|" + artista.isVerificado()
                    + "|" + limpiar(artista.getNombreUsuario())
                    + "|" + limpiar(artista.getContrasena())
                    + "|" + artista.getTipoUsuario());
        }

        Files.write(ARCHIVO_ARTISTAS, lineas);
    }

    private static void guardarEventos(List<Evento> eventos)
            throws IOException {

        List<String> lineas = new ArrayList<>();

        for (Evento evento : eventos) {
            // Por ahora el modulo trabaja con RecitalEnVivo.
            if (evento instanceof RecitalEnVivo) {
                RecitalEnVivo recital = (RecitalEnVivo) evento;

                // Tambien guardo los IDs de artistas asociados al evento.
                lineas.add(recital.getId()
                        + "|" + limpiar(recital.getTitulo())
                        + "|" + limpiar(recital.getDescripcion())
                        + "|" + recital.getFechaHoraInicio()
                        + "|" + recital.getFechaHoraFin()
                        + "|" + recital.getEstado()
                        + "|" + recital.getCapacidadMaxima()
                        + "|" + recital.getPlanMinimoRequerido()
                        + "|" + limpiar(recital.getUbicacion())
                        + "|" + recital.isStreaming()
                        + "|" + recital.isExclusivo()
                        + "|" + obtenerIdsArtistas(recital));
            }
        }

        Files.write(ARCHIVO_EVENTOS, lineas);
    }

    private static void guardarRegistros(List<RegistroAcceso> registros)
            throws IOException {

        List<String> lineas = new ArrayList<>();

        for (RegistroAcceso registro : registros) {
            // Los registros se guardan para dejar trazabilidad de ingresos.
            lineas.add(registro.getFechaHoraIngreso()
                    + "|" + registro.getUsuario().getId()
                    + "|" + registro.getEvento().getId()
                    + "|" + registro.isExitoso()
                    + "|" + limpiar(registro.getMotivoRechazo()));
        }

        Files.write(ARCHIVO_REGISTROS, lineas);
    }

    private static List<Usuario> cargarUsuarios() throws IOException {
        List<Usuario> usuarios = new ArrayList<>();

        if (!Files.exists(ARCHIVO_USUARIOS)) {
            // Si no hay archivo, devuelvo lista vacia y el programa sigue.
            return usuarios;
        }

        for (String linea : Files.readAllLines(ARCHIVO_USUARIOS)) {
            // split separa la linea usando el mismo separador que use al guardar.
            String[] datos = linea.split("\\|", -1);

            if (datos.length >= 8) {
                // Archivos viejos (8 campos) no tienen tipoUsuario: uso
                // USUARIO por defecto para no romper la carga.
                TipoUsuario tipo = datos.length >= 9
                        ? TipoUsuario.valueOf(datos[8])
                        : TipoUsuario.USUARIO;

                // Con los datos leidos reconstruyo el objeto Usuario.
                usuarios.add(new Usuario(
                        Integer.parseInt(datos[0]),
                        datos[1],
                        datos[2],
                        datos[3],
                        datos[4],
                        datos[5],
                        PlanSuscripcion.valueOf(datos[6]),
                        Boolean.parseBoolean(datos[7]),
                        tipo
                ));
            }
        }

        return usuarios;
    }

    private static List<Artista> cargarArtistas() throws IOException {
        List<Artista> artistas = new ArrayList<>();

        if (!Files.exists(ARCHIVO_ARTISTAS)) {
            return artistas;
        }

        for (String linea : Files.readAllLines(ARCHIVO_ARTISTAS)) {
            String[] datos = linea.split("\\|", -1);

            if (datos.length >= 5) {
                // Archivos viejos (5 campos) no tienen credenciales: genero
                // el mismo default que el constructor de compatibilidad de
                // Artista, para no duplicar esa regla en dos lugares.
                String nombreUsuario = datos.length >= 8
                        ? datos[5]
                        : "artista" + datos[0];
                String contrasena = datos.length >= 8 ? datos[6] : "";

                // Reconstruyo el artista con los datos del TXT.
                artistas.add(new Artista(
                        Integer.parseInt(datos[0]),
                        nombreUsuario,
                        contrasena,
                        datos[1],
                        datos[2],
                        datos[3],
                        Boolean.parseBoolean(datos[4])
                ));
            }
        }

        return artistas;
    }

    private static List<Evento> cargarEventos(List<Artista> artistas)
            throws IOException {

        List<Evento> eventos = new ArrayList<>();
        // Uso un mapa para encontrar rapido un artista por su ID.
        Map<Integer, Artista> artistasPorId = mapearArtistas(artistas);

        if (!Files.exists(ARCHIVO_EVENTOS)) {
            return eventos;
        }

        for (String linea : Files.readAllLines(ARCHIVO_EVENTOS)) {
            String[] datos = linea.split("\\|", -1);

            if (datos.length >= 12) {
                // Reconstruyo el recital leyendo los campos en el mismo orden.
                DatosRecital datosRecital = new DatosRecital(
                        datos[1],
                        datos[2],
                        LocalDateTime.parse(datos[3]),
                        LocalDateTime.parse(datos[4]),
                        EstadoEvento.valueOf(datos[5]),
                        Integer.parseInt(datos[6]),
                        PlanSuscripcion.valueOf(datos[7]),
                        datos[8],
                        Boolean.parseBoolean(datos[9]),
                        Boolean.parseBoolean(datos[10])
                );

                RecitalEnVivo recital = new RecitalEnVivo(
                        Integer.parseInt(datos[0]),
                        datosRecital
                );

                // Vuelvo a unir el evento con sus artistas guardados.
                asociarArtistas(recital, datos[11], artistasPorId);
                eventos.add(recital);
            }
        }

        return eventos;
    }

    /*
     * Reconstruye los registros de acceso guardados, buscando el usuario y
     * el evento correspondientes por ID entre los ya cargados. Si alguno
     * no aparece (por ejemplo, se borro al usuario), se descarta esa linea
     * en vez de romper la carga completa.
     */
    private static List<RegistroAcceso> cargarRegistros(
            List<Usuario> usuarios, List<Evento> eventos)
            throws IOException {

        List<RegistroAcceso> registros = new ArrayList<>();

        if (!Files.exists(ARCHIVO_REGISTROS)) {
            return registros;
        }

        for (String linea : Files.readAllLines(ARCHIVO_REGISTROS)) {
            String[] datos = linea.split("\\|", -1);

            if (datos.length >= 5) {
                Usuario usuario = buscarUsuarioPorId(
                        usuarios, Integer.parseInt(datos[1]));
                Evento evento = buscarEventoPorId(
                        eventos, Integer.parseInt(datos[2]));

                if (usuario != null && evento != null) {
                    registros.add(RegistroAcceso.reconstruirRegistro(
                            usuario,
                            evento,
                            LocalDateTime.parse(datos[0]),
                            Boolean.parseBoolean(datos[3]),
                            datos[4]
                    ));
                }
            }
        }

        return registros;
    }

    private static Usuario buscarUsuarioPorId(List<Usuario> usuarios, int id) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }

        return null;
    }

    private static Evento buscarEventoPorId(List<Evento> eventos, int id) {
        for (Evento evento : eventos) {
            if (evento.getId() == id) {
                return evento;
            }
        }

        return null;
    }

    private static Map<Integer, Artista> mapearArtistas(
            List<Artista> artistas) {

        // Mapa: clave = ID del artista, valor = objeto Artista.
        // Sirve para asociar rapido eventos con artistas al cargar.
        Map<Integer, Artista> mapa = new HashMap<>();

        for (Artista artista : artistas) {
            mapa.put(artista.getId(), artista);
        }

        return mapa;
    }

    private static void asociarArtistas(RecitalEnVivo recital,
                                        String idsArtistas,
                                        Map<Integer, Artista> artistasPorId) {

        if (idsArtistas == null || idsArtistas.isEmpty()) {
            return;
        }

        // En el archivo se guardan IDs separados por coma, por ejemplo: 1,3.
        String[] ids = idsArtistas.split(",");

        for (String id : ids) {
            // Busco el artista que corresponde a cada ID.
            Artista artista = artistasPorId.get(Integer.parseInt(id));

            if (artista != null) {
                // agregarArtista tambien registra el evento en el artista.
                recital.agregarArtista(artista);
            }
        }
    }

    private static String obtenerIdsArtistas(Evento evento) {
        List<String> ids = new ArrayList<>();

        for (Artista artista : evento.getArtistas()) {
            // Guardo solo el ID para no repetir todos los datos del artista.
            ids.add(String.valueOf(artista.getId()));
        }

        // Une los IDs con coma: "1,2,3".
        return String.join(",", ids);
    }

    private static String limpiar(String valor) {
        if (valor == null) {
            return "";
        }

        // Evito que el usuario rompa el formato del archivo escribiendo |
        // o saltos de linea dentro de un campo.
        return valor
                .replace("|", " ")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}
