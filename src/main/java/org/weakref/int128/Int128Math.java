/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.weakref.int128;

/*
Modifications made: removed unused methods and function parameters, switched to the pre-jdk18 version of unsignedMultiplyHigh
 */
public final class Int128Math
{
    private Int128Math() {}

    public static long addHigh(long aHigh, long aLow, long bHigh, long bLow)
    {
        return aHigh + bHigh + MoreMath.unsignedCarry(aLow, bLow);
    }

    public static long addLow(long aLow, long bLow)
    {
        return aLow + bLow;
    }

    public static long multiplyHigh(long aHigh, long aLow, long bHigh, long bLow)
    {
        return unsignedMultiplyHigh(aLow, bLow) + aLow * bHigh + aHigh * bLow;
    }

    public static long unsignedMultiplyHigh(long x, long y)
    {
        return Math.multiplyHigh(x, y) + (x & (y >> 63)) + (y & (x >> 63));
    }

    public static long multiplyLow(long aLow, long bLow)
    {
        return aLow * bLow;
    }

    public static long shiftLeftHigh(long high, long low, int shift)
    {
        if (shift < 64) {
            return (high << shift) | (low >>> 1 >>> (63 - shift));
        }
        else {
            return low << (shift - 64);
        }
    }

    public static long shiftLeftLow(long low, int shift)
    {
        if (shift < 64) {
            return low << shift;
        }
        else {
            return 0;
        }
    }

    public static long shiftRightHigh(long high, int shift)
    {
        if (shift < 64) {
            return high >> shift;
        }
        else {
            return high >> 63;
        }
    }

    public static long shiftRightLow(long high, long low, int shift)
    {
        // HD 2-18
        if (shift < 64) {
            return (high << 1 << (63 - shift)) | (low >>> shift);
        }
        else {
            return high >> (shift - 64);
        }
    }
}
