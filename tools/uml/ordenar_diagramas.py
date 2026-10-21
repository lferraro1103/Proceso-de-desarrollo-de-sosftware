"""Vistas de presentación deterministas. Conserva el inventario y las fuentes UML."""
from pathlib import Path
from collections import defaultdict
import json
import re
import math
import sys
from trazado import Scene,wrap,INK,TEAL,LINE,BORDER,BG,PALE,SOFT,GOLD

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/iteracion-4'
DEST=OUT/'diagramas'
from modelo_diagramas import inventory, CLASSES, CODES, relationships, member_lines, GROUPS, ROLES, LABELS
REPORTS=[]

def emit(scene):
    report=scene.save(DEST);REPORTS.append(report)
    print(f"  {scene.name}: {scene.width} x {scene.height}; cero colisiones",flush=True)

BOXW=600;GAP=262;TARGETW=300;PANELW=BOXW+GAP+TARGETW+48
def card_height(c,rels):
    fields,ops=member_lines(c)
    members=70
    for lines in [fields,ops]:
        members+=35
        members+=sum(len(wrap(raw,BOXW-28,14,mono=True))*21+5 for raw in lines) if lines else 24
    return max(members+22,len(rels)*104+20,220)+76

def class_card(s,c,x,y,height,rels):
    name=c['name'];code=CODES[name]
    s.rect(x,y,PANELW,height,SOFT,SOFT)
    s.text(f'{code} · {name.replace("$", ".")}',x+16,y+12,PANELW-32,19,True)
    top=y+54;h=height-76;source='type:'+name
    s.rect(x+16,top,BOXW,h,BG,BORDER,node=source)
    kind=c['kind']+(' · final' if 'final' in c['modifiers'] else '')+(' · anidada' if '$' in name else '')
    s.text('«'+kind+'»',x+30,top+10,BOXW-28,13,color=LINE)
    s.text(name.split('.')[-1].replace('$','.'),x+30,top+33,BOXW-28,18,True)
    yy=top+70
    fields,ops=member_lines(c)
    for heading,lines in [('ATRIBUTOS',fields),('OPERACIONES Y CONSTRUCTORES',ops)]:
        s.line([(x+16,yy),(x+16+BOXW,yy)],audit=False,color=BORDER)
        yy=s.text(heading,x+30,yy+8,BOXW-28,13,True,color=TEAL)+8
        if not lines:yy=s.text('(sin miembros propios)',x+30,yy,BOXW-28,13,color=LINE)+5
        for raw in lines:
            yy=s.text(raw,x+30,yy,BOXW-28,14,mono=True)+5
    if yy>top+h-8:raise ValueError(('altura de clase',name,yy,top+h))
    for i,r in enumerate(rels):
        ry=top+14+i*104
        tx=x+16+BOXW+GAP
        target=f'context:{name}:{i}'
        s.rect(tx,ry,TARGETW,76,PALE,BORDER,node=target)
        ref=CODES.get(r['target'],'JDK')
        pkg=r['target'].rsplit('.',1)[0] if '.' in r['target'] else 'lanzador'
        s.text(f'«contexto» {ref} · {pkg}',tx+12,ry+7,TARGETW-24,13,color=LINE)
        s.text(r['target'].split('.')[-1].replace('$','.'),tx+12,ry+29,TARGETW-24,15,True)
        sx=x+16+BOXW;cy=ry+47
        s.arrow([(sx,cy),(tx,cy)],r['kind'],source,target)
        label=r['label']
        s.text(label,sx+24,ry+1,GAP-46,13,center=True,color=INK)
        if 'mult' in r:s.text(r['mult'],tx-65,cy+10,55,13,color=LINE)
    return yy

