package positions;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import kludwisz.ancientcity.AncientCity;
import kludwisz.ancientcity.AncientCityGenerator;

import java.util.ArrayList;
import java.util.List;

public class AncientCityChestPositions {
    private static final ChunkRand rand = new ChunkRand();
    private static final AncientCity city = new AncientCity(MCVersion.v1_21);
    private static final AncientCityGenerator gen = new AncientCityGenerator();

    public static List<BPos> getInChunk(long worldseed, CPos chunkPos) {
        ArrayList<BPos> chestsInChunk = new ArrayList<>();
        
        RPos region = chunkPos.toRegionPos(city.getSpacing());
        for (int drx = -1; drx <= 1; drx++) {
            for (int drz = -1; drz <= 1; drz++) {
                final int rx = region.getX() + drx;
                final int rz = region.getZ() + drz;
                CPos cityPos = city.getInRegion(worldseed, rx, rz, rand);
                if (isCloseEnoughToChunk(cityPos, chunkPos)) {
                    gen.generate(worldseed, cityPos.getX(), cityPos.getZ(), rand);
                    addChests(chunkPos, chestsInChunk);
                }
            }
        }

        return chestsInChunk;
    }

    private static boolean isCloseEnoughToChunk(CPos cityPos, CPos chunkPos) {
        return cityPos.distanceTo(chunkPos, DistanceMetric.CHEBYSHEV) <= 8;
    }

    private static void addChests(CPos chunkPos, ArrayList<BPos> chestsInChunk) {
        gen.getChests().stream()
                .map(Pair::getFirst)
                .filter(pos -> pos.toChunkPos().equals(chunkPos))
                .forEach(chestsInChunk::add);
    }
}
