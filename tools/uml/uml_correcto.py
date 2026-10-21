"""UML de implementación: clasificadores únicos y relaciones contrastables.

La fuente es inventario AST/bytecode; las composiciones son decisiones explícitas
auditadas en el código, nunca inferidas de List, final, new o del nombre del tipo.
No ejecuta ni publica código de aplicación.
"""
from pathlib import Path
import json, re, textwrap

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'docs/iteracion-4/uml-corregido'
SRC = OUT / 'fuentes'
SRC.mkdir(parents=True, exist_ok=True)
DATA = json.loads((ROOT / 'docs/iteracion-4/inventario.json').read_text(encoding='utf-8'))
C = {c['name']: c for c in DATA['classes']}
ALIAS = {name: 'T'+str(i) for i, name in enumerate(C)}
NORMAL = {name.replace('$', '.'): name for name in C}
ENUMS = {n for n,c in C.items() if c['kind']=='enum'}
OWNED = {
 ('gestor.GestorEventosEnVivo','controlAcceso'),
 ('gestor.GestorEventosEnVivo','sesion'),
 ('gestor.GestorEventosEnVivo','visibilidad'),
 ('gestor.ControlSesion','permisos'),
 ('gestor.PoliticasVisibilidad','porRol'),
 ('persistencia.PersistenciaSqlite','usuarios'),
 ('persistencia.PersistenciaSqlite','artistas'),
 ('persistencia.PersistenciaSqlite','eventos'),
 ('persistencia.PersistenciaSqlite','registros'),
 ('cliente.GestorRemoto','http'),
 ('cliente.GestorRemoto','permisos'),
 ('servidor.ManejadorApi','sesiones'),
}
STYLE = '''@startuml
skinparam backgroundColor #FFFFFF
skinparam shadowing false
skinparam roundcorner 0
skinparam defaultFontName Arial
skinparam defaultFontSize 16
skinparam classFontSize 18
skinparam classAttributeFontSize 15
skinparam ArrowFontSize 14
skinparam ArrowColor #334155
skinparam ArrowThickness 1.2
skinparam classAttributeIconSize 0
skinparam classBackgroundColor #F8FAFC
skinparam classBorderColor #334155
skinparam classHeaderBackgroundColor #DBEAFE
skinparam classStereotypeFontColor #475569
skinparam packageStyle rectangle
skinparam packageBackgroundColor #FFFFFF
skinparam packageBorderColor #CBD5E1
skinparam nodesep 65
skinparam ranksep 85
hide circle
hide empty members
'''

def short(s):
    for pkg in sorted({c['package'] for c in C.values()} - {''}, key=len, reverse=True):
        s = s.replace(pkg+'.', '')
    return s.replace('$','.')

def op_name(s):
    return re.sub(r'^.*?[+#~\-]', '', s).split('(')[0]

def wrap(s, limit=74):
    # PlantUML conserva la operación en su compartimento con un salto visual.
    # No se modifica ni se omite ningún parámetro del inventario.
    return r'\n    '.join(textwrap.wrap(s, width=limit, break_long_words=False,
                                     break_on_hyphens=False))

def classifier(name, mode='brief', selected=None, fields=True):
    c=C[name]; kind=c['kind']; declaration='class'
    stereo=[]
    if kind=='interface': declaration='interface';stereo.append('interface')
    elif kind=='enum': declaration='enum';stereo.append('enumeration')
    elif 'abstract' in c['modifiers'].split(): declaration='abstract class'
    if kind=='record': stereo.append('record')
    if 'final' in c['modifiers'].split() and kind not in ['enum','record']: stereo.append('final')
    if mode=='external': stereo.append('referencia')
    label=short(name)
    lines=[f'{declaration} "{label}" as {ALIAS[name]} '+('<<'+', '.join(stereo)+'>> ' if stereo else '')+'{']
    if fields and mode not in ['external','names']:
        for f in c['fields']:
            if f['enumConstant']:
                lines.append('  '+f['name']); continue
            if mode=='brief' and any(r['source']==name and r.get('role')==f['name'] for r in REL):
                continue  # la propiedad ya está dibujada en el extremo de asociación
            modifiers=('{static} ' if f['static'] else '')
            suffix=' {readOnly}' if f['final'] else ''
            lines.append('  '+modifiers+f['visibility']+f['name']+' : '+short(f['type'])+suffix)
    ops=c['operations'] if mode=='full' else [s for s in c['operations'] if selected and op_name(s) in selected]
    if ops and fields: lines.append('  --')
    for op in ops:
        shown=short(op) if mode=='full' else short(op).split(' throws ')[0]
        lines.append('  '+wrap(shown, 66 if mode=='full' else 52))
    lines.append('}')
    return '\n'.join(lines)

