"""Separación mínima de multiplicidades que Graphviz deja demasiado juntas.

No cambia clases, conectores, roles ni cardinalidades. Desplaza rótulos hasta
54 unidades, conserva el mismo extremo y registra cada ajuste para revisión.
El PNG se vuelve a renderizar desde el SVG final, no se retoca como bitmap.
"""
from pathlib import Path
import xml.etree.ElementTree as E,json,math,argparse
from trazado import font,intersect
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPDF
import pypdfium2 as pdf

ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'docs/iteracion-4/uml-corregido'
E.register_namespace('','http://www.w3.org/2000/svg')
NS='{http://www.w3.org/2000/svg}'
MOVES=[]
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--slug',action='append',help='Procesar sólo esta variante; se puede repetir.')
parser.add_argument('--report',default='ajustes-rotulos.json')
parser.add_argument('--root',type=Path,default=OUT,help='Directorio de salida que contiene diagramas/.')
args=parser.parse_args()
OUT=args.root.resolve()
if ROOT not in OUT.parents:raise ValueError('La salida debe permanecer dentro del proyecto')
for slug in (args.slug or ['00-sistema-completo','implementacion-completa']):
    if not slug.replace('-','').replace('_','').isalnum():raise ValueError('Nombre de variante inválido')
    target=OUT/'diagramas'/(slug+'.svg');tree=E.parse(target);r=tree.getroot()
    texts=[t for t in r.iter(NS+'text')];nodes=[]
    for g in r.iter(NS+'g'):
        if g.attrib.get('class')!='entity':continue
        rects=list(g.findall(NS+'rect'))
        if not rects:continue
        a=max(rects,key=lambda a:float(a.attrib['width'])*float(a.attrib['height']))
        x,y,w,h=(float(a.attrib[k]) for k in ['x','y','width','height'])
        nodes.append((x,y,x+w,y+h))
    cache={}
    def box(t,dx=0,dy=0):
        if id(t) in cache:
            x,y,a,b=cache[id(t)]
            return x+dx,y+dy,a+dx,b+dy
        f=font(round(float(t.attrib.get('font-size',15))),t.attrib.get('font-weight')=='700')
        content=''.join(t.itertext());b=f.getbbox(content,anchor='ls')
        x=float(t.attrib.get('x',0))+dx;y=float(t.attrib.get('y',0))+dy
        result=(x,y+b[1],x+float(t.attrib.get('textLength',f.getlength(content))),y+b[3])
        if not dx and not dy:cache[id(t)]=result
        return result
    for t in texts:box(t)
    changes=[]
    for g in r.iter(NS+'g'):
        if g.attrib.get('class')!='link':continue
        for t in g.findall(NS+'text'):
            if t.text not in ['0..1','0..*','1','3']:continue
            tb=box(t)
            if not any(other is not t and intersect(tb,box(other),1.4) for other in texts):continue
            offsets=sorted([(dx,dy) for dx in range(-48,49,12) for dy in range(-48,49,12) if dx or dy],
                           key=lambda p: math.hypot(*p)+abs(p[0])*.2)
            for dx,dy in offsets:
                bb=box(t,dx,dy)
                if any(intersect(bb,box(other),1.4) for other in texts if other is not t):continue
                if any(intersect(bb,n,1) for n in nodes):continue
                before=(float(t.attrib['x']),float(t.attrib['y']))
                t.set('x',str(round(before[0]+dx,3)));t.set('y',str(round(before[1]+dy,3)))
                cache[id(t)]=bb
                changes.append(dict(link=g.attrib['id'],value=t.text,before=before,after=[before[0]+dx,before[1]+dy]))
                break
            else: raise ValueError((slug,'sin lugar para multiplicidad',t.text))
    tree.write(target,encoding='utf-8',xml_declaration=True)
    drawing=svg2rlg(str(target))
    pdfdata=renderPDF.drawToString(drawing)
    document=pdf.PdfDocument(pdfdata)
    bitmap=document[0].render(scale=4/3)
    bitmap.to_pil().save(target.with_suffix('.png'))
    MOVES.append(dict(id=slug,changes=changes))
(OUT/args.report).write_text(json.dumps(MOVES,ensure_ascii=False,indent=2),encoding='utf-8')
print('Rótulos separados y PNG renderizados desde los SVG definitivos:',sum(len(x['changes']) for x in MOVES))
