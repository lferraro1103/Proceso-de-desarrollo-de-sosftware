"""Cuadernillo A4 vectorial; las vistas se dividen para conservar legibilidad."""
from pathlib import Path
import json,xml.etree.ElementTree as E
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4,landscape
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.graphics import renderPDF
from svglib.svglib import svg2rlg
from reportlab.lib.utils import simpleSplit
from pypdf import PdfReader,PdfWriter
from io import BytesIO

ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'output/pdf';OUT.mkdir(parents=True,exist_ok=True)
UML=ROOT/'docs/iteracion-4/uml-corregido'
for name,file in [('Arial','arial.ttf'),('Arial-Bold','arialbd.ttf'),('Arial-Italic','ariali.ttf'),('Arial-BoldItalic','arialbi.ttf')]:
    pdfmetrics.registerFont(TTFont(name,'C:/Windows/Fonts/'+file))
pdfmetrics.registerFontFamily('Arial',normal='Arial',bold='Arial-Bold',italic='Arial-Italic',boldItalic='Arial-BoldItalic')
views={v['id']:v for v in json.loads((UML/'modelo.json').read_text(encoding='utf-8'))['views']}
buffer=BytesIO();c=canvas.Canvas(buffer,pagesize=A4);page=0;qa=[]
def header(title,size=A4):
    global page
    page+=1;c.setPageSize(size);w,h=size
    c.setFillColorRGB(.10,.19,.29);c.setFont('Arial-Bold',16);c.drawString(32,h-36,title)
    c.setFont('Arial',9);c.setFillColorRGB(.32,.40,.48)
    c.drawString(32,h-53,'UADE Beats | Grupo 9 | Iteración 4 | 21/10/2026')
    c.setStrokeColorRGB(.75,.81,.86);c.line(32,h-64,w-32,h-64)
    c.setFont('Arial',8);c.drawString(32,20,'UML de implementación - Código integrado 361a7bb - revisión correctiva')
    c.drawRightString(w-32,20,str(page))
    return w,h
def paragraph(text,y,w,size=11,bold=False):
    c.setFont('Arial-Bold' if bold else 'Arial',size);c.setFillColorRGB(.10,.19,.29)
    lines=simpleSplit(text,'Arial-Bold' if bold else 'Arial',size,w)
    for line in lines:c.drawString(36,y,line);y-=size*1.45
    return y-9

w,h=header('Diagrama de clases: revisión y láminas A4');y=h-96
for text,bold in [
 ('Qué se corrigió',True),
 ('Se reemplazó el atlas de fichas por clases conectadas, con una sola aparición de cada clasificador por vista. Las relaciones se contrastaron con el código Java después de integrar los cambios de iteración 4.',False),
 ('Alcance completo',True),
 ('91 archivos Java y 97 tipos con nombre. El SVG integral contiene todos los tipos, campos y operaciones declarados; las láminas de detalle permiten consultar cada paquete. Este cuadernillo selecciona vistas pequeñas para leerlas en A4, sin comprimir el sistema entero en una página.',False),
 ('Cómo leer las relaciones',True),
 ('Herencia: línea continua y triángulo hueco hacia la clase general. Realización de interfaz: línea discontinua y triángulo hueco. Asociación navegable: línea continua con punta abierta hacia el objeto referenciado. Dependencia: línea discontinua con punta abierta hacia el tipo utilizado.',False),
 ('Composición y agregación',True),
 ('Rombo negro en el propietario: una parte pertenece como máximo a un compuesto a la vez. Se usa en controles y repositorios privados cuyo ciclo de vida se mantiene dentro del propietario. El rombo blanco corresponde a agregación compartida; no se agregó porque el proyecto no define una semántica adicional de todo-parte que lo justifique.',False),
 ('Multiplicidades y límites',True),
 ('0..* significa cero o muchos; 0..1, referencia opcional. No se dibuja 1 en parámetros que pueden aceptar null. En las asociaciones se indica el campo almacenado; los atributos que se muestran como relaciones pueden omitirse del compartimento de atributos. La lista de participantes de un evento no es composición: los artistas existen y se comparten fuera de él.',False),
 ('Ejemplos para defender en clase',True),
 ('Usuario y Artista heredan de Cuenta. Administrador es un rol de Usuario, no una clase. RecitalEnVivo hereda de Evento. PersistenciaSqlite realiza Persistencia y compone sus cuatro repositorios. VisibilidadGeneral y VisibilidadArtista realizan PoliticaVisibilidadEvento. Un ticket AccesoEvento creado dentro de un método produce una dependencia, no una composición almacenada.',False),
]: y=paragraph(text,y,w-72,10.5 if not bold else 12,bold)
assert y>50,y
c.showPage()