def relationships():
    result=[]
    for name,c in C.items():
        parent=c.get('superclass')
        if parent and parent not in ['java.lang.Object','java.lang.Record','java.lang.Enum']:
            result.append(dict(source=name,target=parent,kind='generalization',evidence='extends '+parent))
        for t in c['interfaces']:
            result.append(dict(source=name,target=t,kind='generalization' if c['kind']=='interface' else 'realization',evidence='extends/implements '+t))
        for f in c['fields']:
            if f['enumConstant']: continue
            for target in sorted(set(f['references']) & C.keys()):
                if target in ENUMS: continue  # atributo tipado, no asociación entre objetos entidad
                if f['static']: continue  # alcance de clasificador: no enlace entre instancias
                comp=(name,f['name']) in OWNED
                many=any(x in f['type'] for x in ['List<','Set<','Map<','[]'])
                mult='3' if (name,f['name'])==('gestor.PoliticasVisibilidad','porRol') else '1' if comp else '0..*' if many else '0..1'
                result.append(dict(source=name,target=target,kind='composition' if comp else 'association',role=f['name'],
                    sourceMultiplicity='0..1' if comp else '0..*',targetMultiplicity=mult,
                    evidence=name+'.'+f['name']+' : '+f['type']))
    return result

REL=relationships()
EXTERNAL={r['target'] for r in REL if r['target'] not in C}
ALIAS.update({n:'J'+str(i) for i,n in enumerate(sorted(EXTERNAL))})

def relation(r, direction='', label=True, norank=False):
    a=ALIAS[r['source']]; b=ALIAS[r['target']]; kind=r['kind']
    if kind=='generalization': return f'{a} -{direction or "up"}-|> {b}'
    if kind=='realization': return f'{a} .{direction or "up"}.|> {b}'
    if kind=='dependency': return f'{a} .{direction}.> {b} : {r.get("role", "usa")}'
    arrow=('*-[norank]->' if kind=='composition' else '-[norank]->') if norank else ('*-->' if kind=='composition' else '-->')
    return f'{a} "{r["sourceMultiplicity"]}" {arrow} "{r["targetMultiplicity"]}" {b}'+(' : '+r['role'] if label else '')