def class_atlas(name,title,selected,columns=2):
    if '--functional' in sys.argv:return
    names=sorted(selected)
    prepared=[(CLASSES[n],relationships(CLASSES[n])) for n in names]
    widths=columns*PANELW+(columns+1)*28
    s=Scene(name,title,widths,
        'Cada ficha contiene todos los miembros del tipo y sus relaciones salientes. Las cajas azules remiten al código de la ficha canónica.',
        ['Triángulo hueco: herencia; triángulo discontinuo: realización; flecha: asociación; rombo lleno: composición; «use»: dependencia.',
         'C001…C089 identifican tipos únicos. Repetir un tipo como contexto no declara otra clase. Todos los miembros se muestran una vez en su ficha.',
         '+ público · - privado · # protegido · ~ paquete · {static} estático · {readOnly} referencia final, no inmutabilidad profunda.',
         'Las multiplicidades describen referencias: 0..* colección; 0..1 referencia Java potencialmente nula; 1 colaborador interno obligatorio.'])
    tops=[s.body_top]*columns
    positions=[]
    for c,rels in prepared:
        col=min(range(columns),key=lambda j:tops[j])
        h=card_height(c,rels)
        x=28+col*(PANELW+28);y=tops[col]
        class_card(s,c,x,y,h,rels)
        positions.append({'name':c['name'],'code':CODES[c['name']],'x':x,'y':y,'height':h,
                          'relationships':rels,'fields':len(c['fields']),'operations':len(c['operations']),
                          'fieldSignatures':member_lines(c)[0],'operationSignatures':member_lines(c)[1]})
        tops[col]=y+h+28
    s.semantic={'kind':'class-atlas','classifiers':names,'cards':positions}
    s.finish(max(tops))
    emit(s)

class_atlas('clases-completo','Atlas integral de clases · 89 tipos completos',CLASSES,4)
for name,title,packages in [
 ('clases-dominio','Clases del dominio',{'modelo'}),
 ('clases-servicios','Clases de servicios, permisos y políticas',{'gestor'}),
 ('clases-persistencia','Clases de persistencia',{'persistencia'}),
 ('clases-red','Clases del servidor y cliente remoto',{'servidor','cliente'}),
 ('clases-vistas','Clases de arranque, consola y Swing',{'app','ui',''}),
 ('clases-api','Clases y records de la API',{'api'}),
 ('clases-integraciones','Calendarios, notificación y excepciones',{'calendario','notificacion','excepciones'}),
]:class_atlas(name,title,[n for n,c in CLASSES.items() if c['package'] in packages],2)

# Casos de uso por objetivos: modelo semántico compartido.

def actor(s,x,y,label,id):
    # El punto de asociación sale por la derecha, sin tocar el nombre.
    s.ellipse(x+17,y,24,24,BG,INK)
    s.line([(x+29,y+24),(x+29,y+64)],audit=False,color=INK)
    s.line([(x+1,y+40),(x+57,y+40)],audit=False,color=INK)
    s.line([(x+29,y+64),(x+9,y+90)],audit=False,color=INK)
    s.line([(x+29,y+64),(x+49,y+90)],audit=False,color=INK)
    s.text(label,x-40,y+101,140,17,True,center=True)

def case_panel(s,role,x,y,width):
    allowed=ROLES[role]
    entries=[('login','Iniciar sesión',[]),('logout','Cerrar sesión',[])]+[(k,label,acts) for k,label,acts in GROUPS if set(acts)<=allowed]
    h=90+len(entries)*95
    s.rect(x+230,y,width-455,h,SOFT,BORDER)
    s.text('UADE Beats · eventos en vivo',x+245,y+16,width-485,17,True,center=True)
    acy=y+h/2
    actor(s,x+70,acy-42,LABELS[role],role)
    case_x=x+280;case_w=width-555
    for i,(key,label,acts) in enumerate(entries):
        cy=y+67+i*95
        id=f'case:{role}:{key}'
        s.ellipse(case_x,cy,case_w,68,BG,TEAL,node=id)
        s.text(label,case_x+15,cy+18,case_w-30,18,center=True)
        s.line([(x+128,acy),(x+185,acy),(case_x,cy+34)],source=None,target=id)
        if key=='calendario':
            actor(s,x+width-150,cy-9,'Google / Apple\nCalendar','calendar')
            s.line([(case_x+case_w,cy+34),(x+width-150,cy+34)],source=id)
    return h,entries

