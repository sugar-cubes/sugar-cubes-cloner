package org.sugarcubes.cloner;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Methods which do not belong to {@link CloningPolicy} interface but are (re)used in the code.
 *
 * @author Maxim Butov
 */
public class CloningPolicyHelper {

    /**
     * Primitive wrappers types.
     */
    private static final Set<Class<?>> WRAPPERS_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class
    )));

    /**
     * Checks the type is primitive wrapper.
     *
     * @param type type
     * @return {@code true} if {@code type} is primitive wrapper
     */
    public static boolean isWrapper(Class<?> type) {
        return WRAPPERS_TYPES.contains(type);
    }

    /**
     * Checks the type is primitive or enum or primitive wrapper.
     *
     * @param type type
     * @return {@code true} if {@code type} is primitive or enum or primitive wrapper
     */
    public static boolean isPrimitiveOrEnumOrWrapper(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || isWrapper(type);
    }

    /**
     * Returns {@code true} if type is immutable and final. This means there is no mutable subtype of this type.
     *
     * @param policy cloning policy
     * @param type type
     * @return {@code true} if type is immutable and final
     */
    public static boolean isFinalAndImmutable(CloningPolicy policy, Class<?> type) {
        return Modifier.isFinal(type.getModifiers()) && policy.isImmutable(type);
    }

    /**
     * Returns {@code true} if array component type or field type guarantees that the stored value is immutable.
     * We cannot use {@code isImmutable(componentType)}, because, for instance, the value of field defined as
     * {@code private BigInteger value;} will be immutable value in this case, although it can refer to some mutable subtype of
     * {@code BigInteger}.
     *
     * @param policy cloning policy
     * @param componentType array component type of field type
     * @return {@code true} if value guaranteed to be immutable
     */
    public static boolean isComponentTypeImmutable(CloningPolicy policy, Class<?> componentType) {
        return CloningPolicyHelper.isPrimitiveOrEnumOrWrapper(componentType) || isFinalAndImmutable(policy, componentType);
    }

    /**
     * Utility class.
     */
    private CloningPolicyHelper() {
    }

}
