package io.github.sugarcubes.cloner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.sugarcubes.cloner.internal.FieldAdapterFactory;
import io.github.sugarcubes.cloner.internal.FieldAdapterFieldCopierFactory;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;

public class ToolsetBuilder {
    private Reflection reflection;
    private List<InstanceAllocatorFactory> allocators = Collections.emptyList();
    private FieldCopierFactory fieldCopier;

    public ToolsetBuilder reflection(Reflection reflection) {
        this.reflection = reflection;
        return this;
    }

    public ToolsetBuilder allocator(InstanceAllocatorFactory allocator) {
        this.allocators = Collections.singletonList(allocator);
        return this;
    }

    public ToolsetBuilder allocators(InstanceAllocatorFactory... allocator) {
        this.allocators = Arrays.asList(allocator);
        return this;
    }

    public ToolsetBuilder fieldCopier(FieldCopierFactory fieldCopier) {
        this.fieldCopier = fieldCopier;
        return this;
    }

    public ToolsetBuilder fieldAdapter(FieldAdapterFactory fieldAdapter) {
        return fieldCopier(new FieldAdapterFieldCopierFactory(fieldAdapter));
    }

    public Toolset build() {
        return null;
    }

}