VIEWS=[]
def view(slug,title,names,selected=None,extras=(),mode='brief',rel_filter=None,layout=(),fields=True,notes=(),extra_relations=()):
    selected=selected or {}; primary=list(names); allnames=list(dict.fromkeys(primary+list(extras)))
    lines=[STYLE, 'title '+title, 'caption UADE Beats | Iteración 4 | UML de implementación']
    if len(primary)>50:
        for pkg in sorted({c['package'] for c in C.values()}):
            lines.append('package "'+(pkg or 'Entrada')+'" {')
            for n in primary:
                if C[n]['package']==pkg: lines.append(classifier(n,mode,selected.get(n),fields))
            lines.append('}')
    else:
        for n in allnames:
            lines.append(classifier(n,mode if n in primary else 'external',selected.get(n),fields))
    jdk=sorted({r['target'] for r in REL if r['source'] in primary and r['target'] in EXTERNAL})
    for n in jdk:
        kind='interface' if n=='com.sun.net.httpserver.HttpHandler' else 'class'
        lines.append(f'{kind} "{n}" as {ALIAS[n]} <<'+('interface, JDK' if kind=='interface' else 'JDK')+'>>')
    allnames.extend(jdk)
    relations=[r for r in REL if r['source'] in allnames and r['target'] in allnames and (not rel_filter or rel_filter(r))]
    relations.extend(extra_relations)
    for r in relations: lines.append(relation(r,norank=slug=='01-dominio'))
    lines.extend(layout)
    # Las explicaciones se documentan fuera del grafo: una nota flotante
    # alteraba las filas y dejaba enormes zonas vacías en versiones anteriores.
    lines.extend(['legend bottom left','  + público   - privado   # protegido   ~ paquete',
        '  Triángulo hueco: herencia (continua) / realización (discontinua)',
        '  Flecha abierta: navegabilidad | Rombo negro: propiedad compuesta',
        '  Multiplicidad en el extremo = objetos alcanzables desde el otro extremo',
        'endlegend','@enduml'])
    (SRC/(slug+'.puml')).write_text('\n'.join(lines),encoding='utf-8')
    VIEWS.append(dict(id=slug,title=title,primary=primary,classifiers=allnames,relations=relations,mode=mode,notes=list(notes)))

def names(pkg): return [n for n,c in C.items() if c['package']==pkg]

# Vistas para leer y explicar: no muestran constructores/getters triviales.
view('01-dominio','Dominio: identidad, eventos y acceso',
     ['modelo.Cuenta','modelo.Usuario','modelo.Artista','modelo.Evento','modelo.RecitalEnVivo','modelo.AccesoEvento','modelo.RegistroAcceso'],
     selected={'modelo.Cuenta':['iniciarSesion','cerrarSesion'], 'modelo.Artista':['crearEvento'],
       'modelo.Usuario':['tieneAccesoPrioritario'], 'modelo.Evento':['agregarArtista','iniciarEvento','finalizarEvento'],
       'modelo.RecitalEnVivo':['permitirIngreso','expulsarUsuario'], 'modelo.AccesoEvento':['validarAutorizacion','validarPlan']},
     notes=['Administrador es un rol de Usuario (TipoUsuario.ADMINISTRADOR).\\nNo existe una clase Administrador ni herencia entre Usuario y Artista.',
            'artistas y eventosCreados son dos referencias almacenadas distintas.\\nagregarArtista sincroniza ambas; crearEvento por sí solo no lo garantiza.\\nLos objetos se comparten: no hay composición entre Evento y Artista.',
            'Las referencias de constructor sin validación de null se modelan 0..1.\\nLos enum se indican como tipos de atributos; su detalle está en la lámina 02.'],
     layout=[f'{ALIAS["modelo.Cuenta"]} -[hidden]right- {ALIAS["modelo.Evento"]}',
             f'{ALIAS["modelo.Artista"]} -[hidden]right- {ALIAS["modelo.Usuario"]}',
             f'{ALIAS["modelo.Usuario"]} -[hidden]right- {ALIAS["modelo.RecitalEnVivo"]}',
             f'{ALIAS["modelo.Usuario"]} -[hidden]down- {ALIAS["modelo.AccesoEvento"]}',
             f'{ALIAS["modelo.AccesoEvento"]} -[hidden]right- {ALIAS["modelo.RegistroAcceso"]}'])

view('02-tipos-dominio','Tipos y valores del dominio',
     ['modelo.TipoUsuario','modelo.PlanSuscripcion','modelo.EstadoEvento','modelo.DatosRecital','modelo.Contrasenas'],
     selected={'modelo.PlanSuscripcion':['cubre'],'modelo.EstadoEvento':['permiteIniciar','permitePausar','permiteReanudar','permiteFinalizar'],
               'modelo.Contrasenas':['hashear','verificar']},
     notes=['Los valores enum y el record son tipos de datos, no entidades propiedad del evento.\\nContrasenas es una utilidad: las invocaciones producen dependencias, no asociaciones.'])

