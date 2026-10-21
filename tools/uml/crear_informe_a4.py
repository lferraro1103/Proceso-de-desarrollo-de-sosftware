"""Informe A4 de iteración 4. Adaptación de la presentación; no usa Git."""

from pathlib import Path

from io import BytesIO

from html import escape

from reportlab.lib.pagesizes import A4

from reportlab.lib import colors

from reportlab.lib.styles import ParagraphStyle

from reportlab.pdfbase import pdfmetrics

from reportlab.pdfbase.ttfonts import TTFont

from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether

from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon

from pypdf import PdfReader, PdfWriter

from esquemas_pdf import drawing as audited_drawing



ROOT=Path(__file__).resolve().parents[2]

OUT=ROOT/'output/pdf'

OUT.mkdir(parents=True,exist_ok=True)

TMP=ROOT/'tmp/pdfs'

TMP.mkdir(parents=True,exist_ok=True)

W,H=A4

CW=W-96

NAVY=colors.HexColor('#18334D')

TEAL=colors.HexColor('#087D83')

PALE=colors.HexColor('#EAF5F4')

BLUE=colors.HexColor('#EEF3F8')

GRAY=colors.HexColor('#53677B')

LINE=colors.HexColor('#D4DFE8')

for name,file in [('Arial','arial.ttf'),('Arial-Bold','arialbd.ttf'),('Arial-Italic','ariali.ttf'),('Mono','consola.ttf')]:

    pdfmetrics.registerFont(TTFont(name,'C:/Windows/Fonts/'+file))

pdfmetrics.registerFontFamily('Arial',normal='Arial',bold='Arial-Bold',italic='Arial-Italic',boldItalic='Arial-Bold')

styles={

 'body':ParagraphStyle('body',fontName='Arial',fontSize=10.6,leading=15.2,textColor=NAVY,spaceAfter=9),

 'small':ParagraphStyle('small',fontName='Arial',fontSize=9,leading=12.6,textColor=GRAY,spaceAfter=7),

 'h1':ParagraphStyle('h1',fontName='Arial-Bold',fontSize=21,leading=26,textColor=NAVY,spaceAfter=15),

 'h2':ParagraphStyle('h2',fontName='Arial-Bold',fontSize=13,leading=18,textColor=TEAL,spaceBefore=8,spaceAfter=7),

 'cover':ParagraphStyle('cover',fontName='Arial-Bold',fontSize=34,leading=41,textColor=NAVY,spaceAfter=18),

 'subtitle':ParagraphStyle('subtitle',fontName='Arial',fontSize=18,leading=25,textColor=TEAL,spaceAfter=15),

 'cell':ParagraphStyle('cell',fontName='Arial',fontSize=9.4,leading=13,textColor=NAVY),

 'cellhead':ParagraphStyle('cellhead',fontName='Arial-Bold',fontSize=9.5,leading=13,textColor=colors.white),

 'code':ParagraphStyle('code',fontName='Mono',fontSize=9.2,leading=14,textColor=NAVY,spaceAfter=10),

}

story=[]

def p(s,style='body'): return Paragraph(s,styles[style])

def add(s,style='body'): story.append(p(s,style))

def heading(s): add(s,'h2')

def page(title):

    if story: story.append(PageBreak())

    add(title,'h1')

def box(s):

    t=Table([[p(s)]],colWidths=[CW])

    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,-1),PALE),('BOX',(0,0),(-1,-1),.6,LINE),

        ('LEFTPADDING',(0,0),(-1,-1),12),('RIGHTPADDING',(0,0),(-1,-1),12),

        ('TOPPADDING',(0,0),(-1,-1),10),('BOTTOMPADDING',(0,0),(-1,-1),3)]))

    story.extend([t,Spacer(1,10)])

def table(headers,rows,widths):

    vals=[[p(escape(s),'cellhead') for s in headers]]+[[p(s,'cell') for s in r] for r in rows]

    t=Table(vals,colWidths=widths,repeatRows=1,hAlign='LEFT')

    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),TEAL),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,BLUE]),

        ('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),9),('RIGHTPADDING',(0,0),(-1,-1),9),

        ('TOPPADDING',(0,0),(-1,-1),8),('BOTTOMPADDING',(0,0),(-1,-1),8),('LINEBELOW',(0,-1),(-1,-1),.5,LINE)]))

    story.extend([t,Spacer(1,12)])

