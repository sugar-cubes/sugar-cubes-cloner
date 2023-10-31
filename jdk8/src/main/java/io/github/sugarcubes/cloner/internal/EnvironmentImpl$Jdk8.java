package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.github.sugarcubes.cloner.ClassUtils;
import io.github.sugarcubes.cloner.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.ReflectionInstanceAllocatorFactory;
import sun.misc.Unsafe;

public class EnvironmentImpl$Jdk8 implements Environment {

    protected static final boolean OBJENESIS = ClassUtils.isClassAvailable("org.objenesis.ObjenesisStd");

    protected List<TypeOpener> openers = new ArrayList<>();
    protected List<Reflection> reflections = new ArrayList<>();
    protected sun.misc.Unsafe unsafe;
    protected List<InstanceAllocatorFactory> allocators = new ArrayList<>();

    public EnvironmentImpl$Jdk8() {
        TypeOpener opener = TypeOpener.NOOP;
        openers.add(opener);
        ReflectionImpl$Jdk8 reflection = new ReflectionImpl$Jdk8();
        reflections.add(reflection);
        unsafe = ClonerExceptionUtils.replaceException(() ->getUnsafeImpl(opener, reflection));
    }

    @Override
    public TypeOpener getOpener() {
        return TypeOpener.NOOP;
    }

    @Override
    public sun.misc.Unsafe getUnsafe() {
        return ClonerExceptionUtils.replaceException(() -> getUnsafeImpl(null, null));
    }

    protected <T> T getFieldValue(Class<?> type, String fieldName, Object target) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    protected sun.misc.Unsafe getUnsafeImpl(TypeOpener opener, Reflection reflection) throws Exception {
        opener.open("sun.misc.Unsafe");
        Field theUnsafe = reflection.getDeclaredField(Unsafe.class, "theUnsafe");
        reflection.makeAccessible(theUnsafe);
        return getFieldValue(sun.misc.Unsafe.class, "theUnsafe", null);
    }

    @Override
    public Reflection getReflection() {
        return new ReflectionImpl$Jdk8();
    }

    @Override
    public InstanceAllocatorFactory getAllocator() {
        return OBJENESIS ? new ObjenesisInstanceAllocatorFactory() : new ReflectionInstanceAllocatorFactory();
    }

}