view('03-contratos','Contratos del servicio y sus implementaciones',
     ['gestor.ServicioEventos','gestor.ServicioSesion','gestor.ServicioConsultas','gestor.ServicioComandos'],
     selected={'gestor.ServicioSesion':['iniciarSesion','cerrarSesion','getCuentaActual'],
               'gestor.ServicioConsultas':['listarEventosVisibles','obtenerTransmision'],
               'gestor.ServicioComandos':['solicitarIngreso','crearEvento','vincularCalendario']},
     extras=['gestor.GestorEventosEnVivo','cliente.GestorRemoto'],fields=False,
     notes=['ServicioEventos extiende tres interfaces (línea continua y triángulo hueco).\\nGestorEventosEnVivo y GestorRemoto realizan ServicioEventos (línea discontinua).'])

view('04-facade','Facade y propiedad de sus colaboradores',
     ['gestor.GestorEventosEnVivo','gestor.ControlSesion','gestor.ControlAcceso'],
     selected={'gestor.GestorEventosEnVivo':['solicitarIngreso','obtenerTransmision'],
               'gestor.ControlAcceso':['procesarIngreso'],'gestor.ControlSesion':['exigir']},
     extras=['gestor.Persistencia','gestor.PublicadorCalendario','gestor.ProveedorTransmision','gestor.PoliticasVisibilidad','gestor.Permisos','gestor.Notificador'],
     notes=['Rombo negro en el propietario: controles internos creados y retenidos sin exponerlos.\\nPersistencia, PublicadorCalendario, ProveedorTransmision y Notificador se inyectan: asociación.\\nLa liberación de memoria en Java no requiere destructor explícito.'])

view('05-strategy','Strategy: selección de visibilidad por rol',
     ['gestor.PoliticasVisibilidad','gestor.PoliticaVisibilidadEvento','gestor.VisibilidadGeneral','gestor.VisibilidadArtista'],
     selected={n:['esVisible'] for n in ['gestor.PoliticasVisibilidad','gestor.PoliticaVisibilidadEvento','gestor.VisibilidadGeneral','gestor.VisibilidadArtista']},
     extras=['gestor.GestorEventosEnVivo'],
     notes=['porRol contiene tres instancias privadas: dos VisibilidadGeneral y una VisibilidadArtista.\\nLa composición apunta al contrato de las partes; no es herencia del contexto.\\nPermisos autoriza acciones y Strategy restringe qué eventos son visibles.'])

view('06-persistencia','Persistencia y repositorios internos',
     ['persistencia.PersistenciaSqlite','persistencia.RepositorioUsuarios','persistencia.RepositorioArtistas','persistencia.RepositorioEventos','persistencia.RepositorioRegistros'],
     selected={n:['guardar','cargar','reemplazarTodos','cargarTodos'] for n in names('persistencia')},
     extras=['gestor.Persistencia'],
     notes=['Los cuatro repositorios se crean en campos private final y no se comparten ni exponen.\\nLas entidades cargadas se comparten con el gestor: no son partes compuestas del repositorio.'])

view('07-calendario-transmision','Integraciones: calendario y transmisión',
     ['gestor.VinculoCalendario','calendario.VinculoGoogle','calendario.VinculoApple','gestor.ProveedorTransmision','transmision.ClienteTransmision','gestor.ConsultaTransmision','gestor.Transmision'],
     selected={n:['vincular','buscar','cerrar','getNombre'] for n in C},
     notes=['VinculoCalendario es Strategy; ClienteTransmision realiza el contrato de proveedor.\\nConsultaTransmision y Transmision son valores que entran/salen de operaciones.\\nNo se dibujan rombos por recibir un record como parámetro.'])

