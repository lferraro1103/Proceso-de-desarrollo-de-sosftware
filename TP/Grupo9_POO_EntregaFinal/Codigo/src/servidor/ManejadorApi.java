package servidor;

import api.ErrorDto;
import api.IdDto;
import api.InicioSesionDto;
import api.Json;
import api.NuevoArtistaDto;
import api.NuevoEventoDto;
import api.NuevoUsuarioDto;
import api.SesionDto;
import api.TransmisionDto;
import api.UrlDto;
import api.UsuarioPedidoDto;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import excepciones.AccesoDenegadoException;
import excepciones.PermisoDenegadoException;
import excepciones.PersistenciaException;
import gestor.GestorEventosEnVivo;
import gestor.ProveedorTransmision;
import gestor.Transmision;
import modelo.Artista;
import modelo.Cuenta;
import modelo.DatosRecital;
import modelo.EstadoEvento;
import modelo.PlanSuscripcion;
import modelo.RecitalEnVivo;
import modelo.Usuario;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/*
 * API HTTP con JSON que usan los clientes remotos. Cada ruta traduce el
 * pedido a una llamada al gestor, que es quien aplica las reglas y los
 * permisos: la API no decide nada por su cuenta.
 *
 *   POST   /api/sesion                          iniciar sesion
 *   DELETE /api/sesion                          cerrar sesion
 *   GET    /api/estado                          lo que ve la cuenta
 *   POST   /api/usuarios                        registrar usuario
 *   POST   /api/usuarios/{id}/alternar-activo   activar o desactivar
 *   POST   /api/artistas                        registrar artista
 *   POST   /api/eventos                         crear evento
 *   POST   /api/eventos/{id}/{iniciar|pausar|reanudar|finalizar|cancelar}
 *   POST   /api/eventos/{id}/ingresos           solicitar ingreso
 *   POST   /api/eventos/{id}/expulsiones        expulsar usuario
 *   GET    /api/eventos/{id}/calendario         direccion del calendario
 *   GET    /api/eventos/{id}/transmision        transmision del evento en curso
 *
 * Despues de iniciar sesion, cada pedido lleva "Authorization: Bearer <token>".
 */
public class ManejadorApi implements HttpHandler {

    private static final String PREFIJO = "/api/";

    private final GestorEventosEnVivo gestor;
    private final ProveedorTransmision proveedorTransmision;
    private final Sesiones sesiones = new Sesiones();

