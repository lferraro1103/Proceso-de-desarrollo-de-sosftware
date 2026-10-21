"""Prueba el detector con colisiones sembradas, antes de validar los entregables."""
import unittest
from trazado import Scene
class AuditoriaTest(unittest.TestCase):
    def scene(self):
        s=Scene('test','Prueba',900);s.height=700;return s
    def kinds(self,s):return {e[0] for e in s.audit()['errors']}
    def test_texto_superpuesto(self):
        s=self.scene();s.text('uno',100,200,200);s.text('dos',100,200,200)
        self.assertIn('texts',self.kinds(s))
    def test_cajas_superpuestas(self):
        s=self.scene();s.rect(100,100,100,100,node='a');s.rect(150,150,100,100,node='b')
        self.assertIn('nodes',self.kinds(s))
    def test_linea_corta_caja(self):
        s=self.scene();s.rect(300,200,100,100,node='c');s.line([(100,250),(600,250)])
        self.assertIn('edge-node',self.kinds(s))
    def test_linea_corta_texto(self):
        s=self.scene();s.text('Etiqueta',300,220,160);s.line([(100,230),(600,230)])
        self.assertIn('edge-text',self.kinds(s))
    def test_cruce(self):
        s=self.scene();s.line([(100,100),(400,400)]);s.line([(100,400),(400,100)])
        self.assertIn('edge-crossing',self.kinds(s))
    def test_rombo(self):
        s=self.scene();s.nodes.append({'id':'d','box':[100,100,400,300],'shape':'diamond'})
        s.text('Fuera del rombo',115,110,200)
        self.assertIn('text-outside-shape',self.kinds(s))
    def test_texto_fuera(self):
        s=self.scene();s.text('Se sale',880,400,150)
        self.assertIn('outside',self.kinds(s))
    def test_extremo_compartido_valido(self):
        s=self.scene();s.line([(100,300),(400,100)]);s.line([(100,300),(400,500)])
        self.assertNotIn('edge-crossing',self.kinds(s))
    def test_geometria_valida(self):
        s=self.scene();s.rect(100,200,200,90,node='a');s.rect(500,200,200,90,node='b')
        s.text('Origen',120,220,160);s.text('Destino',520,220,160)
        s.line([(300,245),(500,245)],source='a',target='b')
        self.assertEqual(s.audit()['errors'],[])
if __name__=='__main__':unittest.main()
