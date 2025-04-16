package notchproject.finders;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcmath.util.Mth;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.LongStream;

/**
 * A class that represents a list of seeds and their associated parameters. These parameters
 * can be chunk positions, block positions, or simple integers. The class allows for easy
 * manipulation of the list, including filtering, file i/o, and operations specific to seedfinding.
 */
@SuppressWarnings("unused")
public class SeedList {
    private final ArrayList<Entry> entries = new ArrayList<>();
    private final List<EntryFormat> formatSequence;
    private int entrySize;

    /**
     * Creates a new, flat SeedList with the default format sequence (only the seed).
     */
    public SeedList() {
        this.formatSequence = Collections.singletonList(EntryFormat.SEED);
        this.parseFormatIndices();
    }

    /**
     * Creates a new SeedList with the specified format sequence.
     * @param formatSequence the format sequence to use.
     */
    public SeedList(List<EntryFormat> formatSequence) {
        this.formatSequence = formatSequence;
        this.parseFormatIndices();
    }

    /**
     * Creates a new SeedList with the specified format sequence.
     * @param formatSequence the format sequence to use.
     */
    public SeedList(EntryFormat... formatSequence) {
        this.formatSequence = Arrays.asList(formatSequence);
        this.parseFormatIndices();
    }

    // ----------------------------------------------------------------------------------

    /**
     * @return the list of all entries in the SeedList.
     */
    public List<Entry> getEntries() {
        return this.entries;
    }

    /**
     * @param ix the index of the entry to get.
     * @return the entry at the specified index.
     */
    public Entry getEntry(int ix) {
        return this.entries.get(ix);
    }

    /**
     * Adds a new entry to the SeedList.
     * @param entry the entry to add.
     */
    public void addEntry(List<Long> entry) {
        if (entry.size() != this.entrySize) {
            throw new IllegalArgumentException("Entry size does not match format sequence size");
        }
        this.entries.add(new Entry(entry));
    }

    /**
     * @return whether the SeedList is flat (only contains one seed per entry, no extra parameters)
     */
    public boolean isFlat() {
        return this.formatSequence.size() == 1 && this.formatSequence.get(0) == EntryFormat.SEED;
    }

    // ----------------------------------------------------------------------------------

    /**
     * Converts the SeedList to a flat SeedList, where each entry contains only the first seed.
     * @return a new SeedList with only the first seed in each entry.
     */
    public SeedList toFlatList() {
        SeedList result = new SeedList();
        for (Entry entry : this.entries) {
            result.addEntry(Collections.singletonList(entry.getSeed()));
        }
        return result;
    }

    /**
     * Creates a new SeedList of the same format as this one, but only containing entries
     * such that the second SeedList contains the entry's seed.
     * @param other the SeedList used for filtering.
     * @return a new SeedList with the aforementioned entries.
     */
    public SeedList filterSeeds(SeedList other) {
        HashSet<Long> otherSeeds = new HashSet<>();
        for (Entry entry : other.getEntries()) {
            otherSeeds.add(entry.getSeed());
        }

        SeedList result = new SeedList(this.formatSequence);
        for (Entry entry : this.entries) {
            if (otherSeeds.contains(entry.getSeed())) {
                result.addEntry(entry.values);
            }
        }

        return result;
    }

    /**
     * Converts the SeedList to a flat SeedList of unique lower 48 bit seeds (structure seeds).
     * @return a new flat SeedList of unique lower 48 bit seeds in the current list.
     */
    public SeedList toFlatStructureSeedList() {
        HashSet<Long> structureSeeds = new HashSet<>();
        for (Entry entry : this.entries) {
            structureSeeds.add(entry.getSeed() & 0xFFFF_FFFF_FFFFL);
        }
        SeedList result = new SeedList();
        for (long seed : structureSeeds) {
            result.addEntry(Collections.singletonList(seed));
        }
        return result;
    }

    /**
     * Adds sister seeds of a given structure seed to the SeedList (requires a flat SeedList).
     * @param structureSeed the structure seed to add sister seeds of.
     * @param sisterSeedCount the number of sister seeds to add for each structure seed.
     */
    public void addSisterSeedsOf(long structureSeed, int sisterSeedCount) {
        if (this.formatSequence.size() != 1 || this.formatSequence.get(0) != EntryFormat.SEED) {
            throw new IllegalArgumentException("Cannot add sister seeds to a non-flat SeedList");
        }

        LongStream.range(1, sisterSeedCount+1).forEach(i -> {
            long seed = (i << 48) | structureSeed;
            this.addEntry(Collections.singletonList(seed));
        });
    }

    /**
     * Creates a new flat SeedList from the current flat SeedList in which each seed is replaced by
     * a specified number of its sister seeds.
     * @param sisterSeedCount the number of sister seeds to add for each structure seed.
     */
    public SeedList extendWithSisterSeeds(int sisterSeedCount) {
        if (this.formatSequence.size() != 1 || this.formatSequence.get(0) != EntryFormat.SEED) {
            throw new IllegalArgumentException("Cannot add sister seeds to a non-flat SeedList");
        }

        SeedList result = new SeedList();
        for (Entry entry : this.entries)
            result.addSisterSeedsOf(entry.getSeed() & Mth.MASK_48, sisterSeedCount);
        return result;
    }

