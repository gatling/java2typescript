package org.bsc.java2typescript;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Java arrays are TypeScript arrays (fixtures in {@link ArrayTypes}): {@code int[]} -> {@code int[]}, not the
 * one-element tuple {@code [int]}, which rejects any index above 0.
 *
 * <p>Tests named {@code *_control} already passed before.
 */
public class ArrayTypeTest extends AbstractConverterTest {

    private static final String PKG = "org.bsc.java2typescript.";

    private Map<String, TSType> fixtureTypeMap() {
        return declaredTypeMap(Stream.concat(
                        Stream.of(ArrayUser.class, ArrayKind.class, String.class).map(TSType::of),
                        TypescriptConverter.PREDEFINED_TYPES.stream())
                .toArray(TSType[]::new));
    }

    private String returnType(String methodName) throws Exception {
        return getReturnType(fixtureTypeMap(), ArrayUser.class, methodName);
    }

    private String signature(String methodName, Class<?>... parameterTypes) throws Exception {
        final Method m = ArrayUser.class.getMethod(methodName, parameterTypes);
        return converter.getMethodParametersAndReturnDecl(m, TSType.of(ArrayUser.class), fixtureTypeMap(), true);
    }

    @Test
    public void testPrimitive() throws Exception {
        Assert.assertThat(returnType("ints"), equalTo("int[]"));
    }

    @Test
    public void testPrimitive2D() throws Exception {
        Assert.assertThat(returnType("intMatrix"), equalTo("int[][]"));
    }

    @Test
    public void testString() throws Exception {
        Assert.assertThat(returnType("strings"), equalTo("string[]"));
    }

    /** "int|null[]" would be a union of int and null[] */
    @Test
    public void testBoxed() throws Exception {
        Assert.assertThat(returnType("boxed"), equalTo("(int|null)[]"));
        Assert.assertThat(returnType("boxedMatrix"), equalTo("(int|null)[][]"));
    }

    @Test
    public void testDeclared() throws Exception {
        Assert.assertThat(returnType("self"), equalTo("ArrayUser[]"));
    }

    @Test
    public void testUndeclared() throws Exception {
        Assert.assertThat(returnType("undeclared"), equalTo("any /*java.time.Duration*/[]"));
    }

    @Test
    public void testTypeArgument() throws Exception {
        Assert.assertThat(returnType("listOfInts"), equalTo("java.util.List<int[]>"));
    }

    @Test
    public void testCharArray() throws Exception {
        Assert.assertThat(returnType("chars"), equalTo("chararray"));
        Assert.assertThat(returnType("charMatrix"), equalTo("chararray[]"));
    }

    @Test
    public void testGenericArray_control() throws Exception {
        Assert.assertThat(returnType("genericArray"), equalTo("java.util.List<string>[]"));
        Assert.assertThat(signature("typeVariableArray"), equalTo("<T>(  ):T[]"));
    }

    @Test
    public void testVarargs() throws Exception {
        Assert.assertThat(signature("varargs", Integer[].class), equalTo("( ...values:(int|null)[] ):void"));
        Assert.assertThat(signature("varargsStrings", String[].class), equalTo("( ...values:string[] ):void"));
    }

    /** MyEnum.values()[1] is rejected by TypeScript with a tuple (TS2493) */
    @Test
    public void testEnumValues() {
        final String result = converter.processStatic(TSType.of(ArrayKind.class).setExport(true), fixtureTypeMap());
        Assert.assertThat(result, containsString("\tvalues(  ):" + PKG + "ArrayKind[];\n"));
    }
}
