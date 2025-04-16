package notchproject.finders;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleFinder {
    public static void main(String[] args) {
//        long seed1 = 123L;
//        CPos tc = new CPos(50,49);
//        BPos hopper = getHopperPos(seed1, tc);
//        System.out.println("Hopper pos: " + hopper);
//        checkTrialChambers(seed1, tc);

        final long total = 100_000_000_000L * 25;
        final long offset = 200_000_000_000L;
        final int taskCount = 4096;
        final long taskSize = total / taskCount + 1;
        final int threads = 8;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < taskCount; i++) {
            long start = offset + i * taskSize;
            long end = offset + Math.min(start + taskSize, total);
            executor.submit(new FindSeedTask(start, end));
        }
        executor.shutdown();

//        for (long seed = 0; seed < total; seed++) {
//            if (seed % step == 0)
//                System.out.println("% done: " + (seed*100.0D / total));
//            processSeed(seed);
//        }

        // ~ 150 in 1 000 000 pass mineshaft check
        // so we need to check ~7k seeds to get a good one
//        for (int x = 0; x < 1_000_000; x++) {
//            long seed = x + 123456789L;
//            checkMineshafts(seed, getHopperPos(seed, new CPos(x, 1)));
//        }
    }
}

