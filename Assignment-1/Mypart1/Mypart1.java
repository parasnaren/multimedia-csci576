// TODO: Extend lines to the edges
// TODO: Update getScaledImage() to use 2D indices

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;


public class Mypart1 {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage mainImg;
	private int SIZE = 512;
	private int R = 256;

	// Draws a black line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawRadialLines(BufferedImage image, int n) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
        for (int i = 0; i < n; i++) {
			int x1 = R;
			int y1 = R;
			int x2 = (int) (R + 2 * R * Math.cos(Math.toRadians(360.0 / n * i)));
			int y2 = (int) (R + 2 * R * Math.sin(Math.toRadians(360.0 / n * i)));
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

	// public BufferedImage old_getAntiAliasedImage(BufferedImage image) {
	// 	/*
	// 	 * Using a custom low pass filter to anti-alias the image
	// 	 * [
	// 			1/9f,	1/9f,	1/9f,
	// 			1/9f,	1/9f,	1/9f,
	// 			1/9f,	1/9f,	1/9f
	// 		]
	// 	*/
	// 	int SIZE = 3;
	// 	float[] lowPassFilter = new float[] {
	// 		1/9f,	1/9f,	1/9f,
	// 		1/9f,	1/9f,	1/9f,
	// 		1/9f,	1/9f,	1/9f
	// 	};
    //     Kernel kernel = new Kernel(SIZE, SIZE, lowPassFilter);

    //     // Applying convolution using the kernel
    //     ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    //     BufferedImage antiAliasedImage = new BufferedImage(image.getsize(), image.getsize(), image.getType());
    //     op.filter(image, antiAliasedImage);

	// 	return antiAliasedImage;
	// }

	// public Image old_getScaledImage(BufferedImage image, double scaleFactor, boolean antiAlias) {
	// 	if (antiAlias) {
	// 		image = getAntiAliasedImage(image);
	// 	}
	// 	int SIZE = (int) (image.getsize() * scaleFactor);
    //     int SIZE = (int) (image.getsize() * scaleFactor);
    //     Image scaledImage = image.getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH);
	// 	return scaledImage;
	// }

	public BufferedImage getAntiAliasedImage(BufferedImage image) {
		BufferedImage antiAliasedImage = new BufferedImage(SIZE, SIZE, image.getType());
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				int sumR = 0;
				int sumG = 0;
				int sumB = 0;
				int count = 0;
				for (int i = x - 1; i <= x + 1; i++) {
					for (int j = y - 1; j <= y + 1; j++) {
						if (i >= 0 && i < SIZE && j >= 0 && j < SIZE) {
							Color color = new Color(image.getRGB(i, j));
							sumR += color.getRed();
							sumG += color.getGreen();
							sumB += color.getBlue();
							count++;
						}
					}
				}
				int avgR = sumR / count;
				int avgG = sumG / count;
				int avgB = sumB / count;
				Color avgColor = new Color(avgR, avgG, avgB);
				antiAliasedImage.setRGB(x, y, avgColor.getRGB());
			}
		}
		return antiAliasedImage;
	}

	public BufferedImage getScaledImage(BufferedImage image, double scaleFactor, boolean antiAlias) {
        int scaledsize = (int) (SIZE * scaleFactor);

		if (antiAlias) {
			image = getAntiAliasedImage(image);
		}

        BufferedImage scaledImage = new BufferedImage(scaledsize, scaledsize, image.getType());
        int[] originalPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int[] resizedPixels = ((DataBufferInt) scaledImage.getRaster().getDataBuffer()).getData();

        for (int y = 0; y < scaledsize; y++) {
            for (int x = 0; x < scaledsize; x++) {
				// Calculate the original pixel index
                int originalPixelIndex = (int) (y / scaleFactor) * SIZE + (int) (x / scaleFactor);
                resizedPixels[y * scaledsize + x] = originalPixels[originalPixelIndex];
            }
        }
        return scaledImage;
    }

	public void showIms(String[] args){

		// Read parameters from command line
		int n = Integer.parseInt(args[0]); // number of radial lines
		System.out.println("n: " + n);

		double scaleFactor = Double.parseDouble(args[1]);  // the scale factor
		System.out.println("scaleFactor: " + scaleFactor);

		boolean antiAlias = "1".equals(args[2]); // anti-aliasing required or not (0 or 1)
		System.out.println("antiAlias: " + antiAlias);

		mainImg = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB); // initialise the main image
		initBackgroundImage(mainImg); // Set white background
		drawRadialLines(mainImg, n); // Draw the radial lines
		
		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);

		lbIm1 = new JLabel(new ImageIcon(mainImg));
		lbIm2 = new JLabel(new ImageIcon(getScaledImage(mainImg, scaleFactor, antiAlias))); // Get the scaled and/or anti-aliased image

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

	public static void main(String[] args) {
		Mypart1 ren = new Mypart1();
		ren.showIms(args);
	}

}