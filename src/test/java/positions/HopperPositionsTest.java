package positions;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HopperPositionsTest {
    @Test
    public void correctnessSmall1() {
        long seed = 2056166027648117641L;
        CPos chunk = new CPos(-1, 73);
        List<BPos> correctList = List.of(
                // /setblock -4 -22 1179 minecraft:hopper[enabled=true,facing=down]{Items:[],TransferCooldown:0}
                new BPos(-4, -22, 1179)
        );

        var hoppers = HopperPositions.getInChunk(seed, chunk);
        ListAssertions.assertIsEqual(hoppers, correctList);
    }

    @Test
    public void correctnessSmall2() {
        long seed = 2056166027648117641L;
        CPos chunk = new CPos(-1, 73);
        List<BPos> correctList = List.of(
                // /setblock -4 -22 1179 minecraft:hopper[enabled=true,facing=down]{Items:[],TransferCooldown:0}
                new BPos(-4, -22, 1179)
        );

        var hoppers = HopperPositions.getInChunk(seed, chunk);
        ListAssertions.assertIsEqual(hoppers, correctList);
    }

    @Test
    public void correctnessSmall3() {
        long seed = 2056166027648117641L;
        CPos chunk = new CPos(-31, -64);
        List<BPos> correctList = List.of(
                // /setblock -493 -38 -1013 minecraft:hopper[enabled=true,facing=down]{Items:[],TransferCooldown:0}
                new BPos(-493, -38, -1013)
        );

        var hoppers = HopperPositions.getInChunk(seed, chunk);
        ListAssertions.assertIsEqual(hoppers, correctList);
    }
}
