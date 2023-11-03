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
package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.sugarcubes.cloner.ClonerExceptionUtils;

/**
 * todo
 */
public class UnsafeModuleOpenerImpl extends AbstractModuleOpener {

    private final UnsafeBridge unsafe;
    private final Method implAddExportsOrOpens;

    public UnsafeModuleOpenerImpl(UnsafeBridge unsafe) {
        this.unsafe = unsafe;
        this.implAddExportsOrOpens = ClonerExceptionUtils.replaceException(() -> {
            Field field = Class.class.getDeclaredField("module");
            long offset = unsafe.objectFieldOffset(field);
            unsafe.putObject(UnsafeModuleOpenerImpl.class, offset, Object.class.getModule());
            Method setAccessible0 = AccessibleObject.class.getDeclaredMethod("setAccessible0", boolean.class);
            setAccessible0.setAccessible(true);
            Method implAddExportsOrOpens = Module.class.getDeclaredMethod("implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class);
            setAccessible0.invoke(implAddExportsOrOpens, true);
            return implAddExportsOrOpens;
        });
    }

    @Override
    protected void openModule(Module module, String packageName, Module thisModule) throws Exception {
        implAddExportsOrOpens.invoke(module, packageName, thisModule, false, true);
        implAddExportsOrOpens.invoke(module, packageName, thisModule, true, true);
    }

}