for role in ROLES:
    s=Scene('casos-'+role,'Casos de uso · '+LABELS[role],1050,
            'Asociaciones de actor sin flecha. Objetivos agrupados; el detalle de las 20 acciones se conserva en TRAZABILIDAD.md.',
            ['Precondición de operaciones: sesión válida. Login no se repite como «include» en cada objetivo.',
             'Gestionar estado comprende iniciar, pausar, reanudar, finalizar y cancelar, según permisos y transición válida.',
             'Abrir Swing es una operación de interfaz: se registra en la matriz, no como objetivo del negocio.',
             'Artista: solo eventos donde participa. Usuario: ingreso a su nombre y registros propios. Administrador: no desactiva su cuenta.'])
    h,entries=case_panel(s,role,0,s.body_top,1050)
    s.semantic={'kind':'usecase','role':role,'cases':entries,'actions':sorted(ROLES[role])}
    emit(s.finish(s.body_top+h))

s=Scene('casos-general','Casos de uso · tres vistas ordenadas del mismo sistema',3210,
        'Cada panel repite el límite del mismo módulo desde un rol. No se afirma herencia entre los actores.',
        ['Los objetivos comunes aparecen en más de una vista; no representan funcionalidades distintas.',
         'La correspondencia de objetivos y acciones se conserva en TRAZABILIDAD.md; no se modela SQLite como actor externo.'])
bottom=0;entries_by_role={}
for i,role in enumerate(ROLES):
    h,entries=case_panel(s,role,i*1070,s.body_top,1050)
    bottom=max(bottom,s.body_top+h);entries_by_role[role]=entries
s.semantic={'kind':'usecase-overview','views':entries_by_role}
emit(s.finish(bottom))

# Actividades: eje vertical y salida lateral independiente por alternativa.
class Activity:
    def __init__(self,name,title,subtitle=''):
        self.s=Scene(name,title,1180,subtitle,
             ['Círculo negro: inicio. Diana: fin de la operación. Rombo: decisión, con guardas explícitas.',
              'Cada final termina esta operación; la interfaz puede volver al menú. No se representa concurrencia.'])
        self.y=self.s.body_top;self.prev=None;self.seq=0;self.actions=[];self.steps=[]
        self.s.ellipse(357,self.y,20,20,INK,INK)
        self.prev=(367,self.y+20,None);self.y+=62
    def connect(self,x,y,id,guard=None):
        if self.prev:
            px,py,pid=self.prev
            self.s.arrow([(px,py),(x,y)],source=pid,target=id)
            if guard:self.s.text(guard,x+12,py+8,90,14,color=TEAL)
    def action(self,label):
        id=f'a{self.seq}';self.seq+=1
        lines=wrap(label,390,18);h=max(70,30+len(lines)*27)
        self.s.rect(157,self.y,420,h,PALE,BORDER,8,node=id)
        self.s.text(label,172,self.y+16,390,18,center=True)
        self.connect(367,self.y,id)
        self.prev=(367,self.y+h,id);self.y+=h+66;self.actions.append(label)
        self.steps.append({'kind':'action','label':label})
        return self
    def decision(self,label,reject,guard='no',continue_guard='sí',call=False):
        id=f'd{self.seq}';self.seq+=1
        label_lines=wrap(label,210,16)
        h=max(140,len(label_lines)*48+20);x=177;w=380;cy=self.y+h/2
        self.s.polygon([(x+w/2,self.y),(x+w,cy),(x+w/2,self.y+h),(x,cy)],BG,TEAL)
        self.s.nodes.append({'id':id,'box':[x,self.y,x+w,self.y+h],'shape':'diamond'})
        self.s.text(label,x+85,self.y+h/2-len(label_lines)*24/2,210,16,center=True)
        self.connect(367,self.y,id)
        rightid=f'r{self.seq}'
        rh=max(84,26+len(wrap(reject,350,17))*25)
        ry=cy-rh/2
        self.s.rect(730,ry,380,rh,GOLD,BORDER,8,node=rightid)
        self.s.text(reject,745,ry+13,350,17,center=True)
        self.s.arrow([(x+w,cy),(730,cy)],source=id,target=rightid)
        self.s.text(guard,x+w+22,cy-30,155,14,color=TEAL,center=True)
        self.s.ellipse(1132,cy-9,18,18,BG,INK);self.s.ellipse(1136,cy-5,10,10,INK,INK)
        self.s.arrow([(1110,cy),(1132,cy)],source=rightid)
        self.prev=(367,self.y+h,id)
        self.s.text(continue_guard,379,self.y+h+12,130,14,color=TEAL)
        self.y=max(self.y+h,ry+rh)+68
        self.actions.extend([label,reject])
        self.steps.append({'kind':'decision','label':label,'alternative':reject,'guard':guard,'continue':continue_guard})
        return self
    def end(self):
        self.s.ellipse(358,self.y,18,18,BG,INK);self.s.ellipse(362,self.y+4,10,10,INK,INK)
        self.connect(367,self.y,None)
        self.s.semantic={'kind':'activity','actions':self.actions,'steps':self.steps}
        body=['@startuml','!include estilo.iuml','title '+self.s.title,'start']
        for step in self.steps:
            if step['kind']=='action':body.append(':'+step['label'].replace('\n','\\n')+';')
            else:body.extend(['if ('+step['label']+') then ('+step['guard']+')',
                              ':'+step['alternative']+';','stop','else ('+step['continue']+')','endif'])
        body.extend(['stop','@enduml'])
        (OUT/'fuentes'/f'{self.s.name}.puml').write_text('\n'.join(body)+'\n',encoding='utf-8')
        emit(self.s.finish(self.y+28))

