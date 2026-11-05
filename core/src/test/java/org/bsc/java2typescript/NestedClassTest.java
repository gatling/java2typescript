package org.bsc.java2typescript;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Naming of Java member classes nested at several levels (fixtures in {@link NestedClasses}).
 *
 * <p>Expected convention, generalised from {@link MemberClassTest} ({@code java.util.Map.Entry} ->
 * {@code java.util.Map$Entry} / {@code Map$Entry}): the Java binary name, without the package for the simple name.
 * E.g. {@code NestedStatic.L1.L2.L3} -> {@code org.bsc.java2typescript.NestedStatic$L1$L2$L3} /
 * {@code NestedStatic$L1$L2$L3}.
 *
 * <p>Tests named {@code *_level1*} are controls and are expected to pass.
 */
public class NestedClassTest extends AbstractConverterTest {

    private static final String PKG = "org.bsc.java2typescript.";

    /** All the fixture types, as if they were all declared with {@code @Type}. */
    private static final Class<?>[] FIXTURE_TYPES = {
            NestedStatic.class, NestedStatic.L1.class, NestedStatic.L1.L2.class, NestedStatic.L1.L2.L3.class,
            NestedStatic.G1.class,
            NestedIfc.class, NestedIfc.Find.class, NestedIfc.Find.Default.class,
            NestedMixed.class, NestedMixed.Ifc.class, NestedMixed.Ifc.Leaf.class,
            NestedMixed.Mode.class, NestedMixed.Mode.Leaf.class,
            NestedMixed.Holder.class, NestedMixed.Holder.Kind.class, NestedMixed.Holder.Inner.class,
            NestedGeneric.class, NestedGeneric.Inner.class, NestedGeneric.Inner.Deep.class,
            NestedGeneric.Mid.class, NestedGeneric.Mid.Leaf.class,
            NestedGenericOther.class, NestedGenericOther.Mid.class, NestedGenericOther.Mid.Leaf.class,
            Mid.class, Mid.Leaf.class,
            NestedSub.class, NestedUser.class,
    };

    private Map<String, TSType> fixtureTypeMap() {
        return declaredTypeMap(Stream.concat(Stream.of(FIXTURE_TYPES), Stream.of(String.class, List.class))
                .map(TSType::of)
                .toArray(TSType[]::new));
    }

    private static void assertNames(Class<?> type, String expectedSimpleTypeName) {
        final TSType t = TSType.of(type);
        Assert.assertThat(t.getSimpleTypeName(), equalTo(expectedSimpleTypeName));
        Assert.assertThat(t.getTypeName(), equalTo(PKG + expectedSimpleTypeName));
    }

    private String classDecl(Class<?> type) {
        return converter.contextOf(TSType.of(type), fixtureTypeMap()).getClassDecl().toString();
    }

    private String usage(String methodName) throws Exception {
        return getReturnType(fixtureTypeMap(), NestedUser.class, methodName);
    }

    // ------------------------------------------------------------------------------------------------------
    // TSType.getTypeName() / getSimpleTypeName()
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testNames_level1() {
        assertNames(NestedStatic.L1.class, "NestedStatic$L1");
        assertNames(NestedIfc.Find.class, "NestedIfc$Find");
        assertNames(NestedMixed.Mode.class, "NestedMixed$Mode");
    }

    @Test
    public void testNames_level2() {
        assertNames(NestedStatic.L1.L2.class, "NestedStatic$L1$L2");
    }

    @Test
    public void testNames_level3() {
        assertNames(NestedStatic.L1.L2.L3.class, "NestedStatic$L1$L2$L3");
    }

    /** like Gatling's CheckBuilder.Find.Default */
    @Test
    public void testNames_level2_interfaceInterfaceClass() {
        assertNames(NestedIfc.Find.Default.class, "NestedIfc$Find$Default");
    }

    @Test
    public void testNames_level2_classInterfaceClass() {
        assertNames(NestedMixed.Ifc.Leaf.class, "NestedMixed$Ifc$Leaf");
    }

