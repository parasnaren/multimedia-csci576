// TODO: Extend lines to the edges
// TODO: Update getScaledImage() to use 2D indices

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;


public class Mypart2 {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage mainImg;
	BufferedImage previousImg;
	BufferedImage newImg;
	private int SIZE = 512;
	private int R = 256;
	private double angle;
	private int step = 10; // move by 5 degrees each iteration
	private boolean complete;
	private long previousDrawnTime;
	private long previousCapturedTime;
	private long currentTime;
	

	// Draws black lines on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawRadialLines(BufferedImage image, int n, double angle) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
        for (int i = 0; i < n; i++) {
			int x1 = R;
			int y1 = R;
			int x2 = (int) (R + 2 * R * Math.cos(Math.toRadians(360.0 / n * i + angle)));
			int y2 = (int) (R + 2 * R * Math.sin(Math.toRadians(360.0 / n * i + angle)));
            g.drawLine(x1, y1, x2, y2);
        }
		g.dispose();
	}

	public void initBackgroundImage(BufferedImage img) {
		Graphics2D g = img.createGraphics();

		// Draw white background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, SIZE, SIZE);

		// Draw border
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.drawLine(0, 0, SIZE-1, 0);			// top edge
		g.drawLine(0, 0, 0, SIZE-1);			// left edge
		g.drawLine(0, SIZE-1, SIZE-1, SIZE-1);		// bottom edge
		g.drawLine(SIZE-1, SIZE-1, SIZE-1, 0);		// right edge
		g.dispose();
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
		long delay = (long) (1000 / (360.0 * x / step)); // delay for generating frames on the main image
		long timeInterval = (long) (1000 / fps); // delay for extracting frames to right side video
		// long currentTime, timeElapsed, previousTime = 0;
		mainImg = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB); // initialise the main image
		newImg = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB); // initialise the new image

		Timer mainImgTimer = new Timer();
		Timer newImgTimer = new Timer();

		mainImgTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				currentTime = System.currentTimeMillis();
				System.out.println("Drawn frame every " + delay + " ms " + "time: " + (currentTime - previousDrawnTime));
				previousDrawnTime = currentTime;
				complete = false;
				initBackgroundImage(mainImg); // Set white background
				drawRadialLines(mainImg, n, angle); // Draw the radial lines
				complete = true;
				BufferedImage subImage = mainImg.getSubimage(0, 0, SIZE, SIZE);
				previousImg = new BufferedImage(subImage.getWidth(), subImage.getHeight(), subImage.getType());
				previousImg.createGraphics().drawImage(subImage, 0, 0, null);
				angle = (angle + step) % 360;
				lbIm1 = new JLabel(new ImageIcon(mainImg));
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 1;
				frame.getContentPane().add(lbIm1, c);
				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		}, 0, delay);

		newImgTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Graphics g = newImg.createGraphics();

				// logic to ensure the picked frame has a completely drawn image, if not the previous frame's image is considered
				if (complete) { 
					g.drawImage(mainImg, 0, 0, null);
				} else {
					g.drawImage(previousImg, 0, 0, null);
				}
				System.out.println("Captured frame every " + timeInterval + " ms " + "time: " + (currentTime - previousCapturedTime));
				previousCapturedTime = currentTime;
				// Update the panel
				lbIm2 = new JLabel(new ImageIcon(newImg));
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = 1;
				frame.getContentPane().add(lbIm2, c);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		}, 0, timeInterval);
	}

	public static void main(String[] args) {
		Mypart2 ren = new Mypart2();
		ren.showIms(args);
	}

}