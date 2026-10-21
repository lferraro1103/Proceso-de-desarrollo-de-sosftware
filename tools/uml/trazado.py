"""Primitivas SVG/PNG con métricas reales y auditoría de colisiones. Sin auto-layout."""
from pathlib import Path
from html import escape
import math
import json
import re
from PIL import Image, ImageDraw, ImageFont

INK='#20374F'; TEAL='#087B83'; LINE='#526A80'; BORDER='#B4C6D6'
BG='#FFFFFF'; PALE='#EAF3F8'; SOFT='#F5F8FB'; GOLD='#FFF4DA'
FONTROOT=Path('C:/Windows/Fonts')
FONTS={}

def font(size=16,bold=False,mono=False):
    key=(size,bold,mono)
    if key not in FONTS:
        name='consolab.ttf' if mono and bold else 'consola.ttf' if mono else 'arialbd.ttf' if bold else 'arial.ttf'
        FONTS[key]=ImageFont.truetype(str(FONTROOT/name),size)
    return FONTS[key]

def wrap(s,width,size=16,bold=False,mono=False):
    f=font(size,bold,mono)
    result=[]
    for paragraph in str(s).split('\n'):
        if not paragraph: result.append('');continue
        words=paragraph.split(' ');line=''
        for word in words:
            trial=(line+' '+word).strip()
            if f.getlength(trial)<=width:
                line=trial;continue
            if line:result.append(line);line=''
            while word and f.getlength(word)>width:
                cut=max(1,int(width/max(f.getlength(word),1)*len(word)))
                while f.getlength(word[:cut])>width:cut-=1
                result.append(word[:cut]);word=word[cut:]
            line=word
        if line:result.append(line)
    return result

def intersect(a,b,pad=0):
    return min(a[2],b[2])-max(a[0],b[0])>pad and min(a[3],b[3])-max(a[1],b[1])>pad

def hits_rect(p,q,r):
    # Liang-Barsky: no cuenta un roce sobre el borde.
    x0,y0=p;x1,y1=q;left,top,right,bottom=r
    left+=1;top+=1;right-=1;bottom-=1
    dx=x1-x0;dy=y1-y0;t0=0.;t1=1.
    for a,b in [(-dx,x0-left),(dx,right-x0),(-dy,y0-top),(dy,bottom-y0)]:
        if abs(a)<1e-9:
            if b<0:return False
        else:
            t=b/a
            if a<0:t0=max(t0,t)
            else:t1=min(t1,t)
            if t0>t1:return False
    return True

