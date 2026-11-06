package org.bsc.java2typescript;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TypeScript generated for Java public fields (fixtures in {@link PublicFields}).
 *
 * <p>Expected convention, following the methods:
 * <ul>
 * <li>{@code processStatic()} ({@code XStatic} interface of exported types): one {@code [readonly ]name:type;} line per
 * public static field declared by the type, {@code readonly} for final fields, sorted by name, right after
 * {@code readonly class:any;}, with fully qualified types;</li>
 * <li>{@code processClass()}: {@code static [readonly ]name:type;} for the static fields of non-exported types,
 * commented out in interfaces (like static methods), then {@code [readonly ]name:type;} for the public instance
 * fields, inherited ones included (like instance methods), sorted by name, before the methods;</li>
 * <li>a field which cannot be declared next to a method of the same name, or named {@code constructor} in a
 * class, is commented out.</li>
 * </ul>
 *
 * <p>Tests named {@code *_control} already passed before public fields were generated.
 */
public class PublicFieldTest extends AbstractConverterTest {

    private static final String PKG = "org.bsc.java2typescript.";

    /** All the fixture types but PfBoxHolder, as if they were all declared with {@code @Type}. */
    private static final Class<?>[] FIXTURE_TYPES = {
            PfHolder.class, PfHolder.Inner.class, PfHolder.Inner.Deep.class,
            PfIfc.class, PfRec.class, PfFn.class, PfKind.class, PfClashEnum.class,
            PfBase.class, PfSub.class, PfImpl.class, PfClash.class, PfWords.class,
            PfTypes.class, PfGeneric.class, PfBox.class,
            PublicFields.Instance.class, PublicFields.InstanceBase.class, PublicFields.InstanceSub.class,
            PublicFields.InstanceFromNonPublic.class, PublicFields.InstanceClash.class, PublicFields.InstanceGeneric.class,
            PublicFields.Coded.class,
            String.class, Integer.class, System.class,
    };

    /** fixtures and the types the processor always declares */
    private Map<String, TSType> fixtureTypeMap() {
        return declaredTypeMap(Stream.concat(
                        Stream.of(FIXTURE_TYPES).map(TSType::of),
                        TypescriptConverter.PREDEFINED_TYPES.stream())
                .toArray(TSType[]::new));
    }

    /** "XStatic" interface and exported constant */
    private String processStatic(Class<?> type) {
        return converter.processStatic(TSType.of(type).setExport(true), fixtureTypeMap());
    }

    /** class or interface declaration, for a non-exported type */
    private String processClass(Class<?> type) {
        return converter.processClass(0, TSType.of(type), fixtureTypeMap());
    }

    /** TypeScript type of a static field, resolved like the parameters and return types of a static method */
    private String fieldType(Class<?> type, String fieldName, boolean packageResolution) throws Exception {
        final Field f = type.getField(fieldName);
        return TypescriptConverter.convertJavaToTS(f.getGenericType(), f, TSType.of(type), fixtureTypeMap(),
                packageResolution, Optional.empty());
    }

    private static int occurrences(String text, String part) {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }

    // ------------------------------------------------------------------------------------------------------
    // processStatic(): static fields in the "XStatic" interface
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testStatic_class() {
        Assert.assertThat(processStatic(PfHolder.class), containsString(
                "\treadonly class:any;\n" +
                "\treadonly DEEP:" + PKG + "PfHolder$Inner$Deep;\n" +
                "\treadonly INSTANCE:" + PKG + "PfHolder;\n" +
                "\treadonly MAX:int;\n" +
                "\treadonly NAME:string;\n" +
                "\treadonly NAMES:java.util.List<string>;\n" +
                "\tcounter:int;\n" +
                "\tnew(  ):" + PKG + "PfHolder;\n"));
    }

    @Test
    public void testStatic_nonPublicAndInstanceFields_control() {
        final String result = processStatic(PfHolder.class);
        Assert.assertThat(result, not(containsString("PROT")));
        Assert.assertThat(result, not(containsString("PKG")));
        Assert.assertThat(result, not(containsString("PRIV")));
        Assert.assertThat(result, not(containsString("instanceField")));
    }

