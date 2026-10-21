"""Presentación académica de iteración 4. ReportLab + pypdf; sin operaciones Git."""

from pathlib import Path

from io import BytesIO

import json

from html import escape

from reportlab.pdfgen import canvas

from reportlab.pdfbase import pdfmetrics

from reportlab.pdfbase.ttfonts import TTFont

from reportlab.platypus import Paragraph, Table, TableStyle

from reportlab.lib.styles import ParagraphStyle

from reportlab.lib.colors import HexColor, white

from pypdf import PdfReader, PdfWriter



ROOT = Path(__file__).resolve().parents[2]

OUT = ROOT / 'output/pdf'

OUT.mkdir(parents=True, exist_ok=True)

TMP = ROOT / 'tmp/pdfs'

TMP.mkdir(parents=True, exist_ok=True)

W, H = 960, 540

INK = HexColor('#17314A')

MUTED = HexColor('#52677D')

TEAL = HexColor('#067C83')

PALE = HexColor('#E8F4F3')

BLUE = HexColor('#EAF0F8')

AMBER = HexColor('#F6B85B')

CREAM = HexColor('#FFFAEF')

BG = HexColor('#F8FAFD')

LINE = HexColor('#D5DFEA')

for name, file in [('Arial', 'arial.ttf'), ('Arial-Bold', 'arialbd.ttf'), ('Arial-Italic', 'ariali.ttf')]:

    pdfmetrics.registerFont(TTFont(name, 'C:/Windows/Fonts/' + file))

pdfmetrics.registerFontFamily('Arial', normal='Arial', bold='Arial-Bold', italic='Arial-Italic', boldItalic='Arial-Bold')

pdfmetrics.registerFont(TTFont('Mono', 'C:/Windows/Fonts/consola.ttf'))

buffer = BytesIO()

c = canvas.Canvas(buffer, pagesize=(W, H))

c.setTitle('UADE Beats - Iteración 4 - Presentación de cambios')

c.setAuthor('Grupo 9')

c.setSubject('Explicación académica de patrones, UML, roles y validación')

pages = []

checks = []





def text(s, x, y, width, size=18, color=INK, bold=False, leading=None, limit=None):

    style = ParagraphStyle('body', fontName='Arial-Bold' if bold else 'Arial', fontSize=size,

                           leading=leading or size * 1.3, textColor=color, spaceAfter=0)

    p = Paragraph(s, style)

    _, height = p.wrap(width, H)

    if limit is not None:

        assert height <= limit + 0.5, (pages[-1] if pages else 'cover', s[:70], height, limit)

    assert x >= 20 and x + width <= W - 20 + 0.1, (x, width)

    assert y + height <= H - 14, (s[:50], y, height)

    p.drawOn(c, x, H - y - height)

    checks.append({'page': len(pages), 'text': s[:70], 'height': round(height, 1), 'top': y})

    return height





def line(x1, y1, x2, y2, color=LINE, width=1):

    c.setStrokeColor(color)

    c.setLineWidth(width)

    c.line(x1, H-y1, x2, H-y2)





def rect(x, y, w, h, fill=white, stroke=None, radius=10):

    c.setFillColor(fill)

    c.setStrokeColor(stroke or fill)

    c.roundRect(x, H-y-h, w, h, radius, stroke=bool(stroke), fill=1)





def arrow(x1, y1, x2, y2, color=TEAL, dashed=False):

    import math

    c.setDash(4, 3) if dashed else c.setDash()

    line(x1, y1, x2, y2, color, 1.7)

    c.setDash()

    ang = math.atan2(y2-y1, x2-x1)

    for d in [-0.5, 0.5]:

        line(x2, y2, x2-8*math.cos(ang+d), y2-8*math.sin(ang+d), color, 1.7)





def inheritance(x1, y1, x2, y2, dashed=False):

    import math

    angle = math.atan2(y2-y1, x2-x1)

    bx, by = x2-13*math.cos(angle), y2-13*math.sin(angle)

    if dashed:c.setDash(4,3)

    line(x1,y1,bx,by,TEAL,1.7)

    c.setDash()

    p=c.beginPath()

    p.moveTo(x2,H-y2)

    p.lineTo(bx+6*math.sin(angle),H-(by-6*math.cos(angle)))

    p.lineTo(bx-6*math.sin(angle),H-(by+6*math.cos(angle)))

    p.close()

    c.setStrokeColor(TEAL);c.setFillColor(white)

    c.drawPath(p,stroke=1,fill=1)





