"""Comprueba cobertura, semántica de relaciones y geometría del SVG exportado."""
from pathlib import Path
import json,re,xml.etree.ElementTree as ET
from collections import Counter
from trazado import intersect,hits_rect,font

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/iteracion-4/uml-corregido'
data=json.loads((ROOT/'docs/iteracion-4/inventario.json').read_text(encoding='utf-8'))
model=json.loads((OUT/'modelo.json').read_text(encoding='utf-8'))
C={c['name']:c for c in data['classes']}; alias={n:'T'+str(i) for i,n in enumerate(C)}
norm=lambda s: re.sub(r'\s+','',s.replace(r'\n',''))
def tag(e):return e.tag.split('}')[-1]
errors=[]; reports=[]
assert data['sourceFiles']==91 and data['declaredTypes']==97
primary=[n for v in model['views'] if v['id'].startswith('detalle-') for n in v['primary']]
assert Counter(primary)==Counter(C.keys()),'Cada tipo debe tener un detalle completo'
for r in model['structuralRelations']:
    c=C[r['source']]
    if r['kind']=='generalization':
        assert r['target']==c.get('superclass') or (c['kind']=='interface' and r['target'] in c['interfaces'])
    elif r['kind']=='realization':
        assert c['kind']!='interface' and r['target'] in c['interfaces']
    else:
        f=next(f for f in c['fields'] if f['name']==r['role'])
        assert r['target'] in f['references'] and not f['static']
        if r['kind']=='composition':
            assert r['sourceMultiplicity'] in ['1','0..1']
            source=ROOT/'TP/Grupo9_POO_EntregaFinal/Codigo/src'/Path(r['source'].split('$')[0].replace('.','/')+'.java')
            content=source.read_text(encoding='utf-8')
            assert re.search(r'\b'+re.escape(r['role'])+r'\s*=\s*(?:new\b|Map\.of)',content),(r,'sin construcción interna')

for v in model['views']:
    slug=v['id']; path=OUT/'diagramas'/(slug+'.svg'); root=ET.parse(path).getroot()
    source=(OUT/'fuentes'/(slug+'.puml')).read_text(encoding='utf-8')
    if slug=='01-dominio':
        principal=json.loads((OUT/'principal-auditoria.json').read_text(encoding='utf-8'))
        assert not principal['errors'] and principal['nodes']==7 and principal['edges']==10
        reports.append(dict(id=slug,kind='rutas manuales auditadas',nodes=7,errors=[]));continue
    entities=[g for g in root.iter() if g.attrib.get('class')=='entity']
    own=[g.attrib['data-qualified-name'].split('.')[-1] for g in entities if re.fullmatch(r'T\d+',g.attrib.get('data-qualified-name','').split('.')[-1])]
    expected=[alias[n] for n in v['classifiers'] if n in C]
    assert Counter(own)==Counter(expected),(slug,'clasificador duplicado, omitido o implícito')
    if v['mode']=='full':
        for n in v['primary']:
            for op in C[n]['operations']:
                shown=op
                for pkg in sorted({c['package'] for c in C.values()}-{''},key=len,reverse=True):shown=shown.replace(pkg+'.','')
                shown=shown.replace('$','.')
                assert norm(shown) in norm(source),(slug,n,op)
            for f in C[n]['fields']:
                assert f['name'] in source,(slug,n,f['name'])
    nodes=[]
    for g in entities:
        rects=[e for e in g if tag(e)=='rect']
        if not rects:continue
        r=max(rects,key=lambda e:float(e.attrib['width'])*float(e.attrib['height']))
        x,y,w,h=(float(r.attrib[k]) for k in ['x','y','width','height'])
        nodes.append(dict(id=g.attrib['id'],box=(x,y,x+w,y+h)))
    texts=[]
    for t in root.iter():
        if tag(t)!='text':continue
        content=''.join(t.itertext());x=float(t.attrib.get('x',0));y=float(t.attrib.get('y',0));size=float(t.attrib.get('font-size',15))
        f=font(round(size),t.attrib.get('font-weight')=='700')
        width=float(t.attrib.get('textLength',f.getlength(content)))
        # PlantUML utiliza ascendente/descendente de Arial. Se cuenta tinta,
        # no toda la caja de línea, para evitar falsos positivos de interlineado.
        bbox=f.getbbox(content,anchor='ls')
        texts.append(dict(text=content,box=(x,y+bbox[1],x+width,y+bbox[3])))
    geometry=[]
    for i,a in enumerate(nodes):
        for b in nodes[i+1:]:
            if intersect(a['box'],b['box'],1):geometry.append(['nodos',a['id'],b['id']])
    active=[]
    for t in sorted(texts,key=lambda x:x['box'][1]):
        active=[a for a in active if a['box'][3]>t['box'][1]+1]
        for a in active:
            if intersect(a['box'],t['box'],1.4):geometry.append(['textos',a['text'],t['text']])
        active.append(t)
    links=[g for g in root.iter() if g.attrib.get('class')=='link']
    for r in v['relations']:
        if r['kind']=='dependency':
            normal={x.replace('$','.') for x in C[r['source']]['dependencies']}
            assert r['target'].replace('$','.') in normal,(slug,'dependencia sin AST',r)
        # Las extensiones se invierten internamente en el SVG; se comprueba
        # el par y el estilo, además de la dirección explícita en PlantUML.
        aa=alias.get(r['source']);bb=alias.get(r['target'])
        if not aa or not bb:continue
        candidates=[g for g in links if any(set(re.findall(r'\b[TJ]\d+\b',e.attrib.get('id','')))=={aa,bb} for e in g)]
        assert candidates,(slug,'relación no renderizada',r)
        if r['kind'] in ['generalization','realization']:
            assert any(any(tag(e)=='polygon' and e.attrib.get('fill')=='none' for e in g) for g in candidates)
            dashed=any(any('stroke-dasharray' in e.attrib.get('style','') for e in g) for g in candidates)
            assert dashed==(r['kind']=='realization'),(slug,'trazo de herencia/realización incorrecto',r)
    errors.extend((slug,e) for e in geometry)
    reports.append(dict(id=slug,nodes=len(nodes),textLines=len(texts),errors=geometry))

result=dict(sourceFiles=91,classifiers=97,structuralRelations=Counter(r['kind'] for r in model['structuralRelations']),
    temporaryDependencies=len(model['codeDependencies']),views=len(reports),reports=reports,errors=errors,
    limitations='El detector de SVG comprueba cajas de clases y textos. La lámina principal también comprueba rutas, cruces y texto-línea. Las curvas de Graphviz y su legibilidad se revisan visualmente; ninguna prueba automática demuestra por sí sola corrección UML.')
(OUT/'VERIFICACION.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k!='reports'},ensure_ascii=False,indent=2))
if errors:raise SystemExit(1)
