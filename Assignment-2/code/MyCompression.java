import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;

public class MyCompression extends JFrame {

	private BufferedImage image;
	private JFrame frame;
	private JLabel lbIm1;
	private JLabel lbIm2;
	private final static int width = 352;
	private final static int height = 288;
	private final static int byteArraySize = width * height;

	private String filename;
	private int m;
	private int n;

	public List<Point> points;
	public List<Point> clusters;

	public Map<Integer, Point> clusterMap;

	static class Point {
		double x, y;
		int cluster; // The cluster that the point belongs to

		Point(double x, double y) {
			this.x = x;
			this.y = y;
			this.cluster = -1;
		}
	}

	static class ScatterPlot extends JFrame {

		public ScatterPlot(int[] xValues, int[] yValues) {
			// Create a new JPanel to draw the scatter plot
			JPanel panel = new JPanel() {
				public void paintComponent(Graphics g) {
					super.paintComponent(g);

					// Set up the coordinate system
					Graphics2D g2d = (Graphics2D) g;
					int xMargin = 50;
					int yMargin = 50;

					int width = getWidth() - 2 * xMargin;
					int height = getHeight() - 2 * yMargin;
					g2d.translate(xMargin, getHeight() - yMargin);
					g2d.scale(1.0, -1.0);

					// Draw the x and y axes
					g2d.setColor(Color.BLACK);
					g2d.drawLine(0, 0, width, 0);
					g2d.drawLine(0, 0, 0, height);

					// Draw the data points
					g2d.setColor(Color.RED);
					for (int i = 0; i < xValues.length; i++) {
						int x = xValues[i];
						int y = yValues[i];
						g2d.fillOval(x * width / 5 - 5, y * height / 5 - 5, 10, 10);
					}
				}
			};

			// Add the JPanel to the JFrame and display the window
			this.getContentPane().add(panel);
			this.setSize(500, 300);
			this.setVisible(true);
		}
	}

	public void findClusterForPoint(Point p) {
		double minDistance = Double.MAX_VALUE;
		for (Point cluster : clusters) {
			double distance = euclideanDistance(p, cluster);
			if (distance < minDistance) {
				minDistance = distance;
				p.cluster = cluster.cluster;
			}
		}
	}

	public void assignClusters() {
		// Assign each point to the nearest cluster
		for (Point p : points) {
			this.findClusterForPoint(p);
		}
	}

	public void updateClusters() {
		int iter = 100;
		while (true) {
			boolean changed = false;
			for (Point cluster : clusters) {
				double sumX = 0, sumY = 0, count = 0;
				for (Point p : points) {
					if (p.cluster == cluster.cluster) {
						sumX += p.x;
						sumY += p.y;
						count++;
					}
				}
				double newX = sumX / count;
				double newY = sumY / count;
				Point newCentroid = new Point(newX, newY);
				if (euclideanDistance(newCentroid, cluster) > 1e-6) {
					cluster.x = newCentroid.x;
					cluster.y = newCentroid.y;
					changed = true;
				}
			}
			if (!changed || --iter == 0) {
				break;
			}
			// Reassign each point to the nearest cluster
			for (Point p : points) {
				double minDistance = Double.MAX_VALUE;
				for (Point cluster : clusters) {
					if (p.cluster != cluster.cluster) {
						double distance = euclideanDistance(p, cluster);
						if (distance < minDistance) {
							minDistance = distance;
							p.cluster = cluster.cluster;
						}
					}
				}
			}
		}
		// System.out.println("############ CLUSTERS ###############");
		for (Point cluster : clusters) {
			clusterMap.put(cluster.cluster, cluster);
			// System.out.println("(" + cluster.x + ", " + cluster.y + ") -> Cluster " + cluster.cluster);
		}
	}

	public void initializeClusters() {
		// Shuffle the list of points
		Collections.shuffle(points);

		// Partition the shuffled list into `n` roughly equal parts
		int partitionSize = points.size() / n;
		int startIndex = 0;
		for (int i = 0; i < n; i++) {
			int endIndex = startIndex + partitionSize;
			if (i == n - 1) {
				// Last partition gets the remaining points
				endIndex = points.size();
			}
			List<Point> partition = points.subList(startIndex, endIndex);
			Point centroid = calculateMean(partition);
			centroid.cluster = i;
			clusters.add(centroid);
			startIndex = endIndex;
		}
	}

