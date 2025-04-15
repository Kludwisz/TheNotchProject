package notchproject.positions;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MinecartPositionsTest {
    @Test
    public void correctnessSmall1() {
        long worldseed = 6962586473162905446L;
        CPos chunk = new CPos(16, -39);
        List<BPos> correctList = List.of(
                //setblock 256 -12 -613 minecraft:red_wool
                //setblock 259 -12 -611 minecraft:red_wool
                //setblock 261 -12 -613 minecraft:red_wool
                //setblock 264 -12 -611 minecraft:red_wool
                //setblock 266 -12 -613 minecraft:red_wool
                //setblock 269 -13 -615 minecraft:red_wool
                //setblock 271 -13 -617 minecraft:red_wool
                new BPos(256, -12, -613),
                new BPos(259, -12, -611),
                new BPos(261, -12, -613),
                new BPos(264, -12, -611),
                new BPos(266, -12, -613),
                new BPos(269, -13, -615),
                new BPos(271, -13, -617)
        );

        var minecartPositions = MinecartPositions.getInChunk(worldseed, chunk);
        ListAssertions.assertIsEqual(minecartPositions, correctList);
    }

    @Test
    public void correctnessSmall2() {
        long worldseed = 6962586473162905446L;
        CPos chunk = new CPos(25, -34);
        List<BPos> partialList = List.of(
                //setblock 408 -24 -530 minecraft:red_wool
                //setblock 407 -20 -536 minecraft:red_wool
                //setblock 412 -26 -543 minecraft:red_wool
                //setblock 414 -16 -534 minecraft:red_wool
                //setblock 409 -16 -529 minecraft:red_wool
                new BPos(408, -24, -530),
                new BPos(407, -20, -536),
                new BPos(412, -26, -543), // from second mineshaft
                new BPos(414, -16, -534),
                new BPos(409, -16, -529)
        );

        var minecartPositions = MinecartPositions.getInChunk(worldseed, chunk);
        ListAssertions.assertIsSublist(minecartPositions, partialList);
    }

    @Test
    public void correctnessSmall3() {
        long worldseed = 6962586473162905446L;
        CPos chunk = new CPos(41, -90);
        List<BPos> partialList = List.of(
                ///setblock 671 -14 -1438 minecraft:red_wool
                ///setblock 659 -14 -1436 minecraft:red_wool
                ///setblock 656 -14 -1433 minecraft:red_wool
                ///setblock 656 -14 -1428 minecraft:red_wool
                ///setblock 662 -18 -1432 minecraft:red_wool
                new BPos(671, -14, -1438),
                new BPos(659, -14, -1436),
                new BPos(656, -14, -1433),
                new BPos(656, -14, -1428),
                new BPos(662, -18, -1432)
        );

        var minecartPositions = MinecartPositions.getInChunk(worldseed, chunk);
        ListAssertions.assertIsSublist(minecartPositions, partialList);
    }
}