def diagram(kind):

    story.extend([audited_drawing(kind,CW),p('Esquema didáctico: las realizaciones apuntan al contrato. Geometría revisada.','small')])



def footer(c,doc):

    c.saveState()

    c.setStrokeColor(LINE);c.line(48,44,W-48,44)

    c.setFont('Arial',8);c.setFillColor(GRAY)

    c.drawString(48,31,'UADE Beats | Grupo 9 | Iteración 4')

    c.drawRightString(W-48,31,str(doc.page))

    if doc.page>1:

        c.setFillColor(TEAL);c.setFont('Arial-Bold',8)

        c.drawString(48,H-34,'INFORME EXPLICATIVO DE LOS CAMBIOS')

    c.restoreState()



# 1

story.append(Spacer(1,45))

add('UADE BEATS','cover')

add('Iteración 4','subtitle')

add('Revisión de patrones de diseño,<br/>documentación UML y verificación','h1')

add('Informe explicativo para presentar en clase','body')

story.append(Spacer(1,15))

box('<b>Idea principal.</b> Esta iteración mejora la organización del código y la forma de explicar el sistema. Se separa una regla de visibilidad mediante Strategy, se revisan otros patrones y se completa la documentación con evidencia verificable.')

table(['89 tipos','19 diagramas','103 comprobaciones'],[

    ['Documentados a partir de 84 archivos Java.','Clases, casos de uso y actividades, en PNG y SVG.','Resultados registrados en la validación de la iteración.']

],[CW/3]*3)

heading('Resumen de la entrega')

add('El proyecto conserva la funcionalidad de la iteración 4: cuentas con tres roles, eventos en vivo, interfaces de consola y Swing, persistencia SQLite y acceso remoto mediante una API HTTP. El cambio de diseño se concentra en hacer explícita la política que determina qué eventos corresponden a cada cuenta.')

add('El informe diferencia los patrones que ya existían, la mejora implementada y las alternativas que se evaluaron pero no se incorporaron. También presenta los diagramas, las pruebas y los límites del trabajo.')

add('<b>Grupo:</b> 9<br/><b>Versión:</b> iteración 4 beta<br/><b>Formato:</b> A4 vertical<br/><b>Base documental:</b> informes y código del proyecto.','small')



# 2

page('1. Punto de partida y método de trabajo')

heading('¿Qué existía antes?')

add('UADE Beats administra el acceso de usuarios a eventos en vivo. La aplicación permite registrar cuentas y artistas, crear eventos, controlar ingresos y conservar la trazabilidad de los intentos. Los permisos distinguen a usuario, artista y administrador.')

table(['Base de iteración 4','Trabajo de iteración 4'],[

 ['Java, consola y Swing.','Revisión de responsabilidades y colaboración entre objetos.'],

 ['SQLite, API HTTP y cliente remoto.','Pruebas sobre los servicios compartidos y documentación reproducible.'],

 ['Reglas y permisos por rol.','Extracción Strategy para el alcance de eventos.'],

 ['Suscripción Google / Apple.','Identificación y explicación del Strategy ya existente.'],

],[CW*.45,CW*.55])

heading('Cómo se organizó con GSD')

add('El trabajo se desarrolló en cuatro pasos: <b>investigar</b> el código y las referencias; <b>planificar</b> el alcance y sus criterios de aceptación; <b>ejecutar</b> la refactorización y la documentación; y <b>verificar</b> el resultado con pruebas, cobertura de tipos e inspección visual.')

add('Los planes y la evidencia permiten relacionar cada decisión con un problema concreto. Para esta adaptación a PDF se mantuvo ese criterio y se respetó la indicación de trabajar sin Git.')

heading('Arquitectura conservada')

diagram('architecture')

add('Las vistas dependen de <b>ServicioEventos</b>. El gestor local coordina los casos de uso; el gestor remoto actúa como Proxy y solicita la ejecución al servidor. Esto evita duplicar las reglas de negocio en cada interfaz.')



# 3

page('2. Strategy: lo existente y lo implementado')

heading('Strategy que ya existía: calendarios')

add('El contrato <b>VinculoCalendario</b> define la operación de suscribir un evento. <b>VinculoGoogle</b> y <b>VinculoApple</b> resuelven esa tarea de forma distinta: una abre un enlace de Google; la otra abre Calendar o utiliza el protocolo webcal. La vista elige el proveedor y el servicio invoca el objeto recibido.')