    @Test
    public void testNames_level2_classEnumClass() {
        assertNames(NestedMixed.Mode.Leaf.class, "NestedMixed$Mode$Leaf");
    }

    @Test
    public void testNames_level2_enum() {
        assertNames(NestedMixed.Holder.Kind.class, "NestedMixed$Holder$Kind");
    }

    @Test
    public void testNames_level2_nonStaticInner() {
        assertNames(NestedMixed.Holder.Inner.class, "NestedMixed$Holder$Inner");
    }

    /** Three different Leaf classes must get three different TS names. */
    @Test
    public void testNames_level2_noCollision() {
        final List<String> names = Stream.of(NestedGeneric.Mid.Leaf.class, NestedGenericOther.Mid.Leaf.class,
                Mid.Leaf.class)
                .map(c -> TSType.of(c).getTypeName())
                .collect(Collectors.toList());

        Assert.assertThat(names, equalTo(Arrays.asList(
                PKG + "NestedGeneric$Mid$Leaf",
                PKG + "NestedGenericOther$Mid$Leaf",
                PKG + "Mid$Leaf")));
    }

    // ------------------------------------------------------------------------------------------------------
    // Declaration: Context.getClassDecl()
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testClassDecl_level1() {
        Assert.assertThat(classDecl(NestedStatic.L1.class),
                equalTo("class NestedStatic$L1/* extends java.lang.Object*/ {"));
    }

    @Test
    public void testClassDecl_level2() {
        Assert.assertThat(classDecl(NestedStatic.L1.L2.class),
                equalTo("class NestedStatic$L1$L2/* extends java.lang.Object*/ {"));
    }

    @Test
    public void testClassDecl_level3() {
        Assert.assertThat(classDecl(NestedStatic.L1.L2.L3.class),
                equalTo("class NestedStatic$L1$L2$L3/* extends java.lang.Object*/ {"));
    }

    @Test
    public void testClassDecl_level2_interfaceInterfaceClass() {
        Assert.assertThat(classDecl(NestedIfc.Find.Default.class),
                equalTo("class NestedIfc$Find$Default<X>/* extends java.lang.Object implements NestedIfc$Find<X>*/ {"));
    }

    @Test
    public void testClassDecl_extendsLevel2() {
        Assert.assertThat(classDecl(NestedSub.class),
                equalTo("class NestedSub/* extends NestedStatic$L1$L2*/ {"));
    }

    // ------------------------------------------------------------------------------------------------------
    // Declaration: processStatic() ("XStatic" interface and exported constant)
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testStatic_level1() {
        final String result = converter.processStatic(TSType.of(NestedStatic.L1.class), fixtureTypeMap());
        Assert.assertThat(result, containsString("interface NestedStatic$L1Static {"));
        Assert.assertThat(result, containsString(
                "export const NestedStatic$L1: NestedStatic$L1Static = Java.type(\"org.bsc.java2typescript.NestedStatic$L1\");"));
    }

    @Test
    public void testStatic_level2() {
        final String result = converter.processStatic(TSType.of(NestedStatic.L1.L2.class), fixtureTypeMap());
        Assert.assertThat(result, containsString("interface NestedStatic$L1$L2Static {"));
        Assert.assertThat(result, containsString(
                "export const NestedStatic$L1$L2: NestedStatic$L1$L2Static = Java.type(\"org.bsc.java2typescript.NestedStatic$L1$L2\");"));
    }

    @Test
    public void testStatic_level2_enum() {
        final String result = converter.processStatic(TSType.of(NestedMixed.Holder.Kind.class), fixtureTypeMap());
        Assert.assertThat(result, containsString("\tA:org.bsc.java2typescript.NestedMixed$Holder$Kind;\n"));
        Assert.assertThat(result, containsString("export const NestedMixed$Holder$Kind: NestedMixed$Holder$KindStatic"));
    }

