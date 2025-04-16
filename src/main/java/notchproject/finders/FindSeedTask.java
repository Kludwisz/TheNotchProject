package notchproject.finders;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockMirror;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcmath.util.Mth;
import kludwisz.ancientcity.AncientCity;
import kludwisz.ancientcity.AncientCityGenerator;
import kludwisz.generator.TrialChambersGenerator;
import kludwisz.mineshafts.MineshaftLoot;
import kludwisz.structure.TrialChambers;

import java.util.concurrent.atomic.AtomicInteger;

public class FindSeedTask implements Runnable {
    private static final AtomicInteger counter = new AtomicInteger(0);

    private static final Object lock = new Object();
    private static final MCVersion VERSION = MCVersion.v1_21;
    private static final ChunkRand staticRand = new ChunkRand();
    private static final TrialChambersGenerator tcgen = new TrialChambersGenerator();
    private static final AncientCityGenerator acgen = new AncientCityGenerator();
    private static final MineshaftLoot mgen = new MineshaftLoot(VERSION);

    private final ChunkRand rand = new ChunkRand();
    private final ChunkRand rand2 = new ChunkRand();
    //private final AncientCity CITY = new AncientCity(VERSION);
    //private final TrialChambers TRIAL = new TrialChambers(VERSION);
    private final int AC_SALT = new AncientCity(VERSION).getSalt();
    private final int TC_SALT = new TrialChambers(VERSION).getSalt();

    private final long start;
    private final long end;

    public FindSeedTask(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        for (long seed = start; seed < end; seed++) {
            checkSeed(seed);
        }
        System.out.printf("Task #%d finished.\n", counter.incrementAndGet());
    }

    private void checkSeed(long seed) {
//        CPos ac = CITY.getInRegion(seed, 0, 0, rand);
//        CPos tc = TRIAL.getInRegion(seed, 0, 0, rand);
//        if (ac.distanceTo(tc, DistanceMetric.CHEBYSHEV) > 5 || ac.distanceTo(tc, DistanceMetric.MANHATTAN) <= 2)
//            return;

//        CPos ac = CITY.getInRegion(seed, 0, 0, rand);
//        CPos tc = TRIAL.getInRegion(seed, 0, 0, rand);
//        if (ac.getX() + 4 != tc.getX() || ac.getZ() + 4 != tc.getZ())
//            return;

        rand.setSeed(seed + AC_SALT);
        rand2.setSeed(seed + TC_SALT);
        int acx = rand.next(32) >>> 32-4; // nextInt(16)
        int tcx = rand2.next(31) % 22; // nextInt(22)
        if (acx + 4 != tcx) return;

        int acz = rand.next(32) >>> 32-4; // nextInt(16)
        int tcz = rand2.next(31) % 22; // nextInt(22)
        if (acz + 4 != tcz) return;

        final CPos ac = new CPos(acx, acz);
        final CPos tc = new CPos(tcx, tcz);

        BPos hopperPos = getHopperPos(seed, tc);
        if (hopperPos.getY() != -34)
            return;

        if (getRotation(seed, tc, true) != BlockRotation.NONE) return;
        if (getRotation(seed, ac, false) != BlockRotation.NONE) return;

        synchronized (lock) {
            acgen.generate(seed, ac.getX(), ac.getZ(), rand);

            for (int i = 0; i < acgen.piecesLen; i++) {
                var piece = acgen.pieces[i];
                final String name = piece.getName();
                if (name.equals("structures/tall_ruin_1")) {
                    Vec3i center = piece.box.getCenter();
                    if (center.getX() == hopperPos.getX() && center.getZ() == hopperPos.getZ())
                        if (checkTrialChambers(seed, tc))
                            checkMineshafts(seed, hopperPos);
                }
            }
        }
    }

    private BlockRotation getRotation(long seed, CPos ac, boolean isTrialChambers) {
        rand.setCarverSeed(seed, ac.getX(), ac.getZ(), MCVersion.v1_21);
        if (isTrialChambers) rand.nextInt(21); // skip y position
        return BlockRotation.getRandom(rand);
    }

    private BPos getHopperPos(long seed, CPos tc) {
        final BPos hopperNorth = new BPos(-2, 3, 45).transform(BlockMirror.NONE, BlockRotation.CLOCKWISE_180, BPos.ORIGIN);
        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
        int pickedY = rand.nextInt(21) - 41;
        BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
        return hopperNorth.transform(BlockMirror.NONE, startPieceRotation, BPos.ORIGIN).add(tc.toBlockPos(pickedY));
    }

    // ----------------------------------------------------------------------------

    private static boolean checkTrialChambers(long seed, CPos tc) {
        tcgen.generate(seed, tc.getX(), tc.getZ(), staticRand);
        for (int i = 0; i < tcgen.piecesLen; i++) {
            var piece = tcgen.pieces[i];
            final String name = piece.getName();
            if (name.contains("intersection_2"))
                return true;
        }
        return false;
    }

    private static void checkMineshafts(long seed, BPos hopperPos) {
        //System.out.println("Reached mineshaft check for seed = " + seed);
        CPos hopperChunk = hopperPos.toChunkPos();
        BlockBox hopperVerticalBox = new BlockBox(
                hopperPos.getX(), hopperPos.getY(), hopperPos.getZ(),
                hopperPos.getX(), hopperPos.getY()+4, hopperPos.getZ()
        );

        boolean haveCandidates = false;
        for (int dcx = -8; dcx <= 8; dcx++) {
            for (int dcz = -8; dcz <= 8; dcz++) {
                final CPos ms = new CPos(hopperChunk.getX() + dcx, hopperChunk.getZ() + dcz);
                if (!mgen.generateMineshaft(seed, ms, false))
                    continue;

                haveCandidates |= mgen.getCorridors().stream()
                        .filter(c -> c.boundingBox.intersects(hopperVerticalBox))
                        .flatMap(c -> c.getPossibleChestPositions().stream())
                        .filter(pos -> pos.equals(hopperPos.add(0, 1, 0)) || pos.equals(hopperPos.add(0, 2, 0)))
                        .anyMatch(pos -> true);
                if (haveCandidates)
                    break;
            }
        }

        if (haveCandidates) {
            SeedList sisterSeeds = new SeedList();
            long structureSeed = seed & Mth.MASK_48;
            //System.out.println(++counter);
            System.out.println("Good seed!!! " + structureSeed);
            sisterSeeds.addSisterSeedsOf(structureSeed, 50_000);
            sisterSeeds.appendToFile("src/main/resources/minecart_hopper_chest.txt");
        }
    }
}
