package notchproject.randomreversal;

import com.seedfinding.mccore.util.pos.CPos;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRandomReversal {
    private static final long testPopseed = 18789082044254L;

    @Test
    public void testReverser() throws FileNotFoundException {
        Scanner reader = new Scanner(new File("src/test/resources/out.txt"));
        ArrayList<Result> expectedResults = new ArrayList<>();
        while (reader.hasNextLong()) {
            Result er = new Result(reader.nextLong(), reader.nextInt(), reader.nextInt());
            expectedResults.add(er);
        }
        reader.close();

        int ourResults = 0;
        XRand rand = new XRand();
        for (long worldseed = testPopseed & 15; worldseed <= 146374238L /*the last result from the original code*/; worldseed += 16) {
            CPos candidate = getCandidateForSeed(worldseed, rand);
            if (candidate == null) continue;
            Result ourResult = new Result(worldseed, candidate.getX(), candidate.getZ());
            if (!expectedResults.contains(ourResult))
                throw new AssertionError("Got bad result: " + ourResult);
            ourResults++;
            if (ourResults >= 100)
                break;
        }

        assertEquals(ourResults, expectedResults.size());
    }

    private CPos getCandidateForSeed(long worldseed, XRand rand) {
        final long target = (worldseed ^ testPopseed) >>> 4;
        rand.setSeed(worldseed);
        long a = (rand.nextLong() | 1L) & ((1L << 60) - 1L);
        long b = (rand.nextLong() | 1L) & ((1L << 60) - 1L);
        return PopulationSeedFinder.findSolutionInBox(a, b, target);
    }

    public record Result(long seed, int cx, int cz) {}
}