def begin(title, section, sub=None):

    if pages:

        c.showPage()

    pages.append(title)

    c.setPageSize((W,H))

    c.setFillColor(BG)

    c.rect(0,0,W,H,fill=1,stroke=0)

    rect(40, 30, 6, 36, TEAL, radius=2)

    text(section.upper(), 58, 29, 820, 10, TEAL, True)

    text(title, 58, 53, 850, 28, bold=True, limit=73)

    if sub:

        text(sub, 58, 96, 844, 14, MUTED, limit=42)

    line(40, 495, 920, 495)

    text('UADE Beats  |  Grupo 9  |  Iteración 4', 40, 508, 670, 9, MUTED)

    text(f'{len(pages):02d}', 884, 506, 36, 11, TEAL, True)

    c.bookmarkPage(f'page{len(pages)}')

    c.addOutlineEntry(title, f'page{len(pages)}', level=0)





def cue(s):

    rect(40, 447, 880, 35, PALE, radius=6)

    text('<b>Para exponer:</b> ' + s, 54, 456, 852, 11, TEAL, limit=22)





def card(x,y,w,h,title,body,fill=white,size=17):

    rect(x,y,w,h,fill,LINE)

    text(title,x+18,y+16,w-36,18,TEAL,True,limit=47)

    text(body,x+18,y+63,w-36,size,limit=h-76)





def node(x,y,w,h,title,subtitle='',fill=white,size=17):

    rect(x,y,w,h,fill,LINE)

    text(title,x+12,y+12,w-24,size,TEAL,True,limit=h-20)

    if subtitle:

        text(subtitle,x+12,y+43,w-24,13,MUTED,limit=h-53)





def table(headers, rows, widths, x=48, y=150, fs=15, rowheight=None):

    style = ParagraphStyle('cell',fontName='Arial',fontSize=fs,leading=fs*1.25,textColor=INK)

    head = ParagraphStyle('head',parent=style,fontName='Arial-Bold',textColor=white)

    vals=[[Paragraph(escape(str(s)),head) for s in headers]]

    vals += [[Paragraph(str(s),style) for s in row] for row in rows]

    t=Table(vals,colWidths=widths,rowHeights=rowheight,hAlign='LEFT')

    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),TEAL),('ROWBACKGROUNDS',(0,1),(-1,-1),[white,BLUE]),

        ('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),12),('RIGHTPADDING',(0,0),(-1,-1),12),

        ('TOPPADDING',(0,0),(-1,-1),10),('BOTTOMPADDING',(0,0),(-1,-1),10),('LINEBELOW',(0,0),(-1,0),0,TEAL)]))

    tw,th=t.wrap(sum(widths),H)

    assert y+th<440, (pages[-1],th)

    t.drawOn(c,x,H-y-th)





# 01

begin('Una iteración para mejorar el diseño', 'Presentación de cambios')

text('UADE BEATS',58,137,844,44,TEAL,True)

text('Iteración 4',60,198,840,27,bold=True)

text('Patrones de diseño, documentación UML y verificación del comportamiento.',60,246,790,23,limit=70)

for x,num,label in [(60,'89','tipos documentados'),(350,'19','diagramas PNG / SVG'),(640,'103','comprobaciones aprobadas')]:

    rect(x,338,260,84,white,LINE)

    text(num,x+16,347,225,28,TEAL,True)

    text(label,x+16,389,225,13,MUTED)

cue('El objetivo fue mejorar cómo está organizado y explicado el sistema.')



# 02

begin('¿De dónde partimos y qué cambió?', '01 / Contexto', 'La base funcional proviene de la iteración 4.')

table(['Ya existía','Se trabajó en iteración 4'],[

    ['Aplicación Java con consola y Swing.','Revisión de responsabilidades y patrones.'],

    ['SQLite y API HTTP para clientes remotos.','Strategy explícito para el alcance de eventos.'],

    ['Tres roles y controles de permisos.','Diagramas y casos de uso trazados a los permisos.'],

    ['Integración con calendarios Google y Apple.','Pruebas de regresión y documentación reproducible.'],

], [405,459],y=146,fs=17)

cue('La API, los DTO y el esquema SQLite conservaron sus contratos.')