	public Point calculateMean(List<Point> points) {
		double sumX = 0;
		double sumY = 0;
		for (Point point : points) {
			sumX += point.x;
			sumY += point.y;
		}
		double meanX = sumX / points.size();
		double meanY = sumY / points.size();
		return new Point(meanX, meanY);
	}

	public double euclideanDistance(Point p1, Point p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public MyCompression(String filename, int m, int n) {
		this.filename = filename;
		this.m = m;
		this.n = n;
		this.points = new ArrayList<>();
		this.clusters = new ArrayList<>();
		this.clusterMap = new HashMap<>();
	}

	public BufferedImage loadImageFromArray(byte[] array) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// Copy the data from the byte array into the BufferedImage
		for (int i = 0; i < array.length; i++) {
			int pixel = array[i] & 0xFF;
			int rgb = (pixel << 16) | (pixel << 8) | pixel;
			int x = i % width;
			int y = i / width;
			image.setRGB(x, y, rgb);
		}
		return image;
	}

	public BufferedImage loadImageFromArray(int[] array) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// Copy the data from the byte array into the BufferedImage
		for (int i = 0; i < array.length; i++) {
			int pixel = array[i];
			int rgb = (pixel << 16) | (pixel << 8) | pixel;
			int x = i % width;
			int y = i / width;
			image.setRGB(x, y, rgb);
		}
		return image;
	}

	public void displayImage(byte[] inputArray, int[] outputArray) {
		// Create a new BufferedImage with the correct width, height, and type
		BufferedImage inputImage = loadImageFromArray(inputArray);
		BufferedImage outputImage = loadImageFromArray(outputArray);

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after compression (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(inputImage));
		lbIm2 = new JLabel(new ImageIcon(outputImage));

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

	public void createVectorSpace() {
		try {
			// Open the .rgb file as a FileInputStream
			FileInputStream fileInputStream = new FileInputStream(filename);

			// Read the contents of the file into a byte array
			byte[] data = fileInputStream.readAllBytes();

			int[] xValues = new int[data.length / 2];
			int[] yValues = new int[data.length / 2];

			for (int i = 0; i < data.length; i = i + 2) {
				Point point = new Point(data[i] & 0xFF, data[i + 1] & 0xFF);
				points.add(point);
			}

			// Close the FileInputStream
			fileInputStream.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public int[] createOutputArray(byte[] inputByteArray) {
		int[] outputArray = new int[byteArraySize];
		for (int i = 0; i < inputByteArray.length; i += 2) {
			int x = inputByteArray[i] & 0xFF;
			int y = inputByteArray[i+1] & 0xFF;
			Point newPoint = new Point(x, y);
			findClusterForPoint(newPoint);
			Point cluster = clusterMap.get(newPoint.cluster);

			outputArray[i] = (int) cluster.x;
			outputArray[i+1] = (int) cluster.y;
		}

		return outputArray;
	}

	public static void main(String[] args) {

		// Read parameters from command line
		String filename = args[0];
		System.out.println("filename: " + filename);

		int m = Integer.parseInt(args[1]);
		System.out.println("m: " + m);

		int n = Integer.parseInt(args[2]);
		System.out.println("n: " + n);

		MyCompression obj = new MyCompression(filename, m, n);

		byte[] inputByteArray = new byte[byteArraySize];

		try {
			// Open the .rgb file as a FileInputStream
			FileInputStream fileInputStream = new FileInputStream(filename);

			// Read the contents of the file into a byte array
			inputByteArray = fileInputStream.readAllBytes();
			fileInputStream.close();
		} catch(Exception e) {
			System.out.println(e);
		}

		obj.createVectorSpace(); // Initialises the list of points
		obj.initializeClusters(); // Initialise the clusters
		obj.assignClusters();
		obj.updateClusters(); // Re-assign cluster centroids
		
		Map<Integer, Integer> clusterCount = new HashMap<>();
		for(Point p : obj.points) {
			clusterCount.put(p.cluster, clusterCount.getOrDefault(p.cluster, 0) + 1);
		}

		int[] outputArray = obj.createOutputArray(inputByteArray);

		obj.displayImage(inputByteArray, outputArray);
	}
}
