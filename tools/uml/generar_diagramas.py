"""Fuentes UML reproducibles a partir del inventario AST/bytecode y semántica revisada.

Solo biblioteca estándar. Ejecutar desde la raíz del repositorio.
"""
import json
from modelo_diagramas import GROUPS as CASE_GROUPS, ROLES as ROLE_ACTIONS
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs/iteracion-4"
SRC = OUT / "fuentes"
SRC.mkdir(parents=True, exist_ok=True)
data = json.loads((OUT / "inventario.json").read_text(encoding="utf-8"))
classes = {c["name"]: c for c in data["classes"]}
ids = {n: "c_" + re.sub(r"\W", "_", n) for n in classes}
canonical = {n.replace("$", "."): n for n in classes}

STYLE = '''skinparam backgroundColor #FAFCFF
skinparam defaultFontName SansSerif
skinparam defaultFontSize 13
skinparam shadowing false
skinparam roundcorner 8
skinparam ArrowColor #52657D
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam classBackgroundColor #FFFFFF
skinparam classBorderColor #355C83
skinparam classHeaderBackgroundColor #E6F0FA
skinparam packageBorderColor #9BACBF
skinparam packageBackgroundColor #F1F5FA
skinparam usecaseBackgroundColor #E6F0FA
skinparam usecaseBorderColor #355C83
skinparam activityBackgroundColor #E6F0FA
skinparam activityBorderColor #355C83
skinparam noteBackgroundColor #FFF5D9
skinparam noteBorderColor #C7A65A
skinparam padding 5
'''
(SRC / "estilo.iuml").write_text(STYLE, encoding="utf-8")


def write(name, title, body, extra=""):
    (SRC / f"{name}.puml").write_text(
        f"@startuml\n!include estilo.iuml\nscale max 12000*12000\n{extra}\ntitle UADE Beats · Iteración 4\\n{title}\n"
        + body + "\nfooter Modelo de implementación · 20/10/2026 · Fuentes Java verificadas\n@enduml\n",
        encoding="utf-8",
    )


def short(text):
    for p in ["modelo", "gestor", "cliente", "servidor", "persistencia", "api", "ui", "app", "calendario", "notificacion", "excepciones"]:
        text = text.replace(p + ".", "")
    return text


# Composición solo para colaboradores creados internamente y no expuestos.
OWNED = {
    ("gestor.GestorEventosEnVivo", "controlAcceso"),
    ("gestor.GestorEventosEnVivo", "sesion"),
    ("gestor.GestorEventosEnVivo", "visibilidad"),
    ("gestor.ControlSesion", "permisos"),
    ("cliente.GestorRemoto", "http"),
    ("cliente.GestorRemoto", "permisos"),
    ("servidor.ManejadorApi", "sesiones"),
    *{("persistencia.PersistenciaSqlite", f) for f in ("usuarios", "artistas", "eventos", "registros")},
}


