package org.bsc.java2typescript;

import java.util.List;
import java.util.Map;

/**
 * Fixture types used by {@link PublicFieldTest} to check how Java public fields are converted to TypeScript.
 *
 * <p>Behaviour of GraalJS ({@code Java.type("pkg.X")}) that the expected TypeScript follows:
 * <ul>
 * <li>public static fields declared by the type are readable, final ones cannot be written;</li>
 * <li>inherited static fields (from a superclass or an interface) are NOT readable through the subtype, unlike
 * inherited static methods;</li>
 * <li>public instance fields are readable on instances, inherited ones included; a hidden field gives the value of
 * the field hiding it; final ones cannot be written; static fields are not readable on instances;</li>
 * <li>non-public fields are not readable, nor public fields declared by a non-public class (even inherited by a
 * public class);</li>
 * <li>when a field and a method have the same name, a property read gives the field and a call gives the
 * method.</li>
 * </ul>
 */
public final class PublicFields {
    private PublicFields() {
    }

    // the instance fixtures are public: the fields of non-public classes are not generated

    /** public instance fields of several kinds, next to members that must not be emitted */
    public static class Instance {
        public int count;
        public final String name = "instance";
        public Instance next;
        public List<String> items;
        public PfHolder.Inner.Deep deep;
        public static int STATIC_ONE;

        protected int prot;
        int pkg;
        private int priv;
    }

    public static class InstanceBase {
        public int inherited;
        public int hidden;
    }

    /** inherits InstanceBase.inherited and hides InstanceBase.hidden with another type */
    public static class InstanceSub extends InstanceBase {
        public int own;
        public String hidden;
    }

    /** inherits fields declared by a non-public class */
    public static class InstanceFromNonPublic extends PfNonPublicBase {
        public int own;
    }

    /** instance fields with the same name as a method, or named "constructor" */
    public static class InstanceClash {
        public int size;
        public int constructor;

        public int size() { return 0; }
    }

    public static class InstanceGeneric<T> {
        public T value;
        public List<T> values;
    }

    public enum Coded {
        X;

        public final int code = 0;
    }
}

/** public static fields of several kinds, next to members that must not be emitted */
class PfHolder {
    public static final int MAX = 3;
    public static int counter = 7;
    public static final String NAME = "holder";
    public static final PfHolder INSTANCE = new PfHolder();
    public static final List<String> NAMES = null;
    public static final Inner.Deep DEEP = null;

    protected static int PROT;
    static int PKG;
    private static int PRIV;

    public int instanceField;

    public PfHolder() { }

    public static int twice(int x) { return 2 * x; }

    public static class Inner {

        public static class Deep {
            public static final Deep SELF = null;
        }
    }
}

/** interface constants (implicitly public static final) */
interface PfIfc {
    int A = 1;
    String S = "s";

    void m();

    static int sm() { return 0; }
}

record PfRec(int x) {
    public PfRec { }

    public static final int ZERO = 0;
    public static final PfRec EMPTY = new PfRec(0);
}

/** processStatic takes another branch for functional interfaces */
@FunctionalInterface
interface PfFn {
    int K = 5;
    PfFn ID = x -> x;

    int apply(int x);
}

/** enum constants and other static fields */
enum PfKind {
    A, B;

    public static final PfKind DEFAULT = A;
    public static int count = 2;
}

/** enum constant named like the static method {@code values()} of every enum */
enum PfClashEnum {
    values, other
}

class PfBase {
    public static final int INHERITED = 1;
    public static int HIDDEN = 1;
}

/** inherits PfBase.INHERITED and hides PfBase.HIDDEN with another type */
class PfSub extends PfBase {
    public static final int OWN = 5;
    public static String HIDDEN = "sub";
}

/** inherits the constants of PfIfc */
class PfImpl implements PfIfc {
    @Override
    public void m() { }
}

/** static field and static method with the same name */
class PfClash {
    public static int value = 1;

    public static int value() { return 2; }
}

/** valid Java names that need care in TypeScript */
class PfWords {
    public static int constructor;
    public static int $x;
}

/** field types that the converter maps wrongly */
class PfTypes {
    public static final char C = 'c';
    public static final byte B = 1;
    public static final Character BOXED_C = 'c';
    public static final Map<String, StringBuilder> MAP_SB = null;
    public static final PfBox<PfBoxHolder> BOX_HOLDER = null;
    public static final PfBox<PfBox<String>> BOX_OF_BOX = null;
}

/** raw type in a generic declaring class */
class PfGeneric<T> {
    @SuppressWarnings("rawtypes")
    public static final List RAW = null;
}

class PfBox<T> {
}

/** never declared with {@code @Type}, its name starts with PfBox */
class PfBoxHolder {
}

/** InstanceBase.hidden is hidden by a field of a non-public class: it is not readable on the subclass */
class PfNonPublicBase extends PublicFields.InstanceBase {
    public int nonPublic;
    public String hidden;
}
