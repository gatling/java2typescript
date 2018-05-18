package org.bsc.java2typescript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.lang.String.format;
/**
 * 
 * @author softphone
 *
 */
@SuppressWarnings("serial")
public class TSType extends HashMap<String, Object> {

    private static final String ALIAS = "alias";
    private static final String VALUE = "value";
    private static final String EXPORT = "export";
    private static final String NAMESPACE = "namespace";

    protected TSType() {
        super(3);
    }

    public static TSType from(Class<?> cl, boolean exports) {
        return new TSType() {
            {
                put(VALUE, cl);
                put(EXPORT, exports);
            }
        };
    }

    public static TSType from(Class<?> cl, String alias, boolean exports) {
        return new TSType() {
            {
                put(VALUE, cl);
                put(EXPORT, exports);
                put(ALIAS, alias);
            }
        };
    }

    public static TSType from(Class<?> cl) {
        return new TSType() {
            {
                put(VALUE, cl);
            }
        };
    }

    /**
     * 
     * @return
     */
    public Class<?> getValue() {
        return getClassFrom(super.get(VALUE));
    }

    /**
     * 
     * @return
     */
    public boolean isExport() {
        return (boolean) super.getOrDefault(EXPORT, false);
    }

    /**
     * 
     * @return
     */
    public TSType setExport(boolean exports) {
        super.put(EXPORT, exports);
        return this;
    }

    /**
     * 
     * @return
     */
    public boolean hasAlias() {
        final String alias = (String) super.get(ALIAS);
        return alias != null && !alias.isEmpty();
    }

    /**
     * 
     * @return
     */
    public String getAlias() {
        return (String) super.get(ALIAS);
    }

    private String getMemberSimpleTypeName() {

        return format( "%s$%s", getValue().getDeclaringClass().getSimpleName(), getValue().getSimpleName());
    }

    /**
     * 
     * @return
     */
    public final String getTypeName() {
        return (hasAlias()) ? getAlias() : format( "%s.%s", getNamespace(), 
                (getValue().isMemberClass() ? getMemberSimpleTypeName() : getValue().getSimpleName()));
    }

    /**
     * 
     * @return
     */
    public final String getSimpleTypeName() {
        return (hasAlias()) ? getAlias() : 
            ((getValue().isMemberClass()) ? getMemberSimpleTypeName() : getValue().getSimpleName());
    }

    /**
     * 
     * @return
     */
    public final boolean supportNamespace() {
        return !hasAlias();
    }
    
    /**
     * 
     * @return
     */
    public final String getNamespace() {
        return (String) super.getOrDefault(NAMESPACE, getValue().getPackage().getName());
    }
    
    /**
     * 
     * @return
     */
    public boolean isFunctionalInterface() {
        return TypescriptConverter.isFunctionalInterface( getValue() );
    }
    /**
     *
     * @param type
     * @return
     */
    public Set<Field> getFields() {

        final Predicate<Field> std = f -> !f.isSynthetic() && Modifier.isPublic(f.getModifiers())
                && Character.isJavaIdentifierStart(f.getName().charAt(0))
                && f.getName().chars().skip(1).allMatch(Character::isJavaIdentifierPart);

        return Stream.concat(Stream.of(getValue().getFields()), Stream.of(getValue().getDeclaredFields())).filter(std)
                .collect(Collectors.toSet());

    }

    /**
     *
     * @param type
     * @return
     */
    public Set<Method> getMethods() {
        final Predicate<Method> include = m -> !m.isBridge() && !m.isSynthetic() && Modifier.isPublic(m.getModifiers())
                && Character.isJavaIdentifierStart(m.getName().charAt(0))
                && m.getName().chars().skip(1).allMatch(Character::isJavaIdentifierPart);

        return Stream.concat(Stream.of(getValue().getMethods()), Stream.of(getValue().getDeclaredMethods()))
                .filter(include).collect(Collectors.toSet());

    }

    /**
     * 
     * @param name
     * @return
     */
    private Class<?> getMemberClassForName( String fqn ) throws ClassNotFoundException {
        char[] ch = fqn.toCharArray();
        int i = fqn.lastIndexOf('.');
        ch[i] = '$';

        return Class.forName(String.valueOf(ch));   
    }
    
    /**
     *
     * @param dt
     * @return
     */
    private Class<?> getClassFrom(Object dt) {
        if (dt instanceof Class)
            return (Class<?>) dt;

        final String fqn = dt.toString();
        try {
            return Class.forName(fqn);

        } catch (ClassNotFoundException e1) {

            try {
                return getMemberClassForName(fqn);
    
            } catch (ClassNotFoundException e2) {
                throw new RuntimeException(String.format("class not found [%s]", dt), e1);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if( o instanceof Class ) {
            return getValue().equals(o);
        }
        if( o instanceof TSType ) {
            return getValue().equals(((TSType) o).getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

}
