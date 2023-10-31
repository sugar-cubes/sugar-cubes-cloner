package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.Method;

import io.github.sugarcubes.cloner.ClonerExceptionUtils;
import io.github.toolfactory.narcissus.Narcissus;

public class NarcissusModuleOpenerImpl extends AbstractModuleOpener {

    private final Method implAddExportsOrOpens;

    public NarcissusModuleOpenerImpl() {
        implAddExportsOrOpens = ClonerExceptionUtils.replaceException(() ->
            Narcissus.findMethod(Module.class, "implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class));
    }

    @Override
    protected void openModule(Module module, String packageName, Module thisModule) throws Exception {
        // export
        Narcissus.invokeVoidMethod(module, implAddExportsOrOpens, packageName, thisModule, false, true);
        // open
        Narcissus.invokeVoidMethod(module, implAddExportsOrOpens, packageName, thisModule, true, true);
    }

}
