/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sugarcubes.cloner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.sugarcubes.cloner.internal.FieldAdapterFactory;
import io.github.sugarcubes.cloner.internal.FieldAdapterFieldCopierFactory;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;

/**
 * todo
 */
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