def class_diagram(name, title, packages, complete=False):
    selected = {n for n, c in classes.items() if complete or c["package"] in packages}
    groups = defaultdict(list)
    for n in sorted(selected):
        groups[classes[n]["package"] or "lanzador"].append(n)
    lines = []
    for pkg, names in sorted(groups.items()):
        lines.append(f'package "{pkg}" {{')
        for n in names:
            c = classes[n]
            kind = c["kind"]
            stereotype = " <<record>>" if kind == "record" else ""
            if "final" in c["modifiers"] and kind not in ("enum", "record"):
                stereotype += " <<final>>"
            if "$" in n:
                stereotype += " <<nested>>"
            if n == "gestor.GestorEventosEnVivo":
                stereotype += " <<Facade>>"
            if n in ("gestor.PoliticaVisibilidadEvento", "gestor.VinculoCalendario"):
                stereotype += " <<Strategy>>"
            if kind == "record":
                kind = "class"
            label = n.split(".")[-1].replace("$", ".")
            lines.append(f'{kind} "{label}" as {ids[n]}{stereotype} {{')
            for f in c["fields"]:
                if f["enumConstant"]:
                    lines.append("  " + f["name"])
                else:
                    flags = "{static} " if f["static"] else ""
                    final = " {readOnly}" if f["final"] else ""
                    lines.append(f'  {flags}{f["visibility"]}{f["name"]} : {short(f["type"])}{final}')
            lines.append("  --")
            lines.extend("  " + short(op) for op in c["operations"])
            lines.append("}")
        lines.append("}")
    # Vistas parciales incluyen clasificadores referenciados como contexto, sin sus miembros.
    external = set()
    edges = []
    def target(n):
        if n in selected:
            return ids[n]
        if n in classes:
            external.add(n)
            return ids[n]
        key = "jdk_" + re.sub(r"\W", "_", n)
        external.add(n)
        return key
    for n in sorted(selected):
        c = classes[n]
        linked = set()
        parent = c.get("superclass")
        if parent and parent not in ("java.lang.Object", "java.lang.Enum", "java.lang.Record"):
            edges.append(f'{target(parent)} <|-- {ids[n]}')
            linked.add(parent)
        for interface in c["interfaces"]:
            arrow = "<|--" if c["kind"] == "interface" else "<|.."
            edges.append(f'{target(interface)} {arrow} {ids[n]}')
            linked.add(interface)
        for f in c["fields"]:
            if f["enumConstant"]:
                continue
            for ref in sorted(set(f["references"]) & classes.keys()):
                if ref == n:
                    continue
                many = any(t in f["type"] for t in ("List<", "Set<", "Map<", "[]"))
                owned = (n, f["name"]) in OWNED
                arrow = "*-->" if owned else "-->"
                mult = "0..*" if many else "1" if owned else "0..1"
                origin = '"1" ' if owned else ""
                role = f["name"] + (" {static}" if f["static"] else "")
                edges.append(f'{ids[n]} {origin}{arrow} "{mult}" {target(ref)} : {role}')
                linked.add(ref)
        for dep in c["dependencies"]:
            dep = canonical.get(dep, dep)
            if dep in classes and dep != n and dep not in linked:
                edges.append(f'{ids[n]} ..> {target(dep)} : usa')
    for n in sorted(external):
        if n in classes:
            c = classes[n]
            kind = "interface" if c["kind"] == "interface" else "enum" if c["kind"] == "enum" else "class"
            lines.append(f'{kind} "{n.replace(chr(36), chr(46))}" as {ids[n]} <<contexto>> #EDF0F5')
        else:
            key = "jdk_" + re.sub(r"\W", "_", n)
            kind = "interface" if n.endswith("HttpHandler") else "class"
            lines.append(f'{kind} "{n}" as {key} <<JDK>> #EDF0F5')
    lines.extend(sorted(set(edges)))
    lines.append('''legend left
  + público   - privado   # protegido   ~ paquete
  Flecha sólida: asociación navegable; 0..1 es conservador si Java admite null.
  Triángulo: generalización; línea discontinua: realización o dependencia «usa».
  Rombo lleno: colaborador interno exclusivo; no confundir colección con Composite.
  0..* indica colección; se muestran roles y tipos genéricos.
  Se declaran miembros propios; los heredados se obtienen por generalización.
  Records/enums incluyen operaciones implícitas públicas del compilador.
  «contexto»: miembros completos en su vista de módulo y en el diagrama integral.
endlegend''')
    write(name, title, "\n".join(lines), "skinparam nodesep 45\nskinparam ranksep 60")


class_diagram("clases-completo", "Diagrama integral · todos los tipos y miembros declarados", set(), True)
for name, title, pkgs in [
    ("clases-dominio", "Dominio y reglas de acceso", {"modelo"}),
    ("clases-servicios", "Servicios, permisos, Facade y Strategy", {"gestor"}),
    ("clases-persistencia", "Persistencia y repositorios", {"persistencia"}),
    ("clases-red", "Servidor, sesiones y cliente remoto", {"servidor", "cliente"}),
    ("clases-vistas", "Arranque, consola e interfaz Swing", {"app", "ui", ""}),
    ("clases-api", "Contratos de transporte · DTO", {"api"}),
    ("clases-integraciones", "Calendarios, notificación y excepciones", {"calendario", "notificacion", "excepciones"}),
]:
    class_diagram(name, title, pkgs)