a=Activity('flujo-autenticacion','Autenticación local y remota',
           'Se presenta la operación de autenticación; la apertura de la interfaz es un paso previo.')
a.action('Ingresar nombre de cuenta y contraseña')
a.action('Servicio local: autenticar\nRemoto: POST /api/sesion')
a.decision('¿Cuenta activa y contraseña válida?','Informar rechazo de autenticación; permitir reintento')
a.action('Local: iniciar ControlSesion\nRemoto: generar token Bearer y pedir /api/estado')
a.action('Construir el estado permitido por rol y mostrar las acciones disponibles').end()

a=Activity('flujo-ingreso','Solicitud de ingreso · validaciones ordenadas',
           'Las ramas laterales terminan por separado. No se confunde falta de permiso con rechazo de acceso.')
a.decision('¿Permiso y cuenta propia?','PermisoDenegadoException. No procesa ingreso ni crea registro de acceso')
a.action('Buscar usuario y evento')
a.decision('¿Ambas entidades existen?','Rechazar con placeholder; registrar en memoria y guardar. SQL omite referencias inexistentes')
a.action('Construir AccesoEvento')
a.decision('¿Autorización y plan válidos?','Registrar rechazo por autorización o plan; guardar y devolver resultado')
a.decision('¿EN_CURSO y dentro del horario?','Registrar rechazo por estado u horario; guardar y devolver resultado')
a.decision('¿Hay capacidad?','Registrar rechazo por capacidad; guardar y devolver resultado')
a.decision('¿Usuario no conectado?','Registrar rechazo por duplicado; guardar y devolver resultado')
a.action('Conectar usuario y crear RegistroAcceso exitoso')
a.decision('¿Guardado confirmado?','Rollback SQL e informar PersistenciaException. No restaura automáticamente la memoria',guard='no',continue_guard='sí')
a.action('Devolver RegistroAcceso exitoso').end()

a=Activity('flujo-calendario','Suscripción y lectura del calendario',
           'Se separan permiso, generación del enlace, apertura local y lectura posterior del feed.')
