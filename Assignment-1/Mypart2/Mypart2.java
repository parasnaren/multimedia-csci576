// TODO: Extend lines to the edges
// TODO: Update getScaledImage() to use 2D indices

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;


public class Mypart2 {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage mainImg;
	BufferedImage newImg;
	private int SIZE = 512;
	private int R = 256;
	private double angle;
	private int step = 5; // move by 5 degrees each iteration

	// Draws a black line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawLine(BufferedImage image, int x1, int y1, int x2, int y2) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.drawLine(x1, y1, x2, y2);
		g.drawImage(image, 0, 0, null);
	}

	public void drawRadialLines(BufferedImage image, int n, double angle) {
        for (int i = 0; i < n; i++) {
			int x1 = R;
			int y1 = R;
			int x2 = (int) (R + R * Math.cos(Math.toRadians(360.0 / n * i + angle)));
			int y2 = (int) (R + R * Math.sin(Math.toRadians(360.0 / n * i + angle)));
            drawLine(image, x1, y1, x2, y2);
        }
	}

	public void initBackgroundImage(BufferedImage img) {
		// Initialize a plain white image
		for(int y = 0; y < SIZE; y++) {
			for(int x = 0; x < SIZE; x++) {
				byte R = (byte) 255;
				byte g = (byte) 255;
				byte b = (byte) 255;
				int pix = 0xff000000 | ((R & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x, y, pix);
			}
		}
		drawLine(img, 0, 0, SIZE-1, 0);			// top edge
		drawLine(img, 0, 0, 0, SIZE-1);			// left edge
		drawLine(img, 0, SIZE-1, SIZE-1, SIZE-1);		// bottom edge
		drawLine(img, SIZE-1, SIZE-1, SIZE-1, 0);		// right edge
	}

	public void showIms(String[] args) {
		// Read parameters from command line
		int n = Integer.parseInt(args[0]); // number of radial lines `n`
		System.out.println("n: " + n);

		double x = Double.parseDouble(args[1]);  // the number of revolutions per second `x`
		System.out.println("x: " + x);

		double fps = Double.parseDouble(args[2]); // fps of the output `fps`
		System.out.println("fps: " + fps);
		
		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original video (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Video after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		// Calculations
		long delay = (long) (1000 / (72 * x));
		long timeInterval = (long) (1000 / fps);
		long currentTime, timeElapsed, previousTime = 0;
		mainImg = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB); // initialise the main image
		newImg = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB); // initialise the new image

		while (true) {
			currentTime = System.currentTimeMillis();
			initBackgroundImage(mainImg); // Set white background
			drawRadialLines(mainImg, n, angle); // Draw the radial lines
			angle = (angle + step) % 360;
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeElapsed = (currentTime - previousTime);
			if (timeElapsed >= timeInterval) {
				Graphics g = newImg.createGraphics();
				g.drawImage(mainImg, 0, 0, null);
				previousTime = currentTime;
				System.out.println("Captured frame: " + timeElapsed);
			}
			lbIm1 = new JLabel(new ImageIcon(mainImg));
			lbIm2 = new JLabel(new ImageIcon(newImg));

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			frame.getContentPane().add(lbIm1, c);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridy = 1;
			frame.getContentPane().add(lbIm2, c);

			frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}

	public static void main(String[] args) {
		Mypart2 ren = new Mypart2();
		ren.showIms(args);
	}

}