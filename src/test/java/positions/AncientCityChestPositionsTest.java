package positions;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import org.junit.jupiter.api.Test;

import java.util.List;


public class AncientCityChestPositionsTest {
    @Test
    public void correctnessSmall1() {
        long seed = 3767692906516439374L;
        CPos chunk = new CPos(-104, 54);
        List<BPos> correctList = List.of(
                // /setblock -1661 -48 877 minecraft:chest[facing=south,type=single,waterlogged=false]{LootTable:"minecraft:chests/ancient_city",LootTableSeed:2011219879389813462L}
                // /setblock -1652 -44 865 minecraft:chest[facing=east,type=single,waterlogged=false]{LootTable:"minecraft:chests/ancient_city",LootTableSeed:-7814492181755106191L}
                new BPos(-1661, -48, 877),
                new BPos(-1652, -44, 865)
        );

        var ancientCityChests = AncientCityChestPositions.getInChunk(seed, chunk);
        ListAssertions.assertIsEqual(ancientCityChests, correctList);
    }

    @Test
    public void correctnessSmall2() {
        long seed = 3767692906516439374L;
        CPos chunk = new CPos(191, 175);
        List<BPos> correctList = List.of();

        var ancientCityChests = AncientCityChestPositions.getInChunk(seed, chunk);
        ListAssertions.assertIsEqual(ancientCityChests, correctList);
    }

    @Test
    public void correctnessSmall3() {
        long seed = 3767692906516439374L;
        CPos chunk = new CPos(227, 703);
        List<BPos> correctList = List.of(
                // /setblock 3645 -47 11261 minecraft:chest[facing=south,type=single,waterlogged=false]{LootTable:"minecraft:chests/ancient_city",LootTableSeed:8439452821953426230L}
                // /setblock 3637 -47 11261 minecraft:chest[facing=south,type=single,waterlogged=false]{LootTable:"minecraft:chests/ancient_city",LootTableSeed:2082251594035532399L}
                // /setblock 3640 -37 11248 minecraft:chest[facing=east,type=single,waterlogged=false]{LootTable:"minecraft:chests/ancient_city",LootTableSeed:2469779279326171310L}
                new BPos(3645, -47, 11261),
                new BPos(3637, -47, 11261),
                new BPos(3640, -37, 11248)
        );

        var ancientCityChests = AncientCityChestPositions.getInChunk(seed, chunk);
        ListAssertions.assertIsEqual(ancientCityChests, correctList);
    }
}