    @Test
    public void testStatic_nestedLevel2() {
        final String result = processStatic(PfHolder.Inner.Deep.class);
        Assert.assertThat(result, containsString("interface PfHolder$Inner$DeepStatic {"));
        Assert.assertThat(result, containsString("\treadonly SELF:" + PKG + "PfHolder$Inner$Deep;\n"));
    }

    @Test
    public void testStatic_interfaceConstants() {
        Assert.assertThat(processStatic(PfIfc.class), containsString(
                "\treadonly class:any;\n" +
                "\treadonly A:int;\n" +
                "\treadonly S:string;\n"));
    }

    @Test
    public void testStatic_record() {
        Assert.assertThat(processStatic(PfRec.class), containsString(
                "\treadonly class:any;\n" +
                "\treadonly EMPTY:" + PKG + "PfRec;\n" +
                "\treadonly ZERO:int;\n" +
                "\tnew("));
    }

    @Test
    public void testStatic_functionalInterface() {
        Assert.assertThat(processStatic(PfFn.class), containsString(
                "\treadonly class:any;\n" +
                "\treadonly ID:" + PKG + "PfFn;\n" +
                "\treadonly K:int;\n" +
                "\tnew("));
    }

    @Test
    public void testStatic_javaLangConstants() {
        Assert.assertThat(processStatic(Integer.class), containsString("\treadonly MAX_VALUE:int;\n"));
        Assert.assertThat(processStatic(System.class), containsString("\treadonly out:any /*java.io.PrintStream*/;\n"));
    }

    // inherited fields: GraalJS doesn't expose them on the subtype, so they must not be declared there

    @Test
    public void testStatic_inheritedFromClass() {
        final String result = processStatic(PfSub.class);
        Assert.assertThat(result, containsString("\treadonly OWN:int;\n"));
        Assert.assertThat(result, not(containsString("INHERITED")));
    }

    @Test
    public void testStatic_inheritedFromInterface_control() {
        final String result = processStatic(PfImpl.class);
        Assert.assertThat(result, not(containsString(" A:")));
        Assert.assertThat(result, not(containsString(" S:")));
    }

    /** PfSub.HIDDEN hides PfBase.HIDDEN: a duplicate would be a TS2300 "Duplicate identifier" error */
    @Test
    public void testStatic_hiddenField() {
        final String result = processStatic(PfSub.class);
        Assert.assertThat(result, containsString("\tHIDDEN:string;\n"));
        Assert.assertThat(occurrences(result, "HIDDEN"), equalTo(1));
    }

    /** an interface can't declare a property and a method with the same name (TS2300), the method is kept */
    @Test
    public void testStatic_sameNameAsStaticMethod() {
        final String result = processStatic(PfClash.class);
        Assert.assertThat(result, containsString("\t// value:int;\n"));
        Assert.assertThat(result, containsString("\tvalue(  ):int;\n"));
    }

    // ------------------------------------------------------------------------------------------------------
    // processStatic(): enum constants (the only static fields generated today)
    // ------------------------------------------------------------------------------------------------------

    /** enum constants can't be written at runtime */
    @Test
    public void testStatic_enumConstantsReadonly() {
        Assert.assertThat(processStatic(PfKind.class), containsString(
                "\treadonly A:" + PKG + "PfKind;\n" +
                "\treadonly B:" + PKG + "PfKind;\n"));
    }

    @Test
    public void testStatic_enumOtherStaticFields() {
        final String result = processStatic(PfKind.class);
        Assert.assertThat(result, containsString(
                "\treadonly class:any;\n" +
                "\treadonly DEFAULT:" + PKG + "PfKind;\n" +
                "\tcount:int;\n"));
        // the constants are static fields too: no duplicate
        Assert.assertThat(occurrences(result, "A:" + PKG + "PfKind;"), equalTo(1));
    }

    /** TS2300 "Duplicate identifier 'values'" today */
    @Test
    public void testStatic_enumConstantSameNameAsStaticMethod() {
        final String result = processStatic(PfClashEnum.class);
        Assert.assertThat(result, containsString("\t// readonly values:" + PKG + "PfClashEnum;\n"));
        Assert.assertThat(result, containsString("\treadonly other:" + PKG + "PfClashEnum;\n"));
        Assert.assertThat(result, containsString("\tvalues(  ):"));
    }

