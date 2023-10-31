package io.github.sugarcubes.cloner.internal;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Set;

public class InstrumentationModuleOpenerImpl extends AbstractModuleOpener {

    private final Instrumentation instrumentation;

    public InstrumentationModuleOpenerImpl(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    protected void openModule(Module module, String packageName, Module thisModule) throws Exception {
        Map<String, Set<Module>> extra = Map.of(packageName, Set.of(thisModule));
        instrumentation.redefineModule(module, Set.of(), extra, extra, Set.of(), Map.of());
    }

}
