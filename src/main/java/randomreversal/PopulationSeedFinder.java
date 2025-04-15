package randomreversal;

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
        // TODO implement this function
    }

    /*
    uint64_t b_inv = modinv64(b) & MASK_60;
    // std::println("b_inv = {}", b_inv);

    // a * x + b * z = t

    int64_t basis[2][2] = {
        { 0, MOD_60 },
        { 1, (int64_t)(-a * b_inv & MASK_60) },
    };
    // std::println("basis =\n{} {}\n{} {}", basis[0][0], basis[0][1], basis[1][0], basis[1][1]);

    lagrange_gauss(basis);
    // std::println("reduced basis =\n{} {}\n{} {}", basis[0][0], basis[0][1], basis[1][0], basis[1][1]);

    uint64_t basis_det = basis[0][0] * basis[1][1] - basis[0][1] * basis[1][0];
    if (basis_det != MOD_60) throw 0;
    // std::println("basis_det = {}", basis_det);

    int64_t basis_inv_num[2][2] = {
        { basis[1][1], -basis[0][1] },
        { -basis[1][0], basis[0][0] },
    };

    int64_t new_z_center = b_inv * target << 4 >> 4;
    // std::println("new_z_center = {}", new_z_center);

    int64_t lx = ((__int128)basis_inv_num[1][0] * -new_z_center + (basis_det >> 1)) >> 60;
    int64_t lz = ((__int128)basis_inv_num[1][1] * -new_z_center + (basis_det >> 1)) >> 60;
    // std::println("lx = {} lz = {}", lx, lz);

    __int128 bx = (__int128)lx * basis[0][0] + (__int128)lz * basis[1][0];
    __int128 bz = (__int128)lx * basis[0][1] + (__int128)lz * basis[1][1] + new_z_center;
    if (bx != bx << 64 >> 64 || bz != bz << 64 >> 64) throw 0;
    // std::println("x = {} z = {}", (int64_t)x, (int64_t)z);

    int64_t x = bx;
    int64_t z = bz;

    uint64_t result = (a * x + b * z) & MASK_60;
    if (result != target) throw 0;
    // std::println("result = {} target = {}", result, target & MASK_60);

    if (x >= min_x && x <= max_x && z >= min_z && z <= max_z) {
        out.push_back({ x, z });
    }

     */
    public static Candidate findSolutionInBox(long a, long b, long target) {
        // TODO andrew's & scriptline's algo goes here
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



        return null;
    }
}
