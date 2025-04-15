import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import positions.AncientCityChestPositions;
import positions.HopperPositions;
import positions.MinecartPositions;

public class ChestArrangementChecker {
    public static boolean testSeed(long worldseed, CPos chunk) {
        // check if an ancient city chest generates in the target chunk
        var ancientCityChests = AncientCityChestPositions.getInChunk(worldseed, chunk);
        if (ancientCityChests.isEmpty())
            return false;

        // check if a chest minecart can attempt to generate 2 or 3 blocks above the chest
        boolean goodMinecart = false;
        var chestMinecarts = MinecartPositions.getInChunk(worldseed, chunk);
        if (chestMinecarts.isEmpty())
            return false;

        for (BPos chest : ancientCityChests) {
            for (BPos minecart : chestMinecarts) {
                if (chest.getX() == minecart.getX() && chest.getZ() == minecart.getZ()) {
                    int dy = minecart.getY() - chest.getY();
                    if (dy == 2 || dy == 3)
                        goodMinecart = true;
                }
            }
        }
        if (!goodMinecart)
            return false;

        // check if a trial chamber hopper generates right above an ancient city chest
        var hoppers = HopperPositions.getInChunk(worldseed, chunk);
        if (hoppers.isEmpty())
            return false;

        final BPos targetOffset = new BPos(0, 1, 0);
        for (BPos chest : ancientCityChests) {
            for (BPos hopper : hoppers) {
                if (hopper.subtract(chest).equals(targetOffset)) {
                    return true;
                }
            }
        }

        // check all the conditions at the same time
        for (BPos chest : ancientCityChests) {
            for (BPos minecart : chestMinecarts) {
                for (BPos hopper : hoppers) {
                    if (chest.getX() != minecart.getX() || chest.getZ() != minecart.getZ()) continue;
                    if (chest.getZ() != hopper.getZ() || chest.getX() != hopper.getX()) continue;

                    int dy1 = hopper.getY() - chest.getY();
                    int dy2 = minecart.getY() - hopper.getY();
                    if (dy1 == 1 && (dy2 == 1 || dy2 == 2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
