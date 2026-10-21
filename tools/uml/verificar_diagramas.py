"""Verifica modelo, PNG/SVG y colisiones reales. No ejecuta Git."""
import json,re,struct
import xml.etree.ElementTree as ET
from pathlib import Path
from trazado import Scene
from modelo_diagramas import CLASSES,ROLES,GROUPS,member_lines,relationships,inventory
ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'docs/iteracion-4'
coverage=json.loads((OUT/'cobertura.json').read_text(encoding='utf-8'))
assert inventory.get('permissions'),'El inventario debe exportar permisos del código compilado'
assert inventory['declaredTypes']==len(CLASSES)==len(coverage['classifiers'])==89
assert set(coverage['classifiers'])==set(CLASSES)
assert inventory['sourceFiles']==len(list((ROOT/'TP/Grupo9_POO_EntregaFinal/Codigo/src').rglob('*.java')))
dimensions={};audits=[];layouts={}
def compact(s):return re.sub(r'\s+','',s)
for name in coverage['diagrams']:
    layout=json.loads((OUT/f'fuentes/{name}.layout.json').read_text(encoding='utf-8'))
    layouts[name]=layout;scene=Scene.__new__(Scene)
    for key,value in layout.items():setattr(scene,key,value)
    report=scene.audit();assert not report['errors'],(name,report['errors'])
    assert report['minimumFont']>=13;audits.append(report)
    svg=ET.parse(OUT/f'diagramas/{name}.svg')
    actual_texts=[t.text or '' for t in svg.getroot().iter('{http://www.w3.org/2000/svg}text')]
    assert actual_texts==[i[1] for i in layout['items'] if i[0]=='text'],name
    binary=(OUT/f'diagramas/{name}.png').read_bytes()
    assert binary[:8]==b'\x89PNG\r\n\x1a\n' and binary[12:16]==b'IHDR'
    w,h=struct.unpack('>II',binary[16:24])
    assert (w,h)==(layout['width'],layout['height']) and 100<w<=6000 and 100<h<=22000
    dimensions[name]={'width':w,'height':h,'bytes':len(binary)}
    if layout['semantic']['kind']=='class-atlas':
        for card in layout['semantic']['cards']:
            c=CLASSES[card['name']];expected_fields,expected_ops=member_lines(c)
            assert card['fieldSignatures']==expected_fields and card['operationSignatures']==expected_ops
            assert card['relationships']==relationships(c),(name,c['name'])
            node=next(n for n in layout['nodes'] if n['id']=='type:'+c['name'])['box']
            text=''.join(t['text'] for t in layout['texts'] if node[0]<t['box'][0] and t['box'][2]<node[2]
                         and node[1]<t['box'][1] and t['box'][3]<node[3])
            for signature in expected_fields+expected_ops:
                assert compact(signature) in compact(text),(name,c['name'],signature)
assert set(layouts['clases-completo']['semantic']['classifiers'])==set(CLASSES)
assert len(layouts['clases-completo']['semantic']['cards'])==89
for role,allowed in ROLES.items():
    sem=layouts['casos-'+role]['semantic'];assert set(sem['actions'])==allowed
    expected={k for k,_,actions in GROUPS if set(actions)<=allowed}
    assert {c[0] for c in sem['cases']}==expected|{'login','logout'}
    mapped=set().union(*(set(c[2]) for c in sem['cases']))
    assert mapped|{'ABRIR_INTERFAZ_GRAFICA'}==allowed,(role,allowed-mapped)
    assert set(coverage['actions'][role])==allowed
(OUT/'dimensiones.json').write_text(json.dumps(dimensions,indent=2)+'\n',encoding='utf-8')
(OUT/'auditoria-geometrica.json').write_text(json.dumps({'version':2,'method':'font-metrics-and-path-intersection',
    'declaredTypes':len(CLASSES),'diagrams':audits,'collisions':0,'permissionsSource':'compiled gestor.Permisos'},
    indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
print(f'OK: {len(CLASSES)} tipos con todos sus miembros y relaciones; permisos productivos de tres roles.')
print(f'OK: {len(audits)} PNG + {len(audits)} SVG; colisiones de cajas, texto y conectores: 0.')
for name,size in dimensions.items():print(f"  {name}: {size['width']} x {size['height']}")
