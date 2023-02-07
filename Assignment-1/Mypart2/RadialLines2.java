import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class RadialLines2 extends JPanel {
    private int n = 8;
    private double x = 1;
    private double rotation = 0;
    private final int width = 500;
    private final int height = 500;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(Color.RED);
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n + rotation;
            int x1 = (int) (width / 2 + width / 2 * Math.cos(angle));
            int y1 = (int) (height / 2 + height / 2 * Math.sin(angle));
            int x2 = (int) (width / 2 - width / 2 * Math.cos(angle));
            int y2 = (int) (height / 2 - height / 2 * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    public void animate() {
        while (true) {
            rotation = rotation + 2 * Math.PI * x / 60;
            if (rotation > 2 * Math.PI) {
                rotation = 0;
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Radial Lines");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new RadialLines2());
        frame.pack();
        frame.setVisible(true);
        new RadialLines2().animate();
    }
}
