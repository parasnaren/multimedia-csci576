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
	BufferedImage img;
	int width = 512;
	int height = 512;

	// Draws a black line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawLine(BufferedImage image, int x1, int y1, int x2, int y2) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.drawLine(x1, y1, x2, y2);
		g.drawImage(image, 0, 0, null);
	}

	public void drawRadialLines(BufferedImage image, int n) {
		int x1 = width / 2; int y1 = height / 2;
        int x2, y2;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            x2 = x1 + (int) (width / 2 * Math.cos(angle));
            y2 = y1 + (int) (height / 2 * Math.sin(angle));
            drawLine(img, x1, y1, x2, y2);
        }
	}

	public BufferedImage old_getAntiAliasedImage(BufferedImage image) {
		/*
		 * Using a custom low pass filter to anti-alias the image
		 * [
				1/9f,	1/9f,	1/9f,
				1/9f,	1/9f,	1/9f,
				1/9f,	1/9f,	1/9f
			]
		*/
		int size = 3;
		float[] lowPassFilter = new float[] {
			1/9f,	1/9f,	1/9f,
			1/9f,	1/9f,	1/9f,
			1/9f,	1/9f,	1/9f
		};
        Kernel kernel = new Kernel(size, size, lowPassFilter);

        // Applying convolution using the kernel
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage antiAliasedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        op.filter(image, antiAliasedImage);

		return antiAliasedImage;
	}

	public BufferedImage getAntiAliasedImage(BufferedImage image) {
		BufferedImage antiAliasedImage = new BufferedImage(width, height, image.getType());
        int[] originalPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int[] antiAliasedPixels = ((DataBufferInt) antiAliasedImage.getRaster().getDataBuffer()).getData();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int sumR = 0;
				int sumG = 0;
				int sumB = 0;
				int count = 0;
				for (int i = x - 1; i <= x + 1; i++) {
					for (int j = y - 1; j <= y + 1; j++) {
						if (i >= 0 && i < width && j >= 0 && j < height) {
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
        int scaledWidth = (int) (width * scaleFactor);
        int scaledHeight = (int) (height * scaleFactor);

		if (antiAlias) {
			image = getAntiAliasedImage(image);
		}

        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());
        int[] originalPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int[] resizedPixels = ((DataBufferInt) scaledImage.getRaster().getDataBuffer()).getData();

        for (int y = 0; y < scaledHeight; y++) {
            for (int x = 0; x < scaledWidth; x++) {
				// Calculate the original pixel index
                int originalPixelIndex = (int) (y / scaleFactor) * width + (int) (x / scaleFactor);
                resizedPixels[y * scaledWidth + x] = originalPixels[originalPixelIndex];
            }
        }
        return scaledImage;
    }

	public Image old_getScaledImage(BufferedImage image, double scaleFactor, boolean antiAlias) {
		if (antiAlias) {
			image = getAntiAliasedImage(image);
		}
		int width = (int) (image.getWidth() * scaleFactor);
        int height = (int) (image.getHeight() * scaleFactor);
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
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

		// Initialize a plain white image
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int ind = 0;
		for(int y = 0; y < height; y++) {

			for(int x = 0; x < width; x++) {

				// byte a = (byte) 255;
				byte r = (byte) 255;
				byte g = (byte) 255;
				byte b = (byte) 255;

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x, y, pix);
				ind++;
			}
		}
		
		drawLine(img, 0, 0, width-1, 0);				// top edge
		drawLine(img, 0, 0, 0, height-1);				// left edge
		drawLine(img, 0, height-1, width-1, height-1);	// bottom edge
		drawLine(img, width-1, height-1, width-1, 0); 	// right edge
		
		// Draw the radially outward lines (filling up a circle)
		// TODO: Extend lines to the edges
		drawRadialLines(img, n);
		
		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);

		lbIm1 = new JLabel(new ImageIcon(img));
		lbIm2 = new JLabel(new ImageIcon(getScaledImage(img, scaleFactor, antiAlias))); // Get the scaled image

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
	}

	public static void main(String[] args) {
		Mypart1 ren = new Mypart1();
		ren.showIms(args);
	}

}