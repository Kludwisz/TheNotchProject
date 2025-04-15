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
/*
Modifications made: removed all methods not used in the project
 */
package org.weakref.int128;

public record Int128(long high, long low) {
    public static final Int128 ZERO = new Int128(0, 0);
    public static final Int128 ONE = new Int128(0, 1);

    // --- conversion functions

    public static Int128 valueOf(long value)
    {
        return new Int128(value >> 63, value);
    }

    public long toLong()
    {
        return low;
    }

    // --- Bitwise operations

    public static Int128 shiftRight(Int128 value, int shift)
    {
        return new Int128(
                Int128Math.shiftRightHigh(value.high(), value.low(), shift),
                Int128Math.shiftRightLow(value.high(), value.low(), shift));
    }

    public static Int128 shiftLeft(Int128 value, int shift)
    {
        return new Int128(
                Int128Math.shiftLeftHigh(value.high(), value.low(), shift),
                Int128Math.shiftLeftLow(value.high(), value.low(), shift));
    }

    // --- Arithmetic operations

    public static Int128 add(Int128 a, Int128 b)
    {
        return new Int128(
                Int128Math.addHigh(a.high(), a.low(), b.high(), b.low()),
                Int128Math.addLow(a.high(), a.low(), b.high(), b.low()));
    }

    public static Int128 multiply(Int128 a, Int128 b)
    {
        return new Int128(
                Int128Math.multiplyHigh(a.high(), a.low(), b.high(), b.low()),
                Int128Math.multiplyLow(a.high(), a.low(), b.high(), b.low()));
    }

    public static Int128 multiply(Int128 a, long b)
    {
        // TODO: optimize
        return multiply(a, valueOf(b));
    }

    /**
     * 64 x 64 -> 128
     */
    public static Int128 multiply(long a, long b)
    {
        // TODO: optimize
        return multiply(valueOf(a), valueOf(b));
    }
}
