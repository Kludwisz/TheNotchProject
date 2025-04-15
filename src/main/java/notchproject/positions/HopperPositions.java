package notchproject.positions;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockMirror;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import kludwisz.generator.TrialChambersGenerator;
import kludwisz.structure.TrialChambers;

import java.util.ArrayList;
import java.util.List;

public class HopperPositions {
    private static final ChunkRand rand = new ChunkRand();
    private static final TrialChambers chambers = new TrialChambers(MCVersion.v1_21);
    private static final TrialChambersGenerator gen = new TrialChambersGenerator();

    public static List<BPos> getInChunk(long worldseed, CPos chunkPos) {
        ArrayList<BPos> chestsInChunk = new ArrayList<>();

        RPos region = chunkPos.toRegionPos(chambers.getSpacing());
        for (int drx = -1; drx <= 1; drx++) {
            for (int drz = -1; drz <= 1; drz++) {
                final int rx = region.getX() + drx;
                final int rz = region.getZ() + drz;
                CPos chambersPos = chambers.getInRegion(worldseed, rx, rz, rand);
                if (isCloseEnoughToChunk(chambersPos, chunkPos)) {
                    gen.generate(worldseed, chambersPos.getX(), chambersPos.getZ(), rand);
                    addHoppers(chunkPos, chestsInChunk);
                }
            }
        }

        return chestsInChunk;
    }

    private static boolean isCloseEnoughToChunk(CPos cityPos, CPos chunkPos) {
        return cityPos.distanceTo(chunkPos, DistanceMetric.CHEBYSHEV) <= 8;
    }

    private static void addHoppers(CPos chunkPos, ArrayList<BPos> hoppersInChunk) {
        gen.getPieces().stream()
                .filter(piece -> piece.getName().equals("decor/disposal"))
                .map(piece -> piece.pos.toImmutable().add(new BPos(0, 1, 1).transform(BlockMirror.NONE, piece.rotation, BPos.ORIGIN)))
                .filter(pos -> pos.toChunkPos().equals(chunkPos))
                .forEach(hoppersInChunk::add);
    }
}
