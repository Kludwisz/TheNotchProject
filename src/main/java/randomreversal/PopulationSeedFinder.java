package randomreversal;

import com.seedfinding.mccore.util.pos.CPos;

import java.math.BigInteger;

public class PopulationSeedFinder {
    private static final long MOD_60 = 1L << 60;
    private static final long MASK_60 = MOD_60 - 1L;

    private static long modinv64(long value) {
        long x = ((((value << 1) ^ value) & 4) << 1) ^ value;
        x += x - value * x * x;
        x += x - value * x * x;
        x += x - value * x * x;
        x += x - value * x * x;
        return x;
    }

    private static float dot(float a, float b, float c, float d) {
        return a * c + b * d;
    }

    private static float lensq(float a, float b) {
        return a * a + b * b;
    }

    private static void lagrange_gauss(long[][] basis) {
        int swaps = 0;
        while (true) {
            int q = Math.round(dot(basis[0][0], basis[0][1], basis[1][0], basis[1][1]) / lensq(basis[1][0], basis[1][1]));
            if (q == 0) break;
            basis[0][0] -= q * basis[1][0];
            basis[0][1] -= q * basis[1][1];
            swaps++;
            swapRows(basis);
        }

        if (swaps % 2 == 1) {
            swapRows(basis);
        }
    }

    private static void swapRows(long[][] basis) {
        long temp = basis[0][0];
        basis[0][0] = basis[1][0];
        basis[1][0] = temp;
        temp = basis[0][1];
        basis[0][1] = basis[1][1];
        basis[1][1] = temp;
    }


    public static CPos findSolutionInBox(long a, long b, long target) {
        final int minX = -30_000_000 / 16;
        final int maxX = 30_000_000 / 16;
        final int minZ = -30_000_000 / 16;
        final int maxZ = 30_000_000 / 16;

        long b_inv = modinv64(b) & MASK_60;

        long[][] basis = {
            { 0, MOD_60 },
            { 1, (-a * b_inv) & MASK_60 },
        };

        lagrange_gauss(basis);

        long basis_det = basis[0][0] * basis[1][1] - basis[0][1] * basis[1][0];

        long[][] basis_inv_num = {
            { basis[1][1], -basis[0][1] },
            { -basis[1][0], basis[0][0] },
        };

        long new_z_center = b_inv * target << 4 >> 4;

        long lx = BigInteger.valueOf(basis_inv_num[1][0])
                .multiply(BigInteger.valueOf(-new_z_center))
                .add(BigInteger.valueOf(basis_det >> 1))
                .shiftRight(60).longValue();
        long lz = BigInteger.valueOf(basis_inv_num[1][1])
                .multiply(BigInteger.valueOf(-new_z_center))
                .add(BigInteger.valueOf(basis_det >> 1))
                .shiftRight(60).longValue();

        long x = BigInteger.valueOf(lx)
                .multiply(BigInteger.valueOf(basis[0][0]))
                .add(BigInteger.valueOf(lz).multiply(BigInteger.valueOf(basis[1][0])))
                .longValue();
        long z = BigInteger.valueOf(lx)
                .multiply(BigInteger.valueOf(basis[0][1]))
                .add(BigInteger.valueOf(lz).multiply(BigInteger.valueOf(basis[1][1])))
                .add(BigInteger.valueOf(new_z_center))
                .longValue();

        long result = (a * x + b * z) & MASK_60;
        if (result != target) return null;

        if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
            return new CPos((int)x, (int)z);
        }
        return null;
    }
}