    // ------------------------------------------------------------------------------------------------------
    // Usage: references from another class (TypescriptConverter.convertJavaToTS)
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testUsage_level1() throws Exception {
        Assert.assertThat(usage("level1"), equalTo("NestedStatic$L1"));
        Assert.assertThat(usage("arrayOfLevel1"), equalTo("[NestedStatic$L1]"));
        Assert.assertThat(usage("genericLevel1"), equalTo("NestedStatic$G1<string>"));
        Assert.assertThat(usage("ifcFind"), equalTo("NestedIfc$Find<string>"));
    }

    @Test
    public void testUsage_level2() throws Exception {
        Assert.assertThat(usage("level2"), equalTo("NestedStatic$L1$L2"));
    }

    @Test
    public void testUsage_level3() throws Exception {
        Assert.assertThat(usage("level3"), equalTo("NestedStatic$L1$L2$L3"));
    }

    @Test
    public void testUsage_level3_list() throws Exception {
        Assert.assertThat(usage("listOfLevel3"), equalTo("java.util.List<NestedStatic$L1$L2$L3>"));
    }

    @Test
    public void testUsage_level3_array() throws Exception {
        Assert.assertThat(usage("arrayOfLevel3"), equalTo("[NestedStatic$L1$L2$L3]"));
    }

    @Test
    public void testUsage_level3_parameter() throws Exception {
        final Method m = NestedUser.class.getMethod("takeLevel3", NestedStatic.L1.L2.L3.class);
        Assert.assertThat(converter.getMethodParametersAndReturnDecl(m, TSType.of(NestedUser.class), fixtureTypeMap(), true),
                equalTo("( arg:NestedStatic$L1$L2$L3 ):void"));
    }

    @Test
    public void testUsage_level2_parameterized() throws Exception {
        Assert.assertThat(usage("genericLeaf"), equalTo("NestedGeneric$Mid$Leaf<string>"));
    }

    @Test
    public void testUsage_level2_parameterizedInterfaceInterfaceClass() throws Exception {
        Assert.assertThat(usage("ifcFindDefault"), equalTo("NestedIfc$Find$Default<string>"));
    }

    // ------------------------------------------------------------------------------------------------------
    // Generated declarations (TypescriptConverter.processClass, as called by the annotation processor)
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testProcessClass_level1() {
        final String result = converter.processClass(0, TSType.of(NestedStatic.L1.class), fixtureTypeMap());
        Assert.assertThat(result, containsString("class NestedStatic$L1/* extends java.lang.Object*/ {"));
        Assert.assertThat(result, containsString("} // end NestedStatic$L1\n"));
    }

    @Test
    public void testProcessClass_level2() {
        final String result = converter.processClass(0, TSType.of(NestedStatic.L1.L2.class), fixtureTypeMap());
        Assert.assertThat(result, containsString("class NestedStatic$L1$L2/* extends java.lang.Object*/ {"));
        Assert.assertThat(result, containsString("\tparent(  ):NestedStatic$L1;\n"));
        Assert.assertThat(result, containsString("\tchild(  ):NestedStatic$L1$L2$L3;\n"));
        Assert.assertThat(result, containsString("} // end NestedStatic$L1$L2\n"));
    }

    private static final Pattern DECL = Pattern.compile("(?m)^(?:/\\* enum \\*/)?(?:class|interface) ([^<{/ ]+)");

    /** Each fixture type, once generated, must be declared once with its own name (no truncation, no duplicates). */
    @Test
    public void testProcessClass_allFixtureTypes() {
        final Map<String, TSType> map = fixtureTypeMap();
        final List<String> declared = new ArrayList<>();
        for (Class<?> c : FIXTURE_TYPES) {
            final Matcher m = DECL.matcher(converter.processClass(0, TSType.of(c), map));
            while (m.find()) declared.add(m.group(1));
        }
        final List<String> expected = Stream.of(FIXTURE_TYPES)
                .map(c -> c.getName().substring(PKG.length()))
                .sorted()
                .collect(Collectors.toList());

        Assert.assertThat(declared.stream().sorted().collect(Collectors.toList()), equalTo(expected));
    }
}