add('Esta colaboración ya era Strategy: un comportamiento común admite algoritmos intercambiables. La estrategia puede recibirse como parámetro, sin que deba permanecer guardada dentro del contexto. En modo remoto, la apertura ocurre en el equipo cliente.')

heading('Strategy nuevo: visibilidad de eventos')

add('Antes, un método de <b>GestorEventosEnVivo</b> comprobaba el rol y recorría los artistas asociados al evento. La regla era válida, pero estaba dentro de una clase que también coordina cuentas, accesos y persistencia. Se extrajo para expresar y probar esa variación por separado.')

diagram('strategy')

table(['Participante','Responsabilidad'],[

 ['PoliticaVisibilidadEvento','Contrato que responde si una cuenta puede ver un evento.'],

 ['VisibilidadGeneral','Da alcance al catálogo a usuario y administrador autenticados.'],

 ['VisibilidadArtista','Comprueba participación por el ID del artista.'],

 ['PoliticasVisibilidad','Selecciona la política correspondiente al rol.'],

],[CW*.38,CW*.62])

add('<b>Delegación en el gestor:</b>','small')

add('return visibilidad.esVisible(actual, evento);','code')

box('El beneficio es separar responsabilidades y facilitar pruebas. No se afirma una mejora de rendimiento ni un cambio en los permisos.')



# 4

page('3. Ejemplo y decisiones sobre otros patrones')

heading('Ejemplo del comportamiento que se conserva')

add('Si el artista 10 participa en el evento 100, pero no en el 101, la política debe producir estos resultados:')

table(['Cuenta','Evento 100','Evento 101'],[

 ['Usuario autenticado','Visible','Visible'],['Administrador autenticado','Visible','Visible'],

 ['Artista 10','Visible','No visible'],['Otra instancia del artista 10, mismo ID','Visible','No visible'],

 ['Sin cuenta','No visible','No visible'],

],[CW*.52,CW*.24,CW*.24])

add('Comparar por ID permite reconocer al artista aunque los objetos se hayan reconstruido desde SQLite. La selección del algoritmo se hace por rol. La política determina alcance; <b>Permisos</b> y <b>ControlSesion</b> siguen autorizando las acciones. Ver un evento no implica poder modificarlo.')

heading('Facade: se identificó un diseño ya existente')

add('<b>GestorEventosEnVivo</b> funciona como Facade: ofrece casos de uso y coordina ControlSesion, ControlAcceso, las entidades, la persistencia y los calendarios. ServicioEventos es su contrato de uso. No se agregó una segunda fachada porque solo duplicaría reenvíos sin simplificar el acceso al subsistema.')

heading('Composite: uso real y aplicación futura')

add('La interfaz Swing aprovecha un árbol de componentes: los paneles pueden contener botones, tablas y otros paneles. Esa estructura utiliza el diseño Composite de la biblioteca. En el dominio no hay una jerarquía recursiva de eventos; una lista de eventos no basta para justificar el patrón. Sería razonable si se pidieran festivales con escenarios y subeventos anidados.')

heading('Decorator: evaluado y pospuesto')

add('No se encontró una familia propia de envoltorios que agregue responsabilidades manteniendo el mismo contrato. Podría servir para incorporar métricas de persistencia o de notificaciones cuando exista esa necesidad. Agregar capas sin un requisito real aumentaría la complejidad. GestorRemoto es Proxy por su intención de acceso remoto; no se lo reclasificó como Decorator.')



# 5

page('4. Documentación UML y casos de uso')

heading('Qué se completó en los diagramas')

add('El relevamiento cubre <b>84 fuentes Java y 89 tipos declarados con nombre</b>. El atlas integral los organiza en fichas canónicas C001 a C089, con miembros completos y relaciones salientes. Las referencias azules identifican la ficha del tipo relacionado; no agregan clases nuevas.')

add('Las composiciones se justifican por propiedad del ciclo de vida. Un artista existe independientemente de los eventos: esa referencia es una asociación. Las cajas, textos y conectores de las 19 vistas se verifican geométricamente; las relaciones y miembros se contrastan con el inventario del código.')

