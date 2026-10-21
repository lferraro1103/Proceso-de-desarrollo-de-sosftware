"""Modelo semántico compartido por PlantUML, render de presentación y verificación."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/iteracion-4'
inventory=json.loads((OUT/'inventario.json').read_text(encoding='utf-8'))
CLASSES={c['name']:c for c in inventory['classes']}
CODES={n:f'C{i+1:03d}' for i,n in enumerate(sorted(CLASSES))}
ALIASES={n.replace('$','.'):n for n in CLASSES}
OWNED={('gestor.GestorEventosEnVivo',f) for f in ['controlAcceso','sesion','visibilidad']}
OWNED|={('persistencia.PersistenciaSqlite',f) for f in ['usuarios','artistas','eventos','registros']}
OWNED|={('gestor.ControlSesion','permisos'),('cliente.GestorRemoto','http'),
        ('cliente.GestorRemoto','permisos'),('servidor.ManejadorApi','sesiones')}

def shorten(s):
    for pkg in ['modelo','gestor','api','app','ui','cliente','servidor','persistencia','calendario','notificacion','excepciones']:
        s=s.replace(pkg+'.','')
    return s

def relationships(c,dependencies=True):
    result=[];linked=set();source=c['name'];parent=c.get('superclass')
    if parent and parent not in ['java.lang.Object','java.lang.Record','java.lang.Enum']:
        result.append(dict(target=parent,kind='generalization',label='hereda de'));linked.add(parent)
    for t in c['interfaces']:
        result.append(dict(target=t,kind='generalization' if c['kind']=='interface' else 'realization',
                           label='extiende interfaz' if c['kind']=='interface' else 'implementa'));linked.add(t)
    for f in c['fields']:
        if f['enumConstant']:continue
        for ref in sorted(set(f['references']) & CLASSES.keys()):
            if ref==source:continue
            many=any(x in f['type'] for x in ['List<','Set<','Map<','[]']);owned=(source,f['name']) in OWNED
            result.append(dict(target=ref,kind='composition' if owned else 'association',
                               label=f['name']+(' {static}' if f['static'] else ''),
                               mult='0..*' if many else '1' if owned else '0..1'));linked.add(ref)
    if dependencies:
        for dep in sorted({ALIASES.get(t,t) for t in c['dependencies']}):
            if dep in CLASSES and dep!=source and dep not in linked:
                result.append(dict(target=dep,kind='dependency',label='«use»'))
    return result

def member_lines(c):
    fields=[]
    for f in c['fields']:
        s=f['name'] if f['enumConstant'] else (f['visibility']+f['name']+' : '+shorten(f['type'])
                 +(' {static}' if f['static'] else '')+(' {readOnly}' if f['final'] else ''))
        fields.append(s)
    return fields,[shorten(s) for s in c['operations']]

GROUPS=[
 ('consulta','Consultar eventos y capacidad',['VER_EVENTOS','VER_CAPACIDAD']),
 ('ingreso','Solicitar ingreso a un evento',['SOLICITAR_INGRESO']),
 ('misreg','Consultar mis registros',['VER_MIS_REGISTROS']),
 ('crear','Crear un evento',['CREAR_EVENTO']),
 ('estado','Gestionar estado del evento',['INICIAR_EVENTO','PAUSAR_EVENTO','REANUDAR_EVENTO','FINALIZAR_EVENTO','CANCELAR_EVENTO']),
 ('conectados','Consultar usuarios conectados',['VER_CONECTADOS']),
 ('usuarios','Registrar un usuario',['REGISTRAR_USUARIO']),
 ('artistas','Registrar un artista',['REGISTRAR_ARTISTA']),
 ('actividad','Activar o desactivar usuario',['ACTIVAR_DESACTIVAR_USUARIO']),
 ('cuentas','Consultar usuarios y artistas',['VER_USUARIOS','VER_ARTISTAS']),
 ('expulsar','Expulsar usuario de un evento',['EXPULSAR_USUARIO']),
 ('registros','Consultar todos los registros',['VER_REGISTROS']),
 ('calendario','Suscribir calendario de evento',['VINCULAR_CALENDARIO']),
]
_roles=inventory.get('permissions') or json.loads((OUT/'cobertura.json').read_text(encoding='utf-8'))['actions']
ROLES={k:set(_roles[k]) for k in ['usuario','artista','administrador']}
LABELS={'usuario':'Usuario','artista':'Artista','administrador':'Administrador'}
