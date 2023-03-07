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

public class MyExtraCredit extends JFrame {

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
		double[] values;
		int cluster; // The cluster that the point belongs to

		Point(double[] values) {
			this.values = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				this.values[i] = values[i];
			}
			this.cluster = -1;
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
		int i = 0;
		for (Point p : points) {
			this.findClusterForPoint(p);
		}
	}

	public void updateClusters() {
		int iter = 100;
		while (true) {
			boolean changed = false;
			for (Point cluster : clusters) {
				double[] clusterValues = new double[m * m];
				int count = 0;
				for (Point p : points) {
					if (p.cluster == cluster.cluster) {
						for (int i = 0; i < p.values.length; i++) {
							clusterValues[i] += p.values[i];
						}
						count++;
					}
				}
				for (int i = 0; i < clusterValues.length; i++) {
					clusterValues[i] /= count;
				}

				Point newCentroid = new Point(clusterValues);
				if (euclideanDistance(newCentroid, cluster) > 1e-6) {
					for (int i = 0; i < cluster.values.length; i++) {
						cluster.values[i] = newCentroid.values[i];
						changed = true;
					}
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

		// Create clusterMap to reference cluster_id to cluster point
		for (Point cluster : clusters) {
			clusterMap.put(cluster.cluster, cluster);
		}

		// System.out.println("############ FINAL CLUSTERS ###############");
		// for (Point cluster : clusters) {
		// 	System.out.print("(");
		// 	for (int i = 0; i < cluster.values.length; i++) {
		// 		System.out.print(cluster.values[i] + ", ");
		// 	}
		// 	System.out.print(") -> Cluster " + cluster.cluster);
		// 	System.out.println();
		// }
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

		// System.out.println("############ INITIAL CLUSTERS ###############");
		// for (Point cluster : clusters) {
		// 	System.out.print("(");
		// 	for (int i = 0; i < cluster.values.length; i++) {
		// 		System.out.print(cluster.values[i] + ", ");
		// 	}
		// 	System.out.print(") -> Cluster " + cluster.cluster);
		// 	System.out.println();
		// }
		// System.exit(0);
	}

	public Point calculateMean(List<Point> points) {
		double[] newValues = new double[m*m];
		for (Point point : points) {
			for (int i = 0; i < m*m; i++) {
				newValues[i] += point.values[i];
			}
		}
		for (int i = 0; i < m*m; i++) {
			newValues[i] = newValues[i] / points.size();
		}
		return new Point(newValues);
	}

	public double euclideanDistance(Point p1, Point p2) {
		double distance = 0;
		for (int i = 0; i < m*m; i++) {
			distance += Math.pow(p1.values[i] - p2.values[i], 2);
		}
		return Math.sqrt(distance);
	}

	public MyExtraCredit(String filename, int m, int n) {
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

			for (int row = 0; row < height; row += m) {
				for (int col = 0; col < width; col += m) {
					double[] values = new double[m*m];
					int index = 0;
					for (int i = 0; i < m; i++) {
						for (int j = 0; j < m; j++) {
							int position = (row * width) + col + i + (j * width);
							values[index++] = data[position] & 0xFF;
							// System.out.print(position + ", ");
						}
					}
					// System.out.println();
					Point point = new Point(values);
					points.add(point);
				}
			}

			// Close the FileInputStream
			fileInputStream.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public int[] createOutputArray(byte[] inputByteArray) {
		int[] outputArray = new int[byteArraySize];
		int index;

		for (int row = 0; row < height; row += m) {
			for (int col = 0; col < width; col += m) {
				double[] values = new double[m*m];
				index = 0;
				for (int i = 0; i < m; i++) {
					for (int j = 0; j < m; j++) {
						int position = (row * width) + col + i + (j * width);
						values[index++] = inputByteArray[position] & 0xFF;
					}
				}

				Point newPoint = new Point(values);
				findClusterForPoint(newPoint);
				Point cluster = clusterMap.get(newPoint.cluster);

				// for (int i = 0; i < m*m; i++) {
					// System.out.print(values[i] + " ");
				// }
				// System.out.print(" -> " + cluster.cluster);
				// System.out.print(String.format("(%s, %s, %s, %s)", cluster.values[0], cluster.values[1], cluster.values[2], cluster.values[3]));
				
				// System.out.println();

				// Map cluster's notation back to the output image
				index = 0;
				for (int i = 0; i < m; i++) {
					for (int j = 0; j < m; j++) {
						int position = (row * width) + col + i + (j * width);
						outputArray[position] = (int) cluster.values[index++];
					}
				}
				// System.out.println();
			}
		}

		// for (int i = 0; i < 100; i++) {
		// 	System.out.print(outputArray[i] + " ");
		// }
		return outputArray;
	}

	public static void main(String[] args) {

		// Read parameters from command line
		String filename = args[0];
		System.out.println("filename: " + filename);

		int m = Integer.parseInt(args[1]);
		System.out.println("m: " + m);
		m = (int) Math.sqrt(m);
		System.out.println("m (using): " + m);

		int n = Integer.parseInt(args[2]);
		System.out.println("n: " + n);

		MyExtraCredit obj = new MyExtraCredit(filename, m, n);

		byte[] inputByteArray = new byte[byteArraySize];

		try {
			// Open the .rgb file as a FileInputStream
			FileInputStream fileInputStream = new FileInputStream(filename);

			// Read the contents of the file into a byte array
			inputByteArray = fileInputStream.readAllBytes();
			fileInputStream.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		obj.createVectorSpace(); // Initialises the list of points
		obj.initializeClusters(); // Initialise the clusters
		obj.assignClusters();
		obj.updateClusters(); // Re-assign cluster centroids

		Map<Integer, Integer> clusterCount = new HashMap<>();
		for (Point p : obj.points) {
			clusterCount.put(p.cluster, clusterCount.getOrDefault(p.cluster, 0) + 1);
		}

		// obj.clusterMap.forEach((key, value) -> System.out.println("Cluster: " + key +
		// " " + value.x + " " + value.y));
		// clusterCount.forEach((key, value) -> System.out.println("CLuster: " + key +
		// ", Count: " + value));
		int[] outputArray = obj.createOutputArray(inputByteArray);

		// for (int i = 0; i < width; i++) {
		// 	System.out.print(outputArray[i] + " ");
		// }
		// System.out.println();
		// for (int i = 0; i < width; i++) {
		// System.out.print((inputByteArray[i] & 0xFF) + " ");
		// }
		// System.out.println();
		// for (int i = 0; i < width; i++) {
		// System.out.print((int) obj.points.get(i).x + " " + (int) obj.points.get(i).y
		// + " ");
		// }

		obj.displayImage(inputByteArray, outputArray);
	}
}
