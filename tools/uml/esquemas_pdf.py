"""Esquemas A4 con geometría auditada y realizaciones UML hacia el contrato."""
from reportlab.graphics.shapes import Drawing,Rect,String,Polygon,PolyLine
from reportlab.lib.colors import HexColor
from trazado import Scene,PALE,BORDER

def make_scene(kind):
    s=Scene('esquema-'+kind,'',1000);s.items=[];s.texts=[];s.nodes=[];s.edges=[];s.height=300
    def node(x,y,w,h,label,detail,id):
        s.rect(x,y,w,h,PALE,BORDER,8,node=id)
        yy=s.text(label,x+13,y+13,w-26,20,True,center=True)
        s.text(detail,x+13,yy+10,w-26,16,center=True)
    if kind=='architecture':
        node(18,91,225,108,'Consola / Swing','Vistas','view')
        node(350,91,280,108,'ServicioEventos','Contrato común','contract')
        node(765,16,220,92,'Gestor local','Coordina casos de uso','local')
        node(765,205,220,92,'GestorRemoto','Proxy HTTP','remote')
        s.arrow([(243,145),(350,145)],'association','view','contract')
        s.text('usa',264,107,66,16,center=True)
        s.arrow([(765,62),(630,116)],'realization','local','contract')
        s.text('implementa',642,29,110,15,center=True)
        s.arrow([(765,251),(630,173)],'realization','remote','contract')
        s.text('implementa',642,266,110,15,center=True)
    else:
        node(18,90,280,112,'PoliticasVisibilidad','Selecciona por rol','selector')
        node(395,90,280,112,'PoliticaVisibilidadEvento','esVisible(cuenta, evento)','contract')
        node(775,16,210,92,'VisibilidadGeneral','Usuario / administrador','general')
        node(775,205,210,92,'VisibilidadArtista','Participación por ID','artist')
        s.arrow([(298,146),(395,146)],'association','selector','contract')
        s.text('porRol',306,109,80,15,center=True)
        s.arrow([(775,62),(675,116)],'realization','general','contract')
        s.arrow([(775,251),(675,173)],'realization','artist','contract')
    s.semantic={'kind':'pdf-scheme'}
    assert not s.audit()['errors'],s.audit()['errors']
    return s

def drawing(kind,width):
    s=make_scene(kind);d=Drawing(s.width,s.height)
    for it in s.items:
        if it[0]=='rect':
            _,x,y,w,h,fill,stroke,r,_=it
            d.add(Rect(x,s.height-y-h,w,h,rx=r,ry=r,fillColor=HexColor(fill),strokeColor=HexColor(stroke)))
        elif it[0]=='line':
            _,pts,color,thick,dashed=it
            d.add(PolyLine([v for x,y in pts for v in (x,s.height-y)],strokeColor=HexColor(color),
                           strokeWidth=thick,strokeDashArray=[6,4] if dashed else None))
        elif it[0]=='polygon':
            _,pts,fill,stroke=it
            d.add(Polygon([v for x,y in pts for v in (x,s.height-y)],fillColor=HexColor(fill),strokeColor=HexColor(stroke)))
        elif it[0]=='text':
            _,text,x,base,size,bold,mono,color,_=it
            d.add(String(x,s.height-base,text,fontName='Arial-Bold' if bold else 'Arial',fontSize=size,fillColor=HexColor(color)))
    scale=width/s.width;d.scale(scale,scale);d.width=width;d.height=s.height*scale
    return d
