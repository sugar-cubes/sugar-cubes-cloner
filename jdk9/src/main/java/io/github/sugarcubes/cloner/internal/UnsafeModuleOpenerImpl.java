package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.sugarcubes.cloner.ClonerExceptionUtils;

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
