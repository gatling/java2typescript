package org.bsc.java2typescript;

import java.util.List;
import java.util.Optional;

/**
 * Fixture types used by {@link PrimitiveTypeTest} to check how Java primitive types and their wrappers are
 * converted to TypeScript.
 *
 * <p>In GraalJS, a {@code char} is a JavaScript string of length 1, the other primitives are numbers or booleans.
 */
public final class PrimitiveTypes {
    private PrimitiveTypes() {
    }
}

class PrimitiveUser {
    public char aChar() { return 'c'; }
    public byte aByte() { return 1; }
    public short aShort() { return 1; }
    public int anInt() { return 1; }
    public long aLong() { return 1; }
    public float aFloat() { return 1; }
    public double aDouble() { return 1; }
    public boolean aBoolean() { return true; }

    public Character boxedChar() { return 'c'; }
    public Byte boxedByte() { return 1; }
    public Short boxedShort() { return 1; }
    public Integer boxedInt() { return 1; }

    public short[] shorts() { return null; }
    public Character[] boxedChars() { return null; }
    public List<Character> listOfChars() { return null; }
    public Optional<Short> optionalShort() { return null; }

    public char toUpper(char c) { return c; }
}