# 03

begin('GSD organizó el trabajo en pasos verificables', '02 / Método', 'Primero se investigó; después se implementó y se contrastó el resultado.')

steps=[('1','Investigar','Código, UML y catálogo de patrones.'),('2','Planificar','Alcance, decisiones y criterios de aceptación.'),

       ('3','Ejecutar','Refactorización y generación de diagramas.'),('4','Verificar','Pruebas, cobertura e inspección visual.')]

for i,(n,title,body) in enumerate(steps):

    x=48+i*220

    rect(x,163,204,214,white,LINE)

    text(n,x+16,178,170,32,TEAL,True)

    text(title,x+16,235,170,20,bold=True)

    text(body,x+16,282,170,17,limit=80)

    if i<3: arrow(x+205,266,x+218,266)

text('Quedaron planes, investigación, resúmenes y evidencia de verificación.',58,398,844,16,MUTED)

cue('Cada decisión se vinculó con un problema real y una evidencia de validación.')



# 04

begin('La arquitectura mantiene un punto de entrada común', '03 / Arquitectura', 'Consola y Swing usan el mismo contrato, tanto en modo local como remoto.')

node(48,167,235,88,'Consola / Swing','Vistas de la aplicación',BLUE)

node(360,167,250,88,'ServicioEventos','Sesión + consultas + comandos',PALE)

node(683,146,229,91,'Gestor local','Coordina reglas y colaboradores')

node(683,268,229,91,'GestorRemoto','Proxy: pide la ejecución por HTTP')

arrow(283,211,360,211)

inheritance(683,191,610,198,True)

inheritance(683,307,610,229,True)

text('El servidor conserva las reglas y el filtrado por rol.',58,330,570,21,bold=True)

text('Las vistas no necesitan conocer las consultas SQL ni reconstruir las reglas del negocio.',58,374,575,17,limit=52)

cue('Cambiar el modo de conexión no obliga a duplicar las pantallas.')



# 05

begin('Strategy ya existía en los calendarios', '04 / Patrones existentes', 'La tarea común es suscribir un evento; cambia el algoritmo de apertura del proveedor.')

node(55,166,260,82,'VinculoCalendario','Contrato: vincular(urlFeed)',PALE)

node(415,153,220,86,'VinculoGoogle','Abre un enlace de Google')

node(415,280,220,86,'VinculoApple','Abre Calendar o webcal')

inheritance(415,191,315,200,True)

inheritance(415,322,315,221,True)

text('La vista elige',690,163,220,22,bold=True)

text('El gestor obtiene una URL autorizada e invoca la estrategia recibida.',690,207,220,18,limit=117)

text('En remoto, la apertura ocurre en el equipo cliente.',55,392,840,18,MUTED)

cue('Strategy también puede recibirse como parámetro; no necesita guardarse en un atributo.')



# 06

begin('Strategy nuevo: separar el alcance de consulta', '05 / Cambio implementado', 'Se extrajo el algoritmo de visibilidad que estaba dentro del gestor.')

card(48,155,416,210,'Antes: decisión dentro del gestor',

     'Un método comprobaba el rol y recorría los artistas del evento.<br/><br/>La regla era válida, pero estaba mezclada con la coordinación de casos de uso.',size=17)

card(488,155,424,210,'Ahora: delegación a políticas',

     'El gestor pide una decisión a un contrato.<br/><br/>Dos algoritmos concretos expresan el catálogo general y la participación del artista.',fill=PALE,size=17)

rect(48,383,864,45,INK)

c.setFillColor(white);c.setFont('Mono',17)

c.drawString(63,H-411,'return visibilidad.esVisible(actual, evento);')

cue('La regla observable se conserva; cambia el lugar donde se expresa y se prueba.')



# 07

begin('Cuatro tipos hacen explícita la colaboración', '05 / Cambio implementado', 'Dos algoritmos alcanzan para resolver los tres roles actuales.')

node(48,162,260,90,'PoliticasVisibilidad','Selecciona según TipoUsuario',BLUE)

node(365,162,290,90,'PoliticaVisibilidadEvento','Contrato: esVisible(cuenta, evento)',PALE,size=16)

node(690,142,222,100,'VisibilidadGeneral','Usuario y administrador',size=16)

node(690,284,222,100,'VisibilidadArtista','Eventos donde participa',size=16)

