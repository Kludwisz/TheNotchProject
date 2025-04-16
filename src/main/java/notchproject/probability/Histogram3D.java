package notchproject.probability;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Histogram3D {
    private final long sampleSize;
    private final int[][][] data;

    public Histogram3D(int xSize, int ySize, int zSize, long sampleSize) {
        if (xSize <= 0 || ySize <= 0 || zSize <= 0) {
            throw new IllegalArgumentException("Histogram dimensions must be positive.");
        }
        this.data = new int[xSize][ySize][zSize];
        this.sampleSize = sampleSize;
    }

    public void addPoint(int x, int y, int z) {
        if (x < 0 || x >= data.length || y < 0 || y >= data[0].length || z < 0 || z >= data[0][0].length) {
            System.err.println("Histogram3D: Point out of bounds: (" + x + ", " + y + ", " + z + ") ignored.");
            return;
        }
        data[x][y][z]++;
    }

    public double getProbabilityAt(int x, int y, int z) {
        if (x < 0 || x >= data.length || y < 0 || y >= data[0].length || z < 0 || z >= data[0][0].length) {
            //System.err.println("Histogram3D: Point out of bounds: (" + x + ", " + y + ", " + z + ") ignored.");
            return 0.0;
        }
        return (double) data[x][y][z] / sampleSize;
    }

    public void saveToFile(String filename) {
        try (FileWriter fout = new FileWriter(filename)) {
            fout.write(sampleSize + "\n");
            fout.write(data.length + " " + data[0].length + " " + data[0][0].length + "\n");
            for (int y = 0; y < data[0].length; y++) {
                for (int x = 0; x < data.length; x++) {
                    for (int z = 0; z < data[0][0].length; z++) {
                        fout.write(data[x][y][z] + " ");
                    }
                    fout.write("\n");
                }
                fout.write("\n");
            }
        }
        catch (IOException ex) {
            System.err.println("Error while writing histogram to file: " + ex.getMessage());
        }
    }

    public static Histogram3D loadFromFile(String filename) {
        try (Scanner fin = new Scanner(new File(filename))) {
            long sampleSize = fin.nextLong();
            int xSize = fin.nextInt();
            int ySize = fin.nextInt();
            int zSize = fin.nextInt();
            Histogram3D histogram = new Histogram3D(xSize, ySize, zSize, sampleSize);
            for (int y = 0; y < ySize; y++) {
                for (int x = 0; x < xSize; x++) {
                    for (int z = 0; z < zSize; z++) {
                        histogram.data[x][y][z] = fin.nextInt();
                    }
                }
            }
            System.out.printf("Loaded %s : %d samples, %d x %d x %d\n", filename, sampleSize, xSize, ySize, zSize);
            return histogram;
        }
        catch (IOException ex) {
            System.err.println("Error while writing histogram to file: " + ex.getMessage());
        }
        return null;
    }
}
