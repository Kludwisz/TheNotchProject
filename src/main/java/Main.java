import com.seedfinding.mccore.util.pos.CPos;
import randomreversal.Candidate;
import randomreversal.PopulationSeedFinder;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        long seedMin = 0L;
        long seedMax = 0L;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--start")) {
                if (i + 1 >= args.length)
                    throw new IllegalArgumentException("Missing value for --start argument");
                seedMin = Long.parseLong(args[i + 1]);
            }
            else if (args[i].equals("--end")) {
                if (i + 1 >= args.length)
                    throw new IllegalArgumentException("Missing value for --end argument");
                seedMax = Long.parseLong(args[i + 1]);
            }
            else {
                System.err.println("Unknown argument: " + args[i]);
            }
        }

        processSeedRange(seedMin, seedMax);
    }

    private static void processSeedRange(long seedMin, long seedMax) {
        for (long seed = seedMin; seed <= seedMax; seed++) {
            List<Candidate> candidates = PopulationSeedFinder.getCandidatesForSeed(seed);
            if (candidates == null) continue;

            for (Candidate candidate : candidates) {
                if (ChestArrangementChecker.testSeed(seed, new CPos(candidate.chunkX(), candidate.chunkZ()))) {
                    System.out.println(seed + " " + candidate.chunkX() + " " + candidate.chunkZ());
                }
            }
        }
    }
}