view('08-red','Servidor HTTP y colaboradores',
     ['servidor.ManejadorApi','servidor.ManejadorCalendario','servidor.PublicadorHttp','servidor.Sesiones'],
     selected={n:['handle','urlFeed','crear','buscar','eliminar'] for n in C},
     extras=['gestor.GestorEventosEnVivo','gestor.ProveedorTransmision','gestor.PublicadorCalendario','servidor.CodigosCalendario','servidor.ConfiguracionServidor'],
     notes=['Sesiones pertenece al ManejadorApi. Los dos manejadores reciben el mismo gestor: asociación compartida.\\nHttpHandler es una interfaz del JDK; su realización se muestra en el detalle de servidor.'])

view('09-cliente-vistas','Proxy remoto e interfaces de usuario',
     ['cliente.GestorRemoto','cliente.ClienteHttp','cliente.EstadoLocal','ui.VentanaLogin','ui.VentanaPrincipal'],
     selected={n:['iniciarSesion','actualizar','getCuenta','get','post'] for n in C},
     extras=['gestor.ServicioEventos','gestor.Permisos','ui.ModeloTabla'],fields=False,
     notes=['Las vistas almacenan ServicioEventos y funcionan con ambas implementaciones.\\nEstadoLocal es reemplazable al actualizar y reconstruye entidades: se representa asociación.\\nSe suprimen atributos Swing y operaciones auxiliares sólo en esta vista de explicación.'])

view('10-dependencias','Dependencias: uso sin referencia almacenada',
     ['gestor.ControlAcceso','modelo.AccesoEvento','modelo.RecitalEnVivo','modelo.DatosRecital','modelo.Cuenta','modelo.Contrasenas'],fields=False,
     extra_relations=[dict(source=a,target=b,kind='dependency',role=role,evidence='Referencia AST resuelta y lectura de método') for a,b,role in [
       ('gestor.ControlAcceso','modelo.AccesoEvento','<<create>>'),
       ('modelo.RecitalEnVivo','modelo.DatosRecital','parámetro'),
       ('modelo.Cuenta','modelo.Contrasenas','llamada estática')]],
     notes=['La dependencia lleva línea discontinua y punta abierta hacia el proveedor.\\nCrear un ticket local no lo convierte en una parte compuesta almacenada en ControlAcceso.'])

view('a4-identidad','Identidad: dos especializaciones de Cuenta',
     ['modelo.Cuenta','modelo.Artista','modelo.Usuario'],
     selected={'modelo.Cuenta':['iniciarSesion'],'modelo.Artista':['crearEvento']},
     notes=['Usuario y Artista son clases hermanas. El administrador se representa como Usuario con rol ADMINISTRADOR.'])
view('a4-eventos','Eventos: herencia y participación de artistas',
     ['modelo.Artista','modelo.Evento','modelo.RecitalEnVivo'],
     selected={'modelo.Evento':['agregarArtista'],'modelo.RecitalEnVivo':['permitirIngreso']},
     notes=['Las dos colecciones conservan enlaces diferentes. No se infiere composición de una lista ni de que un artista cree un evento.'])
view('a4-acceso','Acceso y trazabilidad: referencias compartidas',
     ['modelo.AccesoEvento','modelo.RegistroAcceso'],extras=['modelo.Usuario','modelo.Evento'],
     selected={'modelo.AccesoEvento':['validarAutorizacion','validarPlan']},
     notes=['Los tickets y los registros refieren a usuarios y eventos existentes; no son propietarios de ellos.'])
view('a4-facade','Facade: propiedad interna e inyección',
     ['gestor.GestorEventosEnVivo','gestor.ControlAcceso','gestor.ControlSesion','gestor.PoliticasVisibilidad'],
     extras=['gestor.Persistencia','gestor.ProveedorTransmision','gestor.PublicadorCalendario','gestor.Notificador'],fields=False,
     notes=['Los rombos negros se colocan en el gestor, propietario de sus controles internos. Los colaboradores inyectados llevan asociación.'])