    public ManejadorApi(GestorEventosEnVivo gestor,
                        ProveedorTransmision proveedorTransmision) {
        this.gestor = gestor;
        this.proveedorTransmision = proveedorTransmision;
    }

    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        try (intercambio) {
            try {
                enviar(intercambio, 200, atender(intercambio));
            } catch (SinContenido e) {
                intercambio.sendResponseHeaders(204, -1);
            } catch (AccesoDenegadoException e) {
                enviar(intercambio, 409, new ErrorDto("acceso", e.getMessage()));
            } catch (PermisoDenegadoException e) {
                enviar(intercambio, 403, new ErrorDto("permiso", e.getMessage()));
            } catch (PersistenciaException e) {
                enviar(intercambio, 500, new ErrorDto("persistencia", e.getMessage()));
            } catch (IOException e) {
                enviar(intercambio, 409, new ErrorDto("externo", e.getMessage()));
            } catch (IllegalArgumentException | JsonParseException
                     | NullPointerException e) {
                // Datos mal formados o que no corresponden a nada existente.
                enviar(intercambio, 400, new ErrorDto("pedido", mensaje(e)));
            } catch (NoAutenticado e) {
                enviar(intercambio, 401, new ErrorDto("sesion", e.getMessage()));
            } catch (NoEncontrada e) {
                enviar(intercambio, 404, new ErrorDto("ruta", e.getMessage()));
            }
        }
    }

    // Resuelve el pedido y devuelve el objeto de respuesta.
    private Object atender(HttpExchange intercambio) throws AccesoDenegadoException, IOException {
        String ruta = intercambio.getRequestURI().getPath();
        String metodo = intercambio.getRequestMethod();
        String[] partes = ruta.substring(PREFIJO.length()).split("/");

        if (partes[0].equals("sesion")) {
            return atenderSesion(intercambio, metodo);
        }

        String token = token(intercambio);
        Cuenta cuenta = cuentaDe(token);

        // La busqueda de la transmision va a un servicio externo y puede
        // tardar: se hace fuera del bloqueo del gestor.
        if (metodo.equals("GET") && partes.length == 3
                && partes[0].equals("eventos") && partes[2].equals("transmision")) {
            return atenderTransmision(cuenta, Integer.parseInt(partes[1]));
        }

        // Todo el pedido corre dentro del bloqueo del gestor: la sesion de
        // esta cuenta se abre, se usa y se devuelve a como estaba, para no
        // pisar la de quien use el programa en este mismo equipo.
        synchronized (gestor) {
            Cuenta anterior = gestor.getCuentaActual();
            gestor.asumirSesion(cuenta);

            try {
                return ejecutar(partes, metodo, cuerpo(intercambio));
            } finally {
                restaurarSesion(anterior);
            }
        }
    }

    private Object atenderTransmision(Cuenta cuenta, int idEvento)
            throws AccesoDenegadoException, IOException {

        String consulta;

        synchronized (gestor) {
            Cuenta anterior = gestor.getCuentaActual();
            gestor.asumirSesion(cuenta);

            try {
                consulta = gestor.consultaDeTransmision(idEvento);
            } finally {
                restaurarSesion(anterior);
            }
        }

        Transmision transmision = proveedorTransmision.buscar(consulta);
        return new TransmisionDto(transmision.titulo(), transmision.url());
    }

    // Devuelve la sesion del gestor a como estaba antes del pedido.
    private void restaurarSesion(Cuenta anterior) {
        if (anterior != null) {
            gestor.asumirSesion(anterior);
        } else {
            gestor.cerrarSesion();
        }
    }

    private Object atenderSesion(HttpExchange intercambio, String metodo)
            throws AccesoDenegadoException, IOException {

        if (metodo.equals("POST")) {
            InicioSesionDto pedido = Json.leer(
                    cuerpo(intercambio), InicioSesionDto.class);

            Cuenta cuenta = gestor.autenticar(
                    pedido.nombreUsuario(), pedido.contrasena());

            return new SesionDto(sesiones.abrir(cuenta.getNombreUsuario()),
                    Conversor.aDto(cuenta));
        }

        if (metodo.equals("DELETE")) {
            sesiones.cerrar(token(intercambio));
            throw new SinContenido();
        }

        throw new NoEncontrada("Ruta inexistente.");
    }

    private Object ejecutar(String[] partes, String metodo, String cuerpo)
            throws AccesoDenegadoException, IOException {

        String recurso = partes[0];

        if (recurso.equals("estado") && metodo.equals("GET")) {
            return Instantanea.de(gestor);
        }

        if (!metodo.equals("POST") && !(recurso.equals("eventos")
                && partes.length == 3 && partes[2].equals("calendario"))) {
            throw new NoEncontrada("Ruta inexistente.");
        }

        if (recurso.equals("usuarios")) {
            return ejecutarUsuarios(partes, cuerpo);
        }

        if (recurso.equals("artistas") && partes.length == 1) {
            NuevoArtistaDto p = Json.leer(cuerpo, NuevoArtistaDto.class);
            Artista artista = gestor.registrarArtista(p.nombreUsuario(),
                    p.contrasena(), p.nombreArtistico(), p.generoPrincipal(),
                    p.biografia(), p.verificado());
            return new IdDto(artista == null ? null : artista.getId());
        }

        if (recurso.equals("eventos")) {
            return ejecutarEventos(partes, cuerpo);
        }

        throw new NoEncontrada("Ruta inexistente.");
    }

    private Object ejecutarUsuarios(String[] partes, String cuerpo) {
        if (partes.length == 1) {
            NuevoUsuarioDto p = Json.leer(cuerpo, NuevoUsuarioDto.class);
            Usuario usuario = gestor.registrarUsuario(p.nombreUsuario(),
                    p.nombre(), p.apellido(), p.email(), p.contrasena(),
                    PlanSuscripcion.valueOf(p.plan()));
            return new IdDto(usuario == null ? null : usuario.getId());
        }

        if (partes.length == 3 && partes[2].equals("alternar-activo")) {
            Usuario usuario = gestor.alternarActivo(Integer.parseInt(partes[1]));
            return new IdDto(usuario == null ? null : usuario.getId());
        }

        throw new NoEncontrada("Ruta inexistente.");
    }

    private Object ejecutarEventos(String[] partes, String cuerpo)
            throws AccesoDenegadoException, IOException {

        if (partes.length == 1) {
            NuevoEventoDto p = Json.leer(cuerpo, NuevoEventoDto.class);
            Artista artista = gestor.buscarArtistaPorId(p.idArtista());

            if (artista == null) {
                throw new IllegalArgumentException("Artista inexistente.");
            }

            RecitalEnVivo recital = gestor.crearEvento(new DatosRecital(
                    p.titulo(), p.descripcion(),
                    LocalDateTime.parse(p.inicio()), LocalDateTime.parse(p.fin()),
                    EstadoEvento.valueOf(p.estado()), p.capacidadMaxima(),
                    PlanSuscripcion.valueOf(p.planMinimo()), p.ubicacion(),
                    p.streaming(), p.exclusivo()), artista);

            return new IdDto(recital.getId());
        }

        if (partes.length != 3) {
            throw new NoEncontrada("Ruta inexistente.");
        }

        int idEvento = Integer.parseInt(partes[1]);

        switch (partes[2]) {
            case "iniciar":
                gestor.iniciarEvento(idEvento);
                throw new SinContenido();
            case "pausar":
                gestor.pausarEvento(idEvento);
                throw new SinContenido();
            case "reanudar":
                gestor.reanudarEvento(idEvento);
                throw new SinContenido();
            case "finalizar":
                gestor.finalizarEvento(idEvento);
                throw new SinContenido();
            case "cancelar":
                gestor.cancelarEvento(idEvento);
                throw new SinContenido();
            case "ingresos":
                return Conversor.aDto(gestor.solicitarIngreso(
                        Json.leer(cuerpo, UsuarioPedidoDto.class).idUsuario(),
                        idEvento));
            case "expulsiones":
                Usuario expulsado = gestor.expulsarUsuario(idEvento,
                        Json.leer(cuerpo, UsuarioPedidoDto.class).idUsuario());
                return new IdDto(expulsado == null ? null : expulsado.getId());
            case "calendario":
                return new UrlDto(gestor.urlCalendario(idEvento));
            default:
                throw new NoEncontrada("Ruta inexistente.");
        }
    }

    // Cuenta de un token valido y activa; si no, el pedido se rechaza.
    private Cuenta cuentaDe(String token) {
        String nombre = sesiones.cuentaDe(token);

        if (nombre == null) {
            throw new NoAutenticado("Debe iniciar sesion.");
        }

        Cuenta cuenta;

        synchronized (gestor) {
            cuenta = gestor.buscarCuentaPorNombreUsuario(nombre);
        }

        if (cuenta == null || !cuenta.isActivo()) {
            sesiones.cerrar(token);
            throw new NoAutenticado("La cuenta ya no esta activa.");
        }

        return cuenta;
    }

    private static String token(HttpExchange intercambio) {
        String cabecera = intercambio.getRequestHeaders()
                .getFirst("Authorization");

        return (cabecera != null && cabecera.startsWith("Bearer "))
                ? cabecera.substring("Bearer ".length()) : null;
    }

    private static String cuerpo(HttpExchange intercambio) throws IOException {
        return new String(intercambio.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    private static void enviar(HttpExchange intercambio, int codigo,
                               Object cuerpo) throws IOException {

        byte[] bytes = Json.escribir(cuerpo).getBytes(StandardCharsets.UTF_8);

        intercambio.getResponseHeaders().set(
                "Content-Type", "application/json; charset=utf-8");
        intercambio.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(bytes);
        }
    }

    private static String mensaje(Exception e) {
        return e.getMessage() == null ? "Pedido invalido." : e.getMessage();
    }

    // Respuestas especiales que se resuelven como excepciones internas.
    private static class SinContenido extends RuntimeException {
    }

    private static class NoAutenticado extends RuntimeException {
        NoAutenticado(String mensaje) {
            super(mensaje);
        }
    }

    private static class NoEncontrada extends RuntimeException {
        NoEncontrada(String mensaje) {
            super(mensaje);
        }
    }
}
