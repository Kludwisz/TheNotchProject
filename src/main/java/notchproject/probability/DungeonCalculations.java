package notchproject.probability;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.LootContext;
import com.seedfinding.mcfeature.loot.LootPool;
import com.seedfinding.mcfeature.loot.LootTable;
import com.seedfinding.mcfeature.loot.entry.EmptyEntry;
import com.seedfinding.mcfeature.loot.entry.ItemEntry;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.loot.roll.ConstantRoll;
import com.seedfinding.mcfeature.loot.roll.UniformRoll;
import kludwisz.mineshafts.MineshaftLoot;
import kludwisz.rng.WorldgenRandom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DungeonCalculations {
    public static void main(String[] args) {
        calculateGenerationPreconditionsProbability();

//        double prob = 1D / 87_500;
//        double probExists = 1 - Math.pow(1D - prob, 65536);
//        System.out.printf("Probability of such a popseed existing: %.4f", probExists);

        //calculateLootProbability();
        //calculateChestPositionProbability();
    }

    private static void calculateLootProbability() {
        LootContext ctx = new LootContext(0L);
        HashSet<String> stackables = new HashSet<>();

        int total = 10_000_000;
        int good = 0;

        for (int i = 0; i < total; i++) {
            stackables.clear();
            LinkedList<ItemStack> indexedLoot = DUNGEON_1_21_4.generateIndexed(ctx);

            int slotsFilled = 0;
            for (ItemStack stack : indexedLoot) {
                if (stack.isEmpty()) continue;

                if (UNSTACKABLE_ITEMS.contains(stack.getItem().getName())) {
                    slotsFilled++;
                }
                else {
                    if (stackables.add(stack.getItem().getName()))
                        slotsFilled++;
                }
                if (stack.getItem().equalsName(Items.ENCHANTED_GOLDEN_APPLE) && slotsFilled <= 7) {
                    good++;
                    break;
                }
            }
        }

        System.out.println("Total: " + total);
        System.out.println("Good: " + good);
        System.out.println("Probability: " + (good * 100.0D / total) + "%");
        System.out.println("Expected loot seeds per good seed: " + ((double)total / good));
    }

    private static void calculateChestPositionProbability() {
        BPos target = new BPos(66, -33, 19);
        BPos inChunk = target.subtract(target.toChunkCorner()).add(0, -33, 0);
        System.out.println("target: " + inChunk);

        long total = 100_000_000L;
        long good = 0L;

        WorldgenRandom rand = new WorldgenRandom(WorldgenRandom.Type.XOROSHIRO);

        rand.setSeed(0);
        for (long i = 0; i < total; i++) {
            boolean isGood = generatePossibleDungeonChests(rand).stream().anyMatch(
                    pos -> pos.equals(inChunk)
            );
            if (isGood)
                good++;
        }

        System.out.println("Total: " + total);
        System.out.println("Good: " + good);
        System.out.println("Probability: " + (good * 100.0D / total) + "%");
        System.out.println("Expected population seeds per good seed: " + ((double)total / good));
    }

    private static void calculateGenerationPreconditionsProbability() {
        // for each dungeon size, we'll calculate how likely it is for a mineshaft
        // to generate an arrangement of air blocks that allows the dungeon to generate.

        MineshaftLoot mgen = new MineshaftLoot(MCVersion.v1_21);
        WorldgenRandom rand = new WorldgenRandom(WorldgenRandom.Type.XOROSHIRO);
        rand.setSeed(1);

        for (int sizeX = 3; sizeX <= 4; sizeX++) {
            for (int sizeZ = 3; sizeZ <= 4; sizeZ++) {
                long total = 100_000L;
                long good = 0L;
                System.out.printf("Size: %d x %d\n", sizeX*2 + 1, sizeZ*2 + 1);

                for (long i = 0; i < total; i++) {
                    long seed = rand.nextLong();
                    Dungeon dungeon = new Dungeon(
                            new BPos(rand.nextInt(16), -33, rand.nextInt(16)),
                            sizeX, sizeZ
                    );

                    boolean floorWithoutAir = true;
                    int airColumns = 0;

                    for (int cx = -8; cx <= 8; cx++) {
                        for (int cz = -8; cz <= 8; cz++) {
                            CPos chunk = new CPos(cx, cz);
                            if (!mgen.generateMineshaft(seed, chunk, false))
                                continue;

                            floorWithoutAir &= noAirInFloor(mgen, dungeon);
                            airColumns += countAirColumns(mgen, dungeon);
                        }
                    }

                    if (floorWithoutAir && airColumns >= 1 && airColumns <= 5)
                        good++;
                }

                System.out.println("Total: " + total);
                System.out.println("Good: " + good);
                System.out.println("Probability: " + ((double)good / total));
            }
        }
    }

    private static boolean noAirInFloor(MineshaftLoot mgen, Dungeon dungeon) {
        BlockBox dungeonFloor = new BlockBox(
                dungeon.center.getX() - dungeon.sizeX, dungeon.center.getY() - 1, dungeon.center.getZ() - dungeon.sizeZ,
                dungeon.center.getX() + dungeon.sizeX, dungeon.center.getY() - 1, dungeon.center.getZ() + dungeon.sizeZ
        );

        // firstly, we need to make sure that the miseshaft-placed air blocks
        // won't stop the dungeon from spawning

        boolean floorHasAir = mgen.getPieces().stream()
                .filter(piece -> piece.boundingBox.intersects(dungeonFloor))
                .anyMatch(piece -> piece.airIntersectsBox(dungeonFloor));
        return !floorHasAir;
    }

    private static int countAirColumns(MineshaftLoot mgen, Dungeon dungeon) {
        int cols = 0;

        final int xMin = dungeon.center.getX() - dungeon.sizeX;
        final int xMax = dungeon.center.getX() + dungeon.sizeX;
        final int zMin = dungeon.center.getZ() - dungeon.sizeZ;
        final int zMax = dungeon.center.getZ() + dungeon.sizeZ;

        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                if (x != xMin && x != xMax && z != zMin && z != zMax)
                    continue; // only check the walls

                BPos colLower = new BPos(x, dungeon.center.getY(), z);
                BPos colUpper = new BPos(x, dungeon.center.getY() + 1, z);
                boolean air1 = mgen.getPieces().stream()
                        .filter(piece -> piece.boundingBox.contains(colLower))
                        .anyMatch(piece -> piece.airContainsPos(colLower));
                boolean air2 = mgen.getPieces().stream()
                        .filter(piece -> piece.boundingBox.contains(colUpper))
                        .anyMatch(piece -> piece.airContainsPos(colUpper));

                if (air1 && air2)
                    cols++;
            }
        }

        return cols;
    }

    private static List<BPos> generatePossibleDungeonChests(WorldgenRandom rand) {
        List<BPos> chests = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int dx = rand.nextInt(16);
            int dz = rand.nextInt(16);
            int dy = rand.nextInt(58) - 58;
            BPos spawnerPos = new BPos(dx, dy, dz);

            int sizeX = rand.nextInt(2) + 2;
            int sizeZ = rand.nextInt(2) + 2;

            for (int chest = 0; chest < 2; chest++) {
                for (int attempt = 0; attempt < 3; attempt++) {
                    int cx = dx + rand.nextInt(sizeX * 2 + 1) - sizeX;
                    int cz = dz + rand.nextInt(sizeZ * 2 + 1) - sizeZ;
                    BPos chestPos = new BPos(cx, dy, cz);

                    if (isChestPosValid(chests, spawnerPos, sizeX, sizeZ, chestPos)) {
                        rand.nextLong();
                        chests.add(chestPos);
                        break;
                    }
                }
            }
        }
        return chests;
    }

    private static boolean isChestPosValid(List<BPos> chests, BPos spawnerPos, int sizeX, int sizeZ, BPos chestPos) {
        int deltaX = Math.abs(chestPos.getX() - spawnerPos.getX());
        int deltaZ = Math.abs(chestPos.getZ() - spawnerPos.getZ());

        if (deltaX <= sizeX-2 && deltaZ <= sizeZ-2) return false; // too close to center = never neighbors another block
        if (deltaX == sizeX && deltaZ == sizeZ) return false; // at corner

        if (chests.isEmpty()) {
            return deltaX == sizeX || deltaZ == sizeZ; // at wall
        }
        else {
            // second chest has to be either in second ring and neighboring the first chest
            // or at the wall and not neighboring the first chest
            if (chests.get(0).equals(chestPos)) return false; // can't be inside first chest
            boolean isNeighbor = chests.get(0).distanceTo(chestPos, DistanceMetric.MANHATTAN) == 1;
            if (isNeighbor) {
                return (deltaX == sizeX-1 && deltaZ < sizeZ) || (deltaZ == sizeZ-1 && deltaX < sizeX); // second ring
            }
            else {
                return deltaX == sizeX || deltaZ == sizeZ; // at wall
            }
        }
    }

    // -------------------------------------------------------------------------------

    private static final HashSet<String> UNSTACKABLE_ITEMS = new HashSet<>();
    static {
        UNSTACKABLE_ITEMS.add(Items.SADDLE.getName());
        UNSTACKABLE_ITEMS.add(Items.MUSIC_DISC_FAR.getName());
        UNSTACKABLE_ITEMS.add(Items.MUSIC_DISC_13.getName());
        UNSTACKABLE_ITEMS.add(Items.MUSIC_DISC_CAT.getName());
        UNSTACKABLE_ITEMS.add(Items.GOLDEN_HORSE_ARMOR.getName());
        UNSTACKABLE_ITEMS.add(Items.IRON_HORSE_ARMOR.getName());
        UNSTACKABLE_ITEMS.add(Items.DIAMOND_HORSE_ARMOR.getName());
        UNSTACKABLE_ITEMS.add(Items.ENCHANTED_BOOK.getName());
    }

    private static final LootTable DUNGEON_1_21_4 = new LootTable(
            new LootPool(new UniformRoll(1.0f, 3.0f),
                    new ItemEntry(Items.SADDLE, 20),
                    new ItemEntry(Items.GOLDEN_APPLE, 15),
                    new ItemEntry(Items.ENCHANTED_GOLDEN_APPLE, 2),
                    new ItemEntry(Items.MUSIC_DISC_FAR, 2), //otherside
                    new ItemEntry(Items.MUSIC_DISC_13, 15),
                    new ItemEntry(Items.MUSIC_DISC_CAT, 15),
                    new ItemEntry(Items.NAME_TAG, 20),
                    new ItemEntry(Items.GOLDEN_HORSE_ARMOR, 10),
                    new ItemEntry(Items.IRON_HORSE_ARMOR, 15),
                    new ItemEntry(Items.DIAMOND_HORSE_ARMOR, 5),
                    new ItemEntry(Items.ENCHANTED_BOOK, 10)
            ),
            new LootPool(new UniformRoll(1.0f, 4.0f),
                    new ItemEntry(Items.IRON_INGOT, 10),
                    new ItemEntry(Items.GOLD_INGOT, 5),
                    new ItemEntry(Items.BREAD, 20),
                    new ItemEntry(Items.WHEAT, 20),
                    new ItemEntry(Items.BUCKET, 10),
                    new ItemEntry(Items.REDSTONE, 15),
                    new EmptyEntry(15), // coal, will always go straight into the chest
                    new ItemEntry(Items.MELON_SEEDS, 10),
                    new ItemEntry(Items.PUMPKIN_SEEDS, 10),
                    new ItemEntry(Items.BEETROOT_SEEDS, 10)
            ),
            new LootPool(new ConstantRoll(3),
                    new ItemEntry(Items.BONE, 10),
                    new ItemEntry(Items.GUNPOWDER, 10),
                    new ItemEntry(Items.ROTTEN_FLESH, 10),
                    new ItemEntry(Items.STRING, 10)
            )
    );

    private record Dungeon(BPos center, int sizeX, int sizeZ) {}
}
