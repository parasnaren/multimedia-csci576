import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;


public class MyExtraCredit_2 {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage mainImg;
	BufferedImage previousImg; // image from the previous input frame
    BufferedImage nextImg; // image from the next input frame
	BufferedImage newImg;
    BufferedImage subImage;
	private int SIZE = 512;
	private int R = 256;
	private double angle;
	private boolean complete;
	private long previousDrawnTime;
	private long previousCapturedTime;
	private long currentTime;

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
	
    public BufferedImage performTemporalAntiAliasing(BufferedImage frame1, BufferedImage frame2, BufferedImage frame3) {
        frame1 = getAntiAliasedImage(frame1);
        frame2 = getAntiAliasedImage(frame2);
        frame3 = getAntiAliasedImage(frame3);
        BufferedImage result = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                int color1 = frame1.getRGB(x, y);
                int color2 = frame2.getRGB(x, y);
                int color3 = frame3.getRGB(x, y);
                int red = (new Color(color1).getRed() + new Color(color2).getRed() + new Color(color3).getRed()) / 3;
                int green = (new Color(color1).getGreen() + new Color(color2).getGreen() + new Color(color3).getGreen()) / 3;
                int blue = (new Color(color1).getBlue() + new Color(color2).getBlue() + new Color(color3).getBlue()) / 3;
                result.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
        g.dispose();
        return result;
	}

	public BufferedImage getScaledImage(BufferedImage image, double scaleFactor) {
        int scaledsize = (int) (SIZE * scaleFactor);

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

    public BufferedImage getCopyOfImage(BufferedImage originalImage) {
        BufferedImage subImage = originalImage.getSubimage(0, 0, SIZE, SIZE);
        BufferedImage newImage = new BufferedImage(subImage.getWidth(), subImage.getHeight(), subImage.getType());
        newImage.createGraphics().drawImage(subImage, 0, 0, null);
        return newImage;
    }

	public void showIms(String[] args) {
		// Read parameters from command line
		int n = Integer.parseInt(args[0]); // number of radial lines `n`
		System.out.println("n: " + n);

		double x = Double.parseDouble(args[1]);  // the number of revolutions per second `x`
		System.out.println("x: " + x);

		double fps = Double.parseDouble(args[2]); // fps of the output `fps`
		System.out.println("fps: " + fps);

        boolean antiAlias = "1".equals(args[3]); // anti-aliasing required or not (0 or 1)
		System.out.println("antiAlias: " + antiAlias);

		double scaleFactor = Double.parseDouble(args[4]);  // the scale factor
		System.out.println("scaleFactor: " + scaleFactor);

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
        double mainImgAngleOfRotation = 1000 / (360.0 * x / 60); // angle of rotation of main image
		long mainImgDelay = (long) (1000.0 / 30.0); // delay for generating frames on the main image (main image fps: 30)

		long rightImgDelay = (long) (1000 / fps); // delay for extracting frames to right side video

        // initialise the main image
		mainImg = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        int scaledsize = (int) (SIZE * scaleFactor);
        newImg = new BufferedImage(scaledsize, scaledsize, BufferedImage.TYPE_INT_RGB);

		Timer mainImgTimer = new Timer();
		Timer newImgTimer = new Timer();

		mainImgTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				currentTime = System.currentTimeMillis();
                System.out.println("Drawn frame every " + mainImgDelay + " ms " + "time: " + (currentTime - previousDrawnTime));
				previousDrawnTime = currentTime;

                // Syncronize this block of code using semaphore to mark image completion
                complete = false; 

                // Create the next image
                initBackgroundImage(nextImg);
                drawRadialLines(nextImg, n, angle);
                angle = (angle + mainImgAngleOfRotation) % 360;

                // Display the main image
				lbIm1 = new JLabel(new ImageIcon(mainImg));
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 1;
				frame.getContentPane().add(lbIm1, c);
				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                // Initialise previousImg to mainImg
                previousImg = getCopyOfImage(mainImg);

                // Initialise mainImg to nextImg
                mainImg = getCopyOfImage(nextImg);
                
                // End of syncronized block
				complete = true;
                
			}
		}, 0, mainImgDelay);

		newImgTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Graphics g = newImg.createGraphics();
                BufferedImage antiAliasedImage = getCopyOfImage(mainImg);
                if (antiAlias) {
                    antiAliasedImage = performTemporalAntiAliasing(nextImg, mainImg, previousImg);
                }
                BufferedImage scaledImage = getScaledImage(antiAliasedImage, scaleFactor);

                // Draw the captured frame
				g.drawImage(scaledImage, 0, 0, null);

				System.out.println("Captured frame every " + rightImgDelay + " ms " + "time: " + (currentTime - previousCapturedTime));
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
		}, 0, rightImgDelay);
	}

	public static void main(String[] args) {
		MyExtraCredit_2 ren = new MyExtraCredit_2();
		ren.showIms(args);
	}

}