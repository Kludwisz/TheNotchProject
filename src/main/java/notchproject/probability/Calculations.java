package notchproject.probability;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcseed.rand.JRand;
import notchproject.positions.AncientCityChestPositions;
import notchproject.positions.HopperPositions;
import notchproject.positions.MinecartPositions;

import java.util.List;

public class Calculations {
    public static void main(String[] args) {
        //createMineshaftHistogram(10_000_000L);
        //createAncientCityHistogram(10_000_000L);
        //createTrialChambersHistogram(4_000_000L);

        Histogram3D msh = Histogram3D.loadFromFile("src/main/resources/mineshaft_histogram_10M.txt");
        Histogram3D ach = Histogram3D.loadFromFile("src/main/resources/ancient_city_histogram_10M.txt");
        Histogram3D tch = Histogram3D.loadFromFile("src/main/resources/trial_chambers_histogram_4M.txt");
        calculateCumulativeProbability(msh, ach, tch);
    }

    public static void calculateCumulativeProbability(Histogram3D msh, Histogram3D ach, Histogram3D tch) {
        double expectedValue = 0.0D;

        for (int dy = 2; dy <= 3; dy++) {
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        double prob = 1.0D;
                        prob *= ach.getProbabilityAt(x, y, z);
                        prob *= tch.getProbabilityAt(x, y+1, z);
                        prob *= msh.getProbabilityAt(x, y+dy, z);
                        expectedValue += prob;
                    }
                }
            }
        }

        System.out.printf("EX = %.4E\n", expectedValue);
        // use poisson distribution to approximate probability
        double prob = 1 - Math.exp(-expectedValue);
        System.out.printf("P(X > 0) = %.4E\n", prob);
        double expectedSeedsToCheck = 1 / prob;
        System.out.printf("Expected seeds to check = %.4E\n", expectedSeedsToCheck);
        // minecart chest spawns & has a notch apple = 1 in 7100
        double totalWorldseeds = expectedSeedsToCheck * 7100.0D;
        System.out.printf("Expected total worldseeds needed = %.4E\n", totalWorldseeds);
        // each population seed yields around 3_750_000 ^ 2 worldseeds
        double popSeedYield = 3_750_000D * 3_750_000D;
        double totalPopulationSeeds = totalWorldseeds / popSeedYield;
        System.out.printf("Expected total population seeds needed = %.4E\n", totalPopulationSeeds);
    }

    private static void createMineshaftHistogram(long sampleSize) {
        long step = sampleSize / 100;
        Histogram3D histogram = new Histogram3D(16, 128, 16, sampleSize);

        JRand rand = new JRand(0L);
        for (long i = 0; i < sampleSize; i++) {
            if (i % step == 0)
                System.out.println("--- Progress: " + (i / step) + "%");

            CPos chunk = getRandomChunk(rand);
            updateHistogram(MinecartPositions.getInChunk(rand.nextLong(), chunk), histogram);
        }

        histogram.saveToFile("src/main/resources/mineshaft_histogram.txt");
    }

    private static void createAncientCityHistogram(long sampleSize) {
        long step = sampleSize / 100;
        Histogram3D histogram = new Histogram3D(16, 128, 16, sampleSize);

        JRand rand = new JRand(0L);
        for (long i = 0; i < sampleSize; i++) {
            if (i % step == 0)
                System.out.println("--- Progress: " + (i / step) + "%");

            CPos chunk = getRandomChunk(rand);
            updateHistogram(AncientCityChestPositions.getInChunk(rand.nextLong(), chunk), histogram);
        }

        histogram.saveToFile("src/main/resources/ancient_city_histogram_10M.txt");
    }

    private static void createTrialChambersHistogram(long sampleSize) {
        long step = sampleSize / 100;
        Histogram3D histogram = new Histogram3D(16, 128, 16, sampleSize);

        JRand rand = new JRand(0L);
        for (long i = 0; i < sampleSize; i++) {
            if (i % step == 0)
                System.out.println("--- Progress: " + (i / step) + "%");

            CPos chunk = getRandomChunk(rand);
            updateHistogram(HopperPositions.getInChunk(rand.nextLong(), chunk), histogram);
        }

        histogram.saveToFile("src/main/resources/trial_chambers_histogram_4M.txt");
    }

    private static void updateHistogram(List<BPos> positions, Histogram3D histogram) {
        positions.stream()
                .map(pos -> new BPos(pos.getX() & 15, pos.getY() + 64, pos.getZ() & 15))
                .forEach(pos -> histogram.addPoint(pos.getX(), pos.getY(), pos.getZ()));
    }

    private static CPos getRandomChunk(JRand rand) {
        final int C = 3_750_000;
        double qx = rand.nextFloat();
        double qz = rand.nextFloat();
        int chunkX = (int)Math.round(qx * C) - C/2;
        int chunkZ = (int)Math.round(qz * C) - C/2;
        return new CPos(chunkX, chunkZ);
    }
}
