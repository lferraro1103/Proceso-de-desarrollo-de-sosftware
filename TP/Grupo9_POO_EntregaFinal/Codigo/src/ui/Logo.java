package ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Taskbar;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/*
 * Logo de UADE Beats.
 *
 * Se dibuja con Java2D en lugar de cargarse desde un archivo, asi no hay
 * recursos que empaquetar y se ve nitido en cualquier tamano. Representa un
 * ecualizador (la musica) con un punto rojo de "en vivo" (los eventos).
 */
public final class Logo {

    // Tamanos que usa el sistema operativo para el icono de la ventana.
    private static final int[] LADOS = {16, 32, 48, 64, 128, 256};

    private Logo() {
    }

    // Dibuja el logo en un cuadrado del lado indicado.
    public static BufferedImage dibujar(int lado) {
        BufferedImage imagen = new BufferedImage(
                lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        double s = lado;

        // Fondo: cuadrado redondeado con degradado azul.
        g.setPaint(new GradientPaint(0, 0, new Color(0x16, 0x24, 0x44),
                0, lado, new Color(0x2c, 0x5a, 0xa0)));
        g.fill(new RoundRectangle2D.Double(0, 0, s, s, s * 0.24, s * 0.24));

        // Cinco barras de ecualizador, la del centro es la mas alta.
        double[] alturas = {0.30, 0.50, 0.68, 0.50, 0.30};
        double ancho = s * 0.105;
        double separacion = s * 0.045;
        double total = alturas.length * ancho
                + (alturas.length - 1) * separacion;
        double x = (s - total) / 2;
        double base = s * 0.80;

        for (int i = 0; i < alturas.length; i++) {
            double alto = s * alturas[i];
            Color color = (i == 2)
                    ? new Color(0x3f, 0xd0, 0x7a)
                    : new Color(0xe8, 0xf1, 0xff);
            g.setColor(color);
            g.fill(new RoundRectangle2D.Double(
                    x, base - alto, ancho, alto, ancho, ancho));
            x += ancho + separacion;
        }

        // Punto rojo de "en vivo" con un aro claro.
        double radio = s * 0.095;
        double cx = s * 0.80;
        double cy = s * 0.20;
        g.setColor(new Color(0xff, 0xff, 0xff, 230));
        g.fill(new Ellipse2D.Double(
                cx - radio * 1.35, cy - radio * 1.35,
                radio * 2.7, radio * 2.7));
        g.setColor(new Color(0xe5, 0x39, 0x35));
        g.fill(new Ellipse2D.Double(
                cx - radio, cy - radio, radio * 2, radio * 2));

        g.setStroke(new BasicStroke(0f));
        g.dispose();
        return imagen;
    }

    // Icono para usar dentro de una ventana (por ejemplo, en el login).
    public static Icon icono(int lado) {
        return new ImageIcon(dibujar(lado));
    }

    // Todos los tamanos del icono, para que el sistema elija el adecuado.
    public static List<Image> iconos() {
        List<Image> imagenes = new ArrayList<>();

        for (int lado : LADOS) {
            imagenes.add(dibujar(lado));
        }

        return imagenes;
    }

    /*
     * Reemplaza el icono por defecto de Java en la ventana y, donde el
     * sistema lo permite, tambien en la barra o el Dock.
     */
    public static void aplicar(JFrame ventana) {
        ventana.setIconImages(iconos());

        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar barra = Taskbar.getTaskbar();

                if (barra.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    barra.setIconImage(dibujar(256));
                }
            }
        } catch (UnsupportedOperationException | SecurityException e) {
            // Si el sistema no permite cambiar el icono, se mantiene el de
            // la ventana.
        }
    }
}