arrow(308,207,365,207)

inheritance(690,190,655,190,True)

inheritance(690,330,655,230,True)

text('Sin cuenta o sin evento: no hay visibilidad.',58,310,580,20,bold=True)

text('La participación se compara por ID persistente, incluso después de cargar los datos desde SQLite.',58,357,580,17,limit=61)

cue('Se reutiliza el mismo alcance en listados, calendarios y controles de pertenencia.')



# 08

begin('Un ejemplo permite comprobar la regla', '06 / Comportamiento', 'Supongamos que el artista 10 participa en el evento 100, pero no en el 101.')

table(['Cuenta / situación','Evento 100','Evento 101'],[

    ['Usuario autenticado','Visible','Visible'],['Administrador autenticado','Visible','Visible'],

    ['Artista 10','Visible','No visible'],['Otra instancia del artista 10, mismo ID','Visible','No visible'],

    ['Sin cuenta','No visible','No visible'],

], [480,192,192],y=144,fs=16)

cue('La identidad se reconoce por ID y el algoritmo se elige por rol.')



# 09

begin('Facade ya estaba en el gestor', '07 / Patrones existentes', 'GestorEventosEnVivo simplifica el uso de varios colaboradores internos.')

node(48,174,218,96,'Vista','Solicita un caso de uso',BLUE)

node(336,174,280,96,'GestorEventosEnVivo','Facade: coordina la operación',PALE,size=17)

for y,title in [(144,'ControlSesion'),(231,'ControlAcceso'),(318,'Persistencia')]:

    node(710,y,202,62,title,size=16)

    arrow(616,222,710,y+31)

arrow(266,222,336,222)

text('No se agregó otra fachada de reenvío.',58,339,590,21,bold=True)

text('ServicioEventos es el contrato; GestorRemoto es el Proxy para acceder al servidor.',58,381,590,17,limit=47)

cue('El patrón se identifica por la responsabilidad que cumple, no solo por su nombre.')



# 10

begin('Composite y Decorator: cuándo aportan valor', '08 / Decisiones de diseño', 'Evaluar un patrón también implica decidir cuándo conviene posponerlo.')

card(48,147,416,264,'Composite',

     '<b>Uso real:</b> paneles Swing que contienen otros componentes.<br/><br/>'

     '<b>En el dominio:</b> no existe un árbol de eventos.<br/><br/>'

     '<b>Aplicación futura:</b> festivales con escenarios y subeventos anidados.',size=17)

card(488,147,424,264,'Decorator',

     '<b>Situación actual:</b> no hay envoltorios propios que agreguen capas al mismo contrato.<br/><br/>'

     '<b>Candidato futuro:</b> métricas de persistencia o notificaciones, si se necesitan.<br/><br/>'

     '<b>Decisión:</b> no agregar capas sin un requisito real.',size=17)

cue('Una lista no basta para ser Composite y un Proxy no equivale a Decorator.')



# 11

begin('UML pasó de vistas parciales a cobertura verificable', '09 / Documentación', 'Se documentan clases, interfaces, enums, records y tipos anidados con nombre.')

for x,n,label in [(48,'84','fuentes Java'),(342,'89','tipos con nombre'),(636,'19','diagramas entregados')]:

    rect(x,151,276,91,PALE,LINE)

    text(n,x+16,160,244,31,TEAL,True)

    text(label,x+16,207,244,16)

text('Cada clase muestra atributos y operaciones con tipos y visibilidad.',58,269,840,20,bold=True)

text('Las relaciones distinguen herencia, realización, asociación, composición justificada y dependencia.',58,315,840,19,limit=59)

text('El inventario contrasta lo declarado en los fuentes con las clases compiladas. El SVG permite ampliar el modelo integral.',58,382,840,17,limit=48)

cue('La generación automática controla cobertura; la semántica UML requiere revisión humana.')



# 12

begin('Una colección no implica propiedad del ciclo de vida', '09 / Lectura del diagrama', 'Extracto didáctico del modelo: se muestran relaciones reales y se omiten miembros por claridad.')

node(60,163,229,104,'Cuenta','Clase abstracta',BLUE)

node(380,154,220,103,'Usuario','Especializa a Cuenta')

node(380,303,220,103,'Artista','Especializa a Cuenta')

node(705,216,207,110,'Evento','artistas: 0..*',PALE)

