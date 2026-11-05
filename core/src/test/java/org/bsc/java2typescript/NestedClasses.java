package org.bsc.java2typescript;

import java.util.List;

/**
 * Fixture types used by {@link NestedClassTest} and {@link NestedClassDeclaredTypeTest} to check how Java member
 * classes are named once converted to TypeScript, for several levels of nesting.
 *
 * <p>Each scenario is a separate top-level type, so that "level N" means "N levels below a top-level type":
 * level 1 = {@code Outer.A} (like {@code java.util.Map.Entry}), level 2 = {@code Outer.A.B}, level 3 =
 * {@code Outer.A.B.C}.
 *
 * <p>Expected naming convention (generalisation of {@code java.util.Map$Entry}): the Java binary name without
 * the package, e.g. {@code NestedStatic.L1.L2.L3} -> {@code NestedStatic$L1$L2$L3}.
 */
public final class NestedClasses {
    private NestedClasses() {
    }
}

/** static class > static class > static class (levels 1..3) */
class NestedStatic {

    /** level 1 */
    public static class L1 {
        public L2 child() { return null; }

        /** level 2 */
        public static class L2 {
            public L3 child() { return null; }
            public L1 parent() { return null; }

            /** level 3 */
            public static class L3 {
                public L2 parent() { return null; }
            }
        }
    }

    /** level 1 generic (control) */
    public static class G1<U> {
    }
}

/** interface > interface > class, shaped like Gatling's CheckBuilder.Find.Default */
interface NestedIfc {

    /** level 1 (like CheckBuilder.Find) */
    interface Find<X> extends NestedIfc {
        Default<X> find();

        /** level 2 (like CheckBuilder.Find.Default) */
        class Default<X> implements Find<X> {
            @Override
            public Default<X> find() { return this; }
        }
    }
}

/** Level 2 types below several kinds of level 1 types (interface, enum, class) */
class NestedMixed {

    /** level 1 interface */
    public interface Ifc {

        /** level 2: class > interface > class */
        class Leaf {
        }
    }

    /** level 1 enum */
    public enum Mode {
        ON;

        /** level 2: class > enum > class */
        public static class Leaf {
        }
    }

    /** level 1 class */
    public static class Holder {

        /** level 2 enum */
        public enum Kind {
            A, B
        }

        /** level 2 non-static inner class */
        public class Inner {
            public Kind kind() { return null; }
        }
    }
}

/** generic owner with nested generic types */
class NestedGeneric<T> {

    /** level 1 non-static generic inner class: NestedGeneric&lt;T&gt;.Inner&lt;U&gt; */
    public class Inner<U> {

        /** level 2 non-static generic inner class: NestedGeneric&lt;T&gt;.Inner&lt;U&gt;.Deep&lt;V&gt; */
        public class Deep<V> {
        }
    }

    /** level 1 */
    public static class Mid {

        /** level 2 static generic class */
        public static class Leaf<U> {
        }
    }
}

/** Another outer which also contains a {@code Mid.Leaf}: same simple names as {@code NestedGeneric.Mid.Leaf} */
class NestedGenericOther {

    public static class Mid {

        public static class Leaf<U> {
        }
    }
}

/** A top-level {@code Mid}: its level 1 {@code Mid.Leaf} is correctly named {@code Mid$Leaf} */
class Mid {

    public static class Leaf<U> {
    }
}

/** Top-level class extending a level 2 class */
class NestedSub extends NestedStatic.L1.L2 {
}

/** Type whose members reference the nested types above */
class NestedUser {

    public NestedStatic.L1 level1() { return null; }
    public NestedStatic.L1.L2 level2() { return null; }
    public NestedStatic.L1.L2.L3 level3() { return null; }

    public void takeLevel3(NestedStatic.L1.L2.L3 arg) { }
    public List<NestedStatic.L1.L2.L3> listOfLevel3() { return null; }
    public NestedStatic.L1[] arrayOfLevel1() { return null; }
    public NestedStatic.L1.L2.L3[] arrayOfLevel3() { return null; }

    public NestedStatic.G1<String> genericLevel1() { return null; }
    public NestedIfc.Find<String> ifcFind() { return null; }
    public NestedIfc.Find.Default<String> ifcFindDefault() { return null; }
    public NestedGeneric.Mid.Leaf<String> genericLeaf() { return null; }

    public NestedGeneric<String>.Inner<Integer> genericInner() { return null; }
    public NestedGeneric<String>.Inner<Integer>.Deep<Long> genericDeep() { return null; }
}
