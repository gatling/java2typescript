package org.bsc.java2typescript;

import static org.hamcrest.core.IsEqual.equalTo;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Nested types as the annotation processor sees them: {@code @Type(value = X.class)} gives a {@link DeclaredType}
 * that {@link TSType#getValue()} must resolve back to the Java class (TSType.getClassFrom / getClassName /
 * getMemberClassForName). Fixtures in {@link NestedClasses}.
 *
 * <p>An annotation-processing-only javac task is run once to get the real {@link DeclaredType}s.
 *
 * <p>Tests named {@code *_level1*} are controls and are expected to pass.
 */
public class NestedClassDeclaredTypeTest {

    private static final Class<?>[] TYPES = {
            NestedStatic.L1.class,
            NestedStatic.L1.L2.class,
            NestedStatic.L1.L2.L3.class,
            NestedIfc.Find.class,
            NestedIfc.Find.Default.class,
            NestedMixed.Ifc.class,
            NestedMixed.Ifc.Leaf.class,
            NestedMixed.Mode.class,
            NestedMixed.Mode.Leaf.class,
            NestedMixed.Rec.class,
            NestedMixed.Rec.Leaf.class,
    };

    /** class -> TSType.of() whose "value" is the javac DeclaredType of the class */
    private static final Map<Class<?>, TSType> DECLARED = new HashMap<>();

    @BeforeClass
    public static void collectDeclaredTypes() throws Exception {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("a JDK (not a JRE) is required", compiler);

        final String classpath = classpathOf(NestedClasses.class) + File.pathSeparator + classpathOf(TSType.class);

        final SimpleJavaFileObject dummy = new SimpleJavaFileObject(URI.create("string:///Dummy.java"),
                SimpleJavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return "class Dummy {}";
            }
        };

        final AbstractProcessor processor = new AbstractProcessor() {
            @Override
            public Set<String> getSupportedAnnotationTypes() {
                return Collections.singleton("*");
            }

            @Override
            public SourceVersion getSupportedSourceVersion() {
                return SourceVersion.latestSupported();
            }

            @Override
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                for (Class<?> c : TYPES) {
                    final TypeElement te = processingEnv.getElementUtils().getTypeElement(c.getCanonicalName());
                    // same as AbstractProcessorEx.toMapObject( am, TSType::of ) does with @Type(value=...)
                    final TSType t = TSType.of();
                    t.put("value", te.asType());
                    DECLARED.putIfAbsent(c, t);
                }
                return false;
            }
        };

        final JavaCompiler.CompilationTask task = compiler.getTask(null, null, null,
                Arrays.asList("-proc:only", "-classpath", classpath), null, Collections.singletonList(dummy));
        task.setProcessors(Collections.singletonList(processor));
        Assert.assertTrue("javac task failed", task.call());
    }

    private static String classpathOf(Class<?> c) throws Exception {
        return new File(c.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
    }

    private static void assertResolved(Class<?> type) {
        Assert.assertThat(DECLARED.get(type).getValue(), equalTo(type));
    }

    @Test
    public void testResolve_level1() {
        assertResolved(NestedStatic.L1.class);
        assertResolved(NestedIfc.Find.class);
        assertResolved(NestedMixed.Ifc.class);
        assertResolved(NestedMixed.Mode.class);
        assertResolved(NestedMixed.Rec.class);
    }

    @Test
    public void testResolve_level2() {
        assertResolved(NestedStatic.L1.L2.class);
    }

    @Test
    public void testResolve_level3() {
        assertResolved(NestedStatic.L1.L2.L3.class);
    }

    /** like Gatling's CheckBuilder.Find.Default */
    @Test
    public void testResolve_level2_interfaceInterfaceClass() {
        assertResolved(NestedIfc.Find.Default.class);
    }

    @Test
    public void testResolve_level2_classInterfaceClass() {
        assertResolved(NestedMixed.Ifc.Leaf.class);
    }

    @Test
    public void testResolve_level2_classEnumClass() {
        assertResolved(NestedMixed.Mode.Leaf.class);
    }

    @Test
    public void testResolve_level2_classRecordClass() {
        assertResolved(NestedMixed.Rec.Leaf.class);
    }

    /** the resolved class is then named like in {@link NestedClassTest} */
    @Test
    public void testNames_level2() {
        final TSType t = DECLARED.get(NestedStatic.L1.L2.class);
        Assert.assertThat(t.getSimpleTypeName(), equalTo("NestedStatic$L1$L2"));
        Assert.assertThat(t.getTypeName(), equalTo("org.bsc.java2typescript.NestedStatic$L1$L2"));
    }
}
