import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.Timer;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class RadialLines extends JPanel {
  private static final int SIZE = 512;
  private BufferedImage buffer;
  private int n;
  private double angle = 0;
  private int x;
  private double fps;

  public RadialLines(int n, int x) {
    this.n = n;
    this.x = x;
    // this.fps = fps;
    setPreferredSize(new Dimension(SIZE, SIZE));
    buffer = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(buffer, 0, 0, this);
  }

  public void animate() {
    long start = System.currentTimeMillis();
    double step = 5;
    long delay = 1000 / (72 * x);
    double timeBetweenFrames = 1000 / fps;
    while (true) {
      long end = System.currentTimeMillis();
      Graphics2D g2d = buffer.createGraphics();
      g2d.setColor(Color.WHITE);
      g2d.fillRect(0, 0, SIZE, SIZE);
      g2d.setColor(Color.BLACK);
      for (int i = 0; i < n; i++) {
        int x1 = SIZE / 2;
        int y1 = SIZE / 2;
        int x2 = (int) (SIZE / 2 + SIZE / 4 * Math.cos(Math.toRadians(360.0 / n * i + angle)));
        int y2 = (int) (SIZE / 2 + SIZE / 4 * Math.sin(Math.toRadians(360.0 / n * i + angle)));
        g2d.drawLine(x1, y1, x2, y2);
      }
      g2d.dispose();
      angle = (angle + step) % 360;
      System.out.println(angle);
      repaint();
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Radial Lines");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    RadialLines panel = new RadialLines(2, 1);
    frame.add(panel);
    frame.pack();
    frame.setVisible(true);
    panel.animate();
  }
}