view('a4-persistencia','Repositorios: partes internas de PersistenciaSqlite',
     ['persistencia.PersistenciaSqlite','persistencia.RepositorioUsuarios','persistencia.RepositorioArtistas','persistencia.RepositorioEventos','persistencia.RepositorioRegistros'],
     extras=['gestor.Persistencia'],fields=False,
     notes=['Cada repositorio es un objeto privado, construido dentro de PersistenciaSqlite, sin exposición ni reemplazo. Las entidades de negocio se comparten con el gestor.'])
view('a4-strategy','Strategy: algoritmo intercambiable por rol',
     ['gestor.PoliticasVisibilidad','gestor.PoliticaVisibilidadEvento','gestor.VisibilidadGeneral','gestor.VisibilidadArtista'],
     selected={n:['esVisible'] for n in ['gestor.PoliticasVisibilidad','gestor.PoliticaVisibilidadEvento','gestor.VisibilidadGeneral','gestor.VisibilidadArtista']},
     notes=['El contexto posee tres objetos de política privados. Las clases concretas realizan la interfaz; no heredan del contexto.'])

# Diagramas de detalle: todos los campos y operaciones de TODOS los tipos.
# Contexto externo único por lámina; las dependencias temporales se documentan aparte.
for pkg in sorted({c['package'] for c in C.values()}):
    ns=names(pkg)
    # Dividir bloques grandes sin mezclar APIs ni repetir un clasificador en la misma lámina.
    groups=[ns[i:i+7] for i in range(0,len(ns),7)]
    for i,group in enumerate(groups,1):
        neighbors=sorted({r['target'] for r in REL if r['source'] in group and r['target'] in C}-set(group))
        view('detalle-'+(pkg or 'main')+'-'+str(i),'Detalle de implementación / '+(pkg or 'Main')+' / '+str(i),group,extras=neighbors,mode='full')

# Panorama general: todos los clasificadores UNA vez; atributos y operaciones
# se suprimen expresamente para leer la topología. El detalle está en las láminas.
view('00-sistema-completo','Sistema completo: 97 tipos propios y relaciones estructurales',list(C),mode='names',fields=False)

# Diagrama integral de implementación, sin reducción ni fichas repetidas.
view('implementacion-completa','Implementación completa: tipos, miembros declarados y relaciones',list(C),mode='full')

# Matriz completa de dependencias de código, separada de las asociaciones.
DEPS=[]
for n,c in C.items():
    for dep in sorted({NORMAL.get(t,t) for t in c['dependencies']}):
        if dep in C and dep!=n:
            DEPS.append(dict(source=n,target=dep,kind='dependency',evidence='Referencia AST resuelta; puede ser firma, llamada o creación local.'))

(OUT/'modelo.json').write_text(json.dumps(dict(sourceFiles=DATA['sourceFiles'],declaredTypes=DATA['declaredTypes'],
    structuralRelations=REL,codeDependencies=DEPS,views=VIEWS),ensure_ascii=False,indent=2),encoding='utf-8')

index=['# UML corregido: índice de clasificadores','',
       'Código integrado en `361a7bb`; 91 fuentes Java y 97 tipos con nombre. Cada tipo aparece una sola vez en el diagrama integral. Las vistas de detalle repiten el contexto entre láminas, nunca dentro de una lámina.','',
       '| Clasificador | Tipo | Detalle completo |','|---|---|---|']
for n,c in C.items():
    vs=[v for v in VIEWS if v['id'].startswith('detalle-') and n in v['primary']]
    index.append('| `'+n.replace('$','.')+'` | '+c['kind']+' | '+', '.join('['+v['id']+'](diagramas/'+v['id']+'.svg)' for v in vs)+' |')
(OUT/'INDICE.md').write_text('\n'.join(index)+'\n',encoding='utf-8')
print(f'{len(VIEWS)} vistas; {len(C)} clasificadores; {len(REL)} relaciones estructurales; {len(DEPS)} dependencias AST.')
