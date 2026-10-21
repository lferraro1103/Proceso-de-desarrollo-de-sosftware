# Matriz de relaciones del código integrado

Generada desde el inventario de 91 fuentes / 97 tipos, revisando por separado las decisiones de propiedad. Las referencias escalares sin validación de null tienen extremo 0..1. Las interfaces del JDK necesarias se distinguen como contexto externo.

## Relaciones estructurales (82)

| # | Origen | Relación | Destino | Rol | Extremo origen / destino | Evidencia |
|---|---|---|---|---|---|---|
| 1 | `api.EstadoDto` | Asociación | `api.ArtistaDto` | artistas | 0..* / 0..* | [EstadoDto.java:1](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L1) |
| 2 | `api.EstadoDto` | Asociación | `api.CuentaDto` | cuenta | 0..* / 0..1 | [EstadoDto.java:1](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L1) |
| 3 | `api.EstadoDto` | Asociación | `api.EventoDto` | eventos | 0..* / 0..* | [EstadoDto.java:1](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L1) |
| 4 | `api.EstadoDto` | Asociación | `api.RegistroDto` | registros | 0..* / 0..* | [EstadoDto.java:1](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L1) |
| 5 | `api.EstadoDto` | Asociación | `api.UsuarioDto` | usuarios | 0..* / 0..* | [EstadoDto.java:1](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L1) |
| 6 | `api.SesionDto` | Asociación | `api.CuentaDto` | cuenta | 0..* / 0..1 | [SesionDto.java:1](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/SesionDto.java#L1) |
| 7 | `calendario.VinculoApple` | Realización | `gestor.VinculoCalendario` | — | No aplica | [VinculoApple.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/calendario/VinculoApple.java#L15) |
| 8 | `calendario.VinculoGoogle` | Realización | `gestor.VinculoCalendario` | — | No aplica | [VinculoGoogle.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/calendario/VinculoGoogle.java#L18) |
| 9 | `cliente.EstadoLocal` | Asociación | `modelo.Artista` | artistas | 0..* / 0..* | [EstadoLocal.java:36](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L36) |
| 10 | `cliente.EstadoLocal` | Asociación | `modelo.Cuenta` | cuenta | 0..* / 0..1 | [EstadoLocal.java:34](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L34) |
| 11 | `cliente.EstadoLocal` | Asociación | `modelo.Evento` | eventos | 0..* / 0..* | [EstadoLocal.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L37) |
| 12 | `cliente.EstadoLocal` | Asociación | `modelo.RegistroAcceso` | registros | 0..* / 0..* | [EstadoLocal.java:38](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L38) |
| 13 | `cliente.EstadoLocal` | Asociación | `modelo.Usuario` | usuarios | 0..* / 0..* | [EstadoLocal.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L35) |
| 14 | `cliente.GestorRemoto` | Realización | `gestor.ServicioEventos` | — | No aplica | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| 15 | `cliente.GestorRemoto` | Asociación | `cliente.EstadoLocal` | estado | 0..* / 0..1 | [GestorRemoto.java:53](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L53) |
| 16 | `cliente.GestorRemoto` | Composición | `cliente.ClienteHttp` | http | 0..1 / 1 | [GestorRemoto.java:49](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L49) |
| 17 | `cliente.GestorRemoto` | Composición | `gestor.Permisos` | permisos | 0..1 / 1 | [GestorRemoto.java:50](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L50) |
| 18 | `excepciones.AccesoDenegadoException` | Generalización | `java.lang.Exception` | — | No aplica | [AccesoDenegadoException.java:13](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/excepciones/AccesoDenegadoException.java#L13) |
| 19 | `excepciones.ConexionException` | Generalización | `excepciones.ErrorDeServicioException` | — | No aplica | [ConexionException.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/excepciones/ConexionException.java#L7) |
| 20 | `excepciones.ErrorDeServicioException` | Generalización | `java.lang.RuntimeException` | — | No aplica | [ErrorDeServicioException.java:10](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/excepciones/ErrorDeServicioException.java#L10) |
| 21 | `excepciones.PermisoDenegadoException` | Generalización | `java.lang.RuntimeException` | — | No aplica | [PermisoDenegadoException.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/excepciones/PermisoDenegadoException.java#L7) |
| 22 | `excepciones.PersistenciaException` | Generalización | `excepciones.ErrorDeServicioException` | — | No aplica | [PersistenciaException.java:6](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/excepciones/PersistenciaException.java#L6) |
| 23 | `gestor.ControlAcceso` | Asociación | `gestor.Notificador` | notificador | 0..* / 0..1 | [ControlAcceso.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L32) |
| 24 | `gestor.ControlAcceso` | Asociación | `modelo.RegistroAcceso` | registros | 0..* / 0..* | [ControlAcceso.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L26) |
| 25 | `gestor.ControlSesion` | Asociación | `modelo.Cuenta` | cuentaActual | 0..* / 0..1 | [ControlSesion.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlSesion.java#L22) |
| 26 | `gestor.ControlSesion` | Composición | `gestor.Permisos` | permisos | 0..1 / 1 | [ControlSesion.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlSesion.java#L19) |
| 27 | `gestor.GestorEventosEnVivo` | Realización | `gestor.ServicioEventos` | — | No aplica | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| 28 | `gestor.GestorEventosEnVivo` | Asociación | `modelo.Artista` | artistasRegistrados | 0..* / 0..* | [GestorEventosEnVivo.java:46](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L46) |
| 29 | `gestor.GestorEventosEnVivo` | Composición | `gestor.ControlAcceso` | controlAcceso | 0..1 / 1 | [GestorEventosEnVivo.java:49](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L49) |
| 30 | `gestor.GestorEventosEnVivo` | Asociación | `modelo.Evento` | eventos | 0..* / 0..* | [GestorEventosEnVivo.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L26) |
| 31 | `gestor.GestorEventosEnVivo` | Asociación | `gestor.Persistencia` | persistencia | 0..* / 0..1 | [GestorEventosEnVivo.java:52](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L52) |
| 32 | `gestor.GestorEventosEnVivo` | Asociación | `gestor.ProveedorTransmision` | proveedorTransmision | 0..* / 0..1 | [GestorEventosEnVivo.java:64](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L64) |
| 33 | `gestor.GestorEventosEnVivo` | Asociación | `gestor.PublicadorCalendario` | publicador | 0..* / 0..1 | [GestorEventosEnVivo.java:58](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L58) |
| 34 | `gestor.GestorEventosEnVivo` | Composición | `gestor.ControlSesion` | sesion | 0..1 / 1 | [GestorEventosEnVivo.java:55](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L55) |
| 35 | `gestor.GestorEventosEnVivo` | Asociación | `modelo.Usuario` | usuariosRegistrados | 0..* / 0..* | [GestorEventosEnVivo.java:43](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L43) |
| 36 | `gestor.GestorEventosEnVivo` | Composición | `gestor.PoliticasVisibilidad` | visibilidad | 0..1 / 1 | [GestorEventosEnVivo.java:61](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L61) |
| 37 | `gestor.PoliticasVisibilidad` | Composición | `gestor.PoliticaVisibilidadEvento` | porRol | 0..1 / 3 | [PoliticasVisibilidad.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticasVisibilidad.java#L15) |
| 38 | `gestor.ServicioEventos` | Generalización | `gestor.ServicioComandos` | — | No aplica | [ServicioEventos.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioEventos.java#L11) |
| 39 | `gestor.ServicioEventos` | Generalización | `gestor.ServicioConsultas` | — | No aplica | [ServicioEventos.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioEventos.java#L11) |
| 40 | `gestor.ServicioEventos` | Generalización | `gestor.ServicioSesion` | — | No aplica | [ServicioEventos.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioEventos.java#L11) |
| 41 | `gestor.VisibilidadArtista` | Realización | `gestor.PoliticaVisibilidadEvento` | — | No aplica | [VisibilidadArtista.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadArtista.java#L8) |
| 42 | `gestor.VisibilidadGeneral` | Realización | `gestor.PoliticaVisibilidadEvento` | — | No aplica | [VisibilidadGeneral.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadGeneral.java#L7) |
| 43 | `modelo.AccesoEvento` | Asociación | `modelo.Evento` | evento | 0..* / 0..1 | [AccesoEvento.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/AccesoEvento.java#L23) |
| 44 | `modelo.AccesoEvento` | Asociación | `modelo.Usuario` | usuario | 0..* / 0..1 | [AccesoEvento.java:20](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/AccesoEvento.java#L20) |
| 45 | `modelo.Artista` | Generalización | `modelo.Cuenta` | — | No aplica | [Artista.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Artista.java#L15) |
| 46 | `modelo.Artista` | Asociación | `modelo.Evento` | eventosCreados | 0..* / 0..* | [Artista.java:30](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Artista.java#L30) |
| 47 | `modelo.Evento` | Asociación | `modelo.Artista` | artistas | 0..* / 0..* | [Evento.java:40](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Evento.java#L40) |
| 48 | `modelo.RecitalEnVivo` | Generalización | `modelo.Evento` | — | No aplica | [RecitalEnVivo.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L19) |
| 49 | `modelo.RecitalEnVivo` | Asociación | `modelo.Usuario` | usuariosConectados | 0..* / 0..* | [RecitalEnVivo.java:34](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L34) |
| 50 | `modelo.RegistroAcceso` | Asociación | `modelo.Evento` | evento | 0..* / 0..1 | [RegistroAcceso.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RegistroAcceso.java#L19) |
| 51 | `modelo.RegistroAcceso` | Asociación | `modelo.Usuario` | usuario | 0..* / 0..1 | [RegistroAcceso.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RegistroAcceso.java#L16) |
| 52 | `modelo.Usuario` | Generalización | `modelo.Cuenta` | — | No aplica | [Usuario.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Usuario.java#L12) |
| 53 | `notificacion.NotificadorInactivo` | Realización | `gestor.Notificador` | — | No aplica | [NotificadorInactivo.java:13](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/notificacion/NotificadorInactivo.java#L13) |
| 54 | `persistencia.PersistenciaSqlite` | Realización | `gestor.Persistencia` | — | No aplica | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| 55 | `persistencia.PersistenciaSqlite` | Composición | `persistencia.RepositorioArtistas` | artistas | 0..1 / 1 | [PersistenciaSqlite.java:30](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L30) |
| 56 | `persistencia.PersistenciaSqlite` | Composición | `persistencia.RepositorioEventos` | eventos | 0..1 / 1 | [PersistenciaSqlite.java:31](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L31) |
| 57 | `persistencia.PersistenciaSqlite` | Composición | `persistencia.RepositorioRegistros` | registros | 0..1 / 1 | [PersistenciaSqlite.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L32) |
| 58 | `persistencia.PersistenciaSqlite` | Composición | `persistencia.RepositorioUsuarios` | usuarios | 0..1 / 1 | [PersistenciaSqlite.java:29](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L29) |
| 59 | `servidor.ManejadorApi` | Realización | `com.sun.net.httpserver.HttpHandler` | — | No aplica | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| 60 | `servidor.ManejadorApi` | Asociación | `gestor.GestorEventosEnVivo` | gestor | 0..* / 0..1 | [ManejadorApi.java:61](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L61) |
| 61 | `servidor.ManejadorApi` | Asociación | `gestor.ProveedorTransmision` | proveedorTransmision | 0..* / 0..1 | [ManejadorApi.java:62](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L62) |
| 62 | `servidor.ManejadorApi` | Composición | `servidor.Sesiones` | sesiones | 0..1 / 1 | [ManejadorApi.java:63](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L63) |
| 63 | `servidor.ManejadorApi.NoAutenticado` | Generalización | `java.lang.RuntimeException` | — | No aplica | [ManejadorApi.java:362](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L362) |
| 64 | `servidor.ManejadorApi.NoEncontrada` | Generalización | `java.lang.RuntimeException` | — | No aplica | [ManejadorApi.java:368](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L368) |
| 65 | `servidor.ManejadorApi.SinContenido` | Generalización | `java.lang.RuntimeException` | — | No aplica | [ManejadorApi.java:359](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L359) |
| 66 | `servidor.ManejadorCalendario` | Realización | `com.sun.net.httpserver.HttpHandler` | — | No aplica | [ManejadorCalendario.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L24) |
| 67 | `servidor.ManejadorCalendario` | Asociación | `servidor.CodigosCalendario` | codigos | 0..* / 0..1 | [ManejadorCalendario.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L27) |
| 68 | `servidor.ManejadorCalendario` | Asociación | `gestor.GestorEventosEnVivo` | gestor | 0..* / 0..1 | [ManejadorCalendario.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L26) |
| 69 | `servidor.PublicadorHttp` | Realización | `gestor.PublicadorCalendario` | — | No aplica | [PublicadorHttp.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L12) |
| 70 | `servidor.PublicadorHttp` | Asociación | `servidor.CodigosCalendario` | codigos | 0..* / 0..1 | [PublicadorHttp.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L18) |
| 71 | `servidor.PublicadorHttp` | Asociación | `servidor.ConfiguracionServidor` | configuracion | 0..* / 0..1 | [PublicadorHttp.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L17) |
| 72 | `transmision.ClienteTransmision` | Realización | `gestor.ProveedorTransmision` | — | No aplica | [ClienteTransmision.java:28](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/transmision/ClienteTransmision.java#L28) |
| 73 | `ui.ModeloTabla` | Generalización | `javax.swing.table.DefaultTableModel` | — | No aplica | [ModeloTabla.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/ModeloTabla.java#L11) |
| 74 | `ui.VentanaLogin` | Generalización | `javax.swing.JFrame` | — | No aplica | [VentanaLogin.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaLogin.java#L25) |
| 75 | `ui.VentanaLogin` | Asociación | `gestor.ServicioEventos` | gestor | 0..* / 0..1 | [VentanaLogin.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaLogin.java#L27) |
| 76 | `ui.VentanaPrincipal` | Generalización | `javax.swing.JFrame` | — | No aplica | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| 77 | `ui.VentanaPrincipal` | Asociación | `gestor.ServicioEventos` | gestor | 0..* / 0..1 | [VentanaPrincipal.java:88](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L88) |
| 78 | `ui.VentanaPrincipal` | Asociación | `ui.ModeloTabla` | modeloArtistas | 0..* / 0..1 | [VentanaPrincipal.java:93](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L93) |
| 79 | `ui.VentanaPrincipal` | Asociación | `ui.ModeloTabla` | modeloEventos | 0..* / 0..1 | [VentanaPrincipal.java:94](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L94) |
| 80 | `ui.VentanaPrincipal` | Asociación | `ui.ModeloTabla` | modeloRegistros | 0..* / 0..1 | [VentanaPrincipal.java:95](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L95) |
| 81 | `ui.VentanaPrincipal` | Asociación | `ui.ModeloTabla` | modeloUsuarios | 0..* / 0..1 | [VentanaPrincipal.java:92](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L92) |
| 82 | `ui.VentanaPrincipal.RenderizadorEstado` | Generalización | `javax.swing.table.DefaultTableCellRenderer` | — | No aplica | [VentanaPrincipal.java:287](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L287) |

## Dependencias de tipos resueltas por el compilador (359)

Son referencias en firmas o cuerpos. No implican propiedad, un campo ni una asociación estructural. Se conserva la matriz completa para consultar sin saturar las láminas.

| Cliente | Proveedor de tipo | Código del cliente |
|---|---|---|
| `Main` | `app.App` | [Main.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/Main.java#L8) |
| `api.EstadoDto` | `api.ArtistaDto` | [EstadoDto.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L8) |
| `api.EstadoDto` | `api.CuentaDto` | [EstadoDto.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L8) |
| `api.EstadoDto` | `api.EventoDto` | [EstadoDto.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L8) |
| `api.EstadoDto` | `api.RegistroDto` | [EstadoDto.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L8) |
| `api.EstadoDto` | `api.UsuarioDto` | [EstadoDto.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/EstadoDto.java#L8) |
| `api.SesionDto` | `api.CuentaDto` | [SesionDto.java:6](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/api/SesionDto.java#L6) |
| `app.App` | `app.AppSwing` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.App` | `app.Arranque` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.App` | `app.ConsoleIO` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.App` | `app.MenuAcciones` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.App` | `app.MenuPorRol` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.App` | `cliente.GestorRemoto` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.App` | `gestor.ServicioEventos` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.App` | `servidor.ConfiguracionServidor` | [App.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/App.java#L24) |
| `app.AppSwing` | `gestor.ServicioEventos` | [AppSwing.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/AppSwing.java#L14) |
| `app.AppSwing` | `ui.VentanaLogin` | [AppSwing.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/AppSwing.java#L14) |
| `app.Arranque` | `app.DatosIniciales` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `gestor.GestorEventosEnVivo` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `gestor.ProveedorTransmision` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `notificacion.NotificadorInactivo` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `persistencia.PersistenciaSqlite` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `servidor.CodigosCalendario` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `servidor.ConfiguracionServidor` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `servidor.ManejadorApi` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `servidor.ManejadorCalendario` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `servidor.PublicadorHttp` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `servidor.ServidorHttp` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `transmision.ClienteTransmision` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.Arranque` | `transmision.ProcesoServicio` | [Arranque.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/Arranque.java#L26) |
| `app.DatosIniciales` | `gestor.GestorEventosEnVivo` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.Artista` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.Contrasenas` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.DatosRecital` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.EstadoEvento` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.PlanSuscripcion` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.RecitalEnVivo` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.RegistroAcceso` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.TipoUsuario` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.DatosIniciales` | `modelo.Usuario` | [DatosIniciales.java:26](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/DatosIniciales.java#L26) |
| `app.MenuAcciones` | `app.ConsoleIO` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `calendario.VinculoApple` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `calendario.VinculoGoogle` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `excepciones.AccesoDenegadoException` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `excepciones.ErrorDeServicioException` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `gestor.ServicioEventos` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `gestor.Transmision` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `gestor.VinculoCalendario` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.Artista` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.Cuenta` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.DatosRecital` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.EstadoEvento` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.Evento` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.PlanSuscripcion` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.RecitalEnVivo` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.RegistroAcceso` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.TipoUsuario` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `modelo.Usuario` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `ui.Navegador` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuAcciones` | `ui.VentanaPrincipal` | [MenuAcciones.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuAcciones.java#L35) |
| `app.MenuPorRol` | `app.ConsoleIO` | [MenuPorRol.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuPorRol.java#L19) |
| `app.MenuPorRol` | `app.MenuAcciones` | [MenuPorRol.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuPorRol.java#L19) |
| `app.MenuPorRol` | `excepciones.ErrorDeServicioException` | [MenuPorRol.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuPorRol.java#L19) |
| `app.MenuPorRol` | `excepciones.PermisoDenegadoException` | [MenuPorRol.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuPorRol.java#L19) |
| `app.MenuPorRol` | `gestor.Accion` | [MenuPorRol.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuPorRol.java#L19) |
| `app.MenuPorRol` | `gestor.ServicioEventos` | [MenuPorRol.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/app/MenuPorRol.java#L19) |
| `calendario.FormatoIcs` | `modelo.EstadoEvento` | [FormatoIcs.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/calendario/FormatoIcs.java#L19) |
| `calendario.FormatoIcs` | `modelo.Evento` | [FormatoIcs.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/calendario/FormatoIcs.java#L19) |
| `calendario.VinculoApple` | `gestor.VinculoCalendario` | [VinculoApple.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/calendario/VinculoApple.java#L15) |
| `calendario.VinculoGoogle` | `gestor.VinculoCalendario` | [VinculoGoogle.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/calendario/VinculoGoogle.java#L18) |
| `cliente.ClienteHttp` | `api.ErrorDto` | [ClienteHttp.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/ClienteHttp.java#L22) |
| `cliente.ClienteHttp` | `api.Json` | [ClienteHttp.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/ClienteHttp.java#L22) |
| `cliente.ClienteHttp` | `excepciones.AccesoDenegadoException` | [ClienteHttp.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/ClienteHttp.java#L22) |
| `cliente.ClienteHttp` | `excepciones.ConexionException` | [ClienteHttp.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/ClienteHttp.java#L22) |
| `cliente.ClienteHttp` | `excepciones.PermisoDenegadoException` | [ClienteHttp.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/ClienteHttp.java#L22) |
| `cliente.ClienteHttp` | `excepciones.PersistenciaException` | [ClienteHttp.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/ClienteHttp.java#L22) |
| `cliente.EstadoLocal` | `api.ArtistaDto` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `api.EstadoDto` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `api.EventoDto` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `api.RegistroDto` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `api.UsuarioDto` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.Artista` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.Cuenta` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.DatosRecital` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.EstadoEvento` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.Evento` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.PlanSuscripcion` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.RecitalEnVivo` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.RegistroAcceso` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.TipoUsuario` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.EstadoLocal` | `modelo.Usuario` | [EstadoLocal.java:32](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/EstadoLocal.java#L32) |
| `cliente.GestorRemoto` | `api.EstadoDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.IdDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.InicioSesionDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.Json` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.NuevoArtistaDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.NuevoEventoDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.NuevoUsuarioDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.RegistroDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.SesionDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.TransmisionDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.UrlDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `api.UsuarioPedidoDto` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `cliente.ClienteHttp` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `cliente.EstadoLocal` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `excepciones.AccesoDenegadoException` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `excepciones.ConexionException` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `excepciones.PermisoDenegadoException` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `gestor.Accion` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `gestor.Permisos` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `gestor.ServicioEventos` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `gestor.Transmision` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `gestor.VinculoCalendario` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.Artista` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.Cuenta` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.DatosRecital` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.Evento` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.PlanSuscripcion` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.RecitalEnVivo` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.RegistroAcceso` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `cliente.GestorRemoto` | `modelo.Usuario` | [GestorRemoto.java:47](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/cliente/GestorRemoto.java#L47) |
| `excepciones.ConexionException` | `excepciones.ErrorDeServicioException` | [ConexionException.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/excepciones/ConexionException.java#L7) |
| `excepciones.PersistenciaException` | `excepciones.ErrorDeServicioException` | [PersistenciaException.java:6](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/excepciones/PersistenciaException.java#L6) |
| `gestor.ControlAcceso` | `excepciones.AccesoDenegadoException` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlAcceso` | `gestor.Notificador` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlAcceso` | `modelo.AccesoEvento` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlAcceso` | `modelo.Evento` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlAcceso` | `modelo.RecitalEnVivo` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlAcceso` | `modelo.RegistroAcceso` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlAcceso` | `modelo.TipoUsuario` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlAcceso` | `modelo.Usuario` | [ControlAcceso.java:23](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlAcceso.java#L23) |
| `gestor.ControlSesion` | `excepciones.PermisoDenegadoException` | [ControlSesion.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlSesion.java#L16) |
| `gestor.ControlSesion` | `gestor.Accion` | [ControlSesion.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlSesion.java#L16) |
| `gestor.ControlSesion` | `gestor.Permisos` | [ControlSesion.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlSesion.java#L16) |
| `gestor.ControlSesion` | `modelo.Cuenta` | [ControlSesion.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ControlSesion.java#L16) |
| `gestor.GestorEventosEnVivo` | `excepciones.AccesoDenegadoException` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `excepciones.PermisoDenegadoException` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `excepciones.PersistenciaException` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.Accion` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.ConsultaTransmision` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.ControlAcceso` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.ControlSesion` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.Notificador` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.Persistencia` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.PoliticasVisibilidad` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.ProveedorTransmision` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.PublicadorCalendario` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.ServicioEventos` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.Transmision` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `gestor.VinculoCalendario` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.Artista` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.Contrasenas` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.Cuenta` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.DatosRecital` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.EstadoEvento` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.Evento` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.PlanSuscripcion` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.RecitalEnVivo` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.RegistroAcceso` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.TipoUsuario` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.GestorEventosEnVivo` | `modelo.Usuario` | [GestorEventosEnVivo.java:37](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/GestorEventosEnVivo.java#L37) |
| `gestor.Notificador` | `modelo.Usuario` | [Notificador.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/Notificador.java#L11) |
| `gestor.Permisos` | `gestor.Accion` | [Permisos.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/Permisos.java#L19) |
| `gestor.Permisos` | `modelo.TipoUsuario` | [Permisos.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/Permisos.java#L19) |
| `gestor.Persistencia` | `gestor.GestorEventosEnVivo` | [Persistencia.java:13](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/Persistencia.java#L13) |
| `gestor.PoliticaVisibilidadEvento` | `modelo.Cuenta` | [PoliticaVisibilidadEvento.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticaVisibilidadEvento.java#L7) |
| `gestor.PoliticaVisibilidadEvento` | `modelo.Evento` | [PoliticaVisibilidadEvento.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticaVisibilidadEvento.java#L7) |
| `gestor.PoliticasVisibilidad` | `gestor.PoliticaVisibilidadEvento` | [PoliticasVisibilidad.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticasVisibilidad.java#L14) |
| `gestor.PoliticasVisibilidad` | `gestor.VisibilidadArtista` | [PoliticasVisibilidad.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticasVisibilidad.java#L14) |
| `gestor.PoliticasVisibilidad` | `gestor.VisibilidadGeneral` | [PoliticasVisibilidad.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticasVisibilidad.java#L14) |
| `gestor.PoliticasVisibilidad` | `modelo.Cuenta` | [PoliticasVisibilidad.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticasVisibilidad.java#L14) |
| `gestor.PoliticasVisibilidad` | `modelo.Evento` | [PoliticasVisibilidad.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticasVisibilidad.java#L14) |
| `gestor.PoliticasVisibilidad` | `modelo.TipoUsuario` | [PoliticasVisibilidad.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PoliticasVisibilidad.java#L14) |
| `gestor.ProveedorTransmision` | `gestor.ConsultaTransmision` | [ProveedorTransmision.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ProveedorTransmision.java#L12) |
| `gestor.ProveedorTransmision` | `gestor.Transmision` | [ProveedorTransmision.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ProveedorTransmision.java#L12) |
| `gestor.PublicadorCalendario` | `modelo.Cuenta` | [PublicadorCalendario.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PublicadorCalendario.java#L15) |
| `gestor.PublicadorCalendario` | `modelo.Evento` | [PublicadorCalendario.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/PublicadorCalendario.java#L15) |
| `gestor.ServicioComandos` | `excepciones.AccesoDenegadoException` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioComandos` | `gestor.VinculoCalendario` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioComandos` | `modelo.Artista` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioComandos` | `modelo.DatosRecital` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioComandos` | `modelo.PlanSuscripcion` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioComandos` | `modelo.RecitalEnVivo` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioComandos` | `modelo.RegistroAcceso` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioComandos` | `modelo.Usuario` | [ServicioComandos.java:18](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioComandos.java#L18) |
| `gestor.ServicioConsultas` | `excepciones.AccesoDenegadoException` | [ServicioConsultas.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioConsultas.java#L17) |
| `gestor.ServicioConsultas` | `gestor.Transmision` | [ServicioConsultas.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioConsultas.java#L17) |
| `gestor.ServicioConsultas` | `modelo.Artista` | [ServicioConsultas.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioConsultas.java#L17) |
| `gestor.ServicioConsultas` | `modelo.Evento` | [ServicioConsultas.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioConsultas.java#L17) |
| `gestor.ServicioConsultas` | `modelo.RegistroAcceso` | [ServicioConsultas.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioConsultas.java#L17) |
| `gestor.ServicioConsultas` | `modelo.Usuario` | [ServicioConsultas.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioConsultas.java#L17) |
| `gestor.ServicioEventos` | `gestor.ServicioComandos` | [ServicioEventos.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioEventos.java#L11) |
| `gestor.ServicioEventos` | `gestor.ServicioConsultas` | [ServicioEventos.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioEventos.java#L11) |
| `gestor.ServicioEventos` | `gestor.ServicioSesion` | [ServicioEventos.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioEventos.java#L11) |
| `gestor.ServicioSesion` | `excepciones.AccesoDenegadoException` | [ServicioSesion.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioSesion.java#L14) |
| `gestor.ServicioSesion` | `gestor.Accion` | [ServicioSesion.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioSesion.java#L14) |
| `gestor.ServicioSesion` | `modelo.Cuenta` | [ServicioSesion.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/ServicioSesion.java#L14) |
| `gestor.VisibilidadArtista` | `gestor.PoliticaVisibilidadEvento` | [VisibilidadArtista.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadArtista.java#L8) |
| `gestor.VisibilidadArtista` | `modelo.Artista` | [VisibilidadArtista.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadArtista.java#L8) |
| `gestor.VisibilidadArtista` | `modelo.Cuenta` | [VisibilidadArtista.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadArtista.java#L8) |
| `gestor.VisibilidadArtista` | `modelo.Evento` | [VisibilidadArtista.java:8](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadArtista.java#L8) |
| `gestor.VisibilidadGeneral` | `gestor.PoliticaVisibilidadEvento` | [VisibilidadGeneral.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadGeneral.java#L7) |
| `gestor.VisibilidadGeneral` | `modelo.Cuenta` | [VisibilidadGeneral.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadGeneral.java#L7) |
| `gestor.VisibilidadGeneral` | `modelo.Evento` | [VisibilidadGeneral.java:7](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/gestor/VisibilidadGeneral.java#L7) |
| `modelo.AccesoEvento` | `excepciones.AccesoDenegadoException` | [AccesoEvento.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/AccesoEvento.java#L14) |
| `modelo.AccesoEvento` | `modelo.Evento` | [AccesoEvento.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/AccesoEvento.java#L14) |
| `modelo.AccesoEvento` | `modelo.PlanSuscripcion` | [AccesoEvento.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/AccesoEvento.java#L14) |
| `modelo.AccesoEvento` | `modelo.Usuario` | [AccesoEvento.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/AccesoEvento.java#L14) |
| `modelo.Artista` | `modelo.Cuenta` | [Artista.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Artista.java#L15) |
| `modelo.Artista` | `modelo.Evento` | [Artista.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Artista.java#L15) |
| `modelo.Artista` | `modelo.TipoUsuario` | [Artista.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Artista.java#L15) |
| `modelo.Cuenta` | `modelo.Contrasenas` | [Cuenta.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Cuenta.java#L11) |
| `modelo.Cuenta` | `modelo.TipoUsuario` | [Cuenta.java:11](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Cuenta.java#L11) |
| `modelo.DatosRecital` | `modelo.EstadoEvento` | [DatosRecital.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/DatosRecital.java#L14) |
| `modelo.DatosRecital` | `modelo.PlanSuscripcion` | [DatosRecital.java:14](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/DatosRecital.java#L14) |
| `modelo.Evento` | `excepciones.AccesoDenegadoException` | [Evento.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Evento.java#L16) |
| `modelo.Evento` | `modelo.Artista` | [Evento.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Evento.java#L16) |
| `modelo.Evento` | `modelo.EstadoEvento` | [Evento.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Evento.java#L16) |
| `modelo.Evento` | `modelo.PlanSuscripcion` | [Evento.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Evento.java#L16) |
| `modelo.Evento` | `modelo.Usuario` | [Evento.java:16](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Evento.java#L16) |
| `modelo.RecitalEnVivo` | `excepciones.AccesoDenegadoException` | [RecitalEnVivo.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L19) |
| `modelo.RecitalEnVivo` | `modelo.DatosRecital` | [RecitalEnVivo.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L19) |
| `modelo.RecitalEnVivo` | `modelo.EstadoEvento` | [RecitalEnVivo.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L19) |
| `modelo.RecitalEnVivo` | `modelo.Evento` | [RecitalEnVivo.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L19) |
| `modelo.RecitalEnVivo` | `modelo.PlanSuscripcion` | [RecitalEnVivo.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L19) |
| `modelo.RecitalEnVivo` | `modelo.Usuario` | [RecitalEnVivo.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RecitalEnVivo.java#L19) |
| `modelo.RegistroAcceso` | `modelo.Evento` | [RegistroAcceso.java:13](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RegistroAcceso.java#L13) |
| `modelo.RegistroAcceso` | `modelo.Usuario` | [RegistroAcceso.java:13](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/RegistroAcceso.java#L13) |
| `modelo.Usuario` | `modelo.Contrasenas` | [Usuario.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Usuario.java#L12) |
| `modelo.Usuario` | `modelo.Cuenta` | [Usuario.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Usuario.java#L12) |
| `modelo.Usuario` | `modelo.PlanSuscripcion` | [Usuario.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Usuario.java#L12) |
| `modelo.Usuario` | `modelo.TipoUsuario` | [Usuario.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/modelo/Usuario.java#L12) |
| `notificacion.NotificadorInactivo` | `gestor.Notificador` | [NotificadorInactivo.java:13](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/notificacion/NotificadorInactivo.java#L13) |
| `notificacion.NotificadorInactivo` | `modelo.Usuario` | [NotificadorInactivo.java:13](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/notificacion/NotificadorInactivo.java#L13) |
| `persistencia.PersistenciaSqlite` | `gestor.GestorEventosEnVivo` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `gestor.Persistencia` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `modelo.Artista` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `modelo.Evento` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `modelo.RegistroAcceso` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `modelo.Usuario` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `persistencia.ConexionSqlite` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `persistencia.RepositorioArtistas` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `persistencia.RepositorioEventos` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `persistencia.RepositorioRegistros` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.PersistenciaSqlite` | `persistencia.RepositorioUsuarios` | [PersistenciaSqlite.java:27](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/PersistenciaSqlite.java#L27) |
| `persistencia.RepositorioArtistas` | `modelo.Artista` | [RepositorioArtistas.java:17](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioArtistas.java#L17) |
| `persistencia.RepositorioEventos` | `modelo.Artista` | [RepositorioEventos.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioEventos.java#L25) |
| `persistencia.RepositorioEventos` | `modelo.DatosRecital` | [RepositorioEventos.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioEventos.java#L25) |
| `persistencia.RepositorioEventos` | `modelo.EstadoEvento` | [RepositorioEventos.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioEventos.java#L25) |
| `persistencia.RepositorioEventos` | `modelo.Evento` | [RepositorioEventos.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioEventos.java#L25) |
| `persistencia.RepositorioEventos` | `modelo.PlanSuscripcion` | [RepositorioEventos.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioEventos.java#L25) |
| `persistencia.RepositorioEventos` | `modelo.RecitalEnVivo` | [RepositorioEventos.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioEventos.java#L25) |
| `persistencia.RepositorioEventos` | `modelo.Usuario` | [RepositorioEventos.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioEventos.java#L25) |
| `persistencia.RepositorioRegistros` | `modelo.Evento` | [RepositorioRegistros.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioRegistros.java#L22) |
| `persistencia.RepositorioRegistros` | `modelo.RegistroAcceso` | [RepositorioRegistros.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioRegistros.java#L22) |
| `persistencia.RepositorioRegistros` | `modelo.Usuario` | [RepositorioRegistros.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioRegistros.java#L22) |
| `persistencia.RepositorioUsuarios` | `modelo.PlanSuscripcion` | [RepositorioUsuarios.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioUsuarios.java#L19) |
| `persistencia.RepositorioUsuarios` | `modelo.TipoUsuario` | [RepositorioUsuarios.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioUsuarios.java#L19) |
| `persistencia.RepositorioUsuarios` | `modelo.Usuario` | [RepositorioUsuarios.java:19](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/persistencia/RepositorioUsuarios.java#L19) |
| `servidor.CodigosCalendario` | `modelo.Cuenta` | [CodigosCalendario.java:22](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/CodigosCalendario.java#L22) |
| `servidor.Conversor` | `api.ArtistaDto` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `api.CuentaDto` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `api.EventoDto` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `api.RegistroDto` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `api.UsuarioDto` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `modelo.Artista` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `modelo.Cuenta` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `modelo.Evento` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `modelo.RegistroAcceso` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Conversor` | `modelo.Usuario` | [Conversor.java:21](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Conversor.java#L21) |
| `servidor.Instantanea` | `api.ArtistaDto` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `api.EstadoDto` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `api.EventoDto` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `api.RegistroDto` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `api.UsuarioDto` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `gestor.Accion` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `gestor.GestorEventosEnVivo` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `modelo.Artista` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `modelo.Cuenta` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `modelo.Evento` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `modelo.RegistroAcceso` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `modelo.TipoUsuario` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `modelo.Usuario` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.Instantanea` | `servidor.Conversor` | [Instantanea.java:35](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/Instantanea.java#L35) |
| `servidor.ManejadorApi` | `api.ErrorDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.IdDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.InicioSesionDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.Json` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.NuevoArtistaDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.NuevoEventoDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.NuevoUsuarioDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.SesionDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.TransmisionDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.UrlDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `api.UsuarioPedidoDto` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `excepciones.AccesoDenegadoException` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `excepciones.PermisoDenegadoException` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `excepciones.PersistenciaException` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `gestor.ConsultaTransmision` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `gestor.GestorEventosEnVivo` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `gestor.ProveedorTransmision` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `gestor.Transmision` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `modelo.Artista` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `modelo.Cuenta` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `modelo.DatosRecital` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `modelo.EstadoEvento` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `modelo.PlanSuscripcion` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `modelo.RecitalEnVivo` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `modelo.Usuario` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `servidor.Conversor` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `servidor.Instantanea` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `servidor.ManejadorApi.NoAutenticado` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `servidor.ManejadorApi.NoEncontrada` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `servidor.ManejadorApi.SinContenido` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorApi` | `servidor.Sesiones` | [ManejadorApi.java:57](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorApi.java#L57) |
| `servidor.ManejadorCalendario` | `calendario.FormatoIcs` | [ManejadorCalendario.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L24) |
| `servidor.ManejadorCalendario` | `gestor.GestorEventosEnVivo` | [ManejadorCalendario.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L24) |
| `servidor.ManejadorCalendario` | `modelo.Cuenta` | [ManejadorCalendario.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L24) |
| `servidor.ManejadorCalendario` | `modelo.Evento` | [ManejadorCalendario.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L24) |
| `servidor.ManejadorCalendario` | `servidor.CodigosCalendario` | [ManejadorCalendario.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L24) |
| `servidor.ManejadorCalendario` | `servidor.PublicadorHttp` | [ManejadorCalendario.java:24](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ManejadorCalendario.java#L24) |
| `servidor.PublicadorHttp` | `gestor.PublicadorCalendario` | [PublicadorHttp.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L12) |
| `servidor.PublicadorHttp` | `modelo.Cuenta` | [PublicadorHttp.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L12) |
| `servidor.PublicadorHttp` | `modelo.Evento` | [PublicadorHttp.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L12) |
| `servidor.PublicadorHttp` | `servidor.CodigosCalendario` | [PublicadorHttp.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L12) |
| `servidor.PublicadorHttp` | `servidor.ConfiguracionServidor` | [PublicadorHttp.java:12](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/PublicadorHttp.java#L12) |
| `servidor.ServidorHttp` | `servidor.ConfiguracionServidor` | [ServidorHttp.java:15](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/servidor/ServidorHttp.java#L15) |
| `transmision.ClienteTransmision` | `gestor.ConsultaTransmision` | [ClienteTransmision.java:28](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/transmision/ClienteTransmision.java#L28) |
| `transmision.ClienteTransmision` | `gestor.ProveedorTransmision` | [ClienteTransmision.java:28](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/transmision/ClienteTransmision.java#L28) |
| `transmision.ClienteTransmision` | `gestor.Transmision` | [ClienteTransmision.java:28](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/transmision/ClienteTransmision.java#L28) |
| `transmision.ClienteTransmision` | `transmision.ClienteTransmision.Respuesta` | [ClienteTransmision.java:28](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/transmision/ClienteTransmision.java#L28) |
| `ui.VentanaLogin` | `excepciones.AccesoDenegadoException` | [VentanaLogin.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaLogin.java#L25) |
| `ui.VentanaLogin` | `excepciones.ErrorDeServicioException` | [VentanaLogin.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaLogin.java#L25) |
| `ui.VentanaLogin` | `gestor.ServicioEventos` | [VentanaLogin.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaLogin.java#L25) |
| `ui.VentanaLogin` | `ui.Logo` | [VentanaLogin.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaLogin.java#L25) |
| `ui.VentanaLogin` | `ui.VentanaPrincipal` | [VentanaLogin.java:25](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaLogin.java#L25) |
| `ui.VentanaPrincipal` | `calendario.VinculoApple` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `calendario.VinculoGoogle` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `excepciones.AccesoDenegadoException` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `excepciones.ErrorDeServicioException` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `excepciones.PermisoDenegadoException` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `gestor.Accion` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `gestor.ServicioEventos` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `gestor.Transmision` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `gestor.VinculoCalendario` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.Artista` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.DatosRecital` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.EstadoEvento` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.Evento` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.PlanSuscripcion` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.RegistroAcceso` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.TipoUsuario` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `modelo.Usuario` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `ui.Logo` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `ui.ModeloTabla` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `ui.Navegador` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `ui.VentanaLogin` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `ui.VentanaPrincipal.Formulario` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
| `ui.VentanaPrincipal` | `ui.VentanaPrincipal.RenderizadorEstado` | [VentanaPrincipal.java:77](../../TP/Grupo9_POO_EntregaFinal/Codigo/src/ui/VentanaPrincipal.java#L77) |
