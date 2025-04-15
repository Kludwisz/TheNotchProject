import com.seedfinding.mccore.util.pos.CPos;
import randomreversal.Candidate;
import randomreversal.PopulationSeedFinder;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        long seedMin = 0L;
        long seedMax = 0L;
        int threads = 1;
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
            else if (args[i].equals("--threads")) {
                if (i + 1 >= args.length)
                    throw new IllegalArgumentException("Missing value for --threads argument");
                threads = Integer.parseInt(args[i + 1]);
            }
            else {
                System.err.println("Unknown argument: " + args[i]);
            }
        }

        if (threads <= 1)
            processSeedRange(seedMin, seedMax);
        else
            processSeedRange(seedMin, seedMax, threads);
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

    private static void processSeedRange(long seedMin, long seedMax, int threads) {
        long jobSize = (seedMax - seedMin) / threads;
        Object lock = new Object();

        Thread[] threadPool = new Thread[threads];
        for (int tid = 0; tid < threads; tid++) {
            final long start = seedMin + jobSize * tid;
            final long end = (tid == threads - 1) ? seedMax : start + jobSize;

            threadPool[tid] = new Thread(() -> {
                for (long seed = start; seed < end; seed++) {
                    List<Candidate> candidates = PopulationSeedFinder.getCandidatesForSeed(seed);
                    if (candidates == null) continue;

                    for (Candidate candidate : candidates) {
                        synchronized (lock) { // ChestArrangementChecker.testSeed uses non-parallel code
                            if (ChestArrangementChecker.testSeed(seed, new CPos(candidate.chunkX(), candidate.chunkZ()))) {
                                System.out.println(seed + " " + candidate.chunkX() + " " + candidate.chunkZ());
                            }
                        }
                    }
                }
            });
            threadPool[tid].start();
        }

        // wait for all threads to finish
        for (Thread thread : threadPool) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }
    }
}
