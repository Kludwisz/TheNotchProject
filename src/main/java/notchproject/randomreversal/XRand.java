package notchproject.randomreversal;

public class XRand {
    private long seedLo, seedHi;

    public static long mixStafford13(long l) {
        l = (l ^ l >>> 30) * -4658895280553007687L;
        l = (l ^ l >>> 27) * -7723592293110705685L;
        return l ^ l >>> 31;
    }

    public void setSeed(long l) {
        long l2 = l ^ 0x6A09E667F3BCC909L;
        long l3 = l2 + -7046029254386353131L;
        this.seedLo = mixStafford13(l2);
        this.seedHi = mixStafford13(l3);
    }

    public long xNextLong() {
        long l = this.seedLo;
        long l2 = this.seedHi;
        long l3 = Long.rotateLeft(l + l2, 17) + l;
        this.seedLo = Long.rotateLeft(l, 49) ^ (l2 ^= l) ^ l2 << 21;
        this.seedHi = Long.rotateLeft(l2, 28);
        return l3;
    }

    public long nextLong() {
        int a_ = (int)(this.xNextLong() >> 32);
        int b = (int)(this.xNextLong() >> 32);
        long a = (long)a_ << 32;
        return a + (long)b;
    }
}