a.action('Seleccionar evento y estrategia Google o Apple')
a.decision('¿Permiso y evento visible?','Rechazar pedido; no invocar la estrategia')
a.decision('¿URL pública configurada?','Informar IOException: se necesita una URL pública HTTPS')
a.action('PublicadorHttp arma el enlace ICS con el código HMAC de la cuenta')
a.decision('¿Se puede abrir el proveedor?','Informar IOException y el enlace para suscripción manual')
a.action('Usuario confirma la suscripción en Google o Apple')
a.action('Calendario externo solicita /calendario/código/id.ics')
a.decision('¿Código, cuenta activa y evento visible?','Responder HTTP 404')
a.action('Responder ICS actualizado').end()

a=Activity('flujo-usuario','Operación del usuario',
           'Vista funcional del menú. Cada rama representa una operación completa, no ejecución paralela.')
a.action('Iniciar sesión como usuario y consultar catálogo')
a.decision('¿Eligió solicitar ingreso?','Ejecutar el flujo de ingreso a su nombre; devolver resultado',guard='sí',continue_guard='otra acción')
a.decision('¿Eligió mis registros?','Mostrar únicamente los registros de la cuenta',guard='sí',continue_guard='otra acción')
a.decision('¿Eligió calendario?','Ejecutar el flujo de suscripción Google / Apple',guard='sí',continue_guard='consulta / salir')
a.action('Consultar eventos o capacidad; o cerrar la sesión según la opción elegida').end()

a=Activity('flujo-artista','Operación del artista · alcance propio',
           'La política determina los eventos donde participa. Toda escritura exige además el permiso de la acción.')
a.action('Iniciar sesión como artista y obtener eventos visibles')
a.decision('¿Eligió crear evento?','Capturar DatosRecital; verificar artista actual; crear, asociar y guardar',guard='sí',continue_guard='otra acción')
a.decision('¿Eligió cambiar estado?','Exigir permiso y pertenencia; validar transición; cambiar y guardar, o informar rechazo',guard='sí',continue_guard='otra acción')
a.decision('¿Eligió calendario?','Suscribir un evento propio mediante la estrategia elegida',guard='sí',continue_guard='consulta / salir')
a.action('Consultar eventos propios, conectados y capacidad; o cerrar sesión').end()

a=Activity('flujo-administrador','Operación del administrador',
           'Resumen de casos de uso. Las operaciones delegan al gestor y preservan sus rechazos y errores de persistencia.')
a.action('Iniciar sesión como administrador y seleccionar una acción')
for label,out in [
 ('¿Registrar cuenta o evento?','Verificar permiso y nombre duplicado cuando corresponde; crear entidad y guardar'),
 ('¿Activar o desactivar usuario?','Impedir desactivar su propia cuenta; alternar activo; si desactiva, desconectar y registrar expulsiones; guardar'),
 ('¿Cambiar estado de evento?','Exigir permiso y validar transición; cambiar y guardar, o informar rechazo'),
 ('¿Expulsar usuario?','Exigir permiso; comprobar evento en curso y usuario conectado; expulsar, registrar y guardar, o devolver nulo'),
 ('¿Solicitar ingreso?','Ejecutar el flujo de ingreso para el usuario seleccionado'),
 ('¿Suscribir calendario?','Ejecutar el flujo de suscripción de un evento visible'),
]:a.decision(label,out,guard='sí',continue_guard='otra acción')
a.action('Consultar cuentas, eventos, conectados, capacidad o registros generales; o cerrar sesión').end()

# Un patrón por panel, sin mezclar las aristas de toda la aplicación.
s=Scene('patrones-strategy-facade','Facade y Strategy · colaboraciones separadas',1340,
        'Cuatro paneles independientes permiten seguir cada relación sin cruces entre patrones.',
        ['La Facade coordina subsistemas. El contrato común también lo implementa el Proxy remoto.',
         'Strategy de visibilidad selecciona por rol; Strategy de calendario se recibe como parámetro en el servicio.',
         'La política limita alcance; el permiso de la acción se comprueba por separado.'])
