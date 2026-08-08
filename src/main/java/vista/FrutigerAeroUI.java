package vista;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Utilidades visuales estilo "Frutiger Aero": superficies tipo vidrio/cielo,
 * botones brillantes ("glossy") y bordes redondeados tipo burbuja.
 *
 * IMPORTANTE: todo se dibuja con Graphics2D puro (sin imágenes, iconos ni
 * componentes descargados), para que el proyecto compile y se vea igual
 * en cualquier equipo que clone el repositorio desde GitHub.
 */
public final class FrutigerAeroUI {

    private FrutigerAeroUI() {}

    // =======================================================
    // PALETA DE COLORES
    // =======================================================
    public static final Color CIELO_CLARO   = new Color(214, 240, 253);
    public static final Color CIELO_MEDIO   = new Color(178, 224, 247);
    public static final Color AGUA_PROFUNDA = new Color(59, 150, 205);
    public static final Color VERDE_HOJA    = new Color(126, 197, 90);
    public static final Color VERDE_OSCURO  = new Color(70, 150, 60);
    public static final Color NARANJA_SOL   = new Color(240, 165, 60);
    public static final Color ROJO_CORAL    = new Color(214, 90, 80);
    public static final Color TEXTO_OSCURO  = new Color(25, 60, 85);

    // =======================================================
    // TIPOGRAFÍA (fuente estándar del sistema, redondeada al ojo)
    // =======================================================
    public static final Font FUENTE_TITULO  = fuenteRedondeada(Font.BOLD, 13);
    public static final Font FUENTE_NORMAL  = fuenteRedondeada(Font.PLAIN, 12);
    public static final Font FUENTE_HEADER  = fuenteRedondeada(Font.BOLD, 16);

    private static Font fuenteRedondeada(int estilo, int tam) {
        // "Trebuchet MS" viene preinstalada en Windows (entorno típico de NetBeans
        // en equipos escolares) y es la fuente redonda más cercana sin descargar nada.
        // Si no existe en el sistema, AWT recurre automáticamente a la fuente lógica
        // más parecida, así que nunca truena por falta del archivo de fuente.
        return new Font("Trebuchet MS", estilo, tam);
    }

    // =======================================================
    // ACLARAR / OSCURECER UN COLOR (para gradientes y hover)
    // =======================================================
    public static Color aclarar(Color c, float factor) {
        int r = Math.min(255, Math.round(c.getRed()   + (255 - c.getRed())   * factor));
        int g = Math.min(255, Math.round(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, Math.round(c.getBlue()  + (255 - c.getBlue())  * factor));
        return new Color(r, g, b);
    }

    // =======================================================
    // PANEL "CIELO": fondo con degradado + brillo superior tipo vidrio
    // =======================================================
    public static class PanelCielo extends JPanel {
        public PanelCielo(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setPaint(new GradientPaint(0, 0, CIELO_CLARO, 0, h, Color.WHITE));
            g2.fillRect(0, 0, w, h);

            // Brillo superior tipo "vidrio" (glass highlight)
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 130), 0, h * 0.35f, new Color(255, 255, 255, 0)));
            g2.fillRect(0, 0, w, (int) (h * 0.35));

            // Burbujas decorativas tipo vidrio, agrupadas hacia abajo-derecha
            pintarBurbujas(g2, w, h);

