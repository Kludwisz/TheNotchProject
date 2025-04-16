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

public class SimpleFinder {
    public static void main(String[] args) {
        final long total = 2_000_000_000L;
        final long step = total / 100;

        for (long seed = 0; seed < total; seed++) {
            if (seed % step == 0)
                System.out.println("% done: " + (seed*100.0D / total));
            processSeed(seed);
        }
    }

    private static final ChunkRand rand = new ChunkRand();
    private static final MCVersion VERSION = MCVersion.v1_21;
    private static final AncientCity CITY = new AncientCity(VERSION);
    private static final TrialChambers TRIAL = new TrialChambers(VERSION);

    private static final TrialChambersGenerator tcgen = new TrialChambersGenerator();
    private static final AncientCityGenerator acgen = new AncientCityGenerator();
    private static final MineshaftLoot mgen = new MineshaftLoot(VERSION);

    private static void processSeed(long seed) {
//        CPos ac = CITY.getInRegion(seed, 0, 0, rand);
//        CPos tc = TRIAL.getInRegion(seed, 0, 0, rand);
//        if (ac.distanceTo(tc, DistanceMetric.CHEBYSHEV) > 5 || ac.distanceTo(tc, DistanceMetric.MANHATTAN) <= 2)
//            return;

        CPos ac = CITY.getInRegion(seed, 0, 0, rand);
        CPos tc = TRIAL.getInRegion(seed, 0, 0, rand);
        if (ac.getX() + 4 != tc.getX() || ac.getZ() + 4 != tc.getZ())
            return;

        BPos hopperPos = getHopperPos(seed, tc);
        if (hopperPos.getY() != -34)
            return;

        if (getRotation(seed, ac, false) != BlockRotation.NONE)
            return;

        acgen.generate(seed, ac.getX(), ac.getZ(), rand);

        for (int i = 0; i < acgen.piecesLen; i++) {
            var piece = acgen.pieces[i];
            final String name = piece.getName();
            if (name.equals("structures/tall_ruin_1")) {
                Vec3i center = piece.box.getCenter();
                if (center.getX() == hopperPos.getX() && center.getZ() == hopperPos.getZ()) {
                    if (checkTrialChambers(seed, tc))
                        checkMineshafts(seed, hopperPos);
//                    CPos vec = tc.subtract(ac);
//                    System.out.printf("AC to TC vector: (%d, %d), AC rotation: %s, TC rotation: %s\n",
//                            vec.getX(), vec.getZ(),
//                            getRotation(seed, ac, false).name(),
//                            getRotation(seed, tc, true).name()
//                    );
                }
            }
        }
    }

    private static BlockRotation getRotation(long seed, CPos ac, boolean isTrialChambers) {
        rand.setCarverSeed(seed, ac.getX(), ac.getZ(), MCVersion.v1_21);
        if (isTrialChambers) rand.nextInt(21); // skip y position
        return BlockRotation.getRandom(rand);
    }

    private static BPos getHopperPos(long seed, CPos tc) {
        final BPos hopperNorth = new BPos(-2, 3, 45).transform(BlockMirror.NONE, BlockRotation.CLOCKWISE_180, BPos.ORIGIN);
        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
        int pickedY = rand.nextInt(21) - 41;
        BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
        if (startPieceRotation != BlockRotation.NONE)
            return BPos.ORIGIN;
        return hopperNorth.transform(BlockMirror.NONE, startPieceRotation, BPos.ORIGIN).add(tc.toBlockPos(pickedY));
    }

    // ----------------------------------------------------------------------------

    private static boolean checkTrialChambers(long seed, CPos tc) {
        return true; // TODO
    }

    private static void checkMineshafts(long seed, BPos hopperPos) {
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
            sisterSeeds.addSisterSeedsOf(structureSeed, 20_000);
            sisterSeeds.appendToFile("src/main/resources/minecart_hopper_chest.txt");
        }
    }
}