# Matriz literal contrastada con Permisos.java; el generador valida el conjunto de Accion.
ACTIONS = {
    "REGISTRAR_USUARIO": "Registrar usuario", "REGISTRAR_ARTISTA": "Registrar artista",
    "ACTIVAR_DESACTIVAR_USUARIO": "Activar o desactivar usuario", "CREAR_EVENTO": "Crear evento",
    "VER_USUARIOS": "Consultar usuarios", "VER_ARTISTAS": "Consultar artistas",
    "VER_EVENTOS": "Consultar eventos", "INICIAR_EVENTO": "Iniciar evento",
    "PAUSAR_EVENTO": "Pausar evento", "REANUDAR_EVENTO": "Reanudar evento",
    "FINALIZAR_EVENTO": "Finalizar evento", "CANCELAR_EVENTO": "Cancelar evento",
    "SOLICITAR_INGRESO": "Solicitar ingreso", "VER_CONECTADOS": "Consultar conectados",
    "VER_CAPACIDAD": "Consultar capacidad", "VINCULAR_CALENDARIO": "Suscribir calendario",
    "EXPULSAR_USUARIO": "Expulsar usuario", "VER_REGISTROS": "Consultar todos los accesos",
    "VER_MIS_REGISTROS": "Consultar mis accesos", "ABRIR_INTERFAZ_GRAFICA": "Abrir interfaz Swing",
}
enum_actions = {f["name"] for f in classes["gestor.Accion"]["fields"] if f["enumConstant"]}
assert enum_actions == ACTIONS.keys(), "Actualizar matriz: enum Accion cambio"
USER = {"SOLICITAR_INGRESO", "VER_EVENTOS", "VER_CAPACIDAD", "VINCULAR_CALENDARIO", "VER_MIS_REGISTROS", "ABRIR_INTERFAZ_GRAFICA"}
ARTIST = {"CREAR_EVENTO", "VER_EVENTOS", "INICIAR_EVENTO", "PAUSAR_EVENTO", "REANUDAR_EVENTO", "FINALIZAR_EVENTO", "CANCELAR_EVENTO", "VER_CONECTADOS", "VER_CAPACIDAD", "VINCULAR_CALENDARIO", "ABRIR_INTERFAZ_GRAFICA"}
ADMIN = set(ACTIONS) - {"VER_MIS_REGISTROS"}
ROLES = {"usuario": ("Usuario", USER), "artista": ("Artista", ARTIST), "administrador": ("Administrador", ADMIN)}


def use_cases(roles, name, title):
    lines = ["left to right direction"]
    for key, (label, _) in roles.items():
        lines.append(f'actor "{label}" as {key}')
    lines.append('actor "Google / Apple Calendar" as externo')
    lines.append('rectangle "UADE Beats · módulo de eventos en vivo" {')
    lines.extend(['usecase "Iniciar sesión" as login','usecase "Cerrar sesión" as logout'])
    used = set()
    for key, (_, allowed) in roles.items():
        for case, label, actions in CASE_GROUPS:
            if set(actions) <= allowed: used.add(case)
    for case, label, _ in CASE_GROUPS:
        if case in used: lines.append(f'usecase "{label}" as {case}')
    lines.append('}')
    for key, (_, allowed) in roles.items():
        lines.extend([f"{key} -- login", f"{key} -- logout"])
        for case, _, actions in CASE_GROUPS:
            if set(actions) <= allowed: lines.append(f"{key} -- {case}")
    lines.append('externo -- calendario')
    lines.append('note bottom: Login es precondición. Abrir Swing permanece en la matriz técnica, no como objetivo de negocio.')
    write(name, title, "\n".join(lines))


ROLES = {key: (label, ROLE_ACTIONS[key]) for key, (label, _) in ROLES.items()}
for key, role in ROLES.items():
    use_cases({key: role}, "casos-" + key, "Casos de uso · " + role[0])
use_cases(ROLES, "casos-general", "Casos de uso · vista integrada de los tres roles")

matrix = ["# Trazabilidad de permisos y casos de uso", "", "Fuente de autoridad: `gestor/Permisos.java`; validación de matriz ejecutada por la suite Java.", "",
          "| Acción | Caso de uso | Usuario | Artista | Administrador |", "|---|---|:---:|:---:|:---:|"]
for a, label in ACTIONS.items():
    matrix.append(f"| `{a}` | {label} | " + " | ".join("Sí" if a in allowed else "—" for _, allowed in ROLES.values()) + " |")
matrix += ["", "Autenticación y cierre se modelan además de las acciones del enum. Google y Apple son actores externos; SQLite es parte interna del sistema, no un actor.",
           "No se utiliza generalización Administrador → Usuario: sus permisos no forman un superconjunto, porque el administrador no dispone de VER_MIS_REGISTROS."]
(OUT / "TRAZABILIDAD.md").write_text("\n".join(matrix) + "\n", encoding="utf-8")