panels=[
 ('Facade','gestor.GestorEventosEnVivo',[('gestor.ServicioEventos','realization','implementa'),
  ('gestor.ControlSesion','composition','sesion'),('gestor.ControlAcceso','composition','controlAcceso'),
  ('gestor.Persistencia','association','persistencia inyectada')]),
 ('Selección de política por rol','gestor.PoliticasVisibilidad',[('gestor.PoliticaVisibilidadEvento','association','porRol: tres entradas / dos algoritmos')]),
 ('Strategy de visibilidad: realizaciones','gestor.PoliticaVisibilidadEvento',[
  ('gestor.VisibilidadGeneral','realization','implementa la política'),
  ('gestor.VisibilidadArtista','realization','implementa la política')]),
 ('Strategy de calendario','gestor.VinculoCalendario',[('calendario.VinculoGoogle','realization','realiza el contrato'),
  ('calendario.VinculoApple','realization','realiza el contrato')]),
]
y=s.body_top
for pi,(title,source,rels) in enumerate(panels):
    h=115+len(rels)*104;s.rect(24,y,1292,h,SOFT,SOFT)
    s.text(title,44,y+15,1252,20,True)
    sy=y+64;sh=h-90;sid=f'pattern{pi}'
    s.rect(44,sy,480,sh,BG,BORDER,node=sid)
    s.text(source,60,sy+28,448,19,True)
    for i,(target,kind,label) in enumerate(rels):
        ty=sy+9+i*104;tid=f'pattern{pi}:{i}'
        s.rect(896,ty,390,76,PALE,BORDER,node=tid)
        s.text(target,910,ty+20,362,17,True)
        if source in ('gestor.PoliticaVisibilidadEvento','gestor.VinculoCalendario'):
            # Flecha de realización va desde la implementación al contrato.
            s.arrow([(896,ty+45),(524,ty+45)],kind,tid,sid)
        else:s.arrow([(524,ty+45),(896,ty+45)],kind,sid,tid)
        s.text(label,546,ty+5,325,14,center=True)
    y+=h+30
s.semantic={'kind':'patterns','panels':panels}
emit(s.finish(y))

report={'version':2,'method':'deterministic-layout-real-font-metrics','classifiers':len(CLASSES),
        'diagrams':REPORTS,'actions':{k:sorted(v) for k,v in ROLES.items()},'codes':CODES}
(OUT/'auditoria-geometrica.json').write_text(json.dumps(report,indent=2,ensure_ascii=False),encoding='utf-8')
(OUT/'relaciones.json').write_text(json.dumps({n:relationships(c) for n,c in CLASSES.items()},indent=2,ensure_ascii=False),encoding='utf-8')
index=['# Índice del atlas de clases','',
       'Cada código identifica una ficha canónica con todos sus miembros y relaciones. Los enlaces apuntan al nodo del SVG integral. Si el visor no desplaza la imagen al nodo, buscar el nombre o código con Ctrl+F y ampliar.','',
       '| Código | Tipo | Paquete |','|---|---|---|']
for n in sorted(CLASSES):
    anchor=re.sub(r'[^A-Za-z0-9_-]','_','type:'+n)
    index.append(f'| [{CODES[n]}](diagramas/clases-completo.svg#{anchor}) | `{n.replace(chr(36),chr(46))}` | {CLASSES[n]["package"] or "lanzador"} |')
(OUT/'CLASES-INDICE.md').write_text('\n'.join(index)+'\n',encoding='utf-8')
# La tabla técnica mantiene cada acción; esta sección explica su objetivo gráfico.
trace=OUT/'TRAZABILIDAD.md'
base=trace.read_text(encoding='utf-8').split('\n## Objetivos agrupados')[0]
extra=['','## Objetivos agrupados en las vistas revisadas','','| Objetivo | Acciones |','|---|---|']
for _,label,actions in GROUPS:extra.append('| '+label+' | '+', '.join('`'+a+'`' for a in actions)+' |')
extra.extend(['','`ABRIR_INTERFAZ_GRAFICA` permanece como acción técnica en la matriz, no como objetivo del negocio.'])
trace.write_text(base+'\n'+'\n'.join(extra)+'\n',encoding='utf-8')
print(f'Terminado: {len(REPORTS)} diagramas, {len(CLASSES)} fichas canónicas, sin colisiones detectadas.',flush=True)