    // ------------------------------------------------------------------------------------------------------
    // processClass(): static fields in the class declaration
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testClass_notExported() {
        final String result = processClass(PfHolder.class);
        Assert.assertThat(result, containsString("\tstatic readonly DEEP:PfHolder$Inner$Deep;\n"));
        Assert.assertThat(result, containsString("\tstatic readonly INSTANCE:PfHolder;\n"));
        Assert.assertThat(result, containsString("\tstatic readonly MAX:int;\n"));
        Assert.assertThat(result, containsString("\tstatic readonly NAMES:java.util.List<string>;\n"));
        Assert.assertThat(result, containsString("\tstatic counter:int;\n"));
    }

    /** an interface can't have static members (TS1070), like static methods */
    @Test
    public void testClass_notExportedInterface() {
        final String result = processClass(PfIfc.class);
        Assert.assertThat(result, containsString("\t// static readonly A:int;\n"));
        Assert.assertThat(result, containsString("\t// static readonly S:string;\n"));
    }

    /** a class can't have a static member named "constructor" (TS18006) */
    @Test
    public void testClass_notExportedSpecialNames() {
        final String result = processClass(PfWords.class);
        Assert.assertThat(result, containsString("\t// static constructor:int;\n"));
        Assert.assertThat(result, containsString("\tstatic $x:int;\n"));
    }

    /** static fields of exported types are in the "XStatic" interface only */
    @Test
    public void testClass_exported_control() {
        final String result = converter.processClass(0, TSType.of(PfHolder.class).setExport(true), fixtureTypeMap());
        Assert.assertThat(result, not(containsString("MAX")));
    }

    /** enums are always exported */
    @Test
    public void testClass_enum_control() {
        Assert.assertThat(processClass(PfKind.class), not(containsString("DEFAULT")));
    }

    // ------------------------------------------------------------------------------------------------------
    // processClass(): instance fields in the class declaration
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testInstance_class() {
        final String result = processClass(PublicFields.Instance.class);
        Assert.assertThat(result, containsString(
                "\tstatic STATIC_ONE:int;\n" +
                "\tcount:int;\n" +
                "\tdeep:PfHolder$Inner$Deep;\n" +
                "\titems:java.util.List<string>;\n" +
                "\treadonly name:string;\n" +
                "\tnext:PublicFields$Instance;\n" +
                "\tequals("));
    }

    @Test
    public void testInstance_nonPublic_control() {
        final String result = processClass(PublicFields.Instance.class);
        Assert.assertThat(result, not(containsString("prot")));
        Assert.assertThat(result, not(containsString("pkg:")));
        Assert.assertThat(result, not(containsString("priv")));
    }

    /** instance fields stay in the class of an exported type, like instance methods */
    @Test
    public void testInstance_exported() {
        final String result = converter.processClass(0, TSType.of(PublicFields.Instance.class).setExport(true), fixtureTypeMap());
        Assert.assertThat(result, containsString("\tcount:int;\n"));
        Assert.assertThat(result, not(containsString("STATIC_ONE")));
    }

    @Test
    public void testInstance_notInStatic_control() {
        final String result = processStatic(PublicFields.Instance.class);
        Assert.assertThat(result, not(containsString("count")));
        Assert.assertThat(result, not(containsString("items")));
    }

    /** the class doesn't extend its superclass in TypeScript (it's only in a comment): inherited fields are declared */
    @Test
    public void testInstance_inherited() {
        final String result = processClass(PublicFields.InstanceSub.class);
        Assert.assertThat(result, containsString("\tinherited:int;\n"));
        Assert.assertThat(result, containsString("\town:int;\n"));
    }

    /** InstanceSub.hidden hides InstanceBase.hidden: a duplicate would be a TS2300 "Duplicate identifier" error */
    @Test
    public void testInstance_hiddenField() {
        final String result = processClass(PublicFields.InstanceSub.class);
        Assert.assertThat(result, containsString("\thidden:string;\n"));
        Assert.assertThat(occurrences(result, "hidden:"), equalTo(1));
    }

    /** a class can't declare a property and a method with the same name (TS2300), nor a "constructor" property */
    @Test
    public void testInstance_sameNameAsMethod() {
        final String result = processClass(PublicFields.InstanceClash.class);
        Assert.assertThat(result, containsString("\t// size:int;\n"));
        Assert.assertThat(result, containsString("\tsize(  ):int;\n"));
        Assert.assertThat(result, containsString("\t// constructor:int;\n"));
    }