FLOWS = {
"flujo-autenticacion": ("Flujo de autenticación local y remota", '''start
:Seleccionar modo e interfaz;
:Ingresar cuenta y contraseña;
if (¿Modo remoto?) then (sí)
  :POST /api/sesion;
else (no)
  :Gestor local autentica;
endif
:Buscar cuenta y verificar hash / activo;
if (¿Credenciales válidas?) then (sí)
  if (¿Modo remoto?) then (sí)
    :Crear token aleatorio en memoria;
    :GET /api/estado con Bearer;
    :Filtrar DTO por rol;
  else (no)
    :ControlSesion.iniciar;
  endif
  :Mostrar acciones de Permisos;
else (no)
  :Informar rechazo y permitir reintento;
endif
stop'''),
"flujo-usuario": ("Flujo del usuario", '''start
:Iniciar sesión como usuario;
:Consultar catálogo y capacidad;
switch (Acción elegida)
case (Solicitar ingreso)
  :Elegir evento e ingresar a su nombre;
  :Verificar autorización, plan, estado, horario y capacidad;
  if (¿Aceptado?) then (sí)
    :Conectar usuario;
  else (no)
    :Informar motivo de rechazo;
  endif
  :Registrar intento y guardar en SQLite;
case (Mis registros)
  :Consultar únicamente accesos de la cuenta;
case (Calendario)
  :Elegir Google o Apple;
  :Obtener URL del evento y abrir suscripción;
endswitch
:Cerrar sesión o volver al menú;
stop'''),
"flujo-artista": ("Flujo del artista · alcance propio", '''start
:Iniciar sesión como artista;
:Strategy VisibilidadArtista filtra por participación;
switch (Acción elegida)
case (Crear evento)
  :Capturar DatosRecital;
  :Gestor exige que el artista recibido sea la cuenta actual;
  :Crear y asociar evento;
case (Cambiar estado)
  :Seleccionar evento visible;
  if (¿Participa en el evento?) then (sí)
    :Validar transición del EstadoEvento;
    if (¿Transición válida?) then (sí)
      :Iniciar / pausar / reanudar / finalizar / cancelar;
    else (no)
      :Informar rechazo;
      stop
    endif
  else (no)
    :Denegar operación;
    stop
  endif
case (Consultar)
  :Ver sus eventos, conectados y capacidad;
  stop
case (Calendario)
  :Suscribir Google o Apple a un evento propio;
  stop
endswitch
:Guardar cambios en SQLite;
:Refrescar estado local o remoto;
stop'''),
"flujo-administrador": ("Flujo del administrador", '''start
:Iniciar sesión como administrador;
:ControlSesion verifica acción;
switch (Operación)
case (Altas)
  :Registrar usuario, artista o evento;
  if (¿Datos / cuenta válidos?) then (sí)
    :Agregar entidad y asociaciones;
  else (no)
    :Informar error o nombre duplicado;
    stop
  endif
case (Activación)
  :Seleccionar usuario;
  if (¿Desactiva su propia cuenta?) then (sí)
    :Denegar operación;
    stop
  else (no)
    :Alternar activo;
    :Si desactiva: desconectar y registrar expulsiones;
  endif
case (Evento)
  :Validar transición y cambiar estado;
case (Expulsión)
  if (¿Evento en curso y usuario conectado?) then (sí)
    :Desconectar y registrar expulsión;
  else (no)
    :Informar que no se pudo expulsar;
    stop
  endif
case (Ingreso)
  :Solicitar ingreso de usuario seleccionado;
  :Registrar resultado de validaciones;
case (Consulta / calendario)
  :Consultar cuentas, eventos, registros o suscribir calendario;
  stop
endswitch
:Guardar en SQLite;
if (¿Guardado correcto?) then (sí)
  :Refrescar vistas;
else (no)
  :Informar PersistenciaException;
endif
stop'''),
"flujo-ingreso": ("Ingreso a evento · decisiones y alternativas", '''|Gestor|
start
:Exigir SOLICITAR_INGRESO;
if (¿Rol habilitado y usuario actúa a su nombre?) then (sí)
  :Buscar usuario y evento;
else (no)
  :Lanzar PermisoDenegadoException;
  stop
endif
|ControlAcceso|
if (¿Existen usuario y evento?) then (sí)
  :Construir AccesoEvento;
else (no)
  :Crear registro de rechazo con placeholder;
  :Avisar con NotificadorInactivo (sin efecto);
  |Gestor|
  :Guardar datos;
  note right
    RepositorioRegistros omite referencias
    inexistentes por las claves foráneas.
  end note
  stop
endif
|Dominio|
if (¿Autorización y plan válidos?) then (sí)
  if (¿Estado EN_CURSO y dentro del horario?) then (sí)
    if (¿Hay capacidad?) then (sí)
      if (¿Ya conectado?) then (no)
        :Agregar a usuariosConectados;
        :Crear RegistroAcceso exitoso;
      else (sí)
        :Rechazar duplicado;
      endif
    else (no)
      :Rechazar por capacidad;
    endif
  else (no)
    :Rechazar por estado u horario;
  endif
else (no)
  :Rechazar por autorización o plan;
endif
|ControlAcceso|
:Agregar RegistroAcceso; avisar si hubo rechazo;
|Persistencia|
:Reescribir tablas en transacción;
if (¿Se confirmó la transacción?) then (sí)
  :Devolver resultado;
else (no)
  :Rollback SQL e informar error;
  note right
    El rollback SQL no restaura
    automáticamente el modelo en memoria.
  end note
endif
stop'''),
"flujo-calendario": ("Calendario · Strategy y acceso al feed", '''|Vista|
start
:Elegir evento y Strategy Google / Apple;
|Servicio|
:Exigir VINCULAR_CALENDARIO;
:Evaluar visibilidad con la política del rol;
if (¿Evento visible?) then (sí)
  :PublicadorHttp obtiene URL pública configurada;
else (no)
  :Rechazar pedido;
  stop
endif
if (¿Hay URL pública configurada?) then (sí)
  :Armar URL ICS con código HMAC de cuenta;
else (no)
  :Informar que falta URL pública HTTPS;
  stop
endif
|Vista|
:Invocar VinculoCalendario.vincular(url);
if (¿Proveedor / equipo permite abrir?) then (sí)
  :Google abre navegador / Apple abre webcal;
else (no)
  :Informar IOException y URL manual;
  stop
endif
|Calendario externo|
:Persona confirma la suscripción;
:Solicitar feed /calendario/código/id.ics;
|Servidor|
:Validar código, cuenta activa y evento visible;
if (¿Autorizado?) then (sí)
  :Responder ICS actualizado;
else (no)
  :Responder 404;
endif
stop'''),
}
for name, (title, body) in FLOWS.items():
    write(name, title, body)