inheritance(380,200,289,200)

inheritance(380,345,289,231)

arrow(705,296,600,345)

text('herencia',303,160,90,11,MUTED)

text('herencia',290,294,90,11,MUTED)

text('asociación',615,363,96,11,MUTED)

text('El artista existe independientemente de un evento.',58,417,844,16,bold=True)

cue('El diagrama integral se incorpora como anexo SVG para inspeccionarlo con zoom.')



# 13

begin('Los casos de uso distinguen a los tres actores', '10 / Requisitos y permisos', 'Las 20 acciones se contrastaron con Permisos.java; las reglas de rol se conservaron.')

card(48,150,276,266,'Usuario',

     'Consulta eventos y capacidad.<br/><br/>Solicita ingreso a su nombre.<br/><br/>Ve sus registros y suscribe calendarios.',size=17)

card(342,150,276,266,'Artista',

     'Crea y gestiona eventos donde participa.<br/><br/>Consulta sus conectados.<br/><br/>Suscribe sus eventos a calendarios.',size=17)

card(636,150,276,266,'Administrador',

     'Administra cuentas y eventos.<br/><br/>Activa, desactiva y expulsa usuarios.<br/><br/>Consulta todos los registros.',size=17)

cue('El administrador no hereda todos los casos del usuario: no tiene la acción Mis registros.')



# 14

begin('El flujo de ingreso permite explicar las decisiones', '11 / Flujo principal', 'La autorización de la acción se comprueba antes de evaluar el acceso al recital.')

items=[('1','Permiso y cuenta propia'),('2','Usuario y evento existen'),('3','Autorización y plan'),

       ('4','Estado y horario'),('5','Capacidad y no duplicado'),('6','Registro y persistencia')]

for i,(n,label) in enumerate(items):

    row=i//3

    col=i%3 if row==0 else 2-i%3

    x=48+col*294;y=160+row*126

    rect(x,y,276,84,PALE if i==5 else white,LINE)

    text(n,x+14,y+10,40,23,TEAL,True)

    text(label,x+53,y+17,207,17,bold=True,limit=51)

    if row==0 and col<2: arrow(x+277,y+42,x+292,y+42)

    if row==1 and col>0: arrow(x-1,y+42,x-17,y+42)

arrow(774,244,774,285)

text('Si una validación de acceso falla, se devuelve el motivo y se registra el rechazo.',58,397,844,17,limit=44)

cue('Una falta de permiso se rechaza antes de procesar el ingreso; no es un acceso fallido.')



# 15

begin('Los flujos de gestión también respetan el alcance', '11 / Otros recorridos', 'La misma política se reutiliza después de verificar el permiso de cada operación.')

card(48,152,416,263,'Artista: gestionar un evento',

     '1. Selecciona un evento propio.<br/><br/>'

     '2. Se valida su participación y la transición de estado.<br/><br/>'

     '3. Se guarda y se refresca la vista.<br/><br/>'

     '<b>Evento ajeno:</b> la operación se deniega.',size=17)

card(488,152,424,263,'Administrador: desactivar un usuario',

     '1. Se comprueba el permiso.<br/><br/>'

     '2. Se impide desactivar la propia cuenta.<br/><br/>'

     '3. Si desactiva, desconecta al usuario y registra expulsiones.<br/><br/>'

     '4. Se guardan los cambios.',size=17)

cue('Ver un evento y estar autorizado a modificarlo son controles relacionados, pero distintos.')



# 16

begin('La evidencia de la iteración: 103 comprobaciones', '12 / Verificación', 'Resultados registrados en la ejecución previa de la iteración, no una nueva auditoría integral.')

table(['Área','Qué se comprobó'],[

    ['Políticas y permisos','Roles, ID persistente, nulos, eventos propios y ajenos.'],

    ['Servicio local y HTTP','Misma regla al listar, obtener calendario y cambiar estado.'],

    ['Persistencia SQLite','Guardar y cargar conserva asociaciones y visibilidad.'],

    ['Documentación UML','89 tipos con miembros; 20 acciones; 19 pares PNG/SVG.'],

], [235,629],y=152,fs=17)

cue('Las pruebas de regresión muestran que la refactorización conserva el comportamiento esperado.')



# 17