SLUGS=['a4-identidad','a4-eventos','a4-acceso','02-tipos-dominio','03-contratos','a4-strategy','a4-facade','a4-persistencia','07-calendario-transmision','10-dependencias']
for slug in SLUGS:
    v=views[slug];svg=UML/'diagramas'/(slug+'.svg');root=E.parse(svg).getroot()
    sw,sh=[float(root.attrib[k].replace('px','')) for k in ['width','height']]
    sizes=[A4,landscape(A4)]
    # Reservar espacio fijo para encabezado, pie y explicación. Elegir A4
    # vertical u horizontal según la legibilidad efectiva del mismo vector.
    best=max(sizes,key=lambda sz:min((sz[0]-64)/sw,(sz[1]-170)/sh))
    w,h=header(v['title'],best)
    drawing=svg2rlg(str(svg));scale=min((w-64)/drawing.width,(h-170)/drawing.height)
    x=(w-drawing.width*scale)/2;y=90+(h-170-drawing.height*scale)/2
    c.saveState();c.translate(x,y);c.scale(scale,scale);renderPDF.draw(drawing,c,0,0);c.restoreState()
    notes=' '.join(v.get('notes',[])).replace(r'\n',' ')
    if not notes:notes='Vista parcial del modelo; los tipos y miembros completos se encuentran en los SVG de implementación y en el índice de 97 clasificadores.'
    lines=simpleSplit(notes,'Arial',9,w-72)
    assert len(lines)<=4,(slug,'nota demasiado larga')
    c.setFont('Arial',9);c.setFillColorRGB(.16,.27,.36)
    for i,line in enumerate(lines):c.drawString(36,74-i*12,line)
    minimum=min(float(t.attrib['font-size']) for t in root.iter() if t.tag.endswith('text'))
    effective=minimum*min((w-64)/sw,(h-170)/sh)
    assert effective>=8,(slug,'texto demasiado pequeño',effective)
    qa.append(dict(view=slug,A4='horizontal' if w>h else 'vertical',minimumPrintedFont=round(effective,2)))
    c.showPage()

w,h=header('Fuentes, evidencia y límites de la revisión');y=h-102
for text,bold in [
 ('Fuentes primarias',True),
 ('OMG, Unified Modeling Language 2.5.1 (formal/17-12-05). Apartados 7.5 (multiplicidad), 7.7 (dependencias), 9.2.4 (notación de clasificadores y generalización), 9.5.3-4 (propiedades), 10.4 (interfaces), 11.5.3-4 (asociaciones). https://www.omg.org/spec/UML/2.5.1',False),
 ('PlantUML, documentación oficial de diagramas de clase y de Graphviz: https://plantuml.com/class-diagram y https://plantuml.com/graphviz-dot. Graphviz, splines: https://graphviz.org/docs/attrs/splines/. Java SE 21, JLS capítulos 8 y 9 para extends, implements, enums y records.',False),
 ('Evidencia del proyecto',True),
 ('Inventario AST contrastado con bytecode: 97 tipos / 91 fuentes. Matriz de 82 relaciones estructurales y 359 dependencias de código. Un índice enlaza cada tipo a su detalle. Las dependencias de llamada no se mezclan automáticamente con asociaciones persistentes.',False),
 ('La lámina principal tiene 7 clasificadores únicos y 10 relaciones, con rutas revisadas sin cruces, superposiciones entre nodos ni texto sobre conectores. Las 37 vistas se comprueban por cobertura, tipos de relación y superposición de textos/clases. Los rótulos que Graphviz juntaba en los dos diagramas integrales se separan conservando sus extremos y registrando el ajuste.',False),
 ('El merge conserva Strategy y el nuevo contrato de transmisión. Pasaron 110 comprobaciones locales, HTTP y SQLite. No se verificó reproducción real en YouTube ni se ejecutaron servicios externos en esas pruebas.',False),
 ('Cómo usar la entrega',True),
 ('PNG: consulta rápida. SVG: ampliación sin pérdida y diagrama integral de implementación. PlantUML: fuentes editables y regenerables. El índice INDICE.md y RELACIONES.md permiten revisar cada clasificador y el origen de sus enlaces.',False),
 ('No se declara que un detector geométrico pruebe corrección UML. La verificación combina lectura del código, especificación, controles automáticos y revisión visual. Los clasificadores anónimos y los miembros sintéticos del compilador se excluyen explícitamente del inventario.',False),
]: y=paragraph(text,y,w-72,10.5 if not bold else 12,bold)
assert y>55,y
c.showPage();c.save()
writer=PdfWriter();writer.clone_document_from_reader(PdfReader(BytesIO(buffer.getvalue())))
writer.add_metadata({'/Title':'UADE Beats - UML corregido - Láminas A4','/Author':'Grupo 9'})
for name in ['01-dominio.svg','implementacion-completa.svg']:
    writer.add_attachment(name,(UML/'diagramas'/name).read_bytes())
target=OUT/'UADE_Beats_Diagrama_Clases_Corregido_A4.pdf'
with target.open('wb') as f:writer.write(f)
check=PdfReader(target)
assert len(check.pages)==12
for p in check.pages:
    assert sorted(round(float(x),1) for x in [p.mediabox.width,p.mediabox.height])==sorted(round(x,1) for x in A4)
(UML/'pdf-auditoria.json').write_text(json.dumps(dict(pages=12,views=qa,attachments=2),ensure_ascii=False,indent=2),encoding='utf-8')
print(target,'| 12 páginas A4 | 2 SVG adjuntos | texto vectorial')
