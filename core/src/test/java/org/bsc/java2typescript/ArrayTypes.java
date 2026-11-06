package org.bsc.java2typescript;

import java.util.List;

/**
 * Fixture types used by {@link ArrayTypeTest} to check how Java arrays are converted to TypeScript.
 *
 * <p>In GraalJS, a Java array behaves like a JavaScript array: {@code length}, any index, {@code for...of},
 * {@code Array.isArray()} and the {@code Array.prototype} methods such as {@code map()}.
 */
public final class ArrayTypes {
    private ArrayTypes() {
    }
}

class ArrayUser {
    public int[] ints() { return null; }
    public int[][] intMatrix() { return null; }
    public String[] strings() { return null; }
    public Integer[] boxed() { return null; }
    public Integer[][] boxedMatrix() { return null; }
    public char[] chars() { return null; }
    public char[][] charMatrix() { return null; }
    public ArrayUser[] self() { return null; }
    public java.time.Duration[] undeclared() { return null; }
    public List<String>[] genericArray() { return null; }
    public List<int[]> listOfInts() { return null; }
    public <T> T[] typeVariableArray() { return null; }

    public void varargs(Integer... values) { }
    public void varargsStrings(String... values) { }
}

enum ArrayKind {
    A, B
}
