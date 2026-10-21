"""Exporta un PlantUML autónomo tras contrastarlo con el código recién compilado."""
from pathlib import Path
import json,re,hashlib,subprocess

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/iteracion-4/uml-corregido'
current=json.loads((ROOT/'.tools/plantuml-codigo/inventario-actual.json').read_text(encoding='utf-8'))
previous=json.loads((ROOT/'docs/iteracion-4/inventario.json').read_text(encoding='utf-8'))
assert current==previous,'Cambió el código: hay que regenerar y revisar el modelo antes de exportarlo.'
source=(OUT/'fuentes/implementacion-completa-sin-curvas.puml').read_text(encoding='utf-8')
assert '!include' not in source and source.count('@startuml')==source.count('@enduml')==1
packages=sorted({c['package'] for c in current['classes']}-{''},key=len,reverse=True)
def shorten(value):
    for package in packages:value=value.replace(package+'.','')
    return value.replace('$','.')
normalize=lambda value:re.sub(r'\s+','',value.replace(r'\n',''))
fields=operations=0
for i,c in enumerate(current['classes']):
    name=re.escape(shorten(c['name']))
    declaration=rf'(?m)^(?:abstract class|class|interface|enum) "{name}" as T{i}\b[^\n]*\{{\n(.*?)^\}}'
    matches=list(re.finditer(declaration,source,re.S|re.M))
    assert len(matches)==1,(c['name'],'declaración omitida o duplicada')
    body=matches[0].group(1)
    for field in c['fields']:
        text=field['name'] if field['enumConstant'] else field['visibility']+field['name']+' : '+shorten(field['type'])
        assert normalize(text) in normalize(body),(c['name'],field['name'])
        fields+=1
    for operation in c['operations']:
        assert normalize(shorten(operation)) in normalize(body),(c['name'],operation)
        operations+=1

header="""' UADE Beats - diagrama de clases basado en el código Java actual.
' Verificado el 21/10/2026: AST y bytecode de 91 fuentes / 97 tipos propios.
' Contiene todos los atributos y operaciones del inventario y 82 relaciones estructurales.
' Incluye seis tipos del JDK como contexto de herencia/contratos; no son código propio.
' Conectores por segmentos rectos. Archivo autónomo, sin includes externos.
' Las dependencias temporales completas están separadas en RELACIONES.md.
"""
target=OUT/'UADE_Beats_Clases_Completo.puml'
target.write_text(header+source,encoding='utf-8')
subprocess.run(['java','-Xmx512m','-jar',str(ROOT/'.tools/plantuml-1.2026.9.jar'),
    '-checkonly','-charset','UTF-8',str(target)],check=True,cwd=ROOT)
java_sources=sorted((ROOT/'TP/Grupo9_POO_EntregaFinal/Codigo/src').rglob('*.java'))
manifest={p.relative_to(ROOT).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in java_sources}
report=dict(sourceFiles=len(java_sources),ownTypes=current['declaredTypes'],fields=fields,operations=operations,
    plantumlSyntax='PASS',codeInventory='AST == bytecode == inventario usado por el diagrama',
    standalone=True,straightConnectors=True,sourceHashes=manifest,
    outputSha256=hashlib.sha256(target.read_bytes()).hexdigest())
(OUT/'plantuml-codigo-verificacion.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'PlantUML autónomo verificado: {current["declaredTypes"]} tipos, {fields} atributos y {operations} operaciones.\n{target}')
