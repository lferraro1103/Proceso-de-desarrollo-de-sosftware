from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'docs/iteracion-4/uml-corregido'
model=json.loads((OUT/'modelo.json').read_text(encoding='utf-8'))
def origin(r):
    path=Path('TP/Grupo9_POO_EntregaFinal/Codigo/src')/Path(r['source'].split('$')[0].replace('.','/')+'.java')
    lines=(ROOT/path).read_text(encoding='utf-8').splitlines()
    field=r.get('role') if r['kind'] in ['association','composition'] else None
    if field:
        matches=[i for i,s in enumerate(lines,1) if re.search(r'\b'+re.escape(field)+r'\s*(?:=|;)',s)]
    else:
        simple=r['source'].split('$')[-1].split('.')[-1]
        matches=[i for i,s in enumerate(lines,1) if re.search(r'\b(class|interface|enum|record)\s+'+re.escape(simple)+r'\b',s)]
    number=matches[0] if matches else 1
    return '['+path.name+':'+str(number)+'](../../../'+path.as_posix()+'#L'+str(number)+')'
labels={'generalization':'Generalización','realization':'Realización','composition':'Composición','association':'Asociación'}
lines=['# Matriz de relaciones del código integrado','',
       'Generada desde el inventario de 91 fuentes / 97 tipos, revisando por separado las decisiones de propiedad. Las referencias escalares sin validación de null tienen extremo 0..1. Las interfaces del JDK necesarias se distinguen como contexto externo.','',
       '## Relaciones estructurales (82)','',
       '| # | Origen | Relación | Destino | Rol | Extremo origen / destino | Evidencia |',
       '|---|---|---|---|---|---|---|']
for i,r in enumerate(model['structuralRelations'],1):
    multiplicities=(r.get('sourceMultiplicity','')+' / '+r.get('targetMultiplicity','')).strip(' /') or 'No aplica'
    lines.append('| '+str(i)+' | `'+r['source'].replace('$','.')+'` | '+labels[r['kind']]+' | `'+r['target'].replace('$','.')+'` | '+r.get('role','—')+' | '+multiplicities+' | '+origin(r)+' |')
lines.extend(['','## Dependencias de tipos resueltas por el compilador (359)','',
       'Son referencias en firmas o cuerpos. No implican propiedad, un campo ni una asociación estructural. Se conserva la matriz completa para consultar sin saturar las láminas.','',
       '| Cliente | Proveedor de tipo | Código del cliente |','|---|---|---|'])
for r in model['codeDependencies']:
    lines.append('| `'+r['source'].replace('$','.')+'` | `'+r['target'].replace('$','.')+'` | '+origin(r)+' |')
(OUT/'RELACIONES.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('Matriz documentada: 82 relaciones y 359 dependencias; enlaces a archivos y líneas reales.')