table(['Entregable','Contenido'],[

 ['Clases','Un diagrama integral y siete vistas detalladas por módulo.'],

 ['Patrones','Una vista de colaboración de Strategy y Facade.'],

 ['Casos de uso','Tres vistas por actor y una vista integrada.'],

 ['Actividades','Seis flujos: tres por rol y tres transversales.'],

],[CW*.25,CW*.75])

heading('Responsabilidades de los tres actores')

table(['Actor','Casos de uso y alcance'],[

 ['Usuario','Consulta eventos y capacidad; solicita ingreso a su nombre; ve sus registros y suscribe calendarios.'],

 ['Artista','Crea y gestiona eventos donde participa; consulta sus conectados y suscribe sus eventos a calendarios.'],

 ['Administrador','Administra cuentas y eventos; activa, desactiva y expulsa usuarios; consulta los registros generales.'],

],[CW*.25,CW*.75])

add('Las <b>20 acciones</b> se obtuvieron de Permisos.java compilado. Los objetivos agrupan operaciones afines; abrir Swing permanece en la matriz técnica. El administrador no se modela como especialización del actor usuario: no dispone de la acción de consultar sus propios registros.')

box('El inventario automático controla cobertura. La interpretación de asociaciones, multiplicidades y propiedad requiere además revisar la semántica del código.')



# 6

page('5. Flujos principales explicados')

heading('Solicitud de ingreso a un evento')

add('El ingreso sigue un orden de validación. Ese orden permite explicar por qué un intento se acepta o se rechaza y dónde se conserva su trazabilidad.')

table(['Paso','Responsabilidad y resultado'],[

 ['1. Permiso y cuenta propia','El gestor exige la acción. El usuario normal no puede actuar a nombre ajeno.'],

 ['2. Existencia','Se buscan usuario y evento antes de continuar con el acceso.'],

 ['3. Autorización y plan','AccesoEvento valida actividad, habilitación y cobertura de suscripción.'],

 ['4. Estado y horario','El recital debe estar en curso y dentro de la ventana de ingreso.'],

 ['5. Capacidad y duplicado','Debe quedar lugar y el usuario no debe estar conectado previamente.'],

 ['6. Registro y guardado','Se genera el resultado, se actualizan los datos y se guarda en SQLite.'],

],[CW*.31,CW*.69])

add('Una falta de permiso se rechaza antes de procesar el ingreso; no equivale a un intento de acceso rechazado por el recital. Si faltan usuario o evento, se genera un registro con una referencia de reemplazo en memoria, pero el repositorio lo omite al guardar por sus claves foráneas.')

heading('Gestión de eventos por el artista')

add('El artista selecciona un evento propio. El gestor verifica permiso y pertenencia; el evento valida la transición de estado. Solo entonces se modifica, se guarda y se refresca la vista. Si el evento es ajeno, la operación se deniega. La política de alcance se reutiliza también en este control.')

heading('Desactivación de usuarios por el administrador')

add('Se comprueba el permiso y se impide desactivar la propia cuenta. Al desactivar a otro usuario se lo desconecta de los eventos y se registran las expulsiones antes de guardar. Este caso muestra que un cambio de cuenta también puede afectar conexiones y trazabilidad.')



# 7

page('6. Verificación y límites del resultado')

heading('Evidencia registrada de la iteración')

add('La revisión volvió a ejecutar <b>103 comprobaciones de la aplicación</b> y nueve pruebas del detector de superposiciones. Se verificaron las 19 vistas contra su modelo geométrico y se inspeccionaron los diagramas y los detalles de clases a escala legible.')

table(['Área','Comprobación'],[

 ['Políticas y permisos','Roles, identidad por ID, referencias nulas, eventos propios y ajenos y matriz completa de acciones.'],

 ['Servicio local y HTTP','Listados, obtención de URL de calendario y cambios de estado, incluidos rechazos para eventos ajenos.'],

 ['SQLite','Guardar y cargar conserva asociaciones y visibilidad del artista, usando una base temporal.'],

 ['Diagramas','89 tipos con miembros, 20 acciones trazadas y 19 pares PNG/SVG con render verificado.'],

],[CW*.28,CW*.72])

heading('Qué no demuestra esta evidencia')

add('No se confirmó una suscripción real en Google o Apple ni se automatizaron todos los clics de Swing. La inspección de los diagramas y la cobertura de tipos tampoco equivalen a una certificación formal de conformidad UML o a una auditoría de seguridad integral.')

