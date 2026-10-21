"""Lámina principal UML con rutas manuales, clasificadores únicos y sin cruces.

Mismo modelo semántico que PlantUML; sólo cambia la disposición de la vista 01.
"""
from uml_correcto import OUT, C, REL, classifier, op_name, short
from trazado import Scene, PALE, BG, INK, LINE
import json

s=Scene('01-dominio','UADE Beats - Diagrama de clases del dominio',1820,
    'Código integrado: iteración 4 + iteración 4 (361a7bb). Clasificadores únicos; relaciones verificadas contra Java.')
positions={
 'modelo.Artista':(110,180,440,320), 'modelo.Cuenta':(1350,180,350,340),
 'modelo.Evento':(110,780,470,400), 'modelo.Usuario':(1350,780,350,320),
 'modelo.RecitalEnVivo':(740,550,450,290), 'modelo.AccesoEvento':(740,1010,450,380),
 'modelo.RegistroAcceso':(740,1470,450,240),
}
ops={
 'modelo.Cuenta':['iniciarSesion','cerrarSesion'], 'modelo.Artista':['crearEvento'],
 'modelo.Usuario':['tieneAccesoPrioritario'], 'modelo.Evento':['iniciarEvento','finalizarEvento'],
 'modelo.RecitalEnVivo':['permitirIngreso','expulsarUsuario'],
 'modelo.AccesoEvento':['validarAutorizacion','validarPlan'],
}
for n,(x,y,w,h) in positions.items():
    c=C[n]; abstract='abstract' in c['modifiers'].split()
    s.rect(x,y,w,h,node=n)
    s.rect(x,y,w,48,PALE)
    label=short(n)+(' {abstract}' if abstract else '')
    s.text(label,x+12,y+10,w-24,25,True,center=True)
    yy=y+60
    for f in c['fields']:
        if any(r['source']==n and r.get('role')==f['name'] for r in REL): continue
        yy=s.text(f['visibility']+f['name']+' : '+short(f['type']),x+14,yy,w-28,22)
    selected=[o for o in c['operations'] if op_name(o) in ops.get(n,[])]
    if selected:
        s.line([(x,yy+4),(x+w,yy+4)],audit=False)
        yy+=14
        for o in selected:
            # Mostrar tipo de parámetro sin nombre sólo en esta vista de lectura.
            import re
            shown=re.sub(r'\b\w+ : ', '', short(o)).split(' throws ')[0]
            yy=s.text(shown,x+14,yy,w-28,22)
    if yy>y+h-6: raise ValueError((n,'compartimento insuficiente',yy,y+h))

def edge(a,b,kind,pts,role=None,labels=()):
    candidates=[r for r in REL if r['source']=='modelo.'+a and r['target']=='modelo.'+b and r['kind']==kind and (not role or r.get('role')==role)]
    assert len(candidates)==1,(a,b,kind,role)
    r=candidates[0]
    s.arrow(pts,kind,source=r['source'],target=r['target'])
    for text,x,y,w in labels: s.text(text,x,y,w,21)

edge('Artista','Cuenta','generalization',[(330,180),(330,125),(1530,125),(1530,180)])
edge('Usuario','Cuenta','generalization',[(1530,780),(1530,520)])
edge('RecitalEnVivo','Evento','generalization',[(740,650),(650,650),(650,900),(580,900)])
edge('Evento','Artista','association',[(210,780),(210,500)],'artistas',
     [('artistas',118,625,90),('0..*',218,745,60),('0..*',218,510,60)])
edge('Artista','Evento','association',[(430,500),(430,780)],'eventosCreados',
     [('eventosCreados',441,655,210),('0..*',440,510,60),('0..*',440,745,60)])
edge('RecitalEnVivo','Usuario','association',[(1190,650),(1260,650),(1260,850),(1350,850)],'usuariosConectados',
     [('usuariosConectados',1273,700,250),('0..*',1195,615,60),('0..*',1290,860,60)])
edge('AccesoEvento','Evento','association',[(740,1100),(580,1100)],'evento',
     [('evento',620,1060,110),('0..*',689,1110,60),('0..1',585,1110,60)])
edge('AccesoEvento','Usuario','association',[(1190,1100),(1260,1100),(1260,980),(1350,980)],'usuario',
     [('usuario',1272,1050,90),('0..*',1198,1110,60),('0..1',1290,947,60)])
edge('RegistroAcceso','Evento','association',[(740,1580),(60,1580),(60,1000),(110,1000)],'evento',
     [('evento',75,1420,100),('0..*',683,1590,60),('0..1',64,963,60)])
edge('RegistroAcceso','Usuario','association',[(1190,1580),(1760,1580),(1760,980),(1700,980)],'usuario',
     [('usuario',1620,1540,100),('0..*',1200,1590,60),('0..1',1707,945,60)])
s.legend=[
 'Triángulo hueco + línea continua: herencia; la punta termina en la clase general. {abstract}: clase abstracta.',
 'Línea continua + punta abierta: asociación navegable. El nombre es el campo almacenado; el número pertenece al extremo próximo.',
 '0..*: cero o muchos objetos. 0..1: referencia opcional; los constructores no validan null. No se inventa una obligación de 1.',
 'Evento-Artista son dos referencias distintas; los objetos se comparten. No hay composición ni agregación justificada en el dominio.',
 'Administrador es Usuario con TipoUsuario.ADMINISTRADOR. No existe una clase Administrador.',
 'Se muestran atributos escalares y operaciones relevantes. Propiedades asociadas se expresan en las relaciones; miembros completos en el detalle.',
]
s.semantic={'classifiers':list(positions),'relations':[r for r in REL if r['source'] in positions and r['target'] in positions]}
s.finish(1730)
s.save(OUT/'diagramas')
(OUT/'principal-auditoria.json').write_text(json.dumps(s.audit(),ensure_ascii=False,indent=2),encoding='utf-8')
print('Principal conectado: 7 clasificadores únicos, 10 relaciones, auditoría sin superposiciones ni cruces.')
