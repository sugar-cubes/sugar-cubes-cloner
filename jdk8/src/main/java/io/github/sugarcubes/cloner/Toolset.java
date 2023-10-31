package io.github.sugarcubes.cloner;

import io.github.sugarcubes.cloner.internal.ReflectionImpl$Jdk8;
import io.github.sugarcubes.cloner.internal.FieldAdapterFactory;
import io.github.sugarcubes.cloner.internal.FieldAdapterFieldCopierFactory;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;

public class Toolset {

    public static Toolset failsafe() {
        return new Toolset(new ReflectionImpl$Jdk8(), new ReflectionInstanceAllocatorFactory(), new ReflectionFieldCopierFactory());
    }

    private final Reflection reflection;
    private final InstanceAllocatorFactory allocator;
    private final FieldCopierFactory fieldCopier;

    public Toolset(Reflection reflection, InstanceAllocatorFactory allocator, FieldCopierFactory fieldCopier) {
        this.reflection = reflection;
        this.allocator = allocator;
        this.fieldCopier = fieldCopier;
    }

    public Toolset(Reflection reflection, InstanceAllocatorFactory allocator, FieldAdapterFactory fieldAdapter) {
        this(reflection, allocator, new FieldAdapterFieldCopierFactory(fieldAdapter));
    }

    public Reflection getReflection() {
        return reflection;
    }

    public InstanceAllocatorFactory getAllocator() {
        return allocator;
    }

    public FieldCopierFactory getFieldCopier() {
        return fieldCopier;
    }

}