    @Test
    public void testInstance_typeVariables() {
        final String result = processClass(PublicFields.InstanceGeneric.class);
        Assert.assertThat(result, containsString("class PublicFields$InstanceGeneric<T>"));
        Assert.assertThat(result, containsString("\tvalue:T;\n"));
        Assert.assertThat(result, containsString("\tvalues:java.util.List<T>;\n"));
    }

    @Test
    public void testInstance_enum() {
        Assert.assertThat(processClass(PublicFields.Coded.class), containsString("\treadonly code:int;\n"));
    }

    /** GraalJS doesn't expose the fields declared by a non-public class, nor the public field they hide */
    @Test
    public void testInstance_inheritedFromNonPublicClass() {
        final String result = processClass(PublicFields.InstanceFromNonPublic.class);
        Assert.assertThat(result, containsString("\tinherited:int;\n"));
        Assert.assertThat(result, containsString("\town:int;\n"));
        Assert.assertThat(result, not(containsString("nonPublic")));
        Assert.assertThat(result, not(containsString("hidden")));
    }

    // ------------------------------------------------------------------------------------------------------
    // Field types (TypescriptConverter.convertJavaToTS)
    //
    // The ignored tests show type mapping bugs, which also affect the parameters and return types of methods.
    // ------------------------------------------------------------------------------------------------------

    @Test
    public void testType_control() throws Exception {
        Assert.assertThat(fieldType(PfHolder.class, "MAX", false), equalTo("int"));
        Assert.assertThat(fieldType(PfHolder.class, "NAMES", false), equalTo("java.util.List<string>"));
        Assert.assertThat(fieldType(PfHolder.class, "DEEP", false), equalTo(PKG + "PfHolder$Inner$Deep"));
        Assert.assertThat(fieldType(PfHolder.class, "DEEP", true), equalTo("PfHolder$Inner$Deep"));
        Assert.assertThat(fieldType(PfTypes.class, "BOX_HOLDER", false),
                equalTo(PKG + "PfBox<any /*" + PKG + "PfBoxHolder*/>"));
        Assert.assertThat(fieldType(PfTypes.class, "BOX_OF_BOX", false), equalTo(PKG + "PfBox<" + PKG + "PfBox<string>>"));
    }

    /** headerD.ts declares "char" and "byte", but they're mapped to any */
    @Ignore("type mapping bug, not specific to fields")
    @Test
    public void testType_charAndByte() throws Exception {
        Assert.assertThat(fieldType(PfTypes.class, "C", false), equalTo("char"));
        Assert.assertThat(fieldType(PfTypes.class, "B", false), equalTo("byte"));
        Assert.assertThat(fieldType(PfTypes.class, "BOXED_C", false), equalTo("char|null"));
    }

    /** "String" is replaced inside "StringBuilder" */
    @Ignore("type mapping bug, not specific to fields")
    @Test
    public void testType_typeArgumentWithCommonPrefix() throws Exception {
        Assert.assertThat(fieldType(PfTypes.class, "MAP_SB", false),
                equalTo("java.util.Map<string, any /*java.lang.StringBuilder*/>"));
    }

    /** "org.bsc.java2typescript.PfBox" is replaced inside "org.bsc.java2typescript.PfBoxHolder" (TS2304) */
    @Ignore("type mapping bug, not specific to fields")
    @Test
    public void testType_typeArgumentWithCommonPrefix_packageResolution() throws Exception {
        Assert.assertThat(fieldType(PfTypes.class, "BOX_HOLDER", true),
                equalTo("PfBox<any /*" + PKG + "PfBoxHolder*/>"));
    }

    /** the inner "java.lang.String" is not converted once the outer type name is replaced */
    @Ignore("type mapping bug, not specific to fields")
    @Test
    public void testType_sameGenericTypeAsTypeArgument_packageResolution() throws Exception {
        Assert.assertThat(fieldType(PfTypes.class, "BOX_OF_BOX", true), equalTo("PfBox<PfBox<string>>"));
    }

    /** a static member can't use the type variables of its class */
    @Ignore("type mapping bug, not specific to fields")
    @Test
    public void testType_rawTypeInGenericClass() throws Exception {
        Assert.assertThat(fieldType(PfGeneric.class, "RAW", false), equalTo("java.util.List<any>"));
    }
}
