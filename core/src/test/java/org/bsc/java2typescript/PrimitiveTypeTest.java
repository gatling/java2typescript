package org.bsc.java2typescript;

import static org.hamcrest.core.IsEqual.equalTo;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Java primitive types and their wrappers (fixtures in {@link PrimitiveTypes}) are mapped to the type aliases of
 * headerD.ts ({@code int}, {@code char}...), the wrappers with {@code |null}.
 *
 * <p>Tests named {@code *_control} already passed before.
 */
public class PrimitiveTypeTest extends AbstractConverterTest {

    private Map<String, TSType> fixtureTypeMap() {
        return declaredTypeMap(Stream.concat(
                        Stream.of(PrimitiveUser.class, Optional.class).map(TSType::of),
                        TypescriptConverter.PREDEFINED_TYPES.stream())
                .toArray(TSType[]::new));
    }

    private String returnType(String methodName) throws Exception {
        return getReturnType(fixtureTypeMap(), PrimitiveUser.class, methodName);
    }

    @Test
    public void testPrimitive_control() throws Exception {
        Assert.assertThat(returnType("anInt"), equalTo("int"));
        Assert.assertThat(returnType("aLong"), equalTo("long"));
        Assert.assertThat(returnType("aFloat"), equalTo("float"));
        Assert.assertThat(returnType("aDouble"), equalTo("double"));
        Assert.assertThat(returnType("aBoolean"), equalTo("boolean"));
        Assert.assertThat(returnType("boxedInt"), equalTo("int|null"));
    }

    @Test
    public void testChar() throws Exception {
        Assert.assertThat(returnType("aChar"), equalTo("char"));
        Assert.assertThat(returnType("boxedChar"), equalTo("char|null"));
    }

    @Test
    public void testByte() throws Exception {
        Assert.assertThat(returnType("aByte"), equalTo("byte"));
        Assert.assertThat(returnType("boxedByte"), equalTo("byte|null"));
    }

    /** "short" is declared in headerD.ts */
    @Test
    public void testShort() throws Exception {
        Assert.assertThat(returnType("aShort"), equalTo("short"));
        Assert.assertThat(returnType("boxedShort"), equalTo("short|null"));
    }

    @Test
    public void testArray() throws Exception {
        Assert.assertThat(returnType("shorts"), equalTo("short[]"));
        Assert.assertThat(returnType("boxedChars"), equalTo("(char|null)[]"));
    }

    @Test
    public void testTypeArgument() throws Exception {
        Assert.assertThat(returnType("listOfChars"), equalTo("java.util.List<char|null>"));
        Assert.assertThat(returnType("optionalShort"), equalTo("java.util.Optional<short|null>"));
    }

    @Test
    public void testParameter() throws Exception {
        final Method m = PrimitiveUser.class.getMethod("toUpper", char.class);
        Assert.assertThat(
                converter.getMethodParametersAndReturnDecl(m, TSType.of(PrimitiveUser.class), fixtureTypeMap(), true),
                equalTo("( c:char ):char"));
    }
}