begin('Qué quedó fuera y qué conviene continuar', '13 / Límites y próximos pasos', 'Distinguir evidencia de funcionamiento y tareas pendientes ayuda a defender el trabajo.')

card(48,149,416,266,'Límites de las pruebas',

     'No se confirmó una suscripción real en Google o Apple.<br/><br/>'

     'No se automatizaron todos los clics de Swing.<br/><br/>'

     'La inspección UML no es una certificación formal.',size=18)

card(488,149,424,266,'Mejoras identificadas',

     'Definir vencimiento de sesiones.<br/><br/>'

     'Fortalecer validación de datos y URL pública.<br/><br/>'

     'Revisar consistencia entre memoria y base si falla un guardado.',size=18)

cue('Un rollback SQL no revierte automáticamente los objetos que ya cambiaron en memoria.')



# 18

begin('Aporte principal y preguntas para la defensa', '14 / Cierre', 'La iteración mejora separación, explicación y verificabilidad del diseño.')

text('Una regla explícita, una documentación trazable<br/>y evidencia para sostener las decisiones.',58,153,844,29,TEAL,True,limit=85)

table(['Pregunta','Respuesta breve'],[

    ['¿Por qué Strategy?','Separa dos algoritmos de alcance y permite probarlos sin UI.'],

    ['¿Por qué no aplicar todos los patrones?','Cada abstracción debe resolver una necesidad concreta.'],

    ['¿Cambió lo que puede hacer cada rol?','No. Se preservaron los permisos y se probaron sus resultados.'],

], [350,514],y=273,fs=16)

cue('La calidad se muestra en la justificación y en las pruebas, no en la cantidad de patrones.')



# 19

begin('Material de respaldo y referencias', 'Anexo / Para consultar', 'Los diagramas detallados se incluyen como archivos adjuntos dentro de este PDF.')

text('<b>Adjunto técnico:</b> diagrama completo de clases de esta entrega, en SVG ampliable.',58,145,844,18,limit=62)

text('En un lector compatible, abrir el panel de archivos adjuntos. Algunos visores web no muestran este panel.',58,216,844,15,MUTED,limit=44)

refs=[('UML 2.5.1 - OMG','https://www.omg.org/spec/UML/2.5.1'),

      ('Notación y generación - PlantUML','https://plantuml.com/class-diagram'),

      ('Strategy, Facade, Composite y Decorator','https://refactoring.guru/design-patterns'),

      ('Jerarquía de componentes - Java SE 21','https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Container.html')]

for i,(label,url) in enumerate(refs):

    y=279+i*32

    text(f'<link href="{url}" color="#067C83">{label}</link>',58,y,844,16)

text('Base documental: PATRONES.md, REVISION-UML.md, CASOS-DE-USO.md y VERIFICACION.md, en docs/iteracion-4/.',58,419,844,12,MUTED,limit=27)

cue('Las figuras didácticas resumen el modelo; los anexos conservan el nivel de detalle técnico.')



c.save()

reader=PdfReader(BytesIO(buffer.getvalue()))

writer=PdfWriter()

writer.clone_document_from_reader(reader)

writer.add_metadata({'/Title':'UADE Beats - Iteración 4 - Presentación para clase',

                     '/Author':'Grupo 9','/Subject':'Cambios explicados, patrones, UML y pruebas'})

writer.add_attachment('clases-completo.svg', (ROOT/'TP/Grupo9_POO_Diagramas_Pdf/imagenes/clases-completo.svg').read_bytes())

target=OUT/'UADE_Beats_Iteracion_4_Presentacion.pdf'

with target.open('wb') as f:

    writer.write(f)

reopened=PdfReader(target)

assert len(reopened.pages)==len(pages)==19

assert len(reopened.attachments)==1

content='\n'.join(p.extract_text() or '' for p in reopened.pages)

for term in ['Strategy','Facade','Composite','Decorator','103','89','SQLite','Usuario','Artista','Administrador']:

    assert term in content,term

assert '\ufffd' not in content

(TMP/'contenido-verificado.txt').write_text(content,encoding='utf-8')

(TMP/'maquetacion.json').write_text(json.dumps({'pages':pages,'blocks':checks},indent=2,ensure_ascii=False),encoding='utf-8')

print(f'PDF creado: {target}\nPáginas: {len(pages)}; anexos SVG: {len(reopened.attachments)}; tamaño: {target.stat().st_size} bytes')