    // ----------------------------------------------------------------------------------

    /**
     * Parses a flat SeedList from the given file using the default format sequence of only the seed.
     * @param filename the name of the file to read from.
     * @return the SeedList read from the file, or null if an error occurred.
     */
    public static SeedList fromFile(String filename) {
        return SeedList.fromFile(filename, Collections.singletonList(EntryFormat.SEED));
    }

    /**
     * Parses a SeedList from the given file using the given format.
     * @param filename the name of the file to read from.
     * @param formatSequence the format sequence to use.
     * @return the SeedList read from the file, or null if an error occurred.
     */
    public static SeedList fromFile(String filename, EntryFormat... formatSequence) {
        return SeedList.fromFile(filename, Arrays.asList(formatSequence));
    }

    /**
     * Parses a SeedList from the given file using the given format.
     * @param filename the name of the file to read from.
     * @param formatSequence the format sequence to use.
     * @return the SeedList read from the file, or null if an error occurred.
     */
    public static SeedList fromFile(String filename, List<EntryFormat> formatSequence) {
        try {
            Scanner fin = new Scanner(new File(filename));
            SeedList result = new SeedList(formatSequence);

            while (fin.hasNextLong()) {
                String line = fin.nextLine();
                List<Long> entry = new ArrayList<>();
                for (String s : line.split(" ")) {
                    entry.add(Long.parseLong(s));
                }
                result.addEntry(entry);
            }

            fin.close();
            return result;
        }
        catch (Exception ignored) {}
        return null;
    }

    /**
     * Writes or appends the SeedList to the given file.
     * @param filename the name of the file to write to.
     * @param append whether to append to the file or overwrite it.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean toFile(String filename, boolean append) {
        try {
            FileWriter fout = new FileWriter(filename, append);

            for (Entry entry : this.entries) {
                for (long value : entry.values) {
                    fout.write(value + " ");
                }
                fout.write("\n");
            }

            fout.close();
            return true;
        }
        catch (Exception ignored) {}
        return false;
    }

    /**
     * Writes the SeedList to the given file. Deletes the previous contents of the file.
     * @param filename the name of the file to write to.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean toFile(String filename) {
        return toFile(filename, false);
    }

    /**
     * Appends the SeedList to the given file.
     * @param s the string to append to the file.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean appendToFile(String s) {
        return toFile(s, true);
    }

    // ----------------------------------------------------------------------------------

    /**
     * The possible formats for a single entry inside a SeedList.
     */
    public enum EntryFormat {
        SEED,
        CHUNK_POS,
        BLOCK_POS,
        INTEGER
    }

    private int seedIndex = -1;
    private final List<Integer> chunkIndices = new ArrayList<>();
    private final List<Integer> blockIndices = new ArrayList<>();
    private final List<Integer> integerIndices = new ArrayList<>();

    private void parseFormatIndices() {
        int ix = 0;
        for (EntryFormat format : this.formatSequence) {
            switch (format) {
                case SEED:
                    this.seedIndex = ix;
                    ix++;
                    break;
                case CHUNK_POS:
                    this.chunkIndices.add(ix);
                    ix += 2;
                    break;
                case BLOCK_POS:
                    this.blockIndices.add(ix);
                    ix += 3;
                    break;
                case INTEGER:
                    this.integerIndices.add(ix);
                    ix++;
                    break;
            }
        }
        this.entrySize = ix;
    }

    /**
     * A class that wraps the numerical values stored inside a SeedList.
     * Allows easy access to specific objects, like the seed, chunk positions,
     * block positions, or simple integers based on the entry format.
     */
    public class Entry {
        private final List<Long> values;

        /**
         * Constructs a new Entry with the given values.
         */
        public Entry(List<Long> values) {
            this.values = values;
        }

        /**
         * Constructs a new Entry with the given values.
         */
        public Entry(long... values) {
            this.values = new ArrayList<>();
            for (long value : values) {
                this.values.add(value);
            }
        }

        /**
         * @return the entry's seed.
         */
        public long getSeed() {
            return this.values.get(seedIndex);
        }

        /**
         * @return the i-th integer in the entry.
         * @throws IndexOutOfBoundsException if the entry contains less than i-1 integers.
         */
        public long getInteger(int i) {
            int ix = integerIndices.get(i);
            return this.values.get(ix);
        }

        /**
         * @return the i-th chunk position in the entry.
         * @throws IndexOutOfBoundsException if the entry contains less than i-1 chunk positions.
         * @throws ArithmeticException if the stored values are too large to be chunk coordinates.
         */
        public CPos getChunkPos(int i) {
            int ix = chunkIndices.get(i);
            return new CPos(
                    Math.toIntExact(this.values.get(ix)),
                    Math.toIntExact(this.values.get(ix + 1))
            );
        }

        /**
         * @return the i-th block position in the entry.
         * @throws IndexOutOfBoundsException if the entry contains less than i-1 block positions.
         * @throws ArithmeticException if the stored values are too large to be block coordinates.
         */
        public BPos getBlockPos(int i) {
            int ix = blockIndices.get(i);
            return new BPos(
                    Math.toIntExact(this.values.get(ix)),
                    Math.toIntExact(this.values.get(ix + 1)),
                    Math.toIntExact(this.values.get(ix + 2))
            );
        }
    }
}