class Scene:
    def __init__(self,name,title,width,subtitle='',legend=()):
        self.name=name;self.title=title;self.width=width;self.height=0
        self.items=[];self.nodes=[];self.texts=[];self.edges=[];self.semantic={}
        self.text(title,32,22,width-64,25,True,color=INK)
        y=65
        if subtitle:y=self.text(subtitle,32,y,width-64,15,color=LINE)+10
        self.body_top=y+12
        self.legend=list(legend)

    def rect(self,x,y,w,h,fill=BG,stroke=BORDER,rounding=0,node=None):
        self.items.append(('rect',x,y,w,h,fill,stroke,rounding,node))
        if node:self.nodes.append({'id':node,'box':[x,y,x+w,y+h],'shape':'rect'})

    def line(self,points,color=LINE,width=1.6,dashed=False,source=None,target=None,audit=True):
        pts=[tuple(p) for p in points]
        self.items.append(('line',pts,color,width,dashed))
        if audit:self.edges.append({'points':pts,'source':source,'target':target})

    def polygon(self,points,fill=BG,stroke=LINE):
        self.items.append(('polygon',points,fill,stroke))

    def ellipse(self,x,y,w,h,fill=BG,stroke=LINE,node=None):
        self.items.append(('ellipse',x,y,w,h,fill,stroke))
        if node:self.nodes.append({'id':node,'box':[x,y,x+w,y+h],'shape':'ellipse'})

    def text(self,s,x,y,width,size=16,bold=False,mono=False,color=INK,center=False):
        f=font(size,bold,mono);leading=math.ceil(size*1.45)
        for i,line in enumerate(wrap(s,width,size,bold,mono)):
            if not line:continue
            actual=f.getlength(line)
            tx=x+(width-actual)/2 if center else x
            ty=y+i*leading;baseline=ty+f.getmetrics()[0]
            bbox=f.getbbox(line,anchor='ls')
            box=[tx+bbox[0],baseline+bbox[1],tx+bbox[2],baseline+bbox[3]]
            self.texts.append({'text':line,'box':box,'size':size})
            self.items.append(('text',line,tx,baseline,size,bold,mono,color,actual))
        return y+max(1,len(wrap(s,width,size,bold,mono)))*leading

    def arrow(self,points,kind='association',source=None,target=None):
        pts=[tuple(p) for p in points]
        dashed=kind in ('realization','dependency')
        start=pts[0];end=pts[-1]
        vx=end[0]-pts[-2][0];vy=end[1]-pts[-2][1]
        length=math.hypot(vx,vy);ux=vx/length;uy=vy/length
        if kind in ('generalization','realization'):
            base=(end[0]-13*ux,end[1]-13*uy)
            self.line(pts[:-1]+[base],dashed=dashed,source=source,target=target)
            self.polygon([end,(base[0]+6*uy,base[1]-6*ux),(base[0]-6*uy,base[1]+6*ux)])
        else:
            if kind=='composition':
                ax=pts[1][0]-start[0];ay=pts[1][1]-start[1];al=math.hypot(ax,ay);ax/=al;ay/=al
                mid=(start[0]+8*ax,start[1]+8*ay);tip=(start[0]+16*ax,start[1]+16*ay)
                self.polygon([start,(mid[0]+4*ay,mid[1]-4*ax),tip,(mid[0]-4*ay,mid[1]+4*ax)],LINE)
                pts=[tip]+pts[1:]
            self.line(pts,dashed=dashed,source=source,target=target)
            self.line([(end[0]-9*ux+4*uy,end[1]-9*uy-4*ux),end,
                       (end[0]-9*ux-4*uy,end[1]-9*uy+4*ux)],audit=False)

    def finish(self,bottom):
        y=bottom+30
        if self.legend:
            self.rect(24,y,self.width-48,20+len(self.legend)*24,SOFT,SOFT)
            for s in self.legend:
                y=self.text(s,38,y+9,self.width-76,14,color=LINE)+2
        self.height=int(y+50)
        self.text('UADE Beats | Iteración 4 | Revisión de disposición y semántica',32,self.height-28,self.width-64,13,color=LINE)
        return self

    def audit(self):
        errors=[]
        for i,a in enumerate(self.nodes):
            for b in self.nodes[i+1:]:
                if intersect(a['box'],b['box'],.5):errors.append(('nodes',a['id'],b['id']))
        # Barrido vertical: evita costo cuadrático en el atlas de 89 fichas.
        active=[]
        for t in sorted(self.texts,key=lambda t:t['box'][1]):
            active=[a for a in active if a['box'][3]>t['box'][1]+.5]
            for a in active:
                if intersect(a['box'],t['box'],.7):errors.append(('texts',a['text'],t['text']))
            active.append(t)
            if t['box'][0]<0 or t['box'][1]<0 or t['box'][2]>self.width or t['box'][3]>self.height:
                errors.append(('outside',t['text'],t['box']))
        for e in self.edges:
            for p,q in zip(e['points'],e['points'][1:]):
                for n in self.nodes:
                    if n['id'] not in (e['source'],e['target']) and hits_rect(p,q,n['box']):
                        errors.append(('edge-node',n['id'],p,q))
                for t in self.texts:
                    if hits_rect(p,q,t['box']):errors.append(('edge-text',t['text'],p,q))
        segments=[]
        for e in self.edges:
            segments.extend(zip(e['points'],e['points'][1:]))
        def orient(a,b,p):return (b[0]-a[0])*(p[1]-a[1])-(b[1]-a[1])*(p[0]-a[0])
        for i,(a,b) in enumerate(segments):
            for p,q in segments[i+1:]:
                if max(a[1],b[1])<min(p[1],q[1]) or max(p[1],q[1])<min(a[1],b[1]):continue
                if orient(a,b,p)*orient(a,b,q)<-1e-6 and orient(p,q,a)*orient(p,q,b)<-1e-6:
                    errors.append(('edge-crossing',a,b,p,q))
        for n in self.nodes:
            if n.get('shape') not in ('diamond','ellipse'):continue
            x0,y0,x1,y1=n['box'];cx=(x0+x1)/2;cy=(y0+y1)/2
            for t in self.texts:
                a,b,c,d=t['box']
                if not(x0<a<c<x1 and y0<b<d<y1):continue
                for x,y in [(a,b),(c,b),(a,d),(c,d)]:
                    u=abs((x-cx)/((x1-x0)/2));v=abs((y-cy)/((y1-y0)/2))
                    if (u+v if n['shape']=='diamond' else u*u+v*v)>1.001:
                        errors.append(('text-outside-shape',n['id'],t['text']));break
        return {'name':self.name,'width':self.width,'height':self.height,'nodes':len(self.nodes),
                'textLines':len(self.texts),'edges':len(self.edges),'minimumFont':min(t['size'] for t in self.texts),
                'errors':errors,'semantic':self.semantic}

    def save(self,folder):
        report=self.audit()
        if report['errors']:raise ValueError(json.dumps(report,ensure_ascii=False,indent=2)[:15000])
        svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.width}" height="{self.height}" viewBox="0 0 {self.width} {self.height}">',
             f'<title>{escape(self.title)}</title><desc>Disposición determinista. Textos, nodos y conectores auditados.</desc>',
             f'<rect width="100%" height="100%" fill="{BG}"/>']
        image=Image.new('RGB',(int(self.width),int(self.height)),BG);draw=ImageDraw.Draw(image)
        for item in self.items:
            kind=item[0]
            if kind=='rect':
                _,x,y,w,h,fill,stroke,r,n=item
                data=f' data-node="{escape(n)}" id="{re.sub(r"[^A-Za-z0-9_-]", "_", n)}"' if n else ''
                svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}" stroke="{stroke}"{data}/>')
                draw.rounded_rectangle((x,y,x+w,y+h),radius=r,fill=fill,outline=stroke,width=1)
            elif kind=='ellipse':
                _,x,y,w,h,fill,stroke=item
                svg.append(f'<ellipse cx="{x+w/2}" cy="{y+h/2}" rx="{w/2}" ry="{h/2}" fill="{fill}" stroke="{stroke}"/>')
                draw.ellipse((x,y,x+w,y+h),fill=fill,outline=stroke,width=2)
            elif kind=='polygon':
                _,pts,fill,stroke=item
                svg.append(f'<polygon points="'+ ' '.join(f'{x},{y}' for x,y in pts)+f'" fill="{fill}" stroke="{stroke}"/>')
                draw.polygon(pts,fill=fill,outline=stroke)
            elif kind=='line':
                _,pts,color,width,dashed=item
                dash=' stroke-dasharray="6 4"' if dashed else ''
                svg.append('<polyline points="'+' '.join(f'{x},{y}' for x,y in pts)+f'" fill="none" stroke="{color}" stroke-width="{width}"{dash}/>')
                if not dashed:draw.line(pts,fill=color,width=max(1,round(width)))
                else:
                    for a,b in zip(pts,pts[1:]):
                        length=math.dist(a,b);count=max(1,math.ceil(length/10))
                        for i in range(count):
                            u=min(i*10/length,1);v=min((i*10+6)/length,1)
                            draw.line([(a[0]+(b[0]-a[0])*u,a[1]+(b[1]-a[1])*u),
                                       (a[0]+(b[0]-a[0])*v,a[1]+(b[1]-a[1])*v)],fill=color,width=max(1,round(width)))
            elif kind=='text':
                _,s,x,baseline,size,bold,mono,color,length=item
                family='Consolas' if mono else 'Arial'
                weight='bold' if bold else 'normal'
                svg.append(f'<text x="{x}" y="{baseline}" font-family="{family}" font-size="{size}" font-weight="{weight}" fill="{color}" textLength="{length}" lengthAdjust="spacingAndGlyphs">{escape(s)}</text>')
                draw.text((x,baseline),s,font=font(size,bold,mono),fill=color,anchor='ls')
        svg.append('</svg>')
        folder=Path(folder);folder.mkdir(parents=True,exist_ok=True)
        (folder/f'{self.name}.svg').write_text('\n'.join(svg),encoding='utf-8')
        image.save(folder/f'{self.name}.png',optimize=False)
        image.close()
        layout={'name':self.name,'title':self.title,'width':self.width,'height':self.height,
                'items':self.items,'nodes':self.nodes,'texts':self.texts,'edges':self.edges,
                'semantic':self.semantic}
        source=folder.parent/'fuentes';source.mkdir(exist_ok=True)
        (source/f'{self.name}.layout.json').write_text(json.dumps(layout,ensure_ascii=False,indent=2),encoding='utf-8')
        return report
