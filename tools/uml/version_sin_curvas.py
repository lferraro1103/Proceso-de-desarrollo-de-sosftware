"""Variante adicional del diagrama completo, con segmentos rectos.

Conserva byte por byte la fuente semántica y los archivos aprobados. No usa Git.
"""
from pathlib import Path
import subprocess,sys,hashlib,json,xml.etree.ElementTree as E,re,math
from collections import Counter
from trazado import font,intersect

ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'docs/iteracion-4/uml-corregido'
slug='implementacion-completa-sin-curvas'
original=OUT/'fuentes/implementacion-completa.puml'
preserved=[original,OUT/'diagramas/implementacion-completa.svg',OUT/'diagramas/implementacion-completa.png']
hashes={str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in preserved}
source=original.read_text(encoding='utf-8')
directive='skinparam linetype polyline\n'
variant=source.replace('@startuml\n','@startuml\n'+directive,1)
assert variant.replace(directive,'',1)==source
target=OUT/'fuentes'/(slug+'.puml');target.write_text(variant,encoding='utf-8')
subprocess.run(['java','-Xmx1g','-DPLANTUML_LIMIT_SIZE=32768','-jar',str(ROOT/'.tools/plantuml-1.2026.9.jar'),
    '-charset','UTF-8','-tsvg','-o','../diagramas',str(target)],check=True,cwd=ROOT)
subprocess.run([sys.executable,'-X','utf8',str(ROOT/'tools/uml/ajustar_rotulos_svg.py'),
    '--slug',slug,'--report','ajustes-sin-curvas.json'],check=True,cwd=ROOT)

NS='{http://www.w3.org/2000/svg}'
old=E.parse(OUT/'diagramas/implementacion-completa.svg').getroot()
new=E.parse(OUT/'diagramas'/(slug+'.svg')).getroot()
entities=lambda r:{g.attrib['data-qualified-name']:Counter(''.join(t.itertext()) for t in g.iter(NS+'text'))
    for g in r.iter(NS+'g') if g.attrib.get('class')=='entity'}
assert entities(old)==entities(new),'Cambió un clasificador o sus miembros'
links=lambda r:Counter((g.attrib.get('data-link-type'),tuple(''.join(t.itertext()) for t in g.findall(NS+'text')))
    for g in r.iter(NS+'g') if g.attrib.get('class')=='link')
assert links(old)==links(new),'Cambió una relación, rol o multiplicidad'

# Graphviz puede codificar una recta como Bezier cúbica. Verificar que sus
# puntos de control sean colineales con sus extremos, no sólo buscar la letra C.
nonstraight=[];paths=0
for g in new.iter(NS+'g'):
    if g.attrib.get('class')!='link':continue
    for path in g.findall(NS+'path'):
        paths+=1;tokens=re.findall(r'[A-Za-z]|[-+]?\d*\.?\d+(?:[eE][-+]?\d+)?',path.attrib.get('d',''))
        i=0;current=(0.,0.);command=''
        while i<len(tokens):
            if tokens[i].isalpha():command=tokens[i];i+=1
            if command in ['M','L']:
                current=(float(tokens[i]),float(tokens[i+1]));i+=2
                if command=='M':command='L'
            elif command=='C':
                a=(float(tokens[i]),float(tokens[i+1]));b=(float(tokens[i+2]),float(tokens[i+3]));end=(float(tokens[i+4]),float(tokens[i+5]));i+=6
                dx,dy=end[0]-current[0],end[1]-current[1];length=math.hypot(dx,dy)
                deviations=[abs(dx*(p[1]-current[1])-dy*(p[0]-current[0]))/length if length else math.dist(p,current) for p in [a,b]]
                if max(deviations)>.08:nonstraight.append(dict(link=g.attrib['id'],deviation=max(deviations)))
                current=end
            else:raise ValueError(('Comando SVG no esperado',command))
assert not nonstraight,nonstraight[:5]

texts=[];nodes=[];geometry=[]
for g in new.iter(NS+'g'):
    if g.attrib.get('class')=='entity':
        rects=g.findall(NS+'rect')
        if rects:
            r=max(rects,key=lambda e:float(e.attrib['width'])*float(e.attrib['height']))
            x,y,w,h=[float(r.attrib[k]) for k in ['x','y','width','height']];nodes.append((x,y,x+w,y+h))
for t in new.iter(NS+'text'):
    text=''.join(t.itertext());f=font(round(float(t.attrib.get('font-size',15))),t.attrib.get('font-weight')=='700')
    x=float(t.attrib.get('x',0));y=float(t.attrib.get('y',0));b=f.getbbox(text,anchor='ls')
    texts.append((text,(x,y+b[1],x+float(t.attrib.get('textLength',f.getlength(text))),y+b[3])))
for i,a in enumerate(nodes):
    for b in nodes[i+1:]:
        if intersect(a,b,1):geometry.append(['clases',a,b])
active=[]
for text,box in sorted(texts,key=lambda t:t[1][1]):
    active=[a for a in active if a[1][3]>box[1]+1]
    for other,ob in active:
        if intersect(box,ob,1.4):geometry.append(['texto',text,other])
    active.append((text,box))
assert not geometry,geometry[:10]
assert all(hashlib.sha256(p.read_bytes()).hexdigest()==hashes[str(p)] for p in preserved),'Se alteró la versión aprobada'
report=dict(ownTypes=97,entities=len(entities(new)),relations=sum(links(new).values()),
    linkPaths=paths,curvedSegments=nonstraight,overlaps=geometry,approvedVersionUnchanged=True,
    method='PlantUML linetype polyline; mismas declaraciones y relaciones; controles Bezier colineales; cajas/texto auditados.')
(OUT/'verificacion-sin-curvas.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
