package notchproject;

import com.seedfinding.mccore.util.pos.CPos;
import notchproject.randomreversal.PopulationSeedFinder;
import notchproject.randomreversal.XRand;

public class Main {
    private static final long MASK_60 = (1L << 60) - 1L;
    private static final long TARGET_POPULATION_SEED = 0L; // TODO run cuda kernel to find this
    private static final long TARGET_LOWER_NIBBLE = TARGET_POPULATION_SEED & 15L;

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
        seedMin &= 0xFFFF_FFFF_FFFF_FFF0L;
        seedMin |= TARGET_LOWER_NIBBLE;
        seedMax &= 0xFFFF_FFFF_FFFF_FFF0L;
        seedMax |= TARGET_LOWER_NIBBLE;
        XRand rand = new XRand();

        for (long worldseed = seedMin; worldseed <= seedMax; worldseed += 16) {
            CPos candidate = getCandidateForSeed(worldseed, rand);
            if (candidate == null) continue;
            if (ChestArrangementChecker.testSeed(worldseed, new CPos(candidate.getX(), candidate.getZ()))) {
                System.out.println(worldseed + " " + candidate.getX() + " " + candidate.getZ());
            }
        }
    }

    private static void processSeedRange(long seedMin, long seedMax, int threads) {
        long jobSize = (seedMax - seedMin) / threads;
        Object lock = new Object();

        Thread[] threadPool = new Thread[threads];
        for (int tid = 0; tid < threads; tid++) {
            final long start = ((seedMin + jobSize * tid) & 0xFFFF_FFFF_FFFF_FFF0L) | TARGET_LOWER_NIBBLE;
            final long end = ((seedMin + jobSize * (tid + 1)) & 0xFFFF_FFFF_FFFF_FFF0L) | TARGET_LOWER_NIBBLE;
            XRand rand = new XRand();

            threadPool[tid] = new Thread(() -> {
                for (long worldseed = start; worldseed < end; worldseed++) {
                    CPos candidate = getCandidateForSeed(worldseed, rand);
                    if (candidate == null) continue;

                    synchronized (lock) { // notchproject.ChestArrangementChecker.testSeed uses non-parallel code
                        if (ChestArrangementChecker.testSeed(worldseed, new CPos(candidate.getX(), candidate.getZ()))) {
                            System.out.println(worldseed + " " + candidate.getX() + " " + candidate.getZ());
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

    public static CPos getCandidateForSeed(long worldseed, XRand rand) {
        final long target = (worldseed ^ TARGET_POPULATION_SEED) >>> 4;
        rand.setSeed(worldseed);
        long a = (rand.nextLong() | 1L) & MASK_60;
        long b = (rand.nextLong() | 1L) & MASK_60;
        return PopulationSeedFinder.findSolutionInBox(a, b, target);
    }
}