write("patrones-strategy-facade", "Strategy y Facade · colaboraciones reales", '''left to right direction
class "Consola / Swing / API" as Vista
interface ServicioEventos <<Facade contract>>
class GestorEventosEnVivo <<Facade>>
class GestorRemoto <<Proxy>>
class ControlAcceso
class ControlSesion
interface Persistencia
class PoliticasVisibilidad <<Strategy context>>
interface PoliticaVisibilidadEvento <<Strategy>> {
  +esVisible(cuenta : Cuenta, evento : Evento) : boolean
}
class VisibilidadGeneral
class VisibilidadArtista
interface VinculoCalendario <<Strategy>> {
  +vincular(urlFeed : String) : void
}
class VinculoGoogle
class VinculoApple
Vista --> ServicioEventos
ServicioEventos <|.. GestorEventosEnVivo
ServicioEventos <|.. GestorRemoto
GestorEventosEnVivo *-- ControlAcceso
GestorEventosEnVivo *-- ControlSesion
GestorEventosEnVivo --> Persistencia
GestorEventosEnVivo *-- PoliticasVisibilidad
PoliticasVisibilidad --> "3 entradas / 2 algoritmos" PoliticaVisibilidadEvento
PoliticaVisibilidadEvento <|.. VisibilidadGeneral
PoliticaVisibilidadEvento <|.. VisibilidadArtista
GestorEventosEnVivo ..> VinculoCalendario : parámetro
GestorRemoto ..> VinculoCalendario : parámetro
VinculoCalendario <|.. VinculoGoogle
VinculoCalendario <|.. VinculoApple
Vista ..> VinculoGoogle : elige
Vista ..> VinculoApple : elige
note bottom of PoliticasVisibilidad
La política limita lectura; Permisos autoriza acciones.
Dos algoritmos concretos y selección por tres roles.
end note''')

manifest = {"declaredTypes": len(classes), "sourceFiles": data["sourceFiles"],
            "classifiers": sorted(classes), "diagrams": sorted(p.stem for p in SRC.glob("*.puml")),
            "actions": {k: sorted(v[1]) for k, v in ROLES.items()}}
(OUT / "cobertura.json").write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print(f"Generados {len(manifest['diagrams'])} diagramas; {len(classes)} clasificadores; {len(ACTIONS)} acciones trazadas.")
