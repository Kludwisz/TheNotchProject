package notches;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import kludwisz.mineshafts.MineshaftLoot;

import java.util.ArrayList;
import java.util.List;

public class MinecartPositions {
    private static final MineshaftLoot gen = new MineshaftLoot(MCVersion.v1_21);

    public static List<BPos> getInChunk(long worldseed, CPos chunkPos) {
        ArrayList<BPos> possibleChestsInChunk = new ArrayList<>();

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                final CPos mineshaftPos = chunkPos.add(dx, dz);
                if (gen.generateMineshaft(worldseed, mineshaftPos, false)) {
                    addChests(chunkPos, possibleChestsInChunk);
                }
            }
        }

        return possibleChestsInChunk;
    }

    private static void addChests(CPos chunkPos, ArrayList<BPos> possibleChestsInChunk) {
        BlockBox chunkBB = new BlockBox(
                chunkPos.getX() * 16, -64, chunkPos.getZ() * 16,
                chunkPos.getX() * 16 + 15, 255, chunkPos.getZ() * 16 + 15
        );

        gen.getCorridors().stream()
                .filter(corr -> corr.boundingBox.intersects(chunkBB)) // for efficiency
                .flatMap(corr -> corr.getPossibleChestPositions().stream())
                .filter(pos -> pos.toChunkPos().equals(chunkPos))
                .forEach(possibleChestsInChunk::add);
    }
}