            g2.dispose();
            super.paintComponent(g);
        }

        /**
         * Dibuja un pequeño grupo de burbujas traslúcidas en la esquina inferior
         * derecha del panel, imitando el detalle decorativo típico del estilo
         * Frutiger Aero. Las posiciones/tamaños son relativos al ancho y alto
         * del panel para que se vean bien sin importar el tamaño de la ventana.
         */
        private void pintarBurbujas(Graphics2D g2, int w, int h) {
            // Cada fila: {offsetDesdeDerecha, offsetDesdeAbajo, diametro}
            int[][] burbujas = {
                { 30,  10, 120 },
                { 130, -30, 175 },
                { 220,  70,  70 },
                { 10,  150,  55 },
                { 170, 160,  45 },
                { 60,  230,  30 },
            };
            for (int[] b : burbujas) {
                int diametro = b[2];
                int cx = w - b[0] - diametro / 2;
                int cy = h - b[1] - diametro / 2;
                dibujarBurbuja(g2, cx, cy, diametro);
            }
        }

        private void dibujarBurbuja(Graphics2D g2, int cx, int cy, int diametro) {
            int r = diametro / 2;

            // Relleno tipo vidrio: degradado radial que se desvanece hacia afuera,
            // con el "punto caliente" desplazado arriba-izquierda para simular brillo.
            RadialGradientPaint relleno = new RadialGradientPaint(
                    new Point(cx - r / 3, cy - r / 3), diametro,
                    new float[]{0f, 0.7f, 1f},
                    new Color[]{
                        new Color(255, 255, 255, 145),
                        new Color(255, 255, 255, 70),
                        new Color(255, 255, 255, 18)
                    }
            );
            g2.setPaint(relleno);
            g2.fillOval(cx - r, cy - r, diametro, diametro);

            // Contorno sutil para que la burbuja se distinga del fondo
            g2.setColor(new Color(255, 255, 255, 110));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(cx - r, cy - r, diametro, diametro);
        }
    }

    // =======================================================
    // BOTÓN "GLOSSY": redondeado, degradado y con brillo superior
    // =======================================================
    public static class BotonGlossy extends JButton {
        private final Color colorBase;

        public BotonGlossy(String texto, Color colorBase) {
            super(texto);
            this.colorBase = colorBase;
            setFont(FUENTE_TITULO);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, h, h);

            Color base = colorBase;
            if (!isEnabled()) base = new Color(180, 180, 180);
            else if (getModel().isPressed()) base = colorBase.darker();
            else if (getModel().isRollover()) base = aclarar(colorBase, 0.18f);

            g2.setPaint(new GradientPaint(0, 0, aclarar(base, 0.35f), 0, h, base.darker()));
            g2.fill(forma);

            // Brillo superior (glass highlight) que da el efecto "glossy"
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 150), 0, h * 0.55f, new Color(255, 255, 255, 0)));
            g2.fill(new RoundRectangle2D.Float(2, 2, w - 5, Math.max(1, h * 0.5f), h, h));

            g2.setColor(base.darker().darker());
            g2.draw(forma);
            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =======================================================
    // BORDE "BURBUJA": línea redondeada suave (para campos de texto, etc.)
    // =======================================================
    public static class BordeBurbuja extends AbstractBorder {
        private final Color color;
        public BordeBurbuja(Color color) { this.color = color; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 3, h - 3, 12, 12));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(6, 10, 6, 10); }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(6, 10, 6, 10);
            return insets;
        }
    }

    // =======================================================
    // BORDE TITULADO ESTILO "VIDRIO" (para reemplazar los TitledBorder normales)
    // =======================================================
    public static TitledBorder bordeTitulado(String titulo) {
        TitledBorder borde = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AGUA_PROFUNDA, 1, true), titulo);
        borde.setTitleFont(FUENTE_TITULO);
        borde.setTitleColor(AGUA_PROFUNDA.darker());
        return borde;
    }

    // =======================================================
    // ESTILIZAR UN JTabbedPane (colores + tipografía, sin UI personalizada
    // que pueda romper NetBeans GUI Builder)
    // =======================================================
    public static void estilizarTabbedPane(JTabbedPane tabs) {
        tabs.setFont(FUENTE_TITULO);
        tabs.setBackground(CIELO_MEDIO);
        tabs.setForeground(TEXTO_OSCURO);
    }

    // =======================================================
    // ESTILIZAR EL HEADER DE UN JTable
    // =======================================================
    public static void estilizarHeaderTabla(JTable tabla) {
        tabla.getTableHeader().setBackground(AGUA_PROFUNDA);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(FUENTE_TITULO);
        tabla.setSelectionBackground(CIELO_MEDIO);
        tabla.setSelectionForeground(TEXTO_OSCURO);
        tabla.setGridColor(CIELO_MEDIO);
    }

    // =======================================================
    // VOLVER TRANSPARENTE UN JScrollPane (para que las burbujas del
    // PanelCielo se vean a través del área vacía de tablas y listas)
    // =======================================================
    public static void hacerTransparente(JScrollPane scroll) {
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
    }
}