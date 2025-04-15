package notchproject.randomreversal;

import com.seedfinding.mccore.util.pos.CPos;
import notchproject.Main;
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
        for (long worldseed = testPopseed & 15; worldseed <= 146374238L; worldseed += 16) {
            CPos candidate = Main.getCandidateForSeed(worldseed, rand);
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

    public record Result(long seed, int cx, int cz) {}
}