heading('Mejoras futuras identificadas')

add('Quedaron señaladas la caducidad de sesiones, una validación de datos más completa y la comprobación de la URL pública. También conviene revisar la consistencia entre memoria y base cuando falla un guardado: un rollback SQL no restaura automáticamente los objetos que ya fueron modificados.')

box('<b>Conclusión de validación.</b> La refactorización se verificó sobre los caminos relevantes de la regla extraída. Los límites y problemas heredados se documentaron sin atribuirles una corrección que esta iteración no implementó.')



# 8

page('7. Conclusiones y material para la defensa')

add('El aporte principal de la iteración es una regla de alcance explícita, acompañada de documentación trazable y pruebas para defender las decisiones. La funcionalidad existente mantiene sus contratos de API, DTO y esquema SQLite.')

table(['Pregunta para la exposición','Respuesta orientativa'],[

 ['¿Por qué usar Strategy?','Porque existen dos algoritmos de alcance que pueden expresarse y probarse mediante un contrato común.'],

 ['¿Qué Strategy era previo?','La selección de Google o Apple para suscribir el calendario de un evento.'],

 ['¿Por qué no implementar todos los patrones?','Porque una abstracción debe resolver una necesidad concreta; agregarla sin ese motivo aumenta el costo de mantenimiento.'],

 ['¿Cambiaron los permisos?','No. La política de alcance se separó y sus resultados se contrastaron con los controles existentes.'],

],[CW*.43,CW*.57])

heading('Anexos incorporados al PDF')

add('Se incluye el SVG del diagrama completo de clases de esta entrega. En un lector compatible se accede desde el panel de archivos adjuntos. Algunos visores web no muestran ese panel; los archivos originales también se conservan en la entrega del proyecto.')

heading('Referencias')

for label,url in [

 ('OMG: especificación UML 2.5.1','https://www.omg.org/spec/UML/2.5.1'),

 ('PlantUML: notación de diagramas de clases','https://plantuml.com/class-diagram'),

 ('Refactoring.Guru: catálogo de patrones de diseño','https://refactoring.guru/design-patterns'),

 ('Java SE 21: Component y Container','https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Container.html'),

]: add(f'<link href="{url}" color="#087D83">{label}</link>','small')

add('<b>Fuentes internas:</b> PATRONES.md, REVISION-UML.md, CASOS-DE-USO.md y VERIFICACION.md, en docs/iteracion-4/, más los contratos y las políticas del código Java.','small')

box('<b>Idea para cerrar la exposición:</b> la calidad del diseño se sostiene en la justificación de sus decisiones y en la evidencia del comportamiento, no en la cantidad de patrones utilizados.')



buf=BytesIO()

doc=SimpleDocTemplate(buf,pagesize=A4,leftMargin=48,rightMargin=48,topMargin=57,bottomMargin=58,

                      title='UADE Beats - Iteración 4 - Informe A4',author='Grupo 9')

doc.build(story,onFirstPage=footer,onLaterPages=footer)

reader=PdfReader(BytesIO(buf.getvalue()))

assert len(reader.pages)==8,f'La distribución debe ser de 8 páginas, se generaron {len(reader.pages)}'

writer=PdfWriter();writer.clone_document_from_reader(reader)

writer.add_metadata({'/Title':'UADE Beats - Iteración 4 - Informe explicativo A4','/Author':'Grupo 9'})

writer.add_attachment('clases-completo.svg', (ROOT/'TP/Grupo9_POO_Diagramas_Pdf/imagenes/clases-completo.svg').read_bytes())

for i,title in enumerate(['Portada y resumen','Punto de partida y método','Strategy','Ejemplo y otros patrones',

                          'UML y casos de uso','Flujos','Verificación y límites','Conclusiones y referencias']):

    writer.add_outline_item(title,i)

target=OUT/'UADE_Beats_Iteracion_4_Informe_A4.pdf'

with target.open('wb') as f: writer.write(f)

check=PdfReader(target)

for page in check.pages:

    assert abs(float(page.mediabox.width)-W)<.1 and abs(float(page.mediabox.height)-H)<.1

assert len(check.attachments)==1

print(f'PDF A4: {target}\nPáginas: {len(check.pages)}; adjuntos SVG: 1; bytes: {target.stat().st_size}')
